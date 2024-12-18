package io.github.kale_ko.ejcl.memory;

import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.UnstructuredConfig;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured Memory Config for storing data in memory
 *
 * @version 5.0.0
 * @since 3.0.0
 */
public class UnstructuredMemoryConfig extends UnstructuredConfig {
    /**
     * Create a new UnstructuredMemoryConfig
     *
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected UnstructuredMemoryConfig(@NotNull ObjectProcessor processor) {
        super(processor);
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

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.memory.UnstructuredMemoryConfig}s
     *
     * @version 5.0.0
     * @since 4.0.0
     */
    public static class Builder {
        /**
         * The ObjectProcessor to use for serialization/deserialization
         *
         * @since 4.0.0
         */
        protected @NotNull ObjectProcessor processor;

        /**
         * Create an {@link io.github.kale_ko.ejcl.memory.UnstructuredMemoryConfig} builder
         *
         * @since 4.0.0
         */
        public Builder() {
            this.processor = new ObjectProcessor.Builder().build();
        }

        /**
         * Get the ObjectProcessor to use for serialization/deserialization
         *
         * @return The ObjectProcessor to use for serialization/deserialization
         *
         * @since 4.0.0
         */
        public @NotNull ObjectProcessor getProcessor() {
            return this.processor;
        }

        /**
         * Set the ObjectProcessor to use for serialization/deserialization
         *
         * @param processor The ObjectProcessor to use for serialization/deserialization
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder setProcessor(@NotNull ObjectProcessor processor) {
            this.processor = processor;
            return this;
        }


        /**
         * Creating a new {@link io.github.kale_ko.ejcl.memory.UnstructuredMemoryConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.memory.UnstructuredMemoryConfig}
         *
         * @since 4.0.0
         */
        public @NotNull UnstructuredMemoryConfig build() {
            return new UnstructuredMemoryConfig(this.processor);
        }
    }
}