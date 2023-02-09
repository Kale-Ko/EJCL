package io.github.kale_ko.ejcl.files;

import java.io.File;
import java.io.IOException;
import io.github.kale_ko.bjsl.BJSL;
import io.github.kale_ko.bjsl.parsers.TomlParser;

public class TomlConfig<T> extends FileConfig<T> {
    protected BJSL<TomlParser> bjsl;

    public TomlConfig(Class<T> clazz, File file, BJSL<TomlParser> bjsl) {
        super(clazz, file);

        if (bjsl == null) {
            throw new NullPointerException("Bjsl can not be null");
        }

        this.bjsl = bjsl;
    }

    public TomlConfig(Class<T> clazz, File file) {
        this(clazz, file, new BJSL<TomlParser>(new TomlParser.Builder().build()));
    }

    @Override
    public byte[] create() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        return new byte[0]; // Strange behaviour using normal method
    }

    @Override
    public void load() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        this.config = this.bjsl.parse(this.loadRaw(), this.clazz);
    }

    @Override
    public byte[] saveRaw() throws IOException {
        return this.bjsl.byteify(this.config);
    }

    @Override
    public void close() throws IOException {}
}