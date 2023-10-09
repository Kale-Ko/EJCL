package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.PropertiesParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured Properties File Config for storing Properties data in a File
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class UnstructuredPropertiesFileConfig extends UnstructuredBJSLFileConfig {
    /**
     * Create a new UnstructuredPropertiesFileConfig
     *
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public UnstructuredPropertiesFileConfig(@NotNull File file, @NotNull PropertiesParser parser, @NotNull ObjectProcessor processor) {
        super(file, parser, processor);
    }

    /**
     * Create a new UnstructuredPropertiesFileConfig
     *
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public UnstructuredPropertiesFileConfig(@NotNull File file, @NotNull PropertiesParser parser) {
        super(file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new UnstructuredPropertiesFileConfig
     *
     * @param file The file where data is being stored
     *
     * @since 1.0.0
     */
    public UnstructuredPropertiesFileConfig(@NotNull File file) {
        this(file, new PropertiesParser.Builder().build());
    }
}