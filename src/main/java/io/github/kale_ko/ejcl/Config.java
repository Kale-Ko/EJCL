package io.github.kale_ko.ejcl;

import java.io.IOException;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;

/**
 * An abstract class that all configs extend from
 * <p>
 * Contains all the logic for getting/setting values
 *
 * @param <T>
 *        The type of the data being stored
 * @version 2.0.0
 * @since 1.0.0
 */
public abstract class Config<T> {
    /**
     * The class of the data being stored
     *
     * @since 1.0.0
     */
    protected Class<T> clazz;

    /**
     * The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    protected ObjectProcessor processor;

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
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @since 2.0.0
     */
    protected Config(Class<T> clazz, ObjectProcessor processor) {
        if (clazz == null) {
            throw new NullPointerException("Clazz can not be null");
        }

        if (clazz.isArray() || clazz.isInterface() || clazz.isEnum()) {
            throw new RuntimeException("clazz must be an object");
        }

        this.clazz = clazz;

        this.processor = processor;
    }

    /**
     * Create a new Config
     *
     * @param clazz
     *        The class of the data being stored
     * @since 1.0.0
     */
    protected Config(Class<T> clazz) {
        this(clazz, new ObjectProcessor.Builder().build());
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
     * Get a path being stored
     *
     * @param path
     *        The path to get
     * @return The value being stored
     * @since 2.0.0
     */
    public Object get(String path) {
        if (path == null) {
            throw new NullPointerException("Path can not be null");
        }

        if (!this.getLoaded()) {
            try {
                this.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return PathResolver.resolve(this.processor.toElement(this.config), path);
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
     * Set a path being stored
     *
     * @param path
     *        The path to set
     * @param value
     *        The value to set
     * @since 2.0.0
     */
    public void set(String path, Object value) {
        if (path == null) {
            throw new NullPointerException("Path can not be null");
        }
        if (value == null) {
            throw new NullPointerException("Value can not be null");
        }

        this.set(this.processor.toObject(PathResolver.update(this.processor.toElement(this.config), path, value), this.clazz));
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
    public void load() throws IOException {
        load(true);
    }

    /**
     * Load the config
     *
     * @param save
     *        Weather to save the config after loaded (To update the template)
     * @throws IOException
     *         On load error
     * @since 1.3.0
     */
    public abstract void load(boolean save) throws IOException;

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