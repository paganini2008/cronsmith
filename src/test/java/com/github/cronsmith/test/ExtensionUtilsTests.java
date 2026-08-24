package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import com.github.cronsmith.utils.CamelCasedLinkedHashMap;
import com.github.cronsmith.utils.EnumConstant;
import com.github.cronsmith.utils.EnumUtils;
import com.github.cronsmith.utils.ExceptionUtils;
import com.github.cronsmith.utils.MapUtils;

/**
 * 
 * Unit tests for the shared utility classes the extension relies on.
 * 
 * @Description: ExtensionUtilsTests
 * @Author: Fred Feng
 * @Date: 24/08/2026
 * @Version 1.0.0
 */
public class ExtensionUtilsTests {

    // ---- CamelCasedLinkedHashMap ---- //

    @Test
    public void testCamelKeyMatchesSnakeColumn() {
        CamelCasedLinkedHashMap map = new CamelCasedLinkedHashMap();
        map.put("task_group", "reports");
        assertEquals("reports", map.get("taskGroup"));
        assertEquals("reports", map.get("task_group"));
        assertTrue(map.containsKey("taskGroup"));
    }

    @Test
    public void testCamelPutIsNormalised() {
        CamelCasedLinkedHashMap map = new CamelCasedLinkedHashMap();
        map.put("taskName", "daily");
        assertEquals("daily", map.get("task_name"));
    }

    @Test
    public void testCamelPutAllNormalises() {
        Map<String, Object> source = new HashMap<>();
        source.put("taskClass", "C");
        CamelCasedLinkedHashMap map = new CamelCasedLinkedHashMap(source);
        assertEquals("C", map.get("task_class"));
    }

    @Test
    public void testCamelRemoveAndDefault() {
        CamelCasedLinkedHashMap map = new CamelCasedLinkedHashMap();
        map.put("max_retry_count", 3);
        assertEquals(3, map.getOrDefault("maxRetryCount", 0));
        assertEquals(0, map.getOrDefault("missing", 0));
        map.remove("maxRetryCount");
        assertFalse(map.containsKey("max_retry_count"));
    }

    // ---- ExceptionUtils ---- //

    @Test
    public void testUnwrapInvocationTarget() {
        IllegalStateException cause = new IllegalStateException("real");
        Throwable unwrapped =
                ExceptionUtils.getOriginalException(new InvocationTargetException(cause));
        assertSame(cause, unwrapped);
    }

    @Test
    public void testUnwrapExecutionException() {
        RuntimeException cause = new RuntimeException("real");
        assertSame(cause, ExceptionUtils.getOriginalException(new ExecutionException(cause)));
    }

    @Test
    public void testUnwrapNested() {
        IllegalArgumentException root = new IllegalArgumentException("root");
        Throwable wrapped = new ExecutionException(new InvocationTargetException(root));
        assertSame(root, ExceptionUtils.getOriginalException(wrapped));
    }

    @Test
    public void testUnwrapPlainReturnsItself() {
        RuntimeException plain = new RuntimeException("x");
        assertSame(plain, ExceptionUtils.getOriginalException(plain));
    }

    @Test
    public void testToStringAndArray() {
        Throwable e = new IllegalStateException("boom");
        assertTrue(ExceptionUtils.toString(e).contains("boom"));
        assertTrue(ExceptionUtils.toArray(e).length > 0);
        assertEquals(0, ExceptionUtils.toArray(null).length);
        assertEquals("", ExceptionUtils.toString(null));
    }

    @Test
    public void testIgnoreException() {
        RuntimeException e = new IllegalArgumentException("x");
        assertTrue(ExceptionUtils.ignoreException(e,
                new Class[] {IllegalArgumentException.class}));
        assertFalse(ExceptionUtils.ignoreException(e, new Class[] {NullPointerException.class}));
        assertFalse(ExceptionUtils.ignoreException(e, null));
    }

    // ---- MapUtils ---- //

    @Test
    public void testGetOrCreateConcurrent() {
        Map<String, String> map = new ConcurrentHashMap<>();
        assertEquals("v", MapUtils.getOrCreate(map, "k", () -> "v"));
        assertEquals("v", MapUtils.getOrCreate(map, "k", () -> "other"));
    }

    @Test
    public void testGetOrCreatePlainMap() {
        Map<String, String> map = new HashMap<>();
        assertEquals("v", MapUtils.getOrCreate(map, "k", () -> "v"));
        assertNull(MapUtils.getOrCreate(null, "k", () -> "v"));
        assertNull(MapUtils.getOrCreate(map, "k", null));
    }

    // ---- EnumUtils ---- //

    enum Color implements EnumConstant {
        RED, GREEN;

        @Override
        public Object getValue() {
            return name();
        }

        @Override
        public String getRepr() {
            return name();
        }
    }

    @Test
    public void testEnumValueOf() {
        assertEquals(Color.RED, EnumUtils.valueOf(Color.class, "RED"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumValueOfUnknown() {
        EnumUtils.valueOf(Color.class, "BLUE");
    }

    @Test
    public void testEnumFindAll() {
        List<Color> all = EnumUtils.findAll(Color.class, EnumConstant.DEFAULT_GROUP);
        assertEquals(2, all.size());
    }

}
