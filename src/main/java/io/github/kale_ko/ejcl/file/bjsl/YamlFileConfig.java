package io.github.kale_ko.ejcl.file.bjsl;

import java.io.File;
import io.github.kale_ko.bjsl.parsers.YamlParser;

/**
 * A YAML File Config for storing YAML data in a File
 *
 * @param <T>
 *        The type of the data being stored
 * @version 2.0.0
 * @since 1.0.0
 */
public class YamlFileConfig<T> extends BJSLFileConfig<T> {
    /**
     * Create a new YamlFileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @param parser
     *        The parser/processor to use for parsing and serialization
     * @since 2.0.0
     */
    public YamlFileConfig(Class<T> clazz, File file, YamlParser parser) {
        super(clazz, file, parser);
    }

    /**
     * Create a new YamlFileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @since 1.0.0
     */
    public YamlFileConfig(Class<T> clazz, File file) {
        this(clazz, file, new YamlParser.Builder().build());
    }
}