package com.github.cronsmith.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * A map that stores database column labels but answers to camel-cased property names, so that a row
 * read as {@code task_name} can be looked up as {@code taskName}. Keys are normalised to lower
 * snake case on the way in and on every lookup, which also makes reads case insensitive.
 * 
 * @Description: CamelCasedLinkedHashMap
 * @Author: Fred Feng
 * @Date: 08/04/2025
 * @Version 1.0.0
 */
public class CamelCasedLinkedHashMap extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 7572318238502606513L;

    public CamelCasedLinkedHashMap() {
        super();
    }

    public CamelCasedLinkedHashMap(Map<String, Object> map) {
        super();
        if (map != null) {
            putAll(map);
        }
    }

    public CamelCasedLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public Object put(String key, Object value) {
        return super.put(key != null ? convertKey(key) : null, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        // LinkedHashMap.putAll does not route through put(), so normalisation has to be applied
        // here as well or entries copied from another map would keep their raw keys.
        if (map != null) {
            map.forEach(this::put);
        }
    }

    @Override
    public Object get(Object key) {
        return super.get(key != null ? convertKey(key.toString()) : null);
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return super.getOrDefault(key != null ? convertKey(key.toString()) : null, defaultValue);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key != null ? convertKey(key.toString()) : null);
    }

    @Override
    public Object remove(Object key) {
        return super.remove(key != null ? convertKey(key.toString()) : null);
    }

    /**
     * Turns either spelling of a name into the same lower snake case key: {@code taskName} and
     * {@code TASK_NAME} both become {@code task_name}.
     */
    private String convertKey(String key) {
        StringBuilder str = new StringBuilder(key);
        for (int i = 1; i < str.length(); i++) {
            if (isUnderscoreRequired(str.charAt(i - 1), str.charAt(i))) {
                str.insert(i++, '_');
            }
        }
        return str.toString().toLowerCase();
    }

    private boolean isUnderscoreRequired(char before, char current) {
        return Character.isLowerCase(before) && Character.isUpperCase(current);
    }

}
