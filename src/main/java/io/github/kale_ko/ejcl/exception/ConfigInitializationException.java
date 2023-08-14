package io.github.kale_ko.ejcl.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an exception occurs during initializing a config
 *
 * @version 3.4.0
 * @since 3.4.0
 */
public class ConfigInitializationException extends RuntimeException {
    /**
     * Create a new ConfigInitializationException
     *
     * @param type The type that was unable to be initialized
     */
    public ConfigInitializationException(@NotNull Class<?> type) {
        super("Could not instantiate new config of type \"" + type.getSimpleName() + "\"");
    }
}