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
    private static final @NotNull Pattern arrayPathFixPattern = Pattern.compile("\\[([0-9]*)]");
    private static final @NotNull Pattern pathSplitPattern = Pattern.compile("(?<!\\\\)(?:\\\\\\\\)*\\.");

    private PathResolver() {
    }

    /**
     * Safely parse array index from a string, returning -1 if invalid
     *
     * @param arrayKey The array key to parse (e.g., "[5]")
     * @return The parsed index, or -1 if invalid
     */
    private static int parseArrayIndex(@NotNull String arrayKey) {
        try {
            return Integer.parseInt(arrayPathFixPattern.matcher(arrayKey).replaceAll(".[$1]"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Determines what type of element should be created for the next key in the path
     *
     * @param keys The list of keys in the path
     * @param currentIndex The current index in the keys list
     * @return The appropriate ParsedElement to create
     */
    private static @NotNull ParsedElement createElementForNextKey(@NotNull List<String> keys, int currentIndex) {
        if (currentIndex + 1 < keys.size() && keys.get(currentIndex + 1).startsWith("[") && keys.get(currentIndex + 1).endsWith("]")) {
            return ParsedArray.create();
        } else if (keys.size() > currentIndex + 1) {
            return ParsedObject.create();
        } else {
            return ParsedPrimitive.fromNull();
        }
    }

    /**
     * Properly escape a key to be used in a path
     *
     * @param key The key to escape
     * @return The escaped key
     */
    private static @NotNull String escapePathKey(@NotNull String key) {
        // Replace literal dots with escaped dots to prevent confusion with path separators
        return key.replace("\\", "\\\\").replace(".", "\\.");
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
        if (path.trim().isEmpty()) {
            return element.isPrimitive() ? element.asPrimitive().get() : null;
        }
        
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
                    int index = parseArrayIndex(keys.get(i));
                    
                    if (index < 0) {
                        resolved = null;
                        break;
                    }

                    if (index < resolved.asArray().getSize()) {
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
        if (path.trim().isEmpty()) {
            return element;
        }
        
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
                    int index = parseArrayIndex(keys.get(i));
                    
                    if (index < 0) {
                        resolved = null;
                        break;
                    }

                    if (index < resolved.asArray().getSize()) {
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
        if (path.trim().isEmpty()) {
            // Cannot update root element with primitive value
            return element;
        }
        
        path = arrayPathFixPattern.matcher(path).replaceAll(".[$1]");

        List<String> keys = Arrays.asList(pathSplitPattern.split(path));

        ParsedElement resolved = element;

        for (int i = 0; i < keys.size() - 1; i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys.get(i))) {
                    resolved = resolved.asObject().get(keys.get(i));
                } else {
                    if (force) {
                        ParsedElement elementToCreate = createElementForNextKey(keys, i);
                        resolved.asObject().set(keys.get(i), elementToCreate);

                        resolved = resolved.asObject().get(keys.get(i));
                    } else {
                        resolved = null;
                        break;
                    }
                }
            } else if (resolved.isArray()) {
                if (keys.get(i).startsWith("[") && keys.get(i).endsWith("]")) {
                    int index = parseArrayIndex(keys.get(i));
                    
                    if (index < 0) {
                        resolved = null;
                        break;
                    }

                    if (index < resolved.asArray().getSize()) {
                        resolved = resolved.asArray().get(index);
                    } else {
                        if (force) {
                            // Safety check to prevent excessive memory usage
                            if (index > 10000) {
                                resolved = null;
                                break;
                            }
                            
                            while (resolved.asArray().getSize() <= index) {
                                ParsedElement elementToAdd = createElementForNextKey(keys, i);
                                    resolved.asArray().add(elementToAdd);
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
                int resolvedValueKey = parseArrayIndex(valueKey);
                
                if (resolvedValueKey < 0) {
                    // Invalid array index, skip
                    return element;
                }

                if (resolvedValueKey < resolved.asArray().getSize()) {
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
        if (path.trim().isEmpty()) {
            // Cannot replace root element entirely
            return element;
        }
        
        path = arrayPathFixPattern.matcher(path).replaceAll(".[$1]");

        List<String> keys = Arrays.asList(pathSplitPattern.split(path));

        ParsedElement resolved = element;

        for (int i = 0; i < keys.size() - 1; i++) {
            if (resolved.isObject()) {
                if (resolved.asObject().has(keys.get(i))) {
                    resolved = resolved.asObject().get(keys.get(i));
                } else {
                    if (force) {
                        ParsedElement elementToCreate = createElementForNextKey(keys, i);
                        resolved.asObject().set(keys.get(i), elementToCreate);

                        resolved = resolved.asObject().get(keys.get(i));
                    } else {
                        resolved = null;
                        break;
                    }
                }
            } else if (resolved.isArray()) {
                if (keys.get(i).startsWith("[") && keys.get(i).endsWith("]")) {
                    int index = parseArrayIndex(keys.get(i));
                    
                    if (index < 0) {
                        resolved = null;
                        break;
                    }

                    if (index < resolved.asArray().getSize()) {
                        resolved = resolved.asArray().get(index);
                    } else {
                        if (force) {
                            // Safety check to prevent excessive memory usage
                            if (index > 10000) {
                                resolved = null;
                                break;
                            }
                            
                            while (resolved.asArray().getSize() <= index) {
                                ParsedElement elementToAdd = createElementForNextKey(keys, i);
                                    resolved.asArray().add(elementToAdd);
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
                int resolvedValueKey = parseArrayIndex(valueKey);
                
                if (resolvedValueKey < 0) {
                    // Invalid array index, skip
                    return element;
                }

                if (resolvedValueKey < resolved.asArray().getSize()) {
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
     * @param returnObjArrKeys Whether or not to return the keys for objects and arrays
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
     * @param returnObjArrKeys Whether or not to return the keys for objects and arrays
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
                        keys.add(path + escapePathKey(entry.getKey()));
                    }
                    keys.addAll(getKeys(entry.getValue(), path + escapePathKey(entry.getKey()) + ".", returnObjArrKeys));
                } else if (entry.getValue().isArray()) {
                    if (returnObjArrKeys) {
                        keys.add(path + escapePathKey(entry.getKey()));
                    }
                    keys.addAll(getKeys(entry.getValue(), path + escapePathKey(entry.getKey()), returnObjArrKeys));
                } else if (entry.getValue().isPrimitive()) {
                    keys.add(path + escapePathKey(entry.getKey()));
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