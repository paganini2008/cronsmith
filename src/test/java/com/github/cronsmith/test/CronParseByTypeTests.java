package com.github.cronsmith.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronType;

/**
 * {@link CRON#parse(String, CronType)} parses in the family the caller already knows - declared, not
 * guessed - and the result then carries its own {@link CronType}. Also covers {@link CronType#of},
 * which maps a stored {@code parser} flag ("cron" / "ycron") to that family.
 */
public class CronParseByTypeTests {

    @Test
    public void parsesInTheDeclaredFamily() {
        CronExpression cron = CRON.parse("0 0 12 * * ?", CronType.CRON);
        assertEquals(CronType.CRON, cron.getCronType());
        assertEquals("0 0 12 * * ?", cron.toString());

        CronExpression ycron = CRON.parse("0 0 12 ? ? 100", CronType.YCRON);
        assertEquals(CronType.YCRON, ycron.getCronType());
        assertEquals("0 0 12 ? ? 100", ycron.toString());
    }

    @Test
    public void declaredTypeWinsForStringsBothGrammarsCouldRead() {
        // "0 0 12 3 5 ?" is a valid line in either family - month-based (May 3rd) or year-based
        // (Wednesday of ISO week 5). The declared type decides, with no ambiguity.
        assertEquals(CronType.CRON, CRON.parse("0 0 12 3 5 ?", CronType.CRON).getCronType());
        assertEquals(CronType.YCRON, CRON.parse("0 0 12 3 5 ?", CronType.YCRON).getCronType());
    }

    @Test
    public void cronTypeOfMapsTheParserFlag() {
        assertEquals(CronType.YCRON, CronType.of("ycron"));
        assertEquals(CronType.YCRON, CronType.of("  YCRON "));
        assertEquals(CronType.CRON, CronType.of("cron"));
        assertEquals(CronType.CRON, CronType.of(""));
        assertEquals(CronType.CRON, CronType.of(null));
    }
}
