package io.github.kale_ko.ejcl.files;

import java.io.File;
import java.io.IOException;
import io.github.kale_ko.bjsl.BJSL;
import io.github.kale_ko.bjsl.parsers.JsonParser;

public class JsonConfig<T> extends FileConfig<T> {
    protected BJSL<JsonParser> bjsl;

    protected JsonConfig(Class<T> clazz, File file, BJSL<JsonParser> bjsl) {
        super(clazz, file);

        this.bjsl = bjsl;
    }

    protected JsonConfig(Class<T> clazz, File file) {
        this(clazz, file, new BJSL<JsonParser>(new JsonParser.Builder().build()));
    }

    @Override
    public void load() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        this.config = this.bjsl.parse(this.loadRaw(), this.clazz);
    }

    @Override
    public String saveRaw() throws IOException {
        return this.bjsl.stringify(this.config);
    }

    @Override
    public void close() throws IOException {}
}