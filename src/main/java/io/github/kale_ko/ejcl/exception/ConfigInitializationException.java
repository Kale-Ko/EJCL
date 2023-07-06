package io.github.kale_ko.ejcl.exception;

public class ConfigInitializationException extends RuntimeException {
    public ConfigInitializationException(Class<?> clazz) {
        super("Could not instantiate new config of type \"" + clazz.getSimpleName() + "\"");
    }
}