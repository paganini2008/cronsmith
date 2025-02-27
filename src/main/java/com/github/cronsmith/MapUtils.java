package com.github.cronsmith;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @Description: MapUtils
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public abstract class MapUtils {

    public static <K, V> Map<V, K> exchange(Map<K, V> map) {
        if (map == null) {
            throw new NullPointerException("Null map");
        }
        Map<V, K> results = new LinkedHashMap<V, K>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            results.put(entry.getValue(), entry.getKey());
        }
        return results;
    }

}
