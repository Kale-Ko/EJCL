package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.JsonParser;
import io.github.kale_ko.bjsl.parsers.Parser;
import io.github.kale_ko.bjsl.parsers.SmileParser;
import io.github.kale_ko.bjsl.parsers.YamlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.file.StructuredFileConfig;
import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * A Structured BJSL File Config for storing BJSL data in a File
 *
 * @param <T> The type of the data being stored
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class StructuredBJSLFileConfig<T> extends StructuredFileConfig<T> {
    /**
     * The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    protected final @NotNull Parser parser;

    /**
     * The ObjectProcessor to use for serialization/deserialization
     *
     * @since 3.0.0
     */
    protected final @NotNull ObjectProcessor processor;

    /**
     * Create a new StructuredBJSLFileConfig
     *
     * @param clazz     The class of the data being stored
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    protected StructuredBJSLFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull Parser parser, @NotNull ObjectProcessor processor) {
        super(clazz, file);

        this.parser = parser;
        this.processor = processor;
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

        return this.parser.emptyBytes();
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
            this.config = this.processor.toObject(this.parser.toElement(this.loadRaw()), this.clazz);

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

        return this.parser.toBytes(this.processor.toElement(this.config));
    }

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.file.bjsl.StructuredBJSLFileConfig}s
     *
     * @version 4.0.0
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
        protected @NotNull File file;

        /**
         * The parser/processor to use for parsing and serialization
         *
         * @since 2.0.0
         */
        protected @NotNull Parser parser;

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.bjsl.StructuredBJSLFileConfig} builder
         *
         * @param clazz  The class of the data being stored
         * @param file   The file to use
         * @param parser The parser/processor to use for parsing and serialization
         *
         * @since 4.0.0
         */
        public Builder(@NotNull Class<T> clazz, @NotNull File file, @NotNull Parser parser) {
            this.clazz = clazz;

            this.processor = new ObjectProcessor.Builder().build();

            this.file = file;

            this.parser = parser;
        }

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.bjsl.StructuredBJSLFileConfig} builder
         *
         * @param clazz The class of the data being stored
         * @param file  The file to use
         * @param <T>   The type of the data being stored
         *
         * @return A new Builder
         *
         * @since 4.0.0
         */
        public static <T> Builder<T> createJson(@NotNull Class<T> clazz, @NotNull File file) {
            return new Builder<>(clazz, file, new JsonParser.Builder().build());
        }

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.bjsl.StructuredBJSLFileConfig} builder
         *
         * @param clazz The class of the data being stored
         * @param file  The file to use
         * @param <T>   The type of the data being stored
         *
         * @return A new Builder
         *
         * @since 4.0.0
         */
        public static <T> Builder<T> createYaml(@NotNull Class<T> clazz, @NotNull File file) {
            return new Builder<>(clazz, file, new YamlParser.Builder().build());
        }

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.bjsl.StructuredBJSLFileConfig} builder
         *
         * @param clazz The class of the data being stored
         * @param file  The file to use
         * @param <T>   The type of the data being stored
         *
         * @return A new Builder
         *
         * @since 4.0.0
         */
        public static <T> Builder<T> createSmile(@NotNull Class<T> clazz, @NotNull File file) {
            return new Builder<>(clazz, file, new SmileParser.Builder().build());
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
        public @NotNull Builder<T> setFile(@NotNull File file) {
            this.file = file;
            return this;
        }

        /**
         * Get the parser/processor to use for parsing and serialization
         *
         * @return The parser/processor to use for parsing and serialization
         *
         * @since 4.0.0
         */
        public @NotNull Parser getParser() {
            return this.parser;
        }

        /**
         * Set the parser/processor to use for parsing and serialization
         *
         * @param parser The parser/processor to use for parsing and serialization
         *
         * @return Self for chaining
         *
         * @since 4.0.0
         */
        public @NotNull Builder<T> setParser(@NotNull Parser parser) {
            this.parser = parser;
            return this;
        }

        public @NotNull StructuredBJSLFileConfig<T> build() {
            return new StructuredBJSLFileConfig<>(this.clazz, this.file, this.parser, this.processor);
        }
    }
}