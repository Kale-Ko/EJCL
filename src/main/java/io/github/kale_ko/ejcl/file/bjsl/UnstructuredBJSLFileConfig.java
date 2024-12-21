package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.JsonParser;
import io.github.kale_ko.bjsl.parsers.Parser;
import io.github.kale_ko.bjsl.parsers.SmileParser;
import io.github.kale_ko.bjsl.parsers.YamlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.file.UnstructuredFileConfig;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured BJSL File Config for storing BJSL data in a File
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class UnstructuredBJSLFileConfig extends UnstructuredFileConfig {
    /**
     * The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    protected final @NotNull Parser parser;

    /**
     * Create a new UnstructuredBJSLFileConfig
     *
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    protected UnstructuredBJSLFileConfig(@NotNull Path file, @NotNull Parser parser, @NotNull ObjectProcessor processor) {
        super(file, processor);

        this.parser = parser;
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
            this.config = this.parser.toElement(this.loadRaw()).asObject();

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

        return this.parser.toBytes(this.config);
    }

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig}s
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
         * The parser/processor to use for parsing and serialization
         *
         * @since 2.0.0
         */
        protected @NotNull Parser parser;

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig} builder
         *
         * @param file   The file to use
         * @param parser The parser/processor to use for parsing and serialization
         *
         * @since 4.0.0
         */
        public Builder(@NotNull Path file, @NotNull Parser parser) {
            this.processor = new ObjectProcessor.Builder().build();

            this.file = file;

            this.parser = parser;
        }

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig} builder
         *
         * @param file The file to use
         *
         * @return A new Builder
         *
         * @since 4.0.0
         */
        public static UnstructuredBJSLFileConfig.Builder createJson(@NotNull Path file) {
            return new UnstructuredBJSLFileConfig.Builder(file, new JsonParser.Builder().build());
        }

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig} builder
         *
         * @param file The file to use
         *
         * @return A new Builder
         *
         * @since 4.0.0
         */
        public static UnstructuredBJSLFileConfig.Builder createYaml(@NotNull Path file) {
            return new UnstructuredBJSLFileConfig.Builder(file, new YamlParser.Builder().build());
        }

        /**
         * Create an {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig} builder
         *
         * @param file The file to use
         *
         * @return A new Builder
         *
         * @since 4.0.0
         */
        public static UnstructuredBJSLFileConfig.Builder createSmile(@NotNull Path file) {
            return new UnstructuredBJSLFileConfig.Builder(file, new SmileParser.Builder().build());
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
        public @NotNull UnstructuredBJSLFileConfig.Builder setProcessor(@NotNull ObjectProcessor processor) {
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
        public @NotNull UnstructuredBJSLFileConfig.Builder setFile(@NotNull Path file) {
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
        public @NotNull UnstructuredBJSLFileConfig.Builder setParser(@NotNull Parser parser) {
            this.parser = parser;
            return this;
        }

        /**
         * Creating a new {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig}
         *
         * @since 4.0.0
         */
        public @NotNull UnstructuredBJSLFileConfig build() {
            return new UnstructuredBJSLFileConfig(this.file, this.parser, this.processor);
        }
    }
}