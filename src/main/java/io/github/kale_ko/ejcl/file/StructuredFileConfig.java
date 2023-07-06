package io.github.kale_ko.ejcl.file;

import io.github.kale_ko.bjsl.processor.reflection.InitializationUtil;
import io.github.kale_ko.ejcl.StructuredConfig;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.exception.ConfigInitializationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A File Config for storing data in a file
 *
 * @param <T> The type of the data being stored
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public abstract class StructuredFileConfig<T> extends StructuredConfig<T> {
    /**
     * The file where data is being stored
     *
     * @since 1.0.0
     */
    protected File file;

    /**
     * If this config is closed
     *
     * @since 1.0.0
     */
    protected boolean closed = false;

    /**
     * Create a new FileConfig
     *
     * @param clazz The class of the data being stored
     * @param file  The file where data is being stored
     *
     * @since 1.0.0
     */
    protected StructuredFileConfig(Class<T> clazz, File file) {
        super(clazz);

        if (file == null) {
            throw new NullPointerException("File can not be null");
        }
        this.file = file;

        this.config = InitializationUtil.initializeUnsafe(clazz);
        if (this.config == null) {
            throw new ConfigInitializationException(clazz);
        }
    }

    /**
     * Get the file where data is being stored
     *
     * @return The file where data is being stored
     *
     * @since 1.0.0
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Get if the config is loaded
     *
     * @return If the config is loaded
     *
     * @since 1.0.0
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
     * @since 1.0.0
     */
    public abstract byte[] create() throws IOException;

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
     * @since 1.0.0
     */
    protected byte[] loadRaw() throws IOException {
        if (!Files.exists(this.file.toPath())) {
            Files.createFile(this.file.toPath());

            Files.write(this.file.toPath(), this.create());
        }

        return Files.readAllBytes(this.file.toPath());
    }

    /**
     * Save the config to file
     *
     * @throws IOException On save error
     * @since 1.0.0
     */
    @Override
    public void save() throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        if (!Files.exists(this.file.toPath())) {
            Files.createFile(this.file.toPath());
        }

        Files.write(this.file.toPath(), this.saveRaw());
    }

    /**
     * Save the config to bytes
     *
     * @return The config bytes
     *
     * @throws IOException On save error
     * @since 1.0.0
     */
    protected abstract byte[] saveRaw() throws IOException;

    /**
     * Close the config
     *
     * @throws IOException On close error
     * @since 1.0.0
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
     * @since 1.0.0
     */
    public boolean isClosed() {
        return this.closed;
    }
}