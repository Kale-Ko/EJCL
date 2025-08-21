package io.github.kale_ko.ejcl.file.simple;

import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.file.UnstructuredFileConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A Simple Unstructured File Config for storing key/value pairs in a File
 *
 * @version 5.1.0
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
    protected UnstructuredSimpleFileConfig(@NotNull Path file, @NotNull ObjectProcessor processor) {
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
    protected byte @NotNull [] create() throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        return "\n".getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Load the config
     *
     * @param save Whether to save the config after loaded (To update the template)
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
                if (line.isBlank()) {
                    continue;
                }

                String[] splitLine = line.split("=", 3);

                String path = splitLine[0];
                String type = splitLine[1].toUpperCase();
                ParsedPrimitive.PrimitiveType primitiveType = ParsedPrimitive.PrimitiveType.valueOf(type);
                String value = splitLine[2];

                ParsedPrimitive element = ParsedPrimitive.from(ParsedPrimitive.fromString(value).to(primitiveType));

                this.config.set(path, element);
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
    protected byte @NotNull [] saveRaw() throws IOException {
        if (this.config == null) {
            return this.create();
        }

        StringBuilder data = new StringBuilder();

        for (Map.Entry<String, ParsedElement> entry : this.config.getEntries()) {
            String key = entry.getKey();
            ParsedElement element = entry.getValue();
            if (element.isPrimitive()) {
                if (key.contains("=")) {
                    throw new IllegalArgumentException("Key cannot contain '='");
                }
                data.append(key).append("=").append(element.asPrimitive().getType().name()).append("=").append(!element.asPrimitive().isNull() ? element.asPrimitive().get().toString() : "null").append("\n");
            }
        }

        return data.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.file.simple.UnstructuredSimpleFileConfig}s
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
         * The file to use
         *
         * @since 4.0.0
         */
        protected @NotNull Path file;

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.simple.UnstructuredSimpleFileConfig} builder
         *
         * @param file The file to use
         *
         * @since 4.0.0
         */
        public Builder(@NotNull Path file) {
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
         * Get the file to use
         *
         * @return The file to use
         *
         * @since 4.0.0
         */
        public @NotNull Path getFile() {
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
        public @NotNull Builder setFile(@NotNull Path file) {
            this.file = file;
            return this;
        }


        /**
         * Creating a new {@link io.github.kale_ko.ejcl.file.simple.UnstructuredSimpleFileConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.file.simple.UnstructuredSimpleFileConfig}
         *
         * @since 4.0.0
         */
        public @NotNull UnstructuredSimpleFileConfig build() {
            return new UnstructuredSimpleFileConfig(this.file, this.processor);
        }
    }
}