package io.github.kale_ko.ejcl.file;

import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.UnstructuredConfig;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.exception.ConfigNotLoadedException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured File Config for storing data in a file
 *
 * @version 5.0.0
 * @since 3.0.0
 */
public abstract class UnstructuredFileConfig extends UnstructuredConfig {
    /**
     * The file where data is being stored
     *
     * @since 3.0.0
     */
    protected final @NotNull Path file;

    /**
     * The lock used when saving and loading the config
     *
     * @since 3.8.0
     */
    protected final @NotNull Object SAVELOAD_LOCK = new Object();

    /**
     * If this config is closed
     *
     * @since 3.0.0
     */
    protected boolean closed = false;

    /**
     * Create a new FileConfig
     *
     * @param file      The file where data is being stored
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected UnstructuredFileConfig(@NotNull Path file, @NotNull ObjectProcessor processor) {
        super(processor);

        this.file = file;
    }

    /**
     * Get the file where data is being stored
     *
     * @return The file where data is being stored
     *
     * @since 3.0.0
     */
    public @NotNull Path getFile() {
        return this.file;
    }

    /**
     * Get if the config is loaded
     *
     * @return If the config is loaded
     *
     * @since 3.0.0
     */
    @Override
    public boolean getLoaded() {
        return this.config != null;
    }

    /**
     * Create a blank config file
     *
     * @return The config bytes
     *
     * @throws IOException On create error
     * @since 3.0.0
     */
    protected abstract byte @NotNull [] create() throws IOException;

    /**
     * Load the config
     *
     * @param save Weather to save the config after loaded (To update the template)
     *
     * @throws IOException On load error
     * @since 1.3.0
     */
    @Override
    public abstract void load(boolean save) throws IOException;

    /**
     * Load the config from file
     *
     * @return The file bytes
     *
     * @throws IOException On load error
     * @since 3.0.0
     */
    protected byte @NotNull [] loadRaw() throws IOException {
        if (!Files.exists(this.file)) {
            return this.create();
        }

        return Files.readAllBytes(this.file);
    }

    /**
     * Save the config to file
     *
     * @throws IOException On save error
     * @since 3.0.0
     */
    @Override
    public void save() throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }
        if (this.config == null) {
            throw new ConfigNotLoadedException();
        }

        synchronized (SAVELOAD_LOCK) {
            if (!Files.exists(this.file)) {
                Files.createFile(this.file);
            }
            Files.write(this.file, this.saveRaw(), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    /**
     * Save the config to bytes
     *
     * @return The config bytes
     *
     * @throws IOException On save error
     * @since 3.0.0
     */
    protected abstract byte @NotNull [] saveRaw() throws IOException;

    /**
     * Close the config
     *
     * @throws IOException On close error
     * @since 3.0.0
     */
    @Override
    public void close() throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        this.closed = true;
    }

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     *
     * @since 3.0.0
     */
    public boolean isClosed() {
        return this.closed;
    }
}