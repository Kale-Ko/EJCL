package io.github.kale_ko.ejcl.files;

import java.io.File;
import java.io.IOException;
import io.github.kale_ko.bjsl.BJSL;
import io.github.kale_ko.bjsl.parsers.YamlParser;

public class YamlConfig<T> extends FileConfig<T> {
    protected BJSL<YamlParser> bjsl;

    public YamlConfig(Class<T> clazz, File file, BJSL<YamlParser> bjsl) {
        super(clazz, file);

        if (bjsl == null) {
            throw new NullPointerException("Bjsl can not be null");
        }

        this.bjsl = bjsl;
    }

    public YamlConfig(Class<T> clazz, File file) {
        this(clazz, file, new BJSL<YamlParser>(new YamlParser.Builder().build()));
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