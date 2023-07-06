package io.github.kale_ko.ejcl.exception.mysql;

/**
 * Thrown when an exception occurs in a MySQL connection
 *
 * @version 3.4.0
 * @since 3.4.0
 */
public class MySQLException extends RuntimeException {
    /**
     * Create a new MySQLException
     *
     * @param cause The cause of the exception
     */
    public MySQLException(Exception cause) {
        super("Error executing a MySQL statement:", cause);
    }
}