package io.github.kale_ko.ejcl.file.simple;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.file.UnstructuredFileConfig;

/**
 * A Simple File Config for storing ParsedObjects in a File
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimpleFileConfig extends UnstructuredFileConfig {
    /**
     * Create a new SimpleFileConfig
     *
     * @param file
     *        The file where data is being stored
     * @param processor
     *        The ObjectProcessor to use for serialization/deserialization
     * @since 2.0.0
     */
    public SimpleFileConfig(File file, ObjectProcessor processor) {
        super(file, processor);
    }

    /**
     * Create a new SimpleFileConfig
     *
     * @param file
     *        The file where data is being stored
     * @since 2.0.0
     */
    public SimpleFileConfig(File file) {
        this(file, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a blank config file
     *
     * @throws IOException
     *         On create error
     * @return The config bytes
     * @since 1.0.0
     */
    @Override
    public byte[] create() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        return "\n".getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Load the config
     *
     * @param save
     *        Weather to save the config after loaded (To update the template)
     * @throws IOException
     *         On load error
     * @since 1.3.0
     */
    @Override
    public void load(boolean save) throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        this.config = ParsedObject.create();

        for (String line : new String(this.loadRaw()).split("\n")) {
            line = line.trim();

            this.config.set(line.split("=")[0].trim(), ParsedPrimitive.fromString(line.split("=")[1].trim()));
        }

        if (save) {
            this.save();
        }
    }

    /**
     * Save the config to bytes
     *
     * @throws IOException
     *         On save error
     * @return The config bytes
     * @since 1.0.0
     */
    @Override
    public byte[] saveRaw() throws IOException {
        StringBuilder data = new StringBuilder();

        for (Map.Entry<String, ParsedElement> entry : this.config.getEntries()) {
            data.append(entry.getKey() + "=" + entry.getValue().asPrimitive().toString());
        }

        return data.toString().getBytes(StandardCharsets.UTF_8);
    }
}