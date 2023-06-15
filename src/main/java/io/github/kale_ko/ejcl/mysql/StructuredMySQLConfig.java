package io.github.kale_ko.ejcl.mysql;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.PathResolver;
import io.github.kale_ko.ejcl.StructuredConfig;

/**
 * A MySQL Config for storing data on a MySQL server
 *
 * @param <T>
 *        The type of the data being stored
 * @version 2.0.0
 * @since 1.0.0
 */
public class StructuredMySQLConfig<T> extends StructuredConfig<T> {
    /**
     * The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected ObjectProcessor processor;

    /**
     * The host of the server
     *
     * @since 1.0.0
     */
    protected String host;

    /**
     * The port of the server
     *
     * @since 1.0.0
     */
    protected int port;

    /**
     * The database on the server
     *
     * @since 1.0.0
     */
    protected String database;

    /**
     * The table of the database
     *
     * @since 1.0.0
     */
    protected String table;

    /**
     * The username to the server
     *
     * @since 1.0.0
     */
    protected String username;

    /**
     * The password to the server
     *
     * @since 1.0.0
     */
    protected String password;

    /**
     * The connection to the server
     *
     * @since 1.0.0
     */
    protected Connection connection;

    /**
     * How long to cache the config in memory
     *
     * @since 3.2.0
     */
    protected long cacheLength = 5l;

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
    protected long configExpires = -1l;

    /**
     * If this config is closed
     *
     * @since 1.0.0
     */
    protected boolean closed = false;

