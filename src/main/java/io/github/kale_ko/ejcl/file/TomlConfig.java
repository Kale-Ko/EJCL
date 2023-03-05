package io.github.kale_ko.ejcl.file;

import java.io.File;
import java.io.IOException;
import io.github.kale_ko.bjsl.BJSL;
import io.github.kale_ko.bjsl.parsers.TomlParser;

/**
 * A Toml File Config for storing Toml data in a File
 *
 * @param <T>
 *        The type of the data being stored
 * @version 1.0.0
 * @since 1.0.0
 */
public class TomlConfig<T> extends FileConfig<T> {
    /**
     * The parser/processor to use for parsing and serialization
     *
     * @since 1.0.0
     */
    protected BJSL<TomlParser> bjsl;

    /**
     * Create a new JsonConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @param bjsl
     *        The parser/processor to use for parsing and serialization
     * @since 1.0.0
     */
    public TomlConfig(Class<T> clazz, File file, BJSL<TomlParser> bjsl) {
        super(clazz, file);

        if (bjsl == null) {
            throw new NullPointerException("Bjsl can not be null");
        }

        this.bjsl = bjsl;
    }

    /**
     * Create a new JsonConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @since 1.0.0
     */
    public TomlConfig(Class<T> clazz, File file) {
        this(clazz, file, new BJSL<TomlParser>(new TomlParser.Builder().build()));
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

        return this.bjsl.emptyBytes();
    }

    /**
     * Load the config from file
     *
     * @throws IOException
     *         On load error
     * @since 1.0.0
     */
    @Override
    public void load() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        this.config = this.bjsl.parse(this.loadRaw(), this.clazz);
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
        return this.bjsl.byteify(this.config);
    }
}