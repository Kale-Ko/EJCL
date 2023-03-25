package io.github.kale_ko.ejcl.file.bjsl;

import java.io.File;
import io.github.kale_ko.bjsl.parsers.SmileParser;

/**
 * A Smile File Config for storing Smile data in a File
 *
 * @param <T>
 *        The type of the data being stored
 * @version 2.0.0
 * @since 1.0.0
 */
public class SmileFileConfig<T> extends BJSLFileConfig<T> {
    /**
     * Create a new SmileFileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @param parser
     *        The parser/processor to use for parsing and serialization
     * @since 2.0.0
     */
    public SmileFileConfig(Class<T> clazz, File file, SmileParser parser) {
        super(clazz, file, parser);
    }

    /**
     * Create a new SmileFileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @since 1.0.0
     */
    public SmileFileConfig(Class<T> clazz, File file) {
        this(clazz, file, new SmileParser.Builder().build());
    }
}