package io.github.kale_ko.ejcl.file;

import java.io.File;
import java.io.IOException;
import io.github.kale_ko.bjsl.BJSL;
import io.github.kale_ko.bjsl.parsers.PropertiesParser;

public class PropertiesConfig<T> extends FileConfig<T> {
    protected BJSL<PropertiesParser> bjsl;

    public PropertiesConfig(Class<T> clazz, File file, BJSL<PropertiesParser> bjsl) {
        super(clazz, file);

        if (bjsl == null) {
            throw new NullPointerException("Bjsl can not be null");
        }

        this.bjsl = bjsl;
    }

    public PropertiesConfig(Class<T> clazz, File file) {
        this(clazz, file, new BJSL<PropertiesParser>(new PropertiesParser.Builder().build()));
    }

    @Override
    public byte[] create() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        return this.bjsl.emptyBytes();
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