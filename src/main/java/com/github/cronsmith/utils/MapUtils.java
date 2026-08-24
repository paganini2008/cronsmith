package com.github.cronsmith.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * 
 * @Description: MapUtils
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public class MapUtils {

    private MapUtils() {}

    public static <K, V> Map<V, K> exchange(Map<K, V> map) {
        if (map == null) {
            throw new NullPointerException("Map is null");
        }
        Map<V, K> results = new LinkedHashMap<V, K>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            results.put(entry.getValue(), entry.getKey());
        }
        return results;
    }

    /**
     * The value already mapped to the key, or the one the supplier produces, stored under that key
     * before it is returned. Concurrent maps go through computeIfAbsent so the supplier runs once;
     * plain maps are guarded by their own monitor, which callers must respect for other writes.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> V getOrCreate(Map<K, V> map, K key, Supplier<V> supplier) {
        if (map == null || supplier == null) {
            return null;
        }
        if (map instanceof ConcurrentMap) {
            return ((ConcurrentMap<K, V>) map).computeIfAbsent(key, k -> supplier.get());
        }
        synchronized (map) {
            return map.computeIfAbsent(key, k -> supplier.get());
        }
    }

}
