package com.creatrix.unicorn.UtilityHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SmartAddressResolverTest {

    @Test
    public void cityOnlyIsTooVagueToStopAtGeocoder() {
        assertEquals(1, SmartAddressResolver.detailLevel("Dhaka, Bangladesh"));
        assertEquals(1, SmartAddressResolver.detailLevel("Dhaka, Dhaka Division, Bangladesh"));
    }

    @Test
    public void roadAndAreaCountAsDetailed() {
        assertTrue(SmartAddressResolver.detailLevel("Road 5, Dhanmondi, Dhaka 1205, Bangladesh") >= 3);
        assertEquals(3, SmartAddressResolver.detailLevel("House 12, Zakir Hossain Rd, Khulshi, 4209, Bangladesh"));
    }

    @Test
    public void emptyHasNoDetail() {
        assertEquals(0, SmartAddressResolver.detailLevel(null));
        assertEquals(0, SmartAddressResolver.detailLevel(""));
    }
}
