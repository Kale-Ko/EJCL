package io.github.kale_ko.ejcl.mysql;

import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.PathResolver;
import io.github.kale_ko.ejcl.StructuredConfig;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.exception.ConfigNotLoadedException;
import io.github.kale_ko.ejcl.exception.mysql.MaximumReconnectsException;
import io.github.kale_ko.ejcl.exception.mysql.MySQLException;
import io.github.kale_ko.ejcl.mysql.driver.MySQL;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Structured MySQL Config for storing data on a MySQL or MariaDB server
 *
 * @param <T> The type of the data being stored
 *
 * @version 4.0.0
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
     * The host of the server
     *
     * @since 1.0.0
     */
    protected final @NotNull String host;

    /**
     * The port of the server
     *
     * @since 1.0.0
     */
    protected final short port;

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
    protected final long cacheLength;

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
    protected long configExpires = -1L;

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
    protected final Object SAVELOAD_LOCK = new Object();

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
     * @param host        The host of the server
     * @param port        The port of the server
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
    protected StructuredMySQLConfig(@NotNull Class<T> clazz, @NotNull String host, short port, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password, boolean useMariadb, long cacheLength, @NotNull ObjectProcessor processor) {
        super(clazz);

        this.processor = processor;

        this.host = host;
        this.port = port;

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

        return Instant.now().getEpochSecond() < configExpires;
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
            return this.connection != null && this.connection.isValid(2);
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

                this.connection = DriverManager.getConnection("jdbc:" + (this.useMariadb ? "mariadb:" : "mysql:") + "//" + this.host + ":" + this.port + "/" + this.database, properties);

                if (this.connection.isValid(3)) {
                    reconnectAttempts = 0;

                    MySQL.execute(this.connection, "CREATE TABLE IF NOT EXISTS " + this.table + " (path varchar(256) CHARACTER SET utf8 NOT NULL, value varchar(4096) CHARACTER SET utf8, PRIMARY KEY (path)) CHARACTER SET utf8;");
                } else {
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

        if (!this.getConnected()) {
            reconnectAttempts++;
            if (reconnectAttempts > 5) {
                throw new MaximumReconnectsException();
            }

            this.connect();
        }
        assert this.connection != null;

        synchronized (SAVELOAD_LOCK) {
            ParsedObject object = ParsedObject.create();

            try (ResultSet result = MySQL.query(this.connection, "SELECT path,value FROM " + this.table)) {
                while (result.next()) {
                    PathResolver.update(object, result.getString("path"), result.getString("value"), true);
                }
            } catch (SQLException e) {
                throw new IOException(e);
            }

            this.config = this.processor.toObject(object, this.clazz);
            this.configExpires = Instant.now().getEpochSecond() + this.cacheLength;

            this.configBackup = this.processor.toElement(this.config).asObject();
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

        if (!this.getConnected()) {
            reconnectAttempts++;
            if (reconnectAttempts > 5) {
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
                Object value = PathResolver.resolve(object, key);
                Object oldValue = PathResolver.resolve(this.configBackup, key);

                if (!((value == null && oldValue == null) || (value != null && (value == oldValue || value.equals(oldValue))))) {
                    queryArgs.add(key);
                    queryArgs.add(value != null ? value.toString() : "null");
                }
            }

            this.configBackup = object;

            try {
                if (!queryArgs.isEmpty()) {
                    MySQL.executeBatch(this.connection, "REPLACE INTO " + this.table + " (path, value) VALUES (?, ?);", 2, queryArgs);
                }

                if (!toDelete.isEmpty()) {
                    MySQL.executeBatch(this.connection, "DELETE FROM " + this.table + " WHERE path=?;", 1, List.copyOf(toDelete));
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
     * @version 4.0.0
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
        protected @NotNull String host;

        /**
         * The port of the server
         *
         * @since 4.0.0
         */
        protected short port;

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
        protected long cacheLength = 3000;

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
         */
        public Builder(@NotNull Class<T> clazz, @NotNull String host, short port, @NotNull String database, @NotNull String table) {
            this.clazz = clazz;

            this.processor = new ObjectProcessor.Builder().build();

            this.host = host;
            this.port = port;

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
         * Get the host of the server
         *
         * @return The host of the server
         *
         * @since 4.0.0
         */
        public @NotNull String getHost() {
            return this.host;
        }

        /**
         * Set the host of the server
         *
         * @param host The host of the server
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setHost(@NotNull String host) {
            this.host = host;
            return this;
        }

        /**
         * Get the port of the server
         *
         * @return The port of the server
         *
         * @since 4.0.0
         */
        public short getPort() {
            return this.port;
        }

        /**
         * Set the port of the server
         *
         * @param port The port of the server
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setPort(short port) {
            this.port = port;
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
        public long getCacheLength() {
            return this.cacheLength;
        }

        /**
         * Set how long to cache the config in memory
         * <p>
         * Default is 3s
         *
         * @param cacheLength How long to cache the config in memory
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setCacheLength(long cacheLength) {
            this.cacheLength = cacheLength;
            return this;
        }

        public @NotNull StructuredMySQLConfig<T> build() {
            return new StructuredMySQLConfig<>(this.clazz, this.host, this.port, this.database, this.table, this.username, this.password, this.useMariadb, this.cacheLength, this.processor);
        }
    }
}