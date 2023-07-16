package io.github.kale_ko.ejcl;

import io.github.kale_ko.bjsl.parsers.exception.InvalidTypeException;
import io.github.kale_ko.ejcl.exception.ConfigLoadException;
import java.io.IOException;

/**
 * An abstract class that all structured configs extend from
 * <p>
 * Contains all the logic for getting/setting values
 *
 * @param <T> The type of the data being stored
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public abstract class StructuredConfig<T> {
    /**
     * The class of the data being stored
     *
     * @since 1.0.0
     */
    protected final Class<T> clazz;

    /**
     * The data being stored
     *
     * @since 1.0.0
     */
    protected T config = null;

    /**
     * Create a new Config
     *
     * @param clazz The class of the data being stored
     *
     * @since 2.0.0
     */
    protected StructuredConfig(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Clazz can not be null");
        }

        if (clazz.isArray() || clazz.isInterface() || clazz.isEnum()) {
            throw new InvalidTypeException(clazz);
        }

        this.clazz = clazz;
    }

    /**
     * Get the data being store and loads it if necessary
     *
     * @return The data being stored
     *
     * @since 1.0.0
     */
    public T get() {
        if (!this.getLoaded()) {
            try {
                this.load();
            } catch (IOException e) {
                throw new ConfigLoadException(e);
            }
        }

        return this.config;
    }

    /**
     * Get the data being stored
     *
     * @return The data being stored or null
     *
     * @since 3.5.0
     */
    public T getCached() {
        return this.config;
    }

    /**
     * Set the data being stored
     *
     * @param value The data to be stored
     *
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
     *
     * @since 1.0.0
     */
    public abstract boolean getLoaded();

    /**
     * Load the config
     *
     * @throws IOException On load error
     * @since 1.0.0
     */
    public void load() throws IOException {
        load(false);
    }

    /**
     * Load the config
     *
     * @param save Weather to save the config after loaded (To update the template)
     *
     * @throws IOException On load error
     * @since 1.3.0
     */
    public abstract void load(boolean save) throws IOException;

    /**
     * Save the config
     *
     * @throws IOException On save error
     * @since 1.0.0
     */
    public abstract void save() throws IOException;

    /**
     * Close the config
     *
     * @throws IOException On close error
     * @since 1.0.0
     */
    public abstract void close() throws IOException;

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     *
     * @since 1.0.0
     */
    public abstract boolean isClosed();
}