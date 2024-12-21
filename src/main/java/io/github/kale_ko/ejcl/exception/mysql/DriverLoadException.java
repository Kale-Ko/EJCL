package io.github.kale_ko.ejcl.exception.mysql;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an exception occurs loading the MySQL Driver
 *
 * @version 3.4.0
 * @since 3.4.0
 */
public class DriverLoadException extends RuntimeException {
    /**
     * Create a new DriverLoadException
     *
     * @param cause The cause of the exception
     */
    public DriverLoadException(@NotNull Exception cause) {
        super("Error loading MySQL or MariaDB driver:", cause);
    }
}