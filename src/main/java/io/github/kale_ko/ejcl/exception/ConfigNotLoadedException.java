package io.github.kale_ko.ejcl.exception;

/**
 * Thrown when a config is accessed but was never loaded
 *
 * @version 3.10.0
 * @since 3.10.0
 */
public class ConfigNotLoadedException extends RuntimeException {
    /**
     * Create a new ConfigNotLoadedException
     */
    public ConfigNotLoadedException() {
        super("The config was never loaded");
    }
}