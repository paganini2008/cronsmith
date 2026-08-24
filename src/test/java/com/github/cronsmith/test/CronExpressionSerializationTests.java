package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.utils.SerializationException;
import com.github.cronsmith.utils.SerializationUtils;
/**
 *
 * A cron expression is serializable so it can be stored and picked up again later; these tests
 * cover the file, stream and byte-array round-trips as well as {@code copy()}, which is what
 * {@code consume(..)} relies on to walk the schedule without disturbing the original.
 *
 * @Description: CronExpressionSerializationTests
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class CronExpressionSerializationTests {

    private static CronBuilder builder() {
        return new CronBuilder().setStartTime(CronTestSupport.stableStartTime());
    }

    /**
     * Writes and reads back in one test: the original version relied on a static field filled in by
     * a sibling test, so it only worked when JUnit happened to run them in that order.
     */
    @Test
    public void testFileRoundTrip() throws IOException {
        CronExpression cronExpression = builder().everyMinute(2);
        String filePath = CRON.saveAsTmpFile(cronExpression);
        try {
            CronExpression restored = CRON.loadFromFile(filePath);
            assertEquals(cronExpression.toString(), restored.toString());
            assertEquals(cronExpression.getNextFiredDateTime(), restored.getNextFiredDateTime());
        } finally {
            new File(filePath).delete();
        }
    }

    @Test
    public void testExplicitFileRoundTrip() throws IOException {
        CronExpression cronExpression = builder().everyMonth().lastDay().at(12, 0);
        File file = File.createTempFile("cronsmith", ".bin");
        try {
            CRON.saveAsFile(cronExpression, file.getAbsolutePath());
            assertEquals(cronExpression.toString(),
                    CRON.loadFromFile(file.getAbsolutePath()).toString());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testStreamRoundTrip() throws IOException {
        CronExpression cronExpression = builder().everyHour(3);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CRON.saveAs(cronExpression, out);
        assertEquals(cronExpression.toString(),
                CRON.load(new ByteArrayInputStream(out.toByteArray())).toString());
    }

    @Test
    public void testByteArrayRoundTrip() {
        CronExpression cronExpression = builder().everyDay(2).at(6, 30);
        byte[] bytes = CRON.toByteArray(cronExpression);
        assertEquals(cronExpression.toString(), CRON.load(bytes).toString());
        assertEquals(cronExpression.toString(),
                CronExpression.deserialize(cronExpression.serialize()).toString());
    }

    @Test
    public void testCopyIsIndependentOfTheOriginal() {
        CronExpression cronExpression = builder().everyMinute(1);
        CronExpression copy = cronExpression.copy();
        assertNotSame(cronExpression, copy);
        assertEquals(cronExpression.toString(), copy.toString());

        LocalDateTime beforeWalking = cronExpression.getTime();
        copy.consume(ldt -> {
        }, 100);
        assertEquals(beforeWalking, cronExpression.getTime());
    }

    @Test
    public void testListBetweenTwoBounds() {
        CronExpression cronExpression = builder().everySecond(3);
        LocalDateTime from = CronTestSupport.stableStartTime();
        List<LocalDateTime> list = cronExpression.sync().list(from, from.plusMinutes(1));
        assertTrue("expected a few occurrences, got " + list.size(), list.size() > 1);
        for (LocalDateTime ldt : list) {
            assertTrue(ldt + " out of bounds", !ldt.isBefore(from) && !ldt.isAfter(from.plusMinutes(1)));
        }
    }

    /**
     * {@code consume(..)} replays the schedule from where the expression currently stands, while
     * {@code getNextFiredDateTime()} answers the first occurrence strictly after the start time -
     * so the two line up on the first value that is past the start time, not on the first value.
     */
    @Test
    public void testConsumeLinesUpWithGetNextFiredDateTime() {
        CronExpression cronExpression = builder().everyMinute(1);
        LocalDateTime startTime = CronTestSupport.stableStartTime();
        List<LocalDateTime> list = new ArrayList<>();
        cronExpression.sync().consume(list::add, 10);
        assertEquals(10, list.size());

        LocalDateTime firstAfterStart =
                list.stream().filter(ldt -> ldt.isAfter(startTime)).findFirst().orElse(null);
        assertEquals(firstAfterStart, cronExpression.getNextFiredDateTime());
    }

    @Test
    public void testConsumeDoesNotAdvanceTheOriginal() {
        CronExpression cronExpression = builder().everyMinute(3).sync();
        LocalDateTime before = cronExpression.getTime();
        cronExpression.consume(ldt -> {
        }, 10);
        assertEquals(before, cronExpression.getTime());
    }

    /** {@code getNextFiredDateTime()} is documented to consume the expression as it answers. */
    @Test
    public void testGetNextFiredDateTimeAdvancesTheExpression() {
        CronExpression cronExpression = builder().everyMinute(3);
        LocalDateTime first = cronExpression.getNextFiredDateTime();
        LocalDateTime second = cronExpression.getNextFiredDateTime();
        assertTrue(first + " -> " + second, second.isAfter(first));
    }

    // ------------------------------------------------------------------ //
    // Snapshot format                                                    //
    // ------------------------------------------------------------------ //

    /**
     * A snapshot is what goes into a BLOB column, so it has to say which format it is in. Without
     * that marker a snapshot written by an incompatible build reads back as a half-populated object
     * that only fails later, in whichever thread happens to iterate the schedule.
     */
    @Test
    public void testSnapshotCarriesItsFormatMarker() {
        byte[] snapshot = CRON.toByteArray(builder().everyMinute(5));
        assertEquals('C', snapshot[0]);
        assertEquals('R', snapshot[1]);
        assertEquals('S', snapshot[2]);
        assertEquals('M', snapshot[3]);
        assertEquals(CRON.SNAPSHOT_VERSION, (snapshot[4] << 8) | snapshot[5]);
    }

    @Test
    public void testSnapshotRoundTripKeepsPosition() {
        CronExpression cronExpression = builder().everyMonth().day(15).at(9, 30);
        cronExpression.getNextFiredDateTime();
        cronExpression.getNextFiredDateTime();

        CronExpression restored = CronExpression.deserialize(cronExpression.serialize());
        assertEquals("the schedule is restored", cronExpression.toString(), restored.toString());
        assertEquals("and so is where it stands", cronExpression.getTime(), restored.getTime());
        assertEquals(cronExpression.getNextFiredDateTime(), restored.getNextFiredDateTime());
    }

    @Test(expected = SerializationException.class)
    public void testBytesWithoutTheMarkerAreRejected() {
        // What a snapshot written before the format was versioned looks like: a bare object stream.
        CRON.load(SerializationUtils.serialize(builder().everyMinute(5)));
    }

    @Test(expected = SerializationException.class)
    public void testAnUnknownFormatVersionIsRejected() {
        byte[] snapshot = CRON.toByteArray(builder().everyMinute(5));
        snapshot[5] = (byte) (CRON.SNAPSHOT_VERSION + 1);
        CRON.load(snapshot);
    }

    @Test(expected = SerializationException.class)
    public void testTruncatedSnapshotIsRejected() {
        CRON.load(new byte[] {'C', 'R'});
    }

    @Test(expected = SerializationException.class)
    public void testNullSnapshotIsRejected() {
        CRON.load((byte[]) null);
    }

    @Test
    public void testCopyStaysIndependentOfTheSnapshotFormat() {
        // copy() clones inside the process and never leaves it, so it needs no format marker.
        CronExpression cronExpression = builder().everyMinute(5);
        assertEquals(cronExpression.toString(), cronExpression.copy().toString());
    }

    @Test
    public void testSerializationUtilsCopy() {
        CronExpression cronExpression = builder().everyMinute(5);
        assertNotNull(SerializationUtils.copy(cronExpression));
        assertEquals(cronExpression.toString(), SerializationUtils.copy(cronExpression).toString());
    }

    @Test(expected = SerializationException.class)
    public void testNonSerializableObjectIsRejected() {
        SerializationUtils.serialize(new Object());
    }

    @Test(expected = NullPointerException.class)
    public void testNullOutputStreamIsRejected() {
        SerializationUtils.writeObject("a serializable string", null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInputStreamIsRejected() {
        SerializationUtils.readObject(null);
    }

    @Test(expected = SerializationException.class)
    public void testGarbageBytesAreRejected() {
        SerializationUtils.deserialize(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
    }

}
