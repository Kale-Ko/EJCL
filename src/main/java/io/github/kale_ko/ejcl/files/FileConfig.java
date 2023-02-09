package io.github.kale_ko.ejcl.files;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import io.github.kale_ko.ejcl.Config;

public abstract class FileConfig<T> extends Config<T> {
    protected File file;

    protected boolean closed = false;

    @SuppressWarnings("unchecked")
    protected FileConfig(Class<T> clazz, File file) {
        super(clazz);

        if (file == null) {
            throw new NullPointerException("File can not be null");
        }

        this.file = file;

        try {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if ((constructor.canAccess(null) || constructor.trySetAccessible()) && constructor.getParameterTypes().length == 0) {
                    this.config = (T) constructor.newInstance();

                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {        }

        if (this.config == null) {
            try {
                Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
                this.config = (T) unsafe.allocateInstance(clazz);
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {            }
        }

        if (this.config == null) {
            throw new RuntimeException("Could not instantiate new config");
        }
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