package io.github.kale_ko.ejcl.memory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.Config;

/**
 * A Memory Config for storing data in memory
 *
 * @param <T>
 *        The type of the data being stored
 * @version 2.0.0
 * @since 2.0.0
 */
public class MemoryConfig<T> extends Config<T> {
    /**
     * Create a new MemoryConfig
     *
     * @param clazz
     *        The class of the data being stored
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    protected MemoryConfig(Class<T> clazz) {
        super(clazz, new ObjectProcessor.Builder().build());

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
     * Get if the config is loaded
     *
     * @return If the config is loaded
     * @since 1.0.0
     */
    @Override
    public boolean getLoaded() {
        return true;
    }

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
    public void load(boolean save) throws IOException {}

    /**
     * Save the config to memory
     *
     * @throws IOException
     *         On save error
     * @since 1.0.0
     */
    @Override
    public void save() throws IOException {}

    /**
     * Close the config
     *
     * @throws IOException
     *         On close error
     * @since 1.0.0
     */
    @Override
    public void close() throws IOException {}

    /**
     * Get if the config is closed
     *
     * @return If the config is closed
     * @since 1.0.0
     */
    public boolean isClosed() {
        return false;
    }
}