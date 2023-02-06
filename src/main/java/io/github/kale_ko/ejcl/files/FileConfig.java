package io.github.kale_ko.ejcl.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import io.github.kale_ko.ejcl.Config;

public abstract class FileConfig<T> extends Config<T> {
    protected File file;

    protected boolean closed = false;

    protected FileConfig(Class<T> clazz, File file) {
        super(clazz);

        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    @Override
    public boolean getLoaded() {
        return this.config != null;
    }

    public abstract void load() throws IOException;

    protected String loadRaw() throws IOException {
        return Files.readString(this.file.toPath());
    }

    @Override
    public void save() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        Files.writeString(this.file.toPath(), this.saveRaw());
    }

    protected abstract String saveRaw() throws IOException;

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    public boolean isClosed() {
        return this.closed;
    }
}