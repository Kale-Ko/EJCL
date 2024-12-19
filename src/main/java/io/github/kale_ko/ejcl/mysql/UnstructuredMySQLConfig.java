package io.github.kale_ko.ejcl.mysql;

import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.UnstructuredConfig;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.exception.mysql.MaximumReconnectsException;
import io.github.kale_ko.ejcl.exception.mysql.MySQLException;
import io.github.kale_ko.ejcl.mysql.helper.MySQLHelper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An Unstructured MySQL Config for storing data on a MySQL or MariaDB server
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class UnstructuredMySQLConfig extends UnstructuredConfig {
    /**
     * The host of the server
     *
     * @since 3.0.0
     */
    protected final @NotNull InetSocketAddress address;

    /**
     * The database on the server
     *
     * @since 3.0.0
     */
    protected final @NotNull String database;

    /**
     * The table of the database
     *
     * @since 3.0.0
     */
    protected final @NotNull String table;

    /**
     * The username to the server
     *
     * @since 3.0.0
     */
    protected final @Nullable String username;

    /**
     * The password to the server
     *
     * @since 3.0.0
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
     * @since 3.0.0
     */
    protected @Nullable Connection connection;

    /**
     * How many times we have tried to reconnect
     *
     * @since 2.3.0
     */
    protected int reconnectAttempts = 0;

    /**
     * The lock used when saving and loading the config
     *
     * @since 3.8.0
     */
    protected final Object SAVELOAD_LOCK = new Object();

    /**
     * If this config is closed
     *
     * @since 3.0.0
     */
    protected boolean closed = false;

    /**
     * Create a new UnstructuredMySQLConfig
     *
     * @param address    The address of the server
     * @param database   The database on the server
     * @param table      The table of the database
     * @param username   The username to the server
     * @param password   The password to the server
     * @param useMariadb Weather to use the MariaDB driver
     * @param processor  The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected UnstructuredMySQLConfig(@NotNull InetSocketAddress address, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password, boolean useMariadb, @NotNull ObjectProcessor processor) {
        super(processor);

        this.address = address;

        this.database = database;
        this.table = table;

        this.username = username;
        this.password = password;

        this.useMariadb = useMariadb;
    }

    /**
     * Get a path being stored
     *
     * @param path The path to get
     *
     * @return The value being stored
     *
     * @since 3.0.0
     */
    @Override
    public @Nullable String get(@NotNull String path) {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        try {
            if (!this.getConnected()) {
                reconnectAttempts++;
                if (reconnectAttempts > 5) {
                    throw new MaximumReconnectsException();
                }

                this.connect();
            }

            assert this.connection != null;
        } catch (IOException e) {
            throw new MySQLException(e);
        }

        try (ResultSet result = MySQLHelper.query(this.connection, "SELECT value FROM " + this.table + " WHERE path=?", path)) {
            String value = null;

            while (result.next()) {
                value = result.getString("value");
            }

            return value;
        } catch (SQLException e) {
            throw new MySQLException(e);
        }
    }

    /**
     * Get a path being stored
     * <p>
     * This method <b>will not</b> check if the connection is open before attempting to query the database
     *
     * @param path The path to get
     *
     * @return The value being stored
     *
     * @since 3.5.0
     */
    public @Nullable String fastGet(@NotNull String path) {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        if (this.connection == null) {
            return null;
        }

        try (ResultSet result = MySQLHelper.query(this.connection, "SELECT value FROM " + this.table + " WHERE path=?", path)) {
            String value = null;

            while (result.next()) {
                value = result.getString("value");
            }

            return value;
        } catch (SQLException e) {
            throw new MySQLException(e);
        }
    }

    @Override
    public @Nullable String getCached(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set a path being stored
     *
     * @param path  The path to set
     * @param value The value to set
     *
     * @since 3.0.0
     */
    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        try {
            if (!this.getConnected()) {
                reconnectAttempts++;
                if (reconnectAttempts > 5) {
                    throw new MaximumReconnectsException();
                }

                this.connect();
            }

            assert this.connection != null;
        } catch (IOException e) {
            throw new MySQLException(e);
        }

        try {
            MySQLHelper.execute(this.connection, "REPLACE INTO " + this.table + " (path, value) VALUES (?, ?);", path, (value != null ? value.toString() : "null"));
        } catch (SQLException e) {
            throw new MySQLException(e);
        }
    }

    /**
     * Set a path being stored
     * <p>
     * This method <b>will not</b> check if the connection is open before attempting to query the database
     *
     * @param path  The path to set
     * @param value The value to set
     *
     * @since 3.5.0
     */
    public void fastSet(@NotNull String path, @Nullable Object value) {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        if (this.connection == null) {
            return;
        }

        try {
            MySQLHelper.execute(this.connection, "REPLACE INTO " + this.table + " (path, value) VALUES (?, ?);", path, (value != null ? value.toString() : "null"));
        } catch (SQLException e) {
            throw new MySQLException(e);
        }
    }

    /**
     * Get if the config is loaded
     *
     * @return If the config is loaded
     *
     * @since 3.0.0
     */
    @Override
    public boolean getLoaded() {
        return true;
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
     * @since 3.0.0
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
                    reconnectAttempts = 0;

                    MySQLHelper.execute(this.connection, "CREATE TABLE IF NOT EXISTS " + this.table + " (path varchar(256) CHARACTER SET utf8 NOT NULL, value varchar(4096) CHARACTER SET utf8, PRIMARY KEY (path)) CHARACTER SET utf8;");
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
    }

    /**
     * Save the config to the server
     *
     * @throws IOException On save error
     * @since 3.0.0
     */
    @Override
    public void save() throws IOException {
    }

    /**
     * Close the config
     *
     * @throws IOException On close error
     * @since 3.0.0
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
     * @since 3.0.0
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * A builder class for creating new {@link UnstructuredMySQLConfig}s
     *
     * @version 4.0.0
     * @since 4.0.0
     */
    public static class Builder {
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
         * Create an {@link UnstructuredMySQLConfig} builder
         *
         * @param address  The address of the server
         * @param database The database on the server
         * @param table    The table of the database
         *
         * @since 4.0.0
         */
        public Builder(@NotNull InetSocketAddress address, @NotNull String database, @NotNull String table) {
            this.processor = new ObjectProcessor.Builder().build();

            this.address = address;

            this.database = database;
            this.table = table;
        }

        /**
         * Create an {@link UnstructuredMySQLConfig} builder
         *
         * @param host     The host of the server
         * @param port     The port of the server
         * @param database The database on the server
         * @param table    The table of the database
         *
         * @since 4.0.0
         * @deprecated Use {@link #Builder(InetSocketAddress, String, String)} instead
         */
        @Deprecated
        public Builder(@NotNull String host, short port, @NotNull String database, @NotNull String table) {
            this.processor = new ObjectProcessor.Builder().build();

            this.address = new InetSocketAddress(host, port);

            this.database = database;
            this.table = table;
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
        public @NotNull Builder setProcessor(@NotNull ObjectProcessor processor) {
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
        public @NotNull Builder setAddress(@NotNull InetSocketAddress address) {
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
        @Deprecated
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
        @Deprecated
        public @NotNull Builder setHost(@NotNull String host) {
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
        @Deprecated
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
        @Deprecated
        public @NotNull Builder setPort(short port) {
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
        public @NotNull Builder setDatabase(@NotNull String database) {
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
        public @NotNull Builder setTable(@NotNull String table) {
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
        public @NotNull Builder setUsername(@Nullable String username) {
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
        public @NotNull Builder setPassword(@Nullable String password) {
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
        public @NotNull Builder setUseMariadb(boolean useMariadb) {
            this.useMariadb = useMariadb;
            return this;
        }

        /**
         * Creating a new {@link io.github.kale_ko.ejcl.mysql.UnstructuredMySQLConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.mysql.UnstructuredMySQLConfig}
         *
         * @since 4.0.0
         */
        public @NotNull UnstructuredMySQLConfig build() {
            return new UnstructuredMySQLConfig(this.address, this.database, this.table, this.username, this.password, this.useMariadb, this.processor);
        }
    }
}