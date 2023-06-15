package io.github.kale_ko.ejcl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        return resolve(element, path, true);
    }

    /**
     * Resolve a value on an element
     *
     * @param element
     *        The element to resolve on
     * @param path
     *        The path to resolve to
     * @param returnObjArrValues
     *        Weather or not to return the keys of objects and arrays when queried
     * @return The value resolved
     * @since 1.0.0
     */
    public static Object resolve(ParsedElement element, String path, boolean returnObjArrValues) {
        String[] keys = path.replaceAll("\\[([0-9])\\]", ".[$1]").split("[^\\]\\.");

        ParsedElement resolved = element;

        for (int i = 0; i < keys.length; i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys[i])) {
                    resolved = resolved.asObject().get(keys[i]);
                } else {
                    resolved = null;
                    break;
                }
            } else if (resolved.isArray()) {
                if (keys[i].startsWith("[") && keys[i].endsWith("]")) {
                    int index = Integer.parseInt(keys[i].replaceAll("\\[([0-9])\\]", "$1"));

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
        } else if (resolved.isObject() && returnObjArrValues) {
            return "{obj}," + String.join(",", resolved.asObject().getKeys());
        } else if (resolved.isArray() && returnObjArrValues) {
            return "{arr}," + resolved.asArray().getSize();
        } else if (resolved.isPrimitive()) {
            return resolved.asPrimitive().get();
        }

        return null;
    }

    /**
     * Resolve an element on an element
     *
     * @param element
     *        The element to resolve on
     * @param path
     *        The path to resolve to
     * @return The value resolved
     * @since 1.0.0
     */
    public static ParsedElement resolveElement(ParsedElement element, String path) {
        String[] keys = path.replaceAll("\\[([0-9])\\]", ".[$1]").split("[^\\]\\.");

        ParsedElement resolved = element;

        for (int i = 0; i < keys.length; i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys[i])) {
                    resolved = resolved.asObject().get(keys[i]);
                } else {
                    resolved = null;
                    break;
                }
            } else if (resolved.isArray()) {
                if (keys[i].startsWith("[") && keys[i].endsWith("]")) {
                    int index = Integer.parseInt(keys[i].replaceAll("\\[([0-9])\\]", "$1"));

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

        return resolved;
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
     * @return element for chaining
     * @since 1.0.0
     */
    public static ParsedElement update(ParsedElement element, String path, Object value) {
        return PathResolver.update(element, path, value, true);
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
     * @param force
     *        If the value should be force set (Create objects/arrays that dont exist)
     * @return element for chaining
     * @since 1.0.0
     */
    public static ParsedElement update(ParsedElement element, String path, Object value, boolean force) {
        String[] keys = path.replaceAll("\\[([0-9])\\]", ".[$1]").split("[^\\]\\.");
        String valueKey = keys[keys.length - 1];

        ParsedElement resolved = element;

        for (int i = 0; i < keys.length - 1; i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys[i])) {
                    resolved = resolved.asObject().get(keys[i]);
                } else {
                    if (force) {
                        if (keys[i + 1].equals(keys[i + 1].replaceAll("\\[([0-9])\\]", "$1"))) {
                            resolved.asObject().set(keys[i], ParsedObject.create());
                        } else {
                            resolved.asObject().set(keys[i], ParsedArray.create());
                        }

                        resolved = resolved.asObject().get(keys[i]);
                    } else {
                        resolved = null;
                        break;
                    }
                }
            } else if (resolved.isArray()) {
                if (keys[i].startsWith("[") && keys[i].endsWith("]")) {
                    int index = Integer.parseInt(keys[i].replaceAll("\\[([0-9])\\]", "$1"));

                    if (index >= 0 && index < resolved.asArray().getSize()) {
                        resolved = resolved.asArray().get(index);
                    } else {
                        if (force) {
                            while (resolved.asArray().getSize() <= index) {
                                if (keys[i + 1].equals(keys[i + 1].replaceAll("\\[([0-9])\\]", "$1"))) {
                                    resolved.asArray().add(ParsedObject.create());
                                } else {
                                    resolved.asArray().add(ParsedArray.create());
                                }
                            }

                            resolved = resolved.asArray().get(index);
                        } else {
                            resolved = null;
                            break;
                        }
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
                    if (value instanceof String && ((String) value).startsWith("{obj},")) {
                        resolved.asObject().set(valueKey, ParsedObject.create());
                    } else if (value instanceof String && ((String) value).startsWith("{arr},")) {
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
                        if (value instanceof String && ((String) value).startsWith("{obj},")) {
                            resolved.asArray().add(ParsedObject.create());
                        } else if (value instanceof String && ((String) value).startsWith("{arr},")) {
                            resolved.asArray().add(ParsedArray.create());
                        } else {
                            resolved.asArray().add(ParsedPrimitive.from(value));
                        }
                    }
                }
            }
        }

        return element;
    }

    /**
     * Update an element on an element
     *
     * @param element
     *        The element to update on
     * @param path
     *        The path to update
     * @param value
     *        The value to update to
     * @return element for chaining
     * @since 1.0.0
     */
    public static ParsedElement updateElement(ParsedElement element, String path, ParsedElement value) {
        return PathResolver.updateElement(element, path, value, true);
    }

    /**
     * Update an element on an element
     *
     * @param element
     *        The element to update on
     * @param path
     *        The path to update
     * @param value
     *        The value to update to
     * @param force
     *        If the value should be force set (Create objects/arrays that dont exist)
     * @return element for chaining
     * @since 1.0.0
     */
    public static ParsedElement updateElement(ParsedElement element, String path, ParsedElement value, boolean force) {
        String[] keys = path.replaceAll("\\[([0-9])\\]", ".[$1]").split("[^\\]\\.");
        String valueKey = keys[keys.length - 1];

        ParsedElement resolved = element;

        for (int i = 0; i < keys.length - 1; i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys[i])) {
                    resolved = resolved.asObject().get(keys[i]);
                } else {
                    if (force) {
                        if (keys[i + 1].equals(keys[i + 1].replaceAll("\\[([0-9])\\]", "$1"))) {
                            resolved.asObject().set(keys[i], ParsedObject.create());
                        } else {
                            resolved.asObject().set(keys[i], ParsedArray.create());
                        }

                        resolved = resolved.asObject().get(keys[i]);
                    } else {
                        resolved = null;
                        break;
                    }
                }
            } else if (resolved.isArray()) {
                if (keys[i].startsWith("[") && keys[i].endsWith("]")) {
                    int index = Integer.parseInt(keys[i].replaceAll("\\[([0-9])\\]", "$1"));

                    if (index >= 0 && index < resolved.asArray().getSize()) {
                        resolved = resolved.asArray().get(index);
                    } else {
                        if (force) {
                            while (resolved.asArray().getSize() <= index) {
                                if (keys[i + 1].equals(keys[i + 1].replaceAll("\\[([0-9])\\]", "$1"))) {
                                    resolved.asArray().add(ParsedObject.create());
                                } else {
                                    resolved.asArray().add(ParsedArray.create());
                                }
                            }

                            resolved = resolved.asArray().get(index);
                        } else {
                            resolved = null;
                            break;
                        }
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
                resolved.asObject().set(valueKey, value);
            } else if (resolved.isArray()) {
                if (valueKey.startsWith("[") && valueKey.endsWith("]")) {
                    Integer resolvedValueKey = Integer.parseInt(valueKey.replaceAll("\\[([0-9])\\]", "$1"));

                    if (resolvedValueKey >= 0 && resolvedValueKey < resolved.asArray().getSize()) {
                        resolved.asArray().set(resolvedValueKey, value);
                    } else {
                        resolved.asArray().add(value);
                    }
                }
            }
        }

        return element;
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
        return getKeys(element, true);
    }

    /**
     * Get all the keys in an element
     *
     * @param element
     *        The element to get the keys of
     * @param returnObjArrKeys
     *        Weather or not to return the keys for objects and arrays
     * @return A list of all the keys
     * @since 1.0.0
     */
    public static List<String> getKeys(ParsedElement element, boolean returnObjArrKeys) {
        return getKeys(element, "", returnObjArrKeys);
    }

    /**
     * Get all the keys in an element
     *
     * @param element
     *        The element to get the keys of
     * @param path
     *        The current path
     * @param returnObjArrKeys
     *        Weather or not to return the keys for objects and arrays
     * @return A list of all the keys
     * @since 1.0.0
     */
    protected static List<String> getKeys(ParsedElement element, String path, boolean returnObjArrKeys) {
        List<String> keys = new ArrayList<String>();

        if (element.isObject()) {
            ParsedObject object = element.asObject();

            for (Map.Entry<String, ParsedElement> entry : object.getEntries()) {
                if (entry.getValue().isObject()) {
                    keys.addAll(getKeys(entry.getValue(), path + entry.getKey().replace(".", "\\.") + ".", returnObjArrKeys));
                } else if (entry.getValue().isArray()) {
                    keys.addAll(getKeys(entry.getValue(), path + entry.getKey().replace(".", "\\."), returnObjArrKeys));
                } else if (returnObjArrKeys || entry.getValue().isPrimitive()) {
                    keys.add(path + entry.getKey().replace(".", "\\."));
                }
            }
        } else if (element.isArray()) {
            ParsedArray array = element.asArray();

            for (int i = 0; i < array.getSize(); i++) {
                if (array.get(i).isObject()) {
                    keys.addAll(getKeys(array.get(i), path + "[" + i + "]" + ".", returnObjArrKeys));
                } else if (array.get(i).isArray()) {
                    keys.addAll(getKeys(array.get(i), path + "[" + i + "]", returnObjArrKeys));
                } else if (returnObjArrKeys || array.get(i).isPrimitive()) {
                    keys.add(path + "[" + i + "]");
                }
            }
        }

        return keys;
    }
}