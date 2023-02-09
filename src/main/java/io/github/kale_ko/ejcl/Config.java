package io.github.kale_ko.ejcl;

import java.io.Closeable;
import java.io.IOException;

public abstract class Config<T> implements Closeable {
    protected Class<T> clazz;

    protected T config = null;

    protected Config(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Clazz can not be null");
        }

        if (clazz.isArray() || clazz.isInterface() || clazz.isEnum()) {
            throw new RuntimeException("clazz must be an object");
        }

        this.clazz = clazz;
    }

    public T get() {
        if (!this.getLoaded()) {
            try {
                this.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return this.config;
    }

    public void set(T value) {
        if (value == null) {
            throw new NullPointerException("Value can not be null");
        }

        this.config = value;
    }

    public abstract boolean getLoaded();

    public abstract void load() throws IOException;

    public abstract void save() throws IOException;

    public abstract void close() throws IOException;

    public abstract boolean isClosed();
}