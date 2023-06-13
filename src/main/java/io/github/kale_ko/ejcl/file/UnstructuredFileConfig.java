package io.github.kale_ko.ejcl.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.UnstructuredConfig;

/**
 * A File Config for storing data in a file
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public abstract class UnstructuredFileConfig extends UnstructuredConfig {
    /**
     * The file where data is being stored
     *
     * @since 3.0.0
     */
    protected File file;

    /**
     * If this config is closed
     *
     * @since 3.0.0
     */
    protected boolean closed = false;

    /**
     * Create a new FileConfig
     *
     * @param file
     *        The file where data is being stored
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @since 3.0.0
     */
    protected UnstructuredFileConfig(File file, ObjectProcessor processor) {
        super(processor);

        if (file == null) {
            throw new NullPointerException("File can not be null");
        }

        this.file = file;
    }

    /**
     * Create a new FileConfig
     *
     * @param file
     *        The file where data is being stored
     * @since 3.0.0
     */
    protected UnstructuredFileConfig(File file) {
        this(file, new ObjectProcessor.Builder().build());
    }

    /**
     * Get the file where data is being stored
     *
     * @return The file where data is being stored
     * @since 3.0.0
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Get if the config is loaded
     *
     * @return If the config is loaded
     * @since 3.0.0
     */
    @Override
    public boolean getLoaded() {
        return this.config != null;
    }

    /**
     * Create a blank config file
     *
     * @throws IOException
     *         On create error
     * @return The config bytes
     * @since 3.0.0
     */
    public abstract byte[] create() throws IOException;

    /**
     * Load the config
     *
     * @param save
     *        Weather to save the config after loaded (To update the template)
     * @throws IOException
     *         On load error
     * @since 1.3.0
     */
    @Override
    public abstract void load(boolean save) throws IOException;

    /**
     * Load the config from file
     *
     * @throws IOException
     *         On load error
     * @return The file bytes
     * @since 3.0.0
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
     * @throws IOException
     *         On save error
     * @since 3.0.0
     */
    @Override
    public void save() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        if (!Files.exists(this.file.toPath())) {
            Files.createFile(this.file.toPath());
        }

        Files.write(this.file.toPath(), this.saveRaw());
    }

    /**
     * Save the config to bytes
     *
     * @throws IOException
     *         On save error
     * @return The config bytes
     * @since 3.0.0
     */
    protected abstract byte[] saveRaw() throws IOException;

    /**
     * Close the config
     *
     * @throws IOException
     *         On close error
     * @since 3.0.0
     */
    @Override
    public void close() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        this.closed = true;
    }

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     * @since 3.0.0
     */
    public boolean isClosed() {
        return this.closed;
    }
}