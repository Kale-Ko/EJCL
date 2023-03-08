package io.github.kale_ko.ejcl.file;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import io.github.kale_ko.ejcl.Config;

/**
 * A File Config for storing data in a File
 *
 * @param <T>
 *        The type of the data being stored
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class FileConfig<T> extends Config<T> {
    /**
     * The file where data is being stored
     *
     * @since 1.0.0
     */
    protected File file;

    /**
     * If this config is closed
     *
     * @since 1.0.0
     */
    protected boolean closed = false;

    /**
     * Create a new FileConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @param file
     *        The file where data is being stored
     * @since 1.0.0
     */
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
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
        }

        if (this.config == null) {
            try {
                Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
                this.config = (T) unsafe.allocateInstance(clazz);
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            }
        }

        if (this.config == null) {
            throw new RuntimeException("Could not instantiate new config");
        }
    }

    /**
     * Get the file where data is being stored
     *
     * @return The file where data is being stored
     * @since 1.0.0
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Get if the config is loaded
     *
     * @return If the config is loaded
     * @since 1.0.0
     */
    @Override
    public boolean getLoaded() {
        return this.config != null;
    }

    /**
     * Create a blank config file
     *
     * @throws IOException
     *         On create error
     * @return The config bytes
     * @since 1.0.0
     */
    public abstract byte[] create() throws IOException;

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
    public abstract void load(boolean save) throws IOException;

    /**
     * Load the config from file
     *
     * @throws IOException
     *         On load error
     * @return The file bytes
     * @since 1.0.0
     */
    protected byte[] loadRaw() throws IOException {
        if (!Files.exists(this.file.toPath())) {
            Files.createFile(this.file.toPath());

            Files.write(this.file.toPath(), this.create());
        }

        return Files.readAllBytes(this.file.toPath());
    }

    /**
     * Save the config to file
     *
     * @throws IOException
     *         On save error
     * @since 1.0.0
     */
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

    /**
     * Save the config to bytes
     *
     * @throws IOException
     *         On save error
     * @return The config bytes
     * @since 1.0.0
     */
    protected abstract byte[] saveRaw() throws IOException;

    /**
     * Close the config
     *
     * @throws IOException
     *         On close error
     * @since 1.0.0
     */
    @Override
    public void close() throws IOException {
        if (this.closed) {
            throw new RuntimeException("Config is already closed");
        }

        this.closed = true;
    }

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     * @since 1.0.0
     */
    public boolean isClosed() {
        return this.closed;
    }
}