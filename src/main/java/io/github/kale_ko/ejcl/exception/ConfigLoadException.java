package io.github.kale_ko.ejcl.exception;

public class ConfigLoadException extends RuntimeException {
    public ConfigLoadException(Exception cause) {
        super("Failed to load config:", cause);
    }
}