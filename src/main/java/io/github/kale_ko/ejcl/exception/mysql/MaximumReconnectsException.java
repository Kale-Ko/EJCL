package io.github.kale_ko.ejcl.exception.mysql;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when the too many automatic reconnects are triggered in a certain time period
 *
 * @version 3.4.0
 * @since 3.4.0
 */
public class MaximumReconnectsException extends RuntimeException {
    /**
     * Create a new MaximumReconnectsException
     */
    public MaximumReconnectsException() {
        super("Maximum reconnects reached: No exception found");
    }

    /**
     * Create a new MaximumReconnectsException
     *
     * @param cause The cause of the exception
     */
    public MaximumReconnectsException(@NotNull Exception cause) {
        super("Maximum reconnects reached:", cause);
    }
}