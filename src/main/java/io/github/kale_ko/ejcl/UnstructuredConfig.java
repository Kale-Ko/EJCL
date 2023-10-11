package io.github.kale_ko.ejcl;

import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.exception.ConfigLoadException;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract class that all unstructured configs extend from
 * <p>
 * Contains all the logic for getting/setting values
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public abstract class UnstructuredConfig {
    /**
     * The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected final @NotNull ObjectProcessor processor;

    /**
     * The data being stored
     *
     * @since 3.0.0
     */
    protected @Nullable ParsedObject config;

    /**
     * Create a new Config
     *
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected UnstructuredConfig(@NotNull ObjectProcessor processor) {
        this.processor = processor;

        this.config = ParsedObject.create();
    }

    /**
     * Create a new Config
     *
     * @since 3.0.0
     */
    protected UnstructuredConfig() {
        this(new ObjectProcessor.Builder().build());
    }

    /**
     * Get a path being stored and load it if necessary
     *
     * @param path The path to get
     *
     * @return The value being stored
     *
     * @since 3.0.0
     */
    public @Nullable Object get(@NotNull String path) {
        if (!this.getLoaded()) {
            try {
                this.load();
            } catch (IOException e) {
                throw new ConfigLoadException(e);
            }
        }

        assert this.config != null;
        return PathResolver.resolve(this.config, path);
    }

    /**
     * Get a path being stored
     *
     * @param path The path to get
     *
     * @return The value being stored or null
     *
     * @since 3.5.0
     */
    public @Nullable Object getCached(@NotNull String path) {
        if (this.config != null) {
            return PathResolver.resolve(this.config, path);
        } else {
            return null;
        }
    }

    /**
     * Set a path being stored
     *
     * @param path  The path to set
     * @param value The value to set
     *
     * @since 3.0.0
     */
    public void set(@NotNull String path, @NotNull Object value) {
        if (this.config != null) {
            PathResolver.update(this.config, path, value, true);
        }
    }

    /**
     * Get if the config is loaded
     *
     * @return If the config is loaded
     *
     * @since 3.0.0
     */
    public abstract boolean getLoaded();

    /**
     * Load the config
     *
     * @throws IOException On load error
     * @since 3.0.0
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
     * @since 3.0.0
     */
    public abstract void save() throws IOException;

    /**
     * Close the config
     *
     * @throws IOException On close error
     * @since 3.0.0
     */
    public abstract void close() throws IOException;

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     *
     * @since 3.0.0
     */
    public abstract boolean isClosed();
}