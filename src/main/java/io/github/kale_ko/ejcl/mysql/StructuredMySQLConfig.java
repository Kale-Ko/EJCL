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
 * A Structured MySQL Config for storing data on a MySQL server
 *
 * @param <T> The type of the data being stored
 *
 * @version 3.9.0
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
    protected final int port;

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
     * Create a new MySQLConfig
     *
     * @param clazz       The class of the data being stored
     * @param host        The host of the server
     * @param port        The port of the server
     * @param database    The database on the server
     * @param table       The table of the database
     * @param username    The username to the server
     * @param password    The password to the server
     * @param processor   The ObjectProcessor to use for serialization/deserialization
     * @param cacheLength How long to cache the config in memory
     *
     * @since 2.0.0
     */
    public StructuredMySQLConfig(@NotNull Class<T> clazz, @NotNull String host, int port, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password, @NotNull ObjectProcessor processor, long cacheLength) {
        super(clazz);

        this.processor = processor;

        this.host = host;
        this.port = port;

        this.database = database;
        this.table = table;

        this.username = username;
        this.password = password;

        this.cacheLength = cacheLength;
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz     The class of the data being stored
     * @param host      The host of the server
     * @param port      The port of the server
     * @param database  The database on the server
     * @param table     The table of the database
     * @param username  The username to the server
     * @param password  The password to the server
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public StructuredMySQLConfig(@NotNull Class<T> clazz, @NotNull String host, int port, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password, @NotNull ObjectProcessor processor) {
        this(clazz, host, port, database, table, username, password, processor, 1L);
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz    The class of the data being stored
     * @param host     The host of the server
     * @param port     The port of the server
     * @param database The database on the server
     * @param table    The table of the database
     * @param username The username to the server
     * @param password The password to the server
     *
     * @since 2.0.0
     */
    public StructuredMySQLConfig(@NotNull Class<T> clazz, @NotNull String host, int port, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password) {
        this(clazz, host, port, database, table, username, password, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz       The class of the data being stored
     * @param host        The host of the server
     * @param port        The port of the server
     * @param database    The database on the server
     * @param table       The table of the database
     * @param username    The username to the server
     * @param password    The password to the server
     * @param cacheLength How long to cache the config in memory
     *
     * @since 2.0.0
     */
    public StructuredMySQLConfig(@NotNull Class<T> clazz, @NotNull String host, int port, @NotNull String database, @NotNull String table, @Nullable String username, @Nullable String password, long cacheLength) {
        this(clazz, host, port, database, table, username, password, new ObjectProcessor.Builder().build(), cacheLength);
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz     The class of the data being stored
     * @param host      The host of the server
     * @param port      The port of the server
     * @param database  The database on the server
     * @param table     The table of the database
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public StructuredMySQLConfig(@NotNull Class<T> clazz, @NotNull String host, int port, @NotNull String database, @NotNull String table, @NotNull ObjectProcessor processor) {
        this(clazz, host, port, database, table, null, null, processor);
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz    The class of the data being stored
     * @param host     The host of the server
     * @param port     The port of the server
     * @param database The database on the server
     * @param table    The table of the database
     *
     * @since 2.0.0
     */
    public StructuredMySQLConfig(@NotNull Class<T> clazz, @NotNull String host, int port, @NotNull String database, @NotNull String table) {
        this(clazz, host, port, database, table, null, null, new ObjectProcessor.Builder().build());
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
                Object value = PathResolver.resolve(object, key, false);
                Object oldValue = PathResolver.resolve(this.configBackup, key, false);

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
}