package io.github.kale_ko.ejcl.file.simple;

import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.file.UnstructuredFileConfig;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A Simple Unstructured File Config for storing key/value pairs in a File
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class UnstructuredSimpleFileConfig extends UnstructuredFileConfig {
    /**
     * Create a new UnstructuredSimpleFileConfig
     *
     * @param file      The file where data is being stored
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    protected UnstructuredSimpleFileConfig(@NotNull File file, @NotNull ObjectProcessor processor) {
        super(file, processor);
    }

    /**
     * Create a blank config file
     *
     * @return The config bytes
     *
     * @throws IOException On create error
     * @since 1.0.0
     */
    @Override
    public byte @NotNull [] create() throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        return "\n".getBytes(StandardCharsets.UTF_8);
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
        if (this.closed) {
            throw new ConfigClosedException();
        }

        synchronized (SAVELOAD_LOCK) {
            this.config = ParsedObject.create();

            for (String line : new String(this.loadRaw(), StandardCharsets.UTF_8).split("\n")) {
                line = line.trim();

                this.config.set(line.split("=", 2)[0].trim(), ParsedPrimitive.fromString(line.split("=", 2)[1].trim()));
            }

            if (save) {
                this.save();
            }
        }
    }

    /**
     * Save the config to bytes
     *
     * @return The config bytes
     *
     * @throws IOException On save error
     * @since 1.0.0
     */
    @Override
    public byte @NotNull [] saveRaw() throws IOException {
        if (this.config == null) {
            return this.create();
        }

        StringBuilder data = new StringBuilder();

        for (Map.Entry<String, ParsedElement> entry : this.config.getEntries()) {
            if (entry.getValue().isPrimitive()) {
                data.append(entry.getKey()).append("=").append(!entry.getValue().asPrimitive().isNull() ? entry.getValue().asPrimitive().get().toString() : "null");
            }
        }

        return data.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.file.simple.UnstructuredSimpleFileConfig}s
     *
     * @version 4.0.0
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
         * The file to use
         *
         * @since 4.0.0
         */
        protected @NotNull File file;

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.simple.UnstructuredSimpleFileConfig} builder
         *
         * @param file The file to use
         *
         * @since 4.0.0
         */
        public Builder(@NotNull File file) {
            this.processor = new ObjectProcessor.Builder().build();

            this.file = file;
        }

        /**
         * Get the ObjectProcessor to use for serialization/deserialization
         *
         * @return The ObjectProcessor to use for serialization/deserialization
         *
         * @since 4.0.0
         */
        public @NotNull ObjectProcessor getProcessor() {
            return processor;
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
         * Get the file to use
         *
         * @return The file to use
         *
         * @since 4.0.0
         */
        public @NotNull File getFile() {
            return this.file;
        }

        /**
         * Set the file to use
         *
         * @param file The file to use
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder setFile(@NotNull File file) {
            this.file = file;
            return this;
        }

        public @NotNull UnstructuredSimpleFileConfig build() {
            return new UnstructuredSimpleFileConfig(this.file, this.processor);
        }
    }
}