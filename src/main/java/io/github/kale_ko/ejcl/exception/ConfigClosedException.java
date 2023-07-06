package io.github.kale_ko.ejcl.exception;

/**
 * Thrown if the config is closed when an action is called
 *
 * @version 3.4.0
 * @since 3.4.0
 */
public class ConfigClosedException extends RuntimeException {
    /**
     * Create a new ConfigClosedException
     */
    public ConfigClosedException() {
        super("The config is already closed");
    }
}