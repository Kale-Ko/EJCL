package io.github.kale_ko.ejcl.mysql;

import com.fasterxml.jackson.core.io.BigDecimalParser;
import com.fasterxml.jackson.core.io.BigIntegerParser;
import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.PathResolver;
import io.github.kale_ko.ejcl.StructuredConfig;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.exception.ConfigNotLoadedException;
import io.github.kale_ko.ejcl.exception.mysql.MaximumReconnectsException;
import io.github.kale_ko.ejcl.exception.mysql.MySQLException;
import io.github.kale_ko.ejcl.mysql.helper.MySQLHelper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Structured MySQL Config for storing data on a MySQL or MariaDB server
 *
 * @param <T> The type of the data being stored
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class StructuredMySQLConfig<T> extends StructuredConfig<T> {
    /**
     * The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected final @NotNull ObjectProcessor processor;

    /**
     * The address of the server
     *
     * @since 1.0.0
     */
    protected final @NotNull InetSocketAddress address;

    /**
     * The database on the server
     *
     * @since 1.0.0
     */
    protected final @NotNull String database;

    /**
     * The table of the database
     *
     * @since 1.0.0
     */
    protected final @NotNull String table;

    /**
     * The username to the server
     *
     * @since 1.0.0
     */
    protected final @Nullable String username;

    /**
     * The password to the server
     *
     * @since 1.0.0
     */
    protected final @Nullable String password;

    /**
     * Weather to use the MariaDB driver
     *
     * @since 3.11.0
     */
    protected final boolean useMariadb;

    /**
     * The connection to the server
     *
     * @since 1.0.0
     */
    protected @Nullable Connection connection;

    /**
     * How long to cache the config in memory
     *
     * @since 3.2.0
     */
    protected final @NotNull Duration cacheLength;

    /**
     * How many times we have tried to reconnect
     *
     * @since 2.3.0
     */
    protected int reconnectAttempts = 0;

    /**
     * When the config expires next
     *
     * @since 1.1.0
     */
    protected @NotNull Instant configExpires = Instant.ofEpochSecond(0);

    /**
     * A copy of the data that was fetched
     *
     * @since 3.10.0
     */
    protected @Nullable ParsedObject configBackup = null;

    /**
     * The lock used when saving and loading the config
     *
     * @since 3.8.0
     */
    protected final @NotNull Object SAVELOAD_LOCK = new Object();

    /**
     * If this config is closed
     *
     * @since 1.0.0
     */
    protected boolean closed = false;

    /**
     * Create a new StructuredMySQLConfig
     *
     * @param clazz       The class of the data being stored
     * @param address     The address of the server
     * @param database    The database on the server
     * @param table       The table of the database
     * @param username    The username to the server
     * @param password    The password to the server
     * @param useMariadb  Weather to use the MariaDB driver
     * @param cacheLength How long to cache the config in memory
     * @param processor   The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.11.0
     */
    protected StructuredMySQLConfig(@NotNull Class<T> clazz, @NotNull InetSocketAddress address, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password, boolean useMariadb, @NotNull Duration cacheLength, @NotNull ObjectProcessor processor) {
        super(clazz, false);

        this.processor = processor;

        this.address = address;

        this.database = database;
        this.table = table;

        this.username = username;
        this.password = password;

        this.useMariadb = useMariadb;

        this.cacheLength = cacheLength;
    }

    /**
     * Get if the config is loaded
     *
     * @return If the config is loaded
     *
     * @since 1.0.0
     */
    @Override
    public boolean getLoaded() {
        if (this.config == null) {
            return false;
        }

        return Instant.now().isBefore(this.configExpires);
    }

    /**
     * Get if the driver is connected
     *
     * @return If the driver is connected
     *
     * @since 3.5.0
     */
    public boolean getConnected() {
        try {
            return this.connection != null && this.connection.isValid(1);
        } catch (SQLException e) {
            throw new MySQLException(e);
        }
    }

    /**
     * Connect to the server
     *
     * @throws IOException On connect error
     * @since 1.0.0
     */
    public void connect() throws IOException {
        synchronized (SAVELOAD_LOCK) {
            try {
                Properties properties = new Properties();
                properties.put("characterEncoding", "utf8");
                if (this.username != null) {
                    properties.put("user", this.username);

                    if (this.password != null) {
                        properties.put("password", this.password);
                    }
                }

                try {
                    if (this.useMariadb) {
                        Class.forName("org.mariadb.jdbc.Driver");
                    } else {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                this.connection = DriverManager.getConnection("jdbc:" + (this.useMariadb ? "mariadb:" : "mysql:") + "//" + this.address.getHostString() + ":" + this.address.getPort() + "/" + this.database, properties);

                if (this.connection.isValid(3)) {
                    this.reconnectAttempts = 0;

                    // TODO Migrate tables if exists
                    MySQLHelper.execute(this.connection, "CREATE TABLE IF NOT EXISTS " + this.table + " (path varchar(256) CHARACTER SET utf8 NOT NULL, type varchar(16) CHARACTER SET utf8 NOT NULL, value varchar(4096) CHARACTER SET utf8, PRIMARY KEY (path)) CHARACTER SET utf8;");
                } else {
                    if (this.connection != null) {
                        this.connection.close();
                    }

                    throw new IOException("Failed to connect: Connection is not valid");
                }
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Load the config
     *
     * @param save Weather to save the config after loaded (To update the template)
     *
     * @throws IOException On load error
     * @since 1.3.0
     */
    @Override
    public void load(boolean save) throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        while (!this.getConnected()) {
            this.reconnectAttempts++;
            if (this.reconnectAttempts > 5) {
                throw new MaximumReconnectsException();
            }

            this.connect();
        }
        assert this.connection != null;

        synchronized (SAVELOAD_LOCK) {
            ParsedObject object = ParsedObject.create();

            try (ResultSet result = MySQLHelper.query(this.connection, "SELECT path,type,value FROM " + this.table)) {
                while (result.next()) {
                    String path = result.getString("path");
                    String type = result.getString("type").toUpperCase();
                    ParsedPrimitive.PrimitiveType primitiveType = ParsedPrimitive.PrimitiveType.valueOf(type);
                    String value = result.getString("value");

                    ParsedPrimitive element;
                    switch (primitiveType) {
                        case STRING: {
                            element = ParsedPrimitive.fromString(value);
                            break;
                        }
                        case BYTE: {
                            element = ParsedPrimitive.fromByte(Byte.parseByte(value));
                            break;
                        }
                        case CHAR: {
                            element = ParsedPrimitive.fromChar((char) Short.parseShort(value));
                            break;
                        }
                        case SHORT: {
                            element = ParsedPrimitive.fromShort(Short.parseShort(value));
                            break;
                        }
                        case INTEGER: {
                            element = ParsedPrimitive.fromInteger(Integer.parseInt(value));
                            break;
                        }
                        case LONG: {
                            element = ParsedPrimitive.fromLong(Long.parseLong(value));
                            break;
                        }
                        case BIGINTEGER: {
                            element = ParsedPrimitive.fromBigInteger(BigIntegerParser.parseWithFastParser(value));
                            break;
                        }
                        case FLOAT: {
                            element = ParsedPrimitive.fromFloat(Float.parseFloat(value));
                            break;
                        }
                        case DOUBLE: {
                            element = ParsedPrimitive.fromDouble(Double.parseDouble(value));
                            break;
                        }
                        case BIGDECIMAL: {
                            element = ParsedPrimitive.fromBigDecimal(BigDecimalParser.parse(value));
                            break;
                        }
                        case BOOLEAN: {
                            element = ParsedPrimitive.fromBoolean(Boolean.parseBoolean(value));
                            break;
                        }
                        case NULL: {
                            element = ParsedPrimitive.fromNull();
                            break;
                        }
                        default: {
                            throw new RuntimeException();
                        }
                    }

                    PathResolver.updateElement(object, path, element, true);
                }
            } catch (SQLException e) {
                throw new IOException(e);
            }

            this.config = this.processor.toObject(object, this.clazz);
            this.configExpires = Instant.now().plus(this.cacheLength);

            this.configBackup = object;
        }

        if (save) {
            this.save();
        }
    }

    /**
     * Save the config to the server
     *
     * @throws IOException On save error
     * @since 1.0.0
     */
    @Override
    public void save() throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }
        if (this.config == null || this.configBackup == null) {
            throw new ConfigNotLoadedException();
        }

        while (!this.getConnected()) {
            this.reconnectAttempts++;
            if (this.reconnectAttempts > 5) {
                throw new MaximumReconnectsException();
            }

            this.connect();
        }
        assert this.connection != null;

        synchronized (SAVELOAD_LOCK) {
            ParsedObject object = this.processor.toElement(this.config).asObject();
            Set<String> keys = PathResolver.getKeys(object, false);
            Set<String> oldKeys = PathResolver.getKeys(this.configBackup, false);

            Set<String> toDelete = new HashSet<>(oldKeys);
            toDelete.removeAll(keys);

            List<String> queryArgs = new ArrayList<>();
            for (String key : keys) {
                ParsedElement valueElement = PathResolver.resolveElement(object, key);
                ParsedElement oldValueElement = PathResolver.resolveElement(this.configBackup, key);

                if (valueElement != null && oldValueElement != null) {
                    ParsedPrimitive value = valueElement.asPrimitive();
                    ParsedPrimitive oldValue = oldValueElement.asPrimitive();

                    Object valueObj = value.get();
                    Object oldValueObj = oldValue.get();

                    if (!((value.isNull() && oldValue.isNull()) || (!value.isNull() && (valueObj == oldValueObj || valueObj.equals(oldValueObj))))) {
                        queryArgs.add(key);
                        queryArgs.add(value.getType().name());
                        queryArgs.add(valueObj != null ? valueObj.toString() : "null");
                    }
                }
            }

            this.configBackup = object;

            try {
                if (!queryArgs.isEmpty()) {
                    MySQLHelper.executeBatch(this.connection, "REPLACE INTO " + this.table + " (path, type, value) VALUES (?, ?, ?);", 3, queryArgs);
                }

                if (!toDelete.isEmpty()) {
                    MySQLHelper.executeBatch(this.connection, "DELETE FROM " + this.table + " WHERE path=?;", 1, List.copyOf(toDelete));
                }
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Close the config
     *
     * @throws IOException On close error
     * @since 1.0.0
     */
    @Override
    public void close() throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        this.closed = true;

        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     *
     * @since 1.0.0
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * A builder class for creating new {@link StructuredMySQLConfig}s
     *
     * @version 5.0.0
     * @since 4.0.0
     */
    public static class Builder<T> {
        /**
         * The class of the data being stored
         *
         * @since 4.0.0
         */
        protected final @NotNull Class<T> clazz;

        /**
         * The ObjectProcessor to use for serialization/deserialization
         *
         * @since 4.0.0
         */
        protected @NotNull ObjectProcessor processor;

        /**
         * The host of the server
         *
         * @since 4.0.0
         */
        protected @NotNull InetSocketAddress address;

        /**
         * The database on the server
         *
         * @since 4.0.0
         */
        protected @NotNull String database;

        /**
         * The table of the database
         *
         * @since 4.0.0
         */
        protected @NotNull String table;

        /**
         * The username to the server
         * <p>
         * Default is null
         *
         * @since 4.0.0
         */
        protected @Nullable String username = null;

        /**
         * The password to the server
         * <p>
         * Default is null
         *
         * @since 4.0.0
         */
        protected @Nullable String password = null;

        /**
         * Weather to use the MariaDB driver
         * <p>
         * Default is false
         *
         * @since 4.0.0
         */
        protected boolean useMariadb = false;

        /**
         * How long to cache the config in memory
         * <p>
         * Default is 3s
         *
         * @since 4.0.0
         */
        protected @NotNull Duration cacheLength = Duration.ofMillis(1000);

        /**
         * Create an {@link StructuredMySQLConfig} builder
         *
         * @param clazz    The class of the data being stored
         * @param address  The address of the server
         * @param database The database on the server
         * @param table    The table of the database
         *
         * @since 4.0.0
         */
        public Builder(@NotNull Class<T> clazz, @NotNull InetSocketAddress address, @NotNull String database, @NotNull String table) {
            this.clazz = clazz;

            this.processor = new ObjectProcessor.Builder().build();

            this.address = address;

            this.database = database;
            this.table = table;
        }

        /**
         * Create an {@link StructuredMySQLConfig} builder
         *
         * @param clazz    The class of the data being stored
         * @param host     The host of the server
         * @param port     The port of the server
         * @param database The database on the server
         * @param table    The table of the database
         *
         * @since 4.0.0
         * @deprecated Use {@link #Builder(Class, InetSocketAddress, String, String)} instead
         */
        @Deprecated(since="5.0.0")
        public Builder(@NotNull Class<T> clazz, @NotNull String host, short port, @NotNull String database, @NotNull String table) {
            this.clazz = clazz;

            this.processor = new ObjectProcessor.Builder().build();

            this.address = new InetSocketAddress(host, port);

            this.database = database;
            this.table = table;
        }

        /**
         * Get the class of the data being stored
         *
         * @return The class of the data being stored
         *
         * @since 4.0.0
         */
        public @NotNull Class<T> getClazz() {
            return this.clazz;
        }

        /**
         * Get the ObjectProcessor to use for serialization/deserialization
         *
         * @return The ObjectProcessor to use for serialization/deserialization
         *
         * @since 4.0.0
         */
        public @NotNull ObjectProcessor getProcessor() {
            return this.processor;
        }

        /**
         * Set the ObjectProcessor to use for serialization/deserialization
         *
         * @param processor The ObjectProcessor to use for serialization/deserialization
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setProcessor(@NotNull ObjectProcessor processor) {
            this.processor = processor;
            return this;
        }

        /**
         * Get the address of the server
         *
         * @return The address of the server
         *
         * @since 4.0.0
         */
        public @NotNull InetSocketAddress getAddress() {
            return this.address;
        }

        /**
         * Set the address of the server
         *
         * @param address The address of the server
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setAddress(@NotNull InetSocketAddress address) {
            this.address = address;
            return this;
        }

        /**
         * Get the host of the server
         *
         * @return The host of the server
         *
         * @since 4.0.0
         * @deprecated Use {@link #getAddress()} instead
         */
        @Deprecated(since="5.0.0")
        public @NotNull String getHost() {
            return this.address.getHostString();
        }

        /**
         * Set the host of the server
         *
         * @param host The host of the server
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         * @deprecated Use {@link #setAddress(InetSocketAddress)} instead
         */
        @Deprecated(since="5.0.0")
        public @NotNull Builder<T> setHost(@NotNull String host) {
            this.address = new InetSocketAddress(host, this.address.getPort());
            return this;
        }

        /**
         * Get the port of the server
         *
         * @return The port of the server
         *
         * @since 4.0.0
         * @deprecated Use {@link #getAddress()} instead
         */
        @Deprecated(since="5.0.0")
        public short getPort() {
            return (short) this.address.getPort();
        }

        /**
         * Set the port of the server
         *
         * @param port The port of the server
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         * @deprecated Use {@link #setAddress(InetSocketAddress)} instead
         */
        @Deprecated(since="5.0.0")
        public @NotNull Builder<T> setPort(short port) {
            this.address = new InetSocketAddress(this.address.getAddress(), port);
            return this;
        }

        /**
         * Get the database on the server
         *
         * @return The database on the server
         *
         * @since 4.0.0
         */
        public @NotNull String getDatabase() {
            return this.database;
        }

        /**
         * Set the database on the server
         *
         * @param database The database on the server
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setDatabase(@NotNull String database) {
            this.database = database;
            return this;
        }

        /**
         * Get the table of the database
         *
         * @return The table of the database
         *
         * @since 4.0.0
         */
        public @NotNull String getTable() {
            return this.table;
        }

        /**
         * Set the table of the database
         *
         * @param table The table of the database
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setTable(@NotNull String table) {
            this.table = table;
            return this;
        }

        /**
         * Get the username to the server
         * <p>
         * Default is null
         *
         * @return The username to the server
         *
         * @since 4.0.0
         */
        public @Nullable String getUsername() {
            return this.username;
        }

        /**
         * Set the username to the server
         * <p>
         * Default is null
         *
         * @param username The username to the server
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setUsername(@Nullable String username) {
            this.username = username;
            return this;
        }

        /**
         * Get the password to the server
         * <p>
         * Default is null
         *
         * @return The password to the server
         *
         * @since 4.0.0
         */
        public @Nullable String getPassword() {
            return this.password;
        }

        /**
         * Set the password to the server
         * <p>
         * Default is null
         *
         * @param password The password to the server
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setPassword(@Nullable String password) {
            this.password = password;
            return this;
        }

        /**
         * Get weather to use the MariaDB driver
         * <p>
         * Default is false
         *
         * @return Weather to use the MariaDB driver
         *
         * @since 4.0.0
         */
        public boolean getUseMariadb() {
            return this.useMariadb;
        }

        /**
         * Set weather to use the MariaDB driver
         * <p>
         * Default is false
         *
         * @param useMariadb Weather to use the MariaDB driver
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setUseMariadb(boolean useMariadb) {
            this.useMariadb = useMariadb;
            return this;
        }

        /**
         * Get how long to cache the config in memory
         * <p>
         * Default is 3s
         *
         * @return How long to cache the config in memory
         *
         * @since 4.0.0
         */
        public @NotNull Duration getCacheLength() {
            return this.cacheLength;
        }

        /**
         * Set how long to cache the config in memory
         * <p>
         * Default is 1s
         *
         * @param cacheLength How long to cache the config in memory
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setCacheLength(@NotNull Duration cacheLength) {
            this.cacheLength = cacheLength;
            return this;
        }

        /**
         * Set how long to cache the config in memory
         * <p>
         * Default is 1s
         *
         * @param cacheLength How long to cache the config in memory
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         * @deprecated Use {@link #setCacheLength(Duration)} instead
         */
        @Deprecated(since="5.0.0")
        public @NotNull Builder<T> setCacheLength(long cacheLength) {
            this.cacheLength = Duration.ofMillis(cacheLength);
            return this;
        }

        /**
         * Creating a new {@link io.github.kale_ko.ejcl.mysql.StructuredMySQLConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.mysql.StructuredMySQLConfig}
         *
         * @since 4.0.0
         */
        public @NotNull StructuredMySQLConfig<T> build() {
            return new StructuredMySQLConfig<>(this.clazz, this.address, this.database, this.table, this.username, this.password, this.useMariadb, this.cacheLength, this.processor);
        }
    }
}