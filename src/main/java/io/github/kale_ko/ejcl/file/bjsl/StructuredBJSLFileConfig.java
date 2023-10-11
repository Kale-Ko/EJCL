package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.Parser;
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
    protected final @NotNull Parser<?, ?> parser;

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
    public StructuredBJSLFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull Parser<?, ?> parser, @NotNull ObjectProcessor processor) {
        super(clazz, file);

        this.parser = parser;
        this.processor = processor;
    }

    /**
     * Create a new StructuredBJSLFileConfig
     *
     * @param clazz  The class of the data being stored
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public StructuredBJSLFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull Parser<?, ?> parser) {
        this(clazz, file, parser, new ObjectProcessor.Builder().build());
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
}