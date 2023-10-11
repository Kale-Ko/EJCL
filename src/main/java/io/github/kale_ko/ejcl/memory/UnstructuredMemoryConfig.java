package io.github.kale_ko.ejcl.memory;

import io.github.kale_ko.ejcl.UnstructuredConfig;
import java.io.IOException;

/**
 * An Unstructured Memory Config for storing data in memory
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class UnstructuredMemoryConfig extends UnstructuredConfig {
    /**
     * Create a new UnstructuredMemoryConfig
     *
     * @since 3.0.0
     */
    public UnstructuredMemoryConfig() {
        super();
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
        return true;
    }

    /**
     * Load the config
     *
     * @param save Weather to save the config after loaded (To update the template)
     *
     * @throws IOException On load error
     * @since 1.3.0
     */
    @Override
    public void load(boolean save) throws IOException {
    }

    /**
     * Save the config to memory
     *
     * @throws IOException On save error
     * @since 1.0.0
     */
    @Override
    public void save() throws IOException {
    }

    /**
     * Close the config
     *
     * @throws IOException On close error
     * @since 1.0.0
     */
    @Override
    public void close() throws IOException {
        this.config = null;
    }

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     *
     * @since 1.0.0
     */
    public boolean isClosed() {
        return this.config == null;
    }
}