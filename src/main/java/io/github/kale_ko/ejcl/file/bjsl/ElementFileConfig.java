package io.github.kale_ko.ejcl.file.bjsl;

import java.io.File;
import java.io.IOException;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.parsers.Parser;
import io.github.kale_ko.ejcl.file.FileConfig;

/**
 * A Element File Config for storing ParsedObjects in a File
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ElementFileConfig extends FileConfig<ParsedObject> {
    /**
     * The parser/processor to use for parsing and serialization
     *
     * @since 2.0.0
     */
    protected Parser<?, ?> parser;

    /**
     * Create a new ElementFileConfig
     *
     * @param file
     *        The file where data is being stored
     * @param parser
     *        The parser/processor to use for parsing and serialization
     * @since 2.0.0
     */
    public ElementFileConfig(File file, Parser<?, ?> parser) {
        super(ParsedObject.class, file);

        if (parser == null) {
            throw new NullPointerException("Parser can not be null");
        }

        this.parser = parser;
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

        return this.parser.emptyBytes();
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

        this.config = this.parser.toElement(this.loadRaw()).asObject();

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
        return this.parser.toBytes(this.config);
    }
}