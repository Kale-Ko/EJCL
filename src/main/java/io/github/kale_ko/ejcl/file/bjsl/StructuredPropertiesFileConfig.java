package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.PropertiesParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * A Properties File Config for storing Properties data in a File
 *
 * @param <T> The type of the data being stored
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class StructuredPropertiesFileConfig<T> extends StructuredBJSLFileConfig<T> {
    /**
     * Create a new StructuredPropertiesFileConfig
     *
     * @param clazz     The class of the data being stored
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public StructuredPropertiesFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull PropertiesParser parser, @NotNull ObjectProcessor processor) {
        super(clazz, file, parser, processor);
    }

    /**
     * Create a new StructuredPropertiesFileConfig
     *
     * @param clazz  The class of the data being stored
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public StructuredPropertiesFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull PropertiesParser parser) {
        super(clazz, file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new StructuredPropertiesFileConfig
     *
     * @param clazz The class of the data being stored
     * @param file  The file where data is being stored
     *
     * @since 2.0.0
     */
    public StructuredPropertiesFileConfig(@NotNull Class<T> clazz, @NotNull File file) {
        this(clazz, file, new PropertiesParser.Builder().build());
    }
}