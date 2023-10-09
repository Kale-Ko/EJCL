package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.XmlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * A Structured XML File Config for storing XML data in a File
 *
 * @param <T> The type of the data being stored
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class StructuredXmlFileConfig<T> extends StructuredBJSLFileConfig<T> {
    /**
     * Create a new StructuredXmlFileConfig
     *
     * @param clazz     The class of the data being stored
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public StructuredXmlFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull XmlParser parser, @NotNull ObjectProcessor processor) {
        super(clazz, file, parser, processor);
    }

    /**
     * Create a new StructuredXmlFileConfig
     *
     * @param clazz  The class of the data being stored
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public StructuredXmlFileConfig(@NotNull Class<T> clazz, @NotNull File file, @NotNull XmlParser parser) {
        super(clazz, file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new StructuredXmlFileConfig
     *
     * @param clazz The class of the data being stored
     * @param file  The file where data is being stored
     *
     * @since 2.0.0
     */
    public StructuredXmlFileConfig(@NotNull Class<T> clazz, @NotNull File file) {
        this(clazz, file, new XmlParser.Builder().setPrettyPrint(true).build());
    }
}