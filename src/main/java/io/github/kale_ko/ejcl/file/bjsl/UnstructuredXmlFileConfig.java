package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.XmlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured XML File Config for storing XML data in a File
 *
 * @version 3.9.0
 * @since 3.9.0
 */
public class UnstructuredXmlFileConfig extends UnstructuredBJSLFileConfig {
    /**
     * Create a new UnstructuredXmlFileConfig
     *
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public UnstructuredXmlFileConfig(@NotNull File file, @NotNull XmlParser parser, @NotNull ObjectProcessor processor) {
        super(file, parser, processor);
    }

    /**
     * Create a new UnstructuredXmlFileConfig
     *
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public UnstructuredXmlFileConfig(@NotNull File file, @NotNull XmlParser parser) {
        super(file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new UnstructuredXmlFileConfig
     *
     * @param file The file where data is being stored
     *
     * @since 1.0.0
     */
    public UnstructuredXmlFileConfig(@NotNull File file) {
        this(file, new XmlParser.Builder().setPrettyPrint(true).build());
    }
}