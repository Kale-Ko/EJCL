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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.Config;
import io.github.kale_ko.ejcl.PathResolver;

public class MySQLConfig<T> extends Config<T> {
    protected ObjectProcessor processor;

    protected String host;
    protected int port;

    protected String database;
    protected String table;

    protected String username;
    protected String password;

    protected Connection connection;

    protected boolean closed = false;

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

    public MySQLConfig(Class<T> clazz, String host, int port, String database, String table, String username, String password) {
        this(clazz, new ObjectProcessor.Builder().build(), host, port, database, table, username, password);
    }

    public MySQLConfig(Class<T> clazz, ObjectProcessor processor, String host, int port, String database, String table) {
        this(clazz, processor, host, port, database, table, null, null);
    }

    public MySQLConfig(Class<T> clazz, String host, int port, String database, String table) {
        this(clazz, new ObjectProcessor.Builder().build(), host, port, database, table, null, null);
    }

    @Override
    public boolean getLoaded() {
        return this.config != null;
    }

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

            this.execute("CREATE TABLE IF NOT EXISTS " + table + " (path varchar(512) CHARACTER SET utf8, value varchar(512) CHARACTER SET utf8) CHARACTER SET utf8;");
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

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
            ResultSet existsResult = this.query("SELECT * FROM " + this.table);

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
            ResultSet existsResult = this.query("SELECT * FROM " + this.table);

            while (existsResult.next()) {
                exists.add(existsResult.getString("path"));
            }

            existsResult.getStatement().close();
            existsResult.close();

            List<String> queries = new ArrayList<String>();

            for (String key : keys) {
                String value = PathResolver.resolve(object, key).toString();

                if (!exists.contains(key)) {
                    queries.add("INSERT INTO " + this.table + " (path, value) VALUES (\"" + key + "\", \"" + value + "\");");
                } else {
                    queries.add("UPDATE " + this.table + " SET value=\"" + value + "\" WHERE path=\"" + key + "\";");
                }
            }

            this.executeBatch(queries.toArray(new String[] {}));
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    protected boolean execute(String query) throws SQLException {
        Statement statement = this.connection.createStatement();

        boolean result = statement.execute(query);
        statement.close();

        return result;
    }

    protected void executeBatch(String[] queries) throws SQLException {
        Statement statement = this.connection.createStatement();

        for (String query : queries) {
            statement.addBatch(query);
        }
        statement.executeBatch();

        statement.close();
    }

    protected ResultSet query(String query) throws SQLException {
        Statement statement = this.connection.createStatement();

        return statement.executeQuery(query);
    }

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

    public boolean isClosed() {
        return this.closed;
    }
}