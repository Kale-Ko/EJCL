package io.github.kale_ko.ejcl.file.bjsl;

import io.github.kale_ko.bjsl.parsers.CsvParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * An Unstructured CSV File Config for storing CSV data in a File
 *
 * @version 3.9.0
 * @since 3.9.0
 */
public class UnstructuredCsvFileConfig extends UnstructuredBJSLFileConfig {
    /**
     * Create a new UnstructuredCsvFileConfig
     *
     * @param file      The file where data is being stored
     * @param parser    The parser/processor to use for parsing and serialization
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public UnstructuredCsvFileConfig(@NotNull File file, @NotNull CsvParser parser, @NotNull ObjectProcessor processor) {
        super(file, parser, processor);
    }

    /**
     * Create a new UnstructuredCsvFileConfig
     *
     * @param file   The file where data is being stored
     * @param parser The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    public UnstructuredCsvFileConfig(@NotNull File file, @NotNull CsvParser parser) {
        super(file, parser, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a new UnstructuredCsvFileConfig
     *
     * @param file The file where data is being stored
     *
     * @since 1.0.0
     */
    public UnstructuredCsvFileConfig(@NotNull File file) {
        this(file, new CsvParser.Builder().build());
    }
}