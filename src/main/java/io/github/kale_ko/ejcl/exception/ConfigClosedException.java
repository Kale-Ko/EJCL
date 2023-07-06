package io.github.kale_ko.ejcl.exception;

public class ConfigClosedException extends RuntimeException {
    public ConfigClosedException() {
        super("The config is already closed");
    }
}