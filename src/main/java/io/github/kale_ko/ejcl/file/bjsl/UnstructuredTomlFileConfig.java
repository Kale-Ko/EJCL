package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.TomlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured TOML File Config for storing TOML data in a File
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class UnstructuredTomlFileConfig extends UnstructuredBJSLFileConfig {
    /**
     * Create a new UnstructuredTomlFileConfig
     *
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public UnstructuredTomlFileConfig(@NotNull File file, @NotNull TomlParser parser, @NotNull ObjectProcessor processor) {
        super(file, parser, processor);
    }

    /**
     * Create a new UnstructuredTomlFileConfig
     *
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public UnstructuredTomlFileConfig(@NotNull File file, @NotNull TomlParser parser) {
        super(file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new UnstructuredTomlFileConfig
     *
     * @param file The file where data is being stored
     *
     * @since 1.0.0
     */
    public UnstructuredTomlFileConfig(@NotNull File file) {
        this(file, new TomlParser.Builder().build());
    }
}