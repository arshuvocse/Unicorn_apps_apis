package com.creatrix.unicorn.UtilityHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReportFilterHelperTest {

    @Test
    public void withCode() {
        assertEquals("C001 : Rumi Pharmacy", ReportFilterHelper.withCode("C001", "Rumi Pharmacy"));
        assertEquals("C001 : Rumi", ReportFilterHelper.withCode("C001", "C001 : Rumi")); // code already in the name
        assertEquals("Rumi", ReportFilterHelper.withCode(null, "Rumi"));
        assertEquals("Rumi", ReportFilterHelper.withCode("", "Rumi"));
        assertEquals("C001", ReportFilterHelper.withCode("C001", null));
        assertEquals("", ReportFilterHelper.withCode(null, null));
    }

    @Test
    public void matchesAnywhereIgnoringCase() {
        assertTrue(ReportFilterHelper.matches("C001716", "Rumi Pharmacy", "umi"));
        assertTrue(ReportFilterHelper.matches("C001716", "Rumi Pharmacy", "PHAR"));
        assertTrue(ReportFilterHelper.matches("C001716", "Rumi Pharmacy", "1716"));
        assertTrue(ReportFilterHelper.matches("C001716", "Rumi Pharmacy", " c0017 "));
        assertTrue(ReportFilterHelper.matches(null, null, ""));
        assertFalse(ReportFilterHelper.matches("C001716", "Rumi Pharmacy", "xyz"));
        assertFalse(ReportFilterHelper.matches(null, null, "a"));
    }
}
