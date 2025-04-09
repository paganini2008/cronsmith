package com.github.cronsmith.scheduler;

import java.util.HashMap;

/**
 * 
 * @Description: CamelCasedHashMap
 * @Author: Fred Feng
 * @Date: 08/04/2025
 * @Version 1.0.0
 */
public class CamelCasedHashMap extends HashMap<String, Object> {

    private static final long serialVersionUID = -8565070892761829803L;

    public CamelCasedHashMap() {
        super();
    }

    public CamelCasedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public Object put(String key, Object value) {
        return super.put(key != null ? key.toLowerCase() : null, value);
    }

    @Override
    public Object get(Object key) {
        return super.get(key != null ? convertKey(key.toString()) : null);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key != null ? convertKey(key.toString()) : null);
    }

    private String convertKey(String key) {
        StringBuilder str = new StringBuilder(key);
        for (int i = 1; i < str.length(); i++) {
            if (isUnderscoreRequired(str.charAt(i - 1), str.charAt(i))) {
                str.insert(i++, '_');
            }
        }
        return str.toString().toLowerCase();
    }

    private boolean isUnderscoreRequired(final char before, final char current) {
        return Character.isLowerCase(before) && Character.isUpperCase(current);
    }

}
