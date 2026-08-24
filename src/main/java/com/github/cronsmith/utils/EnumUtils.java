package com.github.cronsmith.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Lookups over {@link EnumConstant} types.
 * 
 * @Description: EnumUtils
 * @Author: Fred Feng
 * @Date: 13/04/2025
 * @Version 1.0.0
 */
public abstract class EnumUtils {

    /**
     * All constants of the given type that belong to the given group.
     */
    public static <T extends EnumConstant> List<T> findAll(Class<T> enumType, String group) {
        List<T> matches = new ArrayList<T>();
        for (T constant : enumType.getEnumConstants()) {
            if (constant.getGroup().equals(group)) {
                matches.add(constant);
            }
        }
        return matches;
    }

    /**
     * The constant whose {@link EnumConstant#getValue()} equals the given value.
     * 
     * @throws IllegalArgumentException if no constant matches
     */
    public static <T extends EnumConstant> T valueOf(Class<T> enumType, Object value) {
        for (T constant : enumType.getEnumConstants()) {
            if (constant.getValue().equals(value)) {
                return constant;
            }
        }
        throw new IllegalArgumentException(
                "No enum constant by value '" + value + "' of " + enumType.getCanonicalName());
    }
}
