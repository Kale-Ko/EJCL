package io.github.kale_ko.ejcl.exception.mysql;

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
        super("Maximum reconnects reached");
    }
}