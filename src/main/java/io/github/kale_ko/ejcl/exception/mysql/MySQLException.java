package io.github.kale_ko.ejcl.exception.mysql;

public class MySQLException extends RuntimeException {
    public MySQLException(Exception cause) {
        super("Error executing a MySQL statement", cause);
    }
}