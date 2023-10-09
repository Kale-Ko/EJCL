package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.SmileParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * A Smile File Config for storing Smile data in a File
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class UnstructuredSmileFileConfig extends UnstructuredBJSLFileConfig {
    /**
     * Create a new StructuredSmileFileConfig
     *
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public UnstructuredSmileFileConfig(@NotNull File file, @NotNull SmileParser parser, @NotNull ObjectProcessor processor) {
        super(file, parser, processor);
    }

    /**
     * Create a new StructuredSmileFileConfig
     *
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public UnstructuredSmileFileConfig(@NotNull File file, @NotNull SmileParser parser) {
        super(file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new StructuredSmileFileConfig
     *
     * @param file The file where data is being stored
     *
     * @since 1.0.0
     */
    public UnstructuredSmileFileConfig(@NotNull File file) {
        this(file, new SmileParser.Builder().build());
    }
}