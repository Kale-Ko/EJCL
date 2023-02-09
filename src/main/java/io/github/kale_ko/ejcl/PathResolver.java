package io.github.kale_ko.ejcl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.github.kale_ko.bjsl.elements.ParsedArray;
import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.elements.ParsedPrimitive;

public class PathResolver {
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
            if (resolved instanceof ParsedObject object) {
                if (object.get(valueKey).isPrimitive()) {
                    object.set(valueKey, ParsedPrimitive.from(value));
                }
            } else if (resolved instanceof ParsedArray array) {
                if (valueKey.startsWith("[") && valueKey.endsWith("]")) {
                    Integer resolvedValueKey = Integer.parseInt(valueKey.replaceAll("\\[([0-9])\\]", "$1"));

                    if (resolvedValueKey >= 0 && resolvedValueKey < resolved.asArray().getSize()) {
                        if (array.get(resolvedValueKey).isPrimitive()) {
                            array.set(resolvedValueKey, ParsedPrimitive.from(value));
                        }
                    }
                }
            }
        }
    }

    public static List<String> getKeys(ParsedElement element) {
        return getKeys(element, "");
    }

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