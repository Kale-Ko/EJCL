package io.github.kale_ko.ejcl.file.bjsl;

import java.io.File;
import io.github.kale_ko.bjsl.parsers.JsonParser;

/**
 * A JSON File Config for storing JSON data in a File
 *
 * @param <T>
 *        The type of the data being stored
 * @version 2.0.0
 * @since 1.0.0
 */
public class JsonFileConfig<T> extends BJSLFileConfig<T> {
    /**
     * Create a new JsonFileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @param parser
     *        The parser/processor to use for parsing and serialization
     * @since 2.0.0
     */
    public JsonFileConfig(Class<T> clazz, File file, JsonParser parser) {
        super(clazz, file, parser);
    }

    /**
     * Create a new JsonFileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @since 1.0.0
     */
    public JsonFileConfig(Class<T> clazz, File file) {
        this(clazz, file, new JsonParser.Builder().setPrettyPrint(true).build());
    }
}