    /**
     * Create a new MySQLConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param host
     *        The host of the server
     * @param port
     *        The port of the server
     * @param database
     *        The database on the server
     * @param table
     *        The table of the database
     * @param username
     *        The username to the server
     * @param password
     *        The password to the server
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @param cacheLength
     *        How long to cache the config in memory
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    public StructuredMySQLConfig(Class<T> clazz, String host, int port, String database, String table, String username, String password, ObjectProcessor processor, long cacheLength) {
        super(clazz);

        if (processor == null) {
            throw new NullPointerException("Processor can not be null");
        }
        if (host == null) {
            throw new NullPointerException("Host can not be null");
        }
        if (database == null) {
            throw new NullPointerException("Database can not be null");
        }
        if (table == null) {
            throw new NullPointerException("Table can not be null");
        }

        this.processor = processor;

        this.host = host;
        this.port = port;

        this.database = database;
        this.table = table;

        this.username = username;
        this.password = password;

        this.cacheLength = cacheLength;

        if (clazz.getConstructors().length > 0) {
            try {
                for (Constructor<?> constructor : clazz.getConstructors()) {
                    if ((constructor.canAccess(null) || constructor.trySetAccessible()) && constructor.getParameterTypes().length == 0) {
                        this.config = (T) constructor.newInstance();

                        break;
                    }
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            }
        } else {
            try {
                Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
                this.config = (T) unsafe.allocateInstance(clazz);
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            }
        }

        if (this.config == null) {
            throw new RuntimeException("Could not instantiate new config");
        }
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param host
     *        The host of the server
     * @param port
     *        The port of the server
     * @param database
     *        The database on the server
     * @param table
     *        The table of the database
     * @param username
     *        The username to the server
     * @param password
     *        The password to the server
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @since 2.0.0
     */
    public StructuredMySQLConfig(Class<T> clazz, String host, int port, String database, String table, String username, String password, ObjectProcessor processor) {
        this(clazz, host, port, database, table, username, password, processor, 5l);
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param host
     *        The host of the server
     * @param port
     *        The port of the server
     * @param database
     *        The database on the server
     * @param table
     *        The table of the database
     * @param username
     *        The username to the server
     * @param password
     *        The password to the server
     * @since 2.0.0
     */
    public StructuredMySQLConfig(Class<T> clazz, String host, int port, String database, String table, String username, String password) {
        this(clazz, host, port, database, table, username, password, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param host
     *        The host of the server
     * @param port
     *        The port of the server
     * @param database
     *        The database on the server
     * @param table
     *        The table of the database
     * @param username
     *        The username to the server
     * @param password
     *        The password to the server
     * @param cacheLength
     *        How long to cache the config in memory
     * @since 2.0.0
     */
    public StructuredMySQLConfig(Class<T> clazz, String host, int port, String database, String table, String username, String password, long cacheLength) {
        this(clazz, host, port, database, table, username, password, new ObjectProcessor.Builder().build(), cacheLength);
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param host
     *        The host of the server
     * @param port
     *        The port of the server
     * @param database
     *        The database on the server
     * @param table
     *        The table of the database
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @since 2.0.0
     */
    public StructuredMySQLConfig(Class<T> clazz, String host, int port, String database, String table, ObjectProcessor processor) {
        this(clazz, host, port, database, table, null, null, processor);
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param host
     *        The host of the server
     * @param port
     *        The port of the server
     * @param database
     *        The database on the server
     * @param table
     *        The table of the database
     * @since 2.0.0
     */
    public StructuredMySQLConfig(Class<T> clazz, String host, int port, String database, String table) {
        this(clazz, host, port, database, table, null, null, new ObjectProcessor.Builder().build());
    }

    /**
     * Get if the config is loaded
     *
     * @return If the config is loaded
     * @since 1.0.0
     */
    @Override
    public boolean getLoaded() {
        if (this.config == null) {
            return false;
        }

        if (Instant.now().getEpochSecond() >= configExpires) {
            return false;
        }

        return true;
    }

    /**
     * Connect to the server
     *
     * @throws IOException
     *         On connect error
     * @since 1.0.0
     */
    public void connect() throws IOException {
        try {
            Properties properties = new Properties();
            properties.put("characterEncoding", "utf8");
            if (this.username != null) {
                properties.put("user", this.username);

                if (this.password != null) {
                    properties.put("password", this.password);
                }
            }
            properties.put("autoReconnect", true);
            properties.put("createDatabaseIfNotExist", true);
            properties.put("allowNanAndInf", true);

            if (this.username != null) {
                if (this.password != null) {
                    this.connection = DriverManager.getConnection("jdbc:mysql://" + this.username + ":" + this.password + "@" + this.host + ":" + this.port + "/" + this.database, properties);
                } else {
                    this.connection = DriverManager.getConnection("jdbc:mysql://" + this.username + "@" + this.host + ":" + this.port + "/" + this.database, properties);
                }
            } else {
                this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, properties);
            }

            this.execute("CREATE TABLE IF NOT EXISTS " + this.table + " (path varchar(256) CHARACTER SET utf8 NOT NULL, value varchar(4096) CHARACTER SET utf8, PRIMARY KEY (path)) CHARACTER SET utf8;");

            reconnectAttempts = 0;
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Load the config
     *
     * @param save
     *        Weather to save the config after loaded (To update the template)
     * @throws IOException
     *         On load error
     * @since 1.3.0
     */
    @Override
    public void load(boolean save) throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        try {
            if (this.connection == null || !this.connection.isValid(5)) {
                reconnectAttempts++;
                if (reconnectAttempts > 5) {
                    throw new RuntimeException("Maximum reconnects reached");
                }

                this.connect();
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }

        ParsedObject object = this.processor.toElement(this.config).asObject();

        try {
            ResultSet result = this.query("SELECT path,value FROM " + this.table);

            while (result.next()) {
                PathResolver.update(object, result.getString("path"), result.getString("value"), true);
            }

            result.getStatement().close();
            result.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.config = this.processor.toObject(object, this.clazz);
        this.configExpires = Instant.now().getEpochSecond() + this.cacheLength;

        if (save) {
            this.save();
        }
    }

    /**
     * Save the config to the server
     *
     * @throws IOException
     *         On save error
     * @since 1.0.0
     */
    @Override
    public void save() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        try {
            if (this.connection == null || !this.connection.isValid(5)) {
                reconnectAttempts++;
                if (reconnectAttempts > 5) {
                    throw new RuntimeException("Maximum reconnects reached");
                }

                this.connect();
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }

        ParsedObject currentObject = ParsedObject.create();
        try {
            ResultSet result = this.query("SELECT path,value FROM " + this.table);

            while (result.next()) {
                currentObject.set(result.getString("path"), ParsedPrimitive.fromString(result.getString("value")));
            }

            result.getStatement().close();
            result.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }

        ParsedObject object = this.processor.toElement(this.config).asObject();
        List<String> keys = PathResolver.getKeys(object, false);

        for (String key : keys) {
            Object value = PathResolver.resolve(object, key, false);

            if (!currentObject.has(key) || !currentObject.get(key).asPrimitive().asString().equals(value != null ? value.toString() : "null")) {
                try {
                    this.execute("REPLACE INTO " + this.table + " (path, value) VALUES (?, ?);", key, (value != null ? value.toString() : "null"));
                } catch (SQLException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    /**
     * Execute a mysql statement
     *
     * @param query
     *        The base query
     * @param args
     *        Extra args
     * @return If the statement was successful
     * @throws SQLException
     *         On sql error
     * @since 1.0.0
     */
    protected boolean execute(String query, String... args) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            statement.setString(i + 1, args[i]);
        }

        boolean result = statement.execute();
        statement.close();

        return result;
    }

    /**
     * Execute a mysql query
     *
     * @param query
     *        The base query
     * @param args
     *        Extra args
     * @return The result of the query
     * @throws SQLException
     *         On sql error
     * @since 1.0.0
     */
    protected ResultSet query(String query, String... args) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            statement.setString(i + 1, args[i]);
        }

        ResultSet results = statement.executeQuery();

        return results;
    }

    /**
     * Close the config
     *
     * @throws IOException
     *         On close error
     * @since 1.0.0
     */
    @Override
    public void close() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        this.closed = true;

        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     * @since 1.0.0
     */
    public boolean isClosed() {
        return this.closed;
    }
}