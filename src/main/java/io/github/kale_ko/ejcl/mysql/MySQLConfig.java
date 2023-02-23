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
import java.util.ArrayList;
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
 * @version 1.0.0
 * @since 1.0.0
 */
public class MySQLConfig<T> extends Config<T> {
    /**
     * The ObjectProcessor to use for serialization/deserialization
     * 
     * @since 1.0.0
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
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
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
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public MySQLConfig(Class<T> clazz, ObjectProcessor processor, String host, int port, String database, String table, String username, String password) {
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

        try {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if ((constructor.canAccess(null) || constructor.trySetAccessible()) && constructor.getParameterTypes().length == 0) {
                    this.config = (T) constructor.newInstance();

                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
        }

        if (this.config == null) {
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
     * @since 1.0.0
     */
    public MySQLConfig(Class<T> clazz, String host, int port, String database, String table, String username, String password) {
        this(clazz, new ObjectProcessor.Builder().build(), host, port, database, table, username, password);
    }

    /**
     * Create a new MySQLConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @param host
     *        The host of the server
     * @param port
     *        The port of the server
     * @param database
     *        The database on the server
     * @param table
     *        The table of the database
     * @since 1.0.0
     */
    public MySQLConfig(Class<T> clazz, ObjectProcessor processor, String host, int port, String database, String table) {
        this(clazz, processor, host, port, database, table, null, null);
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
     * @since 1.0.0
     */
    public MySQLConfig(Class<T> clazz, String host, int port, String database, String table) {
        this(clazz, new ObjectProcessor.Builder().build(), host, port, database, table, null, null);
    }

    /**
     * Get if the config is loaded
     * 
     * @return If the config is loaded
     * @since 1.0.0
     */
    @Override
    public boolean getLoaded() {
        return this.config != null;
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

            this.execute("CREATE TABLE IF NOT EXISTS ? (path varchar(512) CHARACTER SET utf8, value varchar(512) CHARACTER SET utf8) CHARACTER SET utf8;", table);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Load the config from the server
     * 
     * @throws IOException
     *         On load error
     * @since 1.0.0
     */
    public void load() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }
        if (this.connection == null) {
            throw new RuntimeException("Config is not connected");
        }

        ParsedObject object = this.processor.toElement(this.config).asObject();

        List<String> keys = PathResolver.getKeys(object);

        try {
            ResultSet existsResult = this.query("SELECT * FROM ?", this.table);

            while (existsResult.next()) {
                if (keys.contains(existsResult.getString("path"))) {
                    PathResolver.update(object, existsResult.getString("path"), existsResult.getString("value"));
                }
            }

            existsResult.getStatement().close();
            existsResult.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }

        this.config = this.processor.toObject(object, this.clazz);
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
        if (this.connection == null) {
            throw new RuntimeException("Config is not connected");
        }

        ParsedObject object = this.processor.toElement(this.config).asObject();

        List<String> keys = PathResolver.getKeys(object);

        try {
            List<String> exists = new ArrayList<String>();
            ResultSet existsResult = this.query("SELECT * FROM ?", this.table);

            while (existsResult.next()) {
                exists.add(existsResult.getString("path"));
            }

            existsResult.getStatement().close();
            existsResult.close();

            for (String key : keys) {
                String value = PathResolver.resolve(object, key).toString();

                if (!exists.contains(key)) {
                    this.execute("INSERT INTO ? (path, value) VALUES (?, ?);", this.table, key, value);
                } else {
                    this.execute("UPDATE ? SET value=? WHERE path=?;", this.table, value, key);
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Execute a mysql statement
     * 
     * @param query
     *        The base query
     * @param params
     *        Any parameters to be safely passed
     * @return If the statement was successful
     * @throws SQLException
     *         On sql error
     * @since 1.0.0
     */
    protected boolean execute(String query, String... params) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(query);

        int i = 1;
        for (String param : params) {
            statement.setString(i, param);
            i++;
        }

        boolean result = statement.execute(query);
        statement.close();

        return result;
    }

    /**
     * Execute a mysql query
     * 
     * @param query
     *        The base query
     * @param params
     *        Any parameters to be safely passed
     * @return The result of the query
     * @throws SQLException
     *         On sql error
     * @since 1.0.0
     */
    protected ResultSet query(String query, String... params) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(query);

        int i = 1;
        for (String param : params) {
            statement.setString(i, param);
            i++;
        }

        return statement.executeQuery(query);
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