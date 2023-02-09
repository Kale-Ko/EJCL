package io.github.kale_ko.ejcl;

import java.io.Closeable;
import java.io.IOException;

/**
 * An abstract class that all configs extend from
 * <p>
 * Contains all the logic for getting/setting values
 *
 * @param <T>
 *        The type of the data being stored
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class Config<T> implements Closeable {
    /**
     * The class of the data being stored
     * 
     * @since 1.0.0
     */
    protected Class<T> clazz;

    /**
     * The data being stored
     * 
     * @since 1.0.0
     */
    protected T config = null;

    /**
     * Create a new Config
     *
     * @param clazz
     *        The class of the data being stored
     * @since 1.0.0
     */
    protected Config(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Clazz can not be null");
        }

        if (clazz.isArray() || clazz.isInterface() || clazz.isEnum()) {
            throw new RuntimeException("clazz must be an object");
        }

        this.clazz = clazz;
    }

    /**
     * Get the data being stored
     * 
     * @return The data being stored
     * @since 1.0.0
     */
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

    /**
     * Set the data being stored
     * 
     * @param value
     *        The data to be stored
     * @since 1.0.0
     */
    public void set(T value) {
        if (value == null) {
            throw new NullPointerException("Value can not be null");
        }

        this.config = value;
    }

    /**
     * Get if the config is loaded
     * 
     * @return If the config is loaded
     * @since 1.0.0
     */
    public abstract boolean getLoaded();

    /**
     * Load the config
     * 
     * @throws IOException
     *         On load error
     * @since 1.0.0
     */
    public abstract void load() throws IOException;

    /**
     * Save the config
     * 
     * @throws IOException
     *         On save error
     * @since 1.0.0
     */
    public abstract void save() throws IOException;

    /**
     * Close the config
     * 
     * @throws IOException
     *         On close error
     * @since 1.0.0
     */
    public abstract void close() throws IOException;

    /**
     * Get if the config is closed
     * 
     * @return If the config is closed
     * @since 1.0.0
     */
    public abstract boolean isClosed();
}