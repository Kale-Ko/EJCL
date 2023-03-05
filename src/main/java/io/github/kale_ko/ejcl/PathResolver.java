package io.github.kale_ko.ejcl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.github.kale_ko.bjsl.elements.ParsedArray;
import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;

/**
 * Contains methods useful for getting/setting nested values
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class PathResolver {
    /**
     * Create a path resolver
     *
     * @since 1.0.0
     */
    private PathResolver() {}

    /**
     * Resolve a value on an element
     *
     * @param element
     *        The element to resolve on
     * @param path
     *        The path to resolve to
     * @return The value resolved
     * @since 1.0.0
     */
    public static Object resolve(ParsedElement element, String path) {
        String[] keys = path.replaceAll("\\[([0-9])\\]", ".[$1]").split("\\.");

        ParsedElement resolved = element;

        for (String key : keys) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(key)) {
                    resolved = resolved.asObject().get(key);
                } else {
                    resolved = null;
                    break;
                }
            } else if (resolved.isArray()) {
                if (key.startsWith("[") && key.endsWith("]")) {
                    int index = Integer.parseInt(key.replaceAll("\\[([0-9])\\]", "$1"));

                    if (index >= 0 && index < resolved.asArray().getSize()) {
                        resolved = resolved.asArray().get(index);
                    } else {
                        resolved = null;
                        break;
                    }
                } else {
                    resolved = null;
                    break;
                }
            } else {
                resolved = null;
                break;
            }
        }

        if (resolved == null) {
            return null;
        } else if (resolved.isObject()) {
            return "{obj}," + String.join(",", resolved.asObject().getKeys());
        } else if (resolved.isArray()) {
            return "{arr}," + resolved.asArray().getSize();
        } else if (resolved.isPrimitive()) {
            return resolved.asPrimitive().get();
        }

        return null;
    }

    /**
     * Update a value on an element
     *
     * @param element
     *        The element to update on
     * @param path
     *        The path to update
     * @param value
     *        The value to update to
     * @since 1.0.0
     */
    public static void update(ParsedElement element, String path, Object value) {
        String[] keys = path.replaceAll("\\[([0-9])\\]", ".[$1]").split("\\.");
        String valueKey = keys[keys.length - 1];
        keys = Arrays.copyOf(keys, keys.length - 1);

        ParsedElement resolved = element;

        for (String key : keys) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(key)) {
                    resolved = resolved.asObject().get(key);
                } else {
                    resolved = null;
                    break;
                }
            } else if (resolved.isArray()) {
                if (key.startsWith("[") && key.endsWith("]")) {
                    int index = Integer.parseInt(key.replaceAll("\\[([0-9])\\]", "$1"));

                    if (index >= 0 && index < resolved.asArray().getSize()) {
                        resolved = resolved.asArray().get(index);
                    } else {
                        resolved = null;
                        break;
                    }
                } else {
                    resolved = null;
                    break;
                }
            } else {
                resolved = null;
                break;
            }
        }

        if (resolved != null) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(valueKey)) {
                    if (resolved.asObject().get(valueKey).isPrimitive()) {
                        resolved.asObject().set(valueKey, ParsedPrimitive.from(value));
                    }
                } else {
                    if (value instanceof String string && string.startsWith("{obj},")) {
                        resolved.asObject().set(valueKey, ParsedObject.create());
                    } else if (value instanceof String string && string.startsWith("{arr},")) {
                        resolved.asObject().set(valueKey, ParsedArray.create());
                    } else {
                        resolved.asObject().set(valueKey, ParsedPrimitive.from(value));
                    }
                }
            } else if (resolved.isArray()) {
                if (valueKey.startsWith("[") && valueKey.endsWith("]")) {
                    Integer resolvedValueKey = Integer.parseInt(valueKey.replaceAll("\\[([0-9])\\]", "$1"));

                    if (resolvedValueKey >= 0 && resolvedValueKey < resolved.asArray().getSize()) {
                        if (resolved.asArray().get(resolvedValueKey).isPrimitive()) {
                            resolved.asArray().set(resolvedValueKey, ParsedPrimitive.from(value));
                        }
                    } else {
                        if (value instanceof String string && string.startsWith("{obj},")) {
                            resolved.asArray().add(ParsedObject.create());
                        } else if (value instanceof String string && string.startsWith("{arr},")) {
                            resolved.asArray().add(ParsedArray.create());
                        } else {
                            resolved.asArray().add(ParsedPrimitive.from(value));
                        }
                    }
                }
            }
        }
    }

    /**
     * Get all the keys in an element
     *
     * @param element
     *        The element to get the keys of
     * @return A list of all the keys
     * @since 1.0.0
     */
    public static List<String> getKeys(ParsedElement element) {
        return getKeys(element, "");
    }

    /**
     * Get all the keys in an element
     *
     * @param element
     *        The element to get the keys of
     * @param path
     *        The current path
     * @return A list of all the keys
     * @since 1.0.0
     */
    protected static List<String> getKeys(ParsedElement element, String path) {
        List<String> keys = new ArrayList<String>();

        if (element.isObject()) {
            ParsedObject object = element.asObject();

            for (String key : object.getKeys()) {
                keys.add(path + key);

                if (object.get(key).isObject()) {
                    keys.addAll(getKeys(object.get(key), path + key + "."));
                } else if (object.get(key).isArray()) {
                    keys.addAll(getKeys(object.get(key), path + key));
                }
            }
        } else if (element.isArray()) {
            ParsedArray array = element.asArray();

            for (int i = 0; i < array.getSize(); i++) {
                keys.add(path + "[" + i + "]");

                if (array.get(i).isObject()) {
                    keys.addAll(getKeys(array.get(i), path + "[" + i + "]" + "."));
                } else if (array.get(i).isArray()) {
                    keys.addAll(getKeys(array.get(i), path + "[" + i + "]"));
                }
            }
        }

        return keys;
    }
}