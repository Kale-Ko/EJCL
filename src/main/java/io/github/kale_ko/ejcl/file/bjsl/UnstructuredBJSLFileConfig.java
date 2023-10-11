package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.JsonParser;
import io.github.kale_ko.bjsl.parsers.Parser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.file.UnstructuredFileConfig;
import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured BJSL File Config for storing BJSL data in a File
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class UnstructuredBJSLFileConfig extends UnstructuredFileConfig {
    /**
     * The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    protected final @NotNull Parser<?, ?> parser;

    /**
     * Create a new UnstructuredBJSLFileConfig
     *
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    protected UnstructuredBJSLFileConfig(@NotNull File file, @NotNull Parser<?, ?> parser, @NotNull ObjectProcessor processor) {
        super(file, processor);

        this.parser = parser;
    }

    /**
     * A builder class for creating new {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig}s
     *
     * @version 4.0.0
     * @since 4.0.0
     */
    public static class Builder extends UnstructuredFileConfig.Builder {
        /**
         * The parser/processor to use for parsing and serialization
         *
         * @since 4.0.0
         */
        protected @NotNull Parser<?, ?> parser;

        /**
         * Create a new {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig} builder
         *
         * @param file The file where data is stored
         *
         * @since 4.0.0
         */
        public Builder(@NotNull File file) {
            super(file);

            this.parser = new JsonParser.Builder().build();
        }

        /**
         * Get the parser/processor to use for parsing and serialization
         *
         * @return The parser/processor to use for parsing and serialization
         *
         * @since 4.0.0
         */
        public @NotNull Parser<?, ?> getParser() {
            return this.parser;
        }

        /**
         * Get the parser/processor to use for parsing and serialization
         *
         * @param parser The parser/processor to use for parsing and serialization
         *
         * @since 4.0.0
         */
        public UnstructuredBJSLFileConfig.Builder setParser(@NotNull Parser<?, ?> parser) {
            this.parser = parser;

            return this;
        }

        /**
         * Uses the current settings to build a new {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig}
         *
         * @return A new {@link io.github.kale_ko.ejcl.file.bjsl.UnstructuredBJSLFileConfig} instance
         *
         * @since 4.0.0
         */
        @Override
        public @NotNull UnstructuredBJSLFileConfig build() {
            return new UnstructuredBJSLFileConfig(this.file, this.parser, this.processor);
        }
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
    public byte @NotNull [] saveRaw() throws IOException {
        if (this.config == null) {
            return this.create();
        }

        return this.parser.toBytes(this.config);
    }
}