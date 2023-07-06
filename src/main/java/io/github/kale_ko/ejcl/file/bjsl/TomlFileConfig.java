package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.TomlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;

/**
 * A TOML File Config for storing TOML data in a File
 *
 * @param <T> The type of the data being stored
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class TomlFileConfig<T> extends BJSLFileConfig<T> {
    /**
     * Create a new TomlFileConfig
     *
     * @param clazz     The class of the data being stored
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public TomlFileConfig(Class<T> clazz, File file, TomlParser parser, ObjectProcessor processor) {
        super(clazz, file, parser, processor);
    }

    /**
     * Create a new TomlFileConfig
     *
     * @param clazz  The class of the data being stored
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public TomlFileConfig(Class<T> clazz, File file, TomlParser parser) {
        super(clazz, file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new TomlFileConfig
     *
     * @param clazz The class of the data being stored
     * @param file  The file where data is being stored
     *
     * @since 1.0.0
     */
    public TomlFileConfig(Class<T> clazz, File file) {
        this(clazz, file, new TomlParser.Builder().build());
    }
}