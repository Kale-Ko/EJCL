package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.JsonParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured JSON File Config for storing JSON data in a File
 *
 * @version 3.9.0
 * @since 3.9.0
 */
public class UnstructuredJsonFileConfig extends UnstructuredBJSLFileConfig {
    /**
     * Create a new UnstructuredJsonFileConfig
     *
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public UnstructuredJsonFileConfig(@NotNull File file, @NotNull JsonParser parser, @NotNull ObjectProcessor processor) {
        super(file, parser, processor);
    }

    /**
     * Create a new UnstructuredJsonFileConfig
     *
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public UnstructuredJsonFileConfig(@NotNull File file, @NotNull JsonParser parser) {
        super(file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new UnstructuredJsonFileConfig
     *
     * @param file The file where data is being stored
     *
     * @since 1.0.0
     */
    public UnstructuredJsonFileConfig(@NotNull File file) {
        this(file, new JsonParser.Builder().setPrettyPrint(true).build());
    }
}