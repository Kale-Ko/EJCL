package io.github.kale_ko.ejcl.file.simple;

import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.PathResolver;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.file.StructuredFileConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * A Simple Structured File Config for storing key/value pairs in a File
 *
 * @param <T> The type of the data being stored
 *
 * @version 5.1.0
 * @since 3.9.0
 */
public class StructuredSimpleFileConfig<T> extends StructuredFileConfig<T> {
    /**
     * The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected final @NotNull ObjectProcessor processor;

    /**
     * Create a new StructuredSimpleFileConfig
     *
     * @param clazz     The class of the data being stored
     * @param file      The file where data is being stored
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    protected StructuredSimpleFileConfig(@NotNull Class<T> clazz, @NotNull Path file, @NotNull ObjectProcessor processor) {
        super(clazz, file, false);

        this.processor = processor;
    }

    /**
     * Create a blank config file
     *
     * @return The config bytes
     *
     * @throws java.io.IOException On create error
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
     * @throws java.io.IOException On load error
     * @since 1.3.0
     */
    @Override
    public void load(boolean save) throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        synchronized (SAVELOAD_LOCK) {
            ParsedObject object = ParsedObject.create();

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

                PathResolver.updateElement(object, path, element, true);
            }

            this.config = this.processor.toObject(object, this.clazz);

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
     * @throws java.io.IOException On save error
     * @since 1.0.0
     */
    @Override
    protected byte @NotNull [] saveRaw() throws IOException {
        if (this.config == null) {
            return this.create();
        }

        ParsedObject object = this.processor.toElement(this.config).asObject();

        StringBuilder data = new StringBuilder();

        for (String key : PathResolver.getKeys(object, false)) {
            ParsedElement element = PathResolver.resolveElement(object, key);
            if (element != null) {
                if (key.contains("=")) {
                    throw new IllegalArgumentException("Key cannot contain '='");
                }
                data.append(key).append("=").append(element.asPrimitive().getType().name()).append("=").append(!element.asPrimitive().isNull() ? element.asPrimitive().get().toString() : "null").append("\n");
            }
        }

        return data.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig}s
     *
     * @param <T> The type of the data to be stored
     *
     * @version 5.0.0
     * @since 4.0.0
     */
    public static class Builder<T> {
        /**
         * The class of the data being stored
         *
         * @since 4.0.0
         */
        protected final @NotNull Class<T> clazz;

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
         * Create an {@link io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig} builder
         *
         * @param clazz The class of the data being stored
         * @param file  The file to use
         *
         * @since 4.0.0
         */
        public Builder(@NotNull Class<T> clazz, @NotNull Path file) {
            this.clazz = clazz;

            this.processor = new ObjectProcessor.Builder().build();

            this.file = file;
        }

        /**
         * Get the class of the data being stored
         *
         * @return The class of the data being stored
         *
         * @since 4.0.0
         */
        public @NotNull Class<T> getClazz() {
            return this.clazz;
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
        public @NotNull Builder<T> setProcessor(@NotNull ObjectProcessor processor) {
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
        public @NotNull Builder<T> setFile(@NotNull Path file) {
            this.file = file;
            return this;
        }

        /**
         * Creating a new {@link io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig}
         *
         * @since 4.0.0
         */
        public @NotNull StructuredSimpleFileConfig<T> build() {
            return new StructuredSimpleFileConfig<>(this.clazz, this.file, this.processor);
        }
    }
}