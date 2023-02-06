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

    public abstract byte[] create() throws IOException;

    public abstract void load() throws IOException;

    protected byte[] loadRaw() throws IOException {
        if (!Files.exists(this.file.toPath())) {
            Files.createFile(this.file.toPath());

            Files.write(this.file.toPath(), this.create());
        }

        return Files.readAllBytes(this.file.toPath());
    }

    @Override
    public void save() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        if (!Files.exists(this.file.toPath())) {
            Files.createFile(this.file.toPath());
        }

        Files.write(this.file.toPath(), this.saveRaw());
    }

    protected abstract byte[] saveRaw() throws IOException;

    @Override
    public void close() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        this.closed = true;
    }

    public boolean isClosed() {
        return this.closed;
    }
}