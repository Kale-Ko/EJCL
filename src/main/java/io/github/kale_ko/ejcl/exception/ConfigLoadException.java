package io.github.kale_ko.ejcl.exception;

/**
 * Thrown when an exception occurs during loading a config
 *
 * @version 3.4.0
 * @since 3.4.0
 */
public class ConfigLoadException extends RuntimeException {
    /**
     * Create a new ConfigLoadException
     *
     * @param cause The cause of the exception
     */
    public ConfigLoadException(Exception cause) {
        super("Failed to load config:", cause);
    }
}