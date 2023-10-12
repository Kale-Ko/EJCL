package io.github.kale_ko.ejcl.mysql;

import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.UnstructuredConfig;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.exception.mysql.MaximumReconnectsException;
import io.github.kale_ko.ejcl.exception.mysql.MySQLException;
import io.github.kale_ko.ejcl.mysql.driver.MySQL;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An Unstructured MySQL Config for storing data on a MySQL server
 *
 * @version 3.9.0
 * @since 3.0.0
 */
public class UnstructuredMySQLConfig extends UnstructuredConfig {
    /**
     * The host of the server
     *
     * @since 3.0.0
     */
    protected final @NotNull String host;

    /**
     * The port of the server
     *
     * @since 3.0.0
     */
    protected final int port;

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
     * Create a new MySQLConfig
     *
     * @param host      The host of the server
     * @param port      The port of the server
     * @param database  The database on the server
     * @param table     The table of the database
     * @param username  The username to the server
     * @param password  The password to the server
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    public UnstructuredMySQLConfig(@NotNull String host, int port, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password, @NotNull ObjectProcessor processor) {
        super(processor);

        this.host = host;
        this.port = port;

        this.database = database;
        this.table = table;

        this.username = username;
        this.password = password;
    }

    /**
     * Create a new MySQLConfig
     *
     * @param host     The host of the server
     * @param port     The port of the server
     * @param database The database on the server
     * @param table    The table of the database
     * @param username The username to the server
     * @param password The password to the server
     *
     * @since 3.0.0
     */
    public UnstructuredMySQLConfig(@NotNull String host, int port, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password) {
        this(host, port, database, table, username, password, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new MySQLConfig
     *
     * @param host      The host of the server
     * @param port      The port of the server
     * @param database  The database on the server
     * @param table     The table of the database
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    public UnstructuredMySQLConfig(@NotNull String host, int port, @NotNull String database, @NotNull String table, @NotNull ObjectProcessor processor) {
        this(host, port, database, table, null, null, processor);
    }

    /**
     * Create a new MySQLConfig
     *
     * @param host     The host of the server
     * @param port     The port of the server
     * @param database The database on the server
     * @param table    The table of the database
     *
     * @since 3.0.0
     */
    public UnstructuredMySQLConfig(@NotNull String host, int port, @NotNull String database, @NotNull String table) {
        this(host, port, database, table, null, null, new ObjectProcessor.Builder().build());
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

        try (ResultSet result = MySQL.query(this.connection, "SELECT value FROM " + this.table + " WHERE path=?", path)) {
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

        try (ResultSet result = MySQL.query(this.connection, "SELECT value FROM " + this.table + " WHERE path=?", path)) {
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
            MySQL.execute(this.connection, "REPLACE INTO " + this.table + " (path, value) VALUES (?, ?);", path, (value != null ? value.toString() : "null"));
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
            MySQL.execute(this.connection, "REPLACE INTO " + this.table + " (path, value) VALUES (?, ?);", path, (value != null ? value.toString() : "null"));
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

                this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, properties);

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
}