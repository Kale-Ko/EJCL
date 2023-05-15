package io.github.kale_ko.ejcl.mysql;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.Config;
import io.github.kale_ko.ejcl.PathResolver;

/**
 * A MySQL Config for storing data on a MySQL server
 *
 * @param <T>
 *        The type of the data being stored
 * @version 2.0.0
 * @since 1.0.0
 */
public class MySQLConfig<T> extends Config<T> {
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
     * When the config expires next
     *
     * @since 1.1.0
     */
    protected long configExpires = -1l;

    /**
     * How many times we have tried to reconnect
     *
     * @since 2.3.0
     */
    protected int reconnectAttempts = 0;

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
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    public MySQLConfig(Class<T> clazz, String host, int port, String database, String table, String username, String password, ObjectProcessor processor) {
        super(clazz, processor);

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

        this.host = host;
        this.port = port;

        this.database = database;
        this.table = table;

        this.username = username;
        this.password = password;

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
     * @since 2.0.0
     */
    public MySQLConfig(Class<T> clazz, String host, int port, String database, String table, String username, String password) {
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
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @since 2.0.0
     */
    public MySQLConfig(Class<T> clazz, String host, int port, String database, String table, ObjectProcessor processor) {
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
    public MySQLConfig(Class<T> clazz, String host, int port, String database, String table) {
        this(clazz, host, port, database, table, null, null, new ObjectProcessor.Builder().build());
    }

    /**
     * Get a path being stored
     *
     * @param path
     *        The path to get
     * @return The value being stored
     * @since 2.0.0
     */
    @Override
    public Object get(String path) {
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
            throw new RuntimeException(new IOException(e));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Object value = null;

        try {
            ResultSet result = this.query("SELECT path,value FROM " + this.table + " WHERE path=\"" + path + "\"");

            while (result.next()) {
                value = result.getString("value");
            }

            result.getStatement().close();
            result.close();
        } catch (SQLException e) {
            throw new RuntimeException(new IOException(e));
        }

        super.set(path, value);

        return value;
    }

    /**
     * Set a path being stored
     *
     * @param path
     *        The path to set
     * @param value
     *        The value to set
     * @since 2.0.0
     */
    @Override
    public void set(String path, Object value) {
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
            throw new RuntimeException(new IOException(e));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            super.set(path, value);

            this.execute("REPLACE INTO " + this.table + " (path, value) VALUES (\"" + path + "\", \"" + (value != null ? value.toString() : "null") + "\");");
        } catch (SQLException e) {
            throw new RuntimeException(new IOException(e));
        }
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

        try {
            if (this.connection == null || !this.connection.isValid(5)) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();

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

            reconnectAttempts = 0;

            this.execute("CREATE TABLE IF NOT EXISTS " + this.table + " (path varchar(256) CHARACTER SET utf8 NOT NULL, value varchar(4096) CHARACTER SET utf8, PRIMARY KEY (path)) CHARACTER SET utf8;");
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
                PathResolver.update(object, result.getString("path"), result.getString("value"));
            }

            result.getStatement().close();
            result.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.config = this.processor.toObject(object, this.clazz);
        this.configExpires = Instant.now().getEpochSecond() + 5;

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

        ParsedObject object = this.processor.toElement(this.config).asObject();

        List<String> keys = PathResolver.getKeys(object, false);

        for (String key : keys) {
            try {
                Object value = PathResolver.resolve(object, key, false);

                this.execute("REPLACE INTO " + this.table + " (path, value) VALUES (\"" + key + "\", \"" + (value != null ? value.toString() : "null") + "\");");
            } catch (SQLException e) {
                throw new RuntimeException(new IOException(e));
            }
        }
    }

    /**
     * Execute a mysql statement
     *
     * @param query
     *        The base query
     * @return If the statement was successful
     * @throws SQLException
     *         On sql error
     * @since 1.0.0
     */
    protected boolean execute(String query) throws SQLException {
        Statement statement = this.connection.createStatement();

        boolean result = statement.execute(query);
        statement.close();

        return result;
    }

    /**
     * Execute a mysql query
     *
     * @param query
     *        The base query
     * @return The result of the query
     * @throws SQLException
     *         On sql error
     * @since 1.0.0
     */
    protected ResultSet query(String query) throws SQLException {
        Statement statement = this.connection.createStatement();

        ResultSet results = statement.executeQuery(query);

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