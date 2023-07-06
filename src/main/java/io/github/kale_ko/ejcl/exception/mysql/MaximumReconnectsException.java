package io.github.kale_ko.ejcl.exception.mysql;

public class MaximumReconnectsException extends RuntimeException {
    public MaximumReconnectsException() {
        super("Maximum reconnects reached");
    }
}