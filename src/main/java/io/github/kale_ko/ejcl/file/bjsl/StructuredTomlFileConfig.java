package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.TomlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * A Structured TOML File Config for storing TOML data in a File
 *
 * @param <T> The type of the data being stored
 *
 * @version 3.9.0
 * @since 1.0.0
 */
public class StructuredTomlFileConfig<T> extends StructuredBJSLFileConfig<T> {
    /**
     * Create a new StructuredTomlFileConfig
     *
     * @param clazz     The class of the data being stored
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public StructuredTomlFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull TomlParser parser, @NotNull ObjectProcessor processor) {
        super(clazz, file, parser, processor);
    }

    /**
     * Create a new StructuredTomlFileConfig
     *
     * @param clazz  The class of the data being stored
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public StructuredTomlFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull TomlParser parser) {
        super(clazz, file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new StructuredTomlFileConfig
     *
     * @param clazz The class of the data being stored
     * @param file  The file where data is being stored
     *
     * @since 1.0.0
     */
    public StructuredTomlFileConfig(@NotNull Class<T> clazz, @NotNull File file) {
        this(clazz, file, new TomlParser.Builder().build());
    }
}