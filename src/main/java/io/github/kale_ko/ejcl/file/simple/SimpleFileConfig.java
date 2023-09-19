package io.github.kale_ko.ejcl.file.simple;

import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.exception.ConfigClosedException;
import io.github.kale_ko.ejcl.file.UnstructuredFileConfig;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A Simple File Config for storing key/value pairs in a File
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimpleFileConfig extends UnstructuredFileConfig {
    /**
     * Create a new SimpleFileConfig
     *
     * @param file      The file where data is being stored
     * @param processor The ObjectProcessor to use for serialization/deserialization
     *
     * @since 2.0.0
     */
    public SimpleFileConfig(@NotNull File file, @NotNull ObjectProcessor processor) {
        super(file, processor);
    }

    /**
     * Create a new SimpleFileConfig
     *
     * @param file The file where data is being stored
     *
     * @since 2.0.0
     */
    public SimpleFileConfig(@NotNull File file) {
        this(file, new ObjectProcessor.Builder().build());
    }

    /**
     * Create a blank config file
     *
     * @return The config bytes
     *
     * @throws IOException On create error
     * @since 1.0.0
     */
    @Override
    public byte @NotNull [] create() throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        return "\n".getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Load the config
     *
     * @param save Weather to save the config after loaded (To update the template)
     *
     * @throws IOException On load error
     * @since 1.3.0
     */
    @Override
    public void load(boolean save) throws IOException {
        if (this.closed) {
            throw new ConfigClosedException();
        }

        synchronized (SAVELOAD_LOCK) {
            this.config = ParsedObject.create();

            for (String line : new String(this.loadRaw(), StandardCharsets.UTF_8).split("\n")) {
                line = line.trim();

                this.config.set(line.split("=")[0].trim(), ParsedPrimitive.fromString(line.split("=")[1].trim()));
            }

            if (save) {
                this.save();
            }
        }
    }

    /**
     * Save the config to bytes
     *
     * @return The config bytes
     *
     * @throws IOException On save error
     * @since 1.0.0
     */
    @Override
    public byte @NotNull [] saveRaw() throws IOException {
        if (this.config == null) {
            return this.create();
        }

        StringBuilder data = new StringBuilder();

        for (Map.Entry<String, ParsedElement> entry : this.config.getEntries()) {
            if (entry.getValue().isPrimitive()) {
                data.append(entry.getKey()).append("=").append(entry.getValue().asPrimitive().get().toString());
            }
        }

        return data.toString().getBytes(StandardCharsets.UTF_8);
    }
}