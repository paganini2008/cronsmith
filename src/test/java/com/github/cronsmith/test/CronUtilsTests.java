package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.github.cronsmith.cron.TimeSlot;
import com.github.cronsmith.parser.CronParserException;
import com.github.cronsmith.parser.UnsupportedTagException;
import com.github.cronsmith.scheduler.CronTaskException;
import com.github.cronsmith.utils.AbbreviationUtils;
import com.github.cronsmith.utils.IteratorUtils;
import com.github.cronsmith.utils.MapUtils;
import com.github.cronsmith.utils.SerializationException;
/**
 *
 * Small helpers the rest of the library leans on: abbreviation lookup, map inversion, iterator
 * shortcuts, time-slot rounding and the exception types.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronUtilsTests {

    // ------------------------------------------------------------------ //
    // AbbreviationUtils                                                  //
    // ------------------------------------------------------------------ //

    @Test
    public void testDayOfWeekNamesRoundTrip() {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            String name = AbbreviationUtils.getDayOfWeekName(dayOfWeek.getValue());
            assertNotNull(dayOfWeek.name(), name);
            assertEquals(name, dayOfWeek.getValue(), AbbreviationUtils.getDayOfWeekValue(name));
        }
    }

    @Test
    public void testMonthNamesRoundTrip() {
        for (Month month : Month.values()) {
            String name = AbbreviationUtils.getMonthName(month.getValue());
            assertNotNull(month.name(), name);
            assertEquals(name, month.getValue(), AbbreviationUtils.getMonthValue(name));
        }
    }

    @Test
    public void testUnknownAbbreviations() {
        assertEquals(-1, AbbreviationUtils.getDayOfWeekValue("XXX"));
        assertEquals(-1, AbbreviationUtils.getMonthValue("XXX"));
        assertNull(AbbreviationUtils.getDayOfWeekName(0));
        assertNull(AbbreviationUtils.getDayOfWeekName(8));
        assertNull(AbbreviationUtils.getMonthName(0));
        assertNull(AbbreviationUtils.getMonthName(13));
    }

    // ------------------------------------------------------------------ //
    // MapUtils                                                           //
    // ------------------------------------------------------------------ //

    @Test
    public void testMapExchangeSwapsKeysAndValues() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        Map<Integer, String> exchanged = MapUtils.exchange(map);
        assertEquals(2, exchanged.size());
        assertEquals("one", exchanged.get(1));
        assertEquals("two", exchanged.get(2));
    }

    @Test(expected = NullPointerException.class)
    public void testMapExchangeRejectsNull() {
        MapUtils.exchange(null);
    }

    // ------------------------------------------------------------------ //
    // IteratorUtils                                                      //
    // ------------------------------------------------------------------ //

    @Test
    public void testForEachWrapsAnIterator() {
        List<String> source = Arrays.asList("a", "b", "c");
        StringBuilder sb = new StringBuilder();
        for (String s : IteratorUtils.forEach(source.iterator())) {
            sb.append(s);
        }
        assertEquals("abc", sb.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testForEachRejectsNull() {
        IteratorUtils.forEach(null);
    }

    @Test
    public void testGetFirst() {
        List<String> source = Arrays.asList("a", "b", "c");
        assertEquals("a", IteratorUtils.getFirst(source.iterator()));
        assertEquals("a", IteratorUtils.getFirst(source));
        assertNull(IteratorUtils.getFirst(java.util.Collections.<String>emptyList().iterator()));
        assertEquals("z", IteratorUtils.getFirst(java.util.Collections.<String>emptyList(), "z"));
        assertEquals("z", IteratorUtils.getFirst((Iterable<String>) null, "z"));
        assertNull(IteratorUtils.getFirst((Iterable<String>) null));
        assertEquals("z", IteratorUtils.getFirst((Iterator<String>) null, "z"));
    }

    @Test
    public void testGetLast() {
        List<String> source = Arrays.asList("a", "b", "c");
        assertEquals("c", IteratorUtils.getLast(source.iterator()));
        assertEquals("c", IteratorUtils.getLast(source));
        assertNull(IteratorUtils.getLast(java.util.Collections.<String>emptyList().iterator()));
        assertEquals("z", IteratorUtils.getLast(java.util.Collections.<String>emptyList(), "z"));
        assertEquals("z", IteratorUtils.getLast((Iterable<String>) null, "z"));
        assertNull(IteratorUtils.getLast((Iterable<String>) null));
        assertEquals("z", IteratorUtils.getLast((Iterator<String>) null, "z"));
    }

    @Test
    public void testGetByIndex() {
        List<String> source = Arrays.asList("a", "b", "c");
        assertEquals("a", IteratorUtils.get(source.iterator(), 0));
        assertEquals("b", IteratorUtils.get(source.iterator(), 1));
        assertEquals("c", IteratorUtils.get(source.iterator(), 2));
        assertNull(IteratorUtils.get(source.iterator(), 3));
        assertEquals("z", IteratorUtils.get(java.util.Collections.<String>emptyList().iterator(), 0, "z"));
        assertEquals("z", IteratorUtils.get(null, 0, "z"));
    }

    // ------------------------------------------------------------------ //
    // TimeSlot                                                           //
    // ------------------------------------------------------------------ //

    @Test
    public void testTimeSlotRoundsDownToTheSlotBoundary() {
        LocalDateTime ldt = LocalDateTime.of(2026, 7, 17, 13, 47, 53);
        assertEquals(LocalDateTime.of(2026, 7, 17, 12, 47, 53), TimeSlot.HOUR.adjust(ldt, 6));
        assertEquals(LocalDateTime.of(2026, 7, 17, 13, 45, 53), TimeSlot.MINUTE.adjust(ldt, 15));
        assertEquals(LocalDateTime.of(2026, 7, 17, 13, 47, 50), TimeSlot.SECOND.adjust(ldt, 10));
        assertEquals(7, TimeSlot.MONTH.adjust(ldt, 1).getMonthValue());
        assertEquals(17, TimeSlot.DAY.adjust(ldt, 1).getDayOfMonth());
    }

    /**
     * Month, day-of-month, day-of-year and day-of-week all count from 1, so rounding down must not
     * be allowed to produce a 0 - which used to make these throw for the first slot of the cycle.
     */
    @Test
    public void testTimeSlotHandlesTheFirstSlotOfOneBasedFields() {
        LocalDateTime firstOfJanuary = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        for (int span = 1; span <= 6; span++) {
            assertEquals(1, TimeSlot.MONTH.adjust(firstOfJanuary, span).getMonthValue());
            assertEquals(1, TimeSlot.DAY.adjust(firstOfJanuary, span).getDayOfMonth());
            assertEquals(1, TimeSlot.DAY_OF_YEAR.adjust(firstOfJanuary, span).getDayOfYear());
            assertNotNull(TimeSlot.DAY_OF_WEEK.adjust(firstOfJanuary, span));
        }
    }

    @Test
    public void testTimeSlotMonthKeepsAValidDayOfMonth() {
        // 31 March rounded back to a shorter month must not ask for a 31st that does not exist.
        LocalDateTime endOfMarch = LocalDateTime.of(2026, 3, 31, 8, 0, 0);
        LocalDateTime adjusted = TimeSlot.MONTH.adjust(endOfMarch, 2);
        assertEquals(3, adjusted.getMonthValue());
        assertTrue(adjusted.getDayOfMonth() <= LocalDate.of(2026, adjusted.getMonthValue(), 1)
                .lengthOfMonth());
    }

    @Test
    public void testTimeSlotRejectsANonPositiveSpan() {
        LocalDateTime ldt = LocalDateTime.of(2026, 7, 17, 13, 47, 53);
        for (TimeSlot slot : TimeSlot.values()) {
            try {
                slot.adjust(ldt, 0);
                org.junit.Assert.fail(slot + " accepted a span of 0");
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test
    public void testTimeSlotValueOf() {
        for (TimeSlot slot : TimeSlot.values()) {
            assertSame(slot, TimeSlot.valueOf(slot.name()));
        }
    }

    // ------------------------------------------------------------------ //
    // Exception types                                                    //
    // ------------------------------------------------------------------ //

    @Test
    public void testExceptionTypes() {
        CronParserException parserException = new CronParserException("bad");
        assertEquals("bad", parserException.getMessage());
        Throwable cause = new IllegalStateException("cause");
        assertSame(cause, new CronParserException("bad", cause).getCause());

        UnsupportedTagException tagException = new UnsupportedTagException("Q");
        assertEquals("Q", tagException.getMessage());
        assertTrue(tagException instanceof CronParserException);

        assertEquals("boom", new SerializationException("boom").getMessage());
        assertSame(cause, new SerializationException(cause).getCause());
        assertSame(cause, new SerializationException("boom", cause).getCause());

        assertEquals("task failed", new CronTaskException("task failed").getMessage());
        assertSame(cause, new CronTaskException("task failed", cause).getCause());
    }

}
