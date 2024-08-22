package io.github.kale_ko.ejcl;

import io.github.kale_ko.bjsl.elements.ParsedArray;
import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;
import java.util.*;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains methods useful for getting/setting nested values
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class PathResolver {
    protected static final Pattern arrayPathFixPattern = Pattern.compile("\\[([0-9]*)]");
    protected static final Pattern pathSplitPattern = Pattern.compile("(?<!\\\\)(?:\\\\\\\\)*\\.");

    private PathResolver() {
    }

    /**
     * Resolve a value on an element
     *
     * @param element The element to resolve on
     * @param path    The path to resolve to
     *
     * @return The value resolved
     *
     * @since 1.0.0
     */
    public static @Nullable Object resolve(@NotNull ParsedElement element, @NotNull String path) {
        path = arrayPathFixPattern.matcher(path).replaceAll(".[$1]");

        List<String> keys = Arrays.asList(pathSplitPattern.split(path));

        ParsedElement resolved = element;

        for (int i = 0; i < keys.size(); i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys.get(i))) {
                    resolved = resolved.asObject().get(keys.get(i));
                } else {
                    resolved = null;
                    break;
                }
            } else if (resolved.isArray()) {
                if (keys.get(i).startsWith("[") && keys.get(i).endsWith("]")) {
                    int index = Integer.parseInt(arrayPathFixPattern.matcher(keys.get(i)).replaceAll(".[$1]"));

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

        if (resolved != null && resolved.isPrimitive()) {
            return resolved.asPrimitive().get();
        } else {
            return null;
        }
    }

    /**
     * Resolve an element on an element
     *
     * @param element The element to resolve on
     * @param path    The path to resolve to
     *
     * @return The value resolved
     *
     * @since 1.0.0
     */
    public static @Nullable ParsedElement resolveElement(@NotNull ParsedElement element, @NotNull String path) {
        path = arrayPathFixPattern.matcher(path).replaceAll(".[$1]");

        List<String> keys = Arrays.asList(pathSplitPattern.split(path));

        ParsedElement resolved = element;

        for (int i = 0; i < keys.size(); i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys.get(i))) {
                    resolved = resolved.asObject().get(keys.get(i));
                } else {
                    resolved = null;
                    break;
                }
            } else if (resolved.isArray()) {
                if (keys.get(i).startsWith("[") && keys.get(i).endsWith("]")) {
                    int index = Integer.parseInt(arrayPathFixPattern.matcher(keys.get(i)).replaceAll(".[$1]"));

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
     * @param element The element to update on
     * @param path    The path to update
     * @param value   The value to update to
     *
     * @return element for chaining
     *
     * @since 1.0.0
     */
    public static @NotNull ParsedElement update(@NotNull ParsedElement element, @NotNull String path, @Nullable Object value) {
        return update(element, path, value, true);
    }

    /**
     * Update a value on an element
     *
     * @param element The element to update on
     * @param path    The path to update
     * @param value   The value to update to
     * @param force   If the value should be force set (Create objects/arrays that don't exist)
     *
     * @return element for chaining
     *
     * @since 1.0.0
     */
    public static @NotNull ParsedElement update(@NotNull ParsedElement element, @NotNull String path, @Nullable Object value, boolean force) {
        path = arrayPathFixPattern.matcher(path).replaceAll(".[$1]");

        List<String> keys = Arrays.asList(pathSplitPattern.split(path));

        ParsedElement resolved = element;

        for (int i = 0; i < keys.size() - 1; i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys.get(i))) {
                    resolved = resolved.asObject().get(keys.get(i));
                } else {
                    if (force) {
                        if (keys.get(i + 1).startsWith("[") && keys.get(i + 1).endsWith("]")) {
                            resolved.asObject().set(keys.get(i), ParsedArray.create());
                        } else if (keys.size() > i + 1) {
                            resolved.asObject().set(keys.get(i), ParsedObject.create());
                        } else {
                            resolved.asObject().set(keys.get(i), ParsedPrimitive.fromNull());
                        }

                        resolved = resolved.asObject().get(keys.get(i));
                    } else {
                        resolved = null;
                        break;
                    }
                }
            } else if (resolved.isArray()) {
                if (keys.get(i).startsWith("[") && keys.get(i).endsWith("]")) {
                    int index = Integer.parseInt(arrayPathFixPattern.matcher(keys.get(i)).replaceAll(".[$1]"));

                    if (index >= 0 && index < resolved.asArray().getSize()) {
                        resolved = resolved.asArray().get(index);
                    } else {
                        if (force) {
                            while (resolved.asArray().getSize() <= index) {
                                if (keys.get(i + 1).startsWith("[") && keys.get(i + 1).endsWith("]")) {
                                    resolved.asArray().add(ParsedArray.create());
                                } else if (keys.size() > i + 1) {
                                    resolved.asArray().add(ParsedObject.create());
                                } else {
                                    resolved.asArray().add(ParsedPrimitive.fromNull());
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

        String valueKey = keys.get(keys.size() - 1);

        if (resolved != null) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(valueKey)) {
                    if (resolved.asObject().get(valueKey).isPrimitive()) {
                        resolved.asObject().set(valueKey, ParsedPrimitive.from(value));
                    }
                } else {
                    resolved.asObject().set(valueKey, ParsedPrimitive.from(value));
                }
            } else if (resolved.isArray() && (valueKey.startsWith("[") && valueKey.endsWith("]"))) {
                int resolvedValueKey = Integer.parseInt(arrayPathFixPattern.matcher(valueKey).replaceAll(".[$1]"));

                if (resolvedValueKey >= 0 && resolvedValueKey < resolved.asArray().getSize()) {
                    if (resolved.asArray().get(resolvedValueKey).isPrimitive()) {
                        resolved.asArray().set(resolvedValueKey, ParsedPrimitive.from(value));
                    }
                } else {
                    resolved.asArray().add(ParsedPrimitive.from(value));
                }
            }
        }

        return element;
    }

    /**
     * Update an element on an element
     *
     * @param element The element to update on
     * @param path    The path to update
     * @param value   The value to update to
     *
     * @return element for chaining
     *
     * @since 1.0.0
     */
    public static @NotNull ParsedElement updateElement(@NotNull ParsedElement element, @NotNull String path, @NotNull ParsedElement value) {
        return updateElement(element, path, value, true);
    }

    /**
     * Update an element on an element
     *
     * @param element The element to update on
     * @param path    The path to update
     * @param value   The value to update to
     * @param force   If the value should be force set (Create objects/arrays that don't exist)
     *
     * @return element for chaining
     *
     * @since 1.0.0
     */
    public static @NotNull ParsedElement updateElement(@NotNull ParsedElement element, @NotNull String path, @NotNull ParsedElement value, boolean force) {
        path = arrayPathFixPattern.matcher(path).replaceAll(".[$1]");

        List<String> keys = Arrays.asList(pathSplitPattern.split(path));

        System.out.println(keys);

        ParsedElement resolved = element;

        for (int i = 0; i < keys.size() - 1; i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys.get(i))) {
                    resolved = resolved.asObject().get(keys.get(i));
                } else {
                    if (force) {
                        if (keys.get(i + 1).startsWith("[") && keys.get(i + 1).endsWith("]")) {
                            resolved.asObject().set(keys.get(i), ParsedArray.create());
                        } else if (keys.size() > i + 1) {
                            resolved.asObject().set(keys.get(i), ParsedObject.create());
                        } else {
                            resolved.asObject().set(keys.get(i), ParsedPrimitive.fromNull());
                        }

                        resolved = resolved.asObject().get(keys.get(i));
                    } else {
                        resolved = null;
                        break;
                    }
                }
            } else if (resolved.isArray()) {
                if (keys.get(i).startsWith("[") && keys.get(i).endsWith("]")) {
                    int index = Integer.parseInt(arrayPathFixPattern.matcher(keys.get(i)).replaceAll(".[$1]"));

                    if (index >= 0 && index < resolved.asArray().getSize()) {
                        resolved = resolved.asArray().get(index);
                    } else {
                        if (force) {
                            while (resolved.asArray().getSize() <= index) {
                                if (keys.get(i + 1).startsWith("[") && keys.get(i + 1).endsWith("]")) {
                                    resolved.asArray().add(ParsedArray.create());
                                } else if (keys.size() > i + 1) {
                                    resolved.asArray().add(ParsedObject.create());
                                } else {
                                    resolved.asArray().add(ParsedPrimitive.fromNull());
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

        String valueKey = keys.get(keys.size() - 1);

        if (resolved != null) {
            if (resolved.isObject()) {
                resolved.asObject().set(valueKey, value);
            } else if (resolved.isArray() && (valueKey.startsWith("[") && valueKey.endsWith("]"))) {
                int resolvedValueKey = Integer.parseInt(arrayPathFixPattern.matcher(valueKey).replaceAll(".[$1]"));

                if (resolvedValueKey >= 0 && resolvedValueKey < resolved.asArray().getSize()) {
                    resolved.asArray().set(resolvedValueKey, value);
                } else {
                    resolved.asArray().add(value);
                }

            }
        }

        return element;
    }

    /**
     * Get all the keys in an element
     *
     * @param element The element to get the keys of
     *
     * @return A list of all the keys
     *
     * @since 1.0.0
     */
    public static @NotNull Set<String> getKeys(@NotNull ParsedElement element) {
        return getKeys(element, "", false);
    }

    /**
     * Get all the keys in an element
     *
     * @param element          The element to get the keys of
     * @param returnObjArrKeys Weather or not to return the keys for objects and arrays
     *
     * @return A list of all the keys
     *
     * @since 1.0.0
     */
    public static @NotNull Set<String> getKeys(@NotNull ParsedElement element, boolean returnObjArrKeys) {
        return getKeys(element, "", returnObjArrKeys);
    }

    /**
     * Get all the keys in an element
     *
     * @param element          The element to get the keys of
     * @param path             The current path
     * @param returnObjArrKeys Weather or not to return the keys for objects and arrays
     *
     * @return A list of all the keys
     *
     * @since 1.0.0
     */
    protected static @NotNull Set<String> getKeys(@NotNull ParsedElement element, @NotNull String path, boolean returnObjArrKeys) {
        Set<String> keys = new HashSet<>();

        if (element.isObject()) {
            ParsedObject object = element.asObject();

            for (Map.Entry<String, ParsedElement> entry : object.getEntries()) {
                if (entry.getValue().isObject()) {
                    if (returnObjArrKeys) {
                        keys.add(path + entry.getKey().replace(".", "\\.")); // FIXME \. -> \\. -> split
                    }
                    keys.addAll(getKeys(entry.getValue(), path + entry.getKey().replace(".", "\\.") + ".", returnObjArrKeys));
                } else if (entry.getValue().isArray()) {
                    if (returnObjArrKeys) {
                        keys.add(path + entry.getKey().replace(".", "\\."));
                    }
                    keys.addAll(getKeys(entry.getValue(), path + entry.getKey().replace(".", "\\."), returnObjArrKeys));
                } else if (entry.getValue().isPrimitive()) {
                    keys.add(path + entry.getKey().replace(".", "\\."));
                }
            }
        } else if (element.isArray()) {
            ParsedArray array = element.asArray();

            for (int i = 0; i < array.getSize(); i++) {
                if (array.get(i).isObject()) {
                    if (returnObjArrKeys) {
                        keys.add(path + "[" + i + "]");
                    }
                    keys.addAll(getKeys(array.get(i), path + "[" + i + "]" + ".", returnObjArrKeys));
                } else if (array.get(i).isArray()) {
                    if (returnObjArrKeys) {
                        keys.add(path + "[" + i + "]");
                    }
                    keys.addAll(getKeys(array.get(i), path + "[" + i + "]", returnObjArrKeys));
                } else if (array.get(i).isPrimitive()) {
                    keys.add(path + "[" + i + "]");
                }
            }
        }

        return keys;
    }
}