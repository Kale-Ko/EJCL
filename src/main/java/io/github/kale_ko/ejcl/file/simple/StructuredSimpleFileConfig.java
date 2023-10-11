package io.github.kale_ko.ejcl.file.simple;

import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.bjsl.processor.exception.ProcessorException;
import io.github.kale_ko.ejcl.PathResolver;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.file.StructuredFileConfig;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

/**
 * A Simple Structured File Config for storing key/value pairs in a File
 *
 * @version 4.0.0
 * @since 4.0.0
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
    public StructuredSimpleFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull ObjectProcessor processor) {
        super(clazz, file);

        this.processor = processor;
    }

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig}s
     *
     * @version 4.0.0
     * @since 4.0.0
     */
    public static class Builder<T> extends StructuredFileConfig.Builder<T> {
        /**
         * The ObjectProcessor to use for serialization/deserialization
         *
         * @since 4.0.0
         */
        protected @NotNull ObjectProcessor processor;

        /**
         * Create a new {@link io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig} builder
         *
         * @param file The file where data is stored
         *
         * @since 4.0.0
         */
        public Builder(@NotNull Class<T> clazz, @NotNull File file) {
            super(clazz, file);

            this.processor = new ObjectProcessor.Builder().build();
        }

        /**
         * The ObjectProcessor to use for serialization/deserialization
         *
         * @return The ObjectProcessor to use for serialization/deserialization
         *
         * @since 4.0.0
         */
        public @NotNull ObjectProcessor getProcessor() {
            return this.processor;
        }

        /**
         * The ObjectProcessor to use for serialization/deserialization
         *
         * @param processor The ObjectProcessor to use for serialization/deserialization
         *
         * @since 4.0.0
         */
        public @NotNull StructuredSimpleFileConfig.Builder<T> setProcessor(@NotNull ObjectProcessor processor) {
            this.processor = processor;

            return this;
        }

        /**
         * Uses the current settings to build a new {@link io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig} instance
         *
         * @since 4.0.0
         */
        @Override
        public @NotNull StructuredFileConfig<T> build() {
            return new StructuredSimpleFileConfig<>(this.clazz, this.file, this.processor);
        }
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

                PathResolver.updateElement(object, line.split("=", 2)[0].trim(), ParsedPrimitive.fromString(line.split("=", 2)[1].trim()), true);
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
    public byte @NotNull [] saveRaw() throws IOException {
        if (this.config == null) {
            return this.create();
        }

        ParsedObject object = this.processor.toElement(this.config).asObject();

        StringBuilder data = new StringBuilder();

        for (String key : PathResolver.getKeys(object, false)) {
            ParsedElement element = PathResolver.resolveElement(object, key);
            if (element != null) {
                data.append(key).append("=").append(element.asPrimitive().get().toString());
            } else {
                throw new ProcessorException(new NullPointerException("Key does not exist on object"));
            }
        }

        return data.toString().getBytes(StandardCharsets.UTF_8);
    }
}