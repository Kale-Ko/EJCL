package io.github.kale_ko.ejcl.mysql.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class for executing MySQL statements and queries
 *
 * @version 3.4.0
 * @since 3.4.0
 */
public class MySQL {
    private MySQL() {
    }

    /**
     * Execute a mysql statement
     *
     * @param connection The connection to execute on
     * @param query      The base query to send
     * @param args       Extra args to replace into the query
     *
     * @return If the statement was successful
     *
     * @throws java.sql.SQLException When an SQLException is throw by the driver
     * @since 3.4.0
     */
    public static boolean execute(Connection connection, String query, String... args) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < args.length; i++) {
                statement.setString(i + 1, args[i]);
            }

            return statement.execute();
        }
    }

    /**
     * Execute a mysql statement
     *
     * @param connection The connection to execute on
     * @param query      The base query to send
     * @param argsSize   The number of argument to replace into the queries per statement
     * @param args       Extra args
     *
     * @return If the statement was successful
     *
     * @throws SQLException When an SQLException is throw by the driver
     * @since 3.4.0
     */
    public static boolean executeBatch(Connection connection, String query, int argsSize, List<String> args) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                statement.setString((i % argsSize) + 1, args.get(i));

                if ((i + 1) % argsSize == 0) {
                    statement.addBatch();
                }
            }

            statement.executeBatch();
            return true;
        }
    }

    /**
     * Execute a mysql query and return the result
     * <p>
     * <b>The returned {@link ResultSet} *must* be closed by you to prevent memory leaks</b>
     *
     * @param connection The connection to execute on
     * @param query      The base query to send
     * @param args       Extra args to replace into the query
     *
     * @return The result of the query
     *
     * @throws SQLException When an SQLException is throw by the driver
     * @since 3.4.0
     */
    public static ResultSet query(Connection connection, String query, String... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            statement.setString(i + 1, args[i]);
        }

        return statement.executeQuery();
    }
}