package io.github.kale_ko.ejcl.memory;

import io.github.kale_ko.bjsl.processor.reflection.InitializationUtil;
import io.github.kale_ko.ejcl.StructuredConfig;
import io.github.kale_ko.ejcl.exception.ConfigInitializationException;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * A Structured Memory Config for storing data in memory
 *
 * @param <T> The type of the data being stored
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class StructuredMemoryConfig<T> extends StructuredConfig<T> {
    /**
     * Create a new StructuredMemoryConfig
     *
     * @param clazz The class of the data being stored
     *
     * @since 2.0.0
     */
    protected StructuredMemoryConfig(@NotNull Class<T> clazz) {
        super(clazz);

        this.config = InitializationUtil.initializeUnsafe(clazz);
        if (this.config == null) {
            throw new ConfigInitializationException(clazz);
        }
    }

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.memory.StructuredMemoryConfig}s
     *
     * @version 4.0.0
     * @since 4.0.0
     */
    public static class Builder<T> extends StructuredConfig.Builder<T> {
        /**
         * Create a new {@link io.github.kale_ko.ejcl.memory.StructuredMemoryConfig} builder
         *
         * @since 4.0.0
         */
        public Builder(@NotNull Class<T> clazz) {
            super(clazz);
        }

        /**
         * Uses the current settings to build a new {@link io.github.kale_ko.ejcl.memory.StructuredMemoryConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.memory.StructuredMemoryConfig} instance
         *
         * @since 4.0.0
         */
        @Override
        public @NotNull StructuredMemoryConfig<T> build() {
            return new StructuredMemoryConfig<>(this.clazz);
        }
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