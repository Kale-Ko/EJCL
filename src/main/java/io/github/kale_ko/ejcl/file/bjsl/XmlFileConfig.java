package io.github.kale_ko.ejcl.file.bjsl;

import java.io.File;
import io.github.kale_ko.bjsl.parsers.XmlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;

/**
 * A XML File Config for storing XML data in a File
 *
 * @param <T>
 *        The type of the data being stored
 * @version 2.0.0
 * @since 1.0.0
 */
public class XmlFileConfig<T> extends BJSLFileConfig<T> {
    /**
     * Create a new XmlFileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @param parser
     *        The parser/processor to use for parsing and serialization
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @since 2.0.0
     */
    public XmlFileConfig(Class<T> clazz, File file, XmlParser parser, ObjectProcessor processor) {
        super(clazz, file, parser, processor);
    }

    /**
     * Create a new XmlFileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @param parser
     *        The parser/processor to use for parsing and serialization
     * @since 2.0.0
     */
    public XmlFileConfig(Class<T> clazz, File file, XmlParser parser) {
        super(clazz, file, parser, new ObjectProcessor.Builder().build());
    }
}