package io.github.kale_ko.ejcl.file;

import java.io.File;
import java.io.IOException;
import io.github.kale_ko.bjsl.BJSL;
import io.github.kale_ko.bjsl.parsers.JsonParser;

public class JsonConfig<T> extends FileConfig<T> {
    protected BJSL<JsonParser> bjsl;

    public JsonConfig(Class<T> clazz, File file, BJSL<JsonParser> bjsl) {
        super(clazz, file);

        if (bjsl == null) {
            throw new NullPointerException("Bjsl can not be null");
        }

        this.bjsl = bjsl;
    }

    public JsonConfig(Class<T> clazz, File file) {
        this(clazz, file, new BJSL<JsonParser>(new JsonParser.Builder().build()));
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