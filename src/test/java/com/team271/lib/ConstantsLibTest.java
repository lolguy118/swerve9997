package com.team271.lib;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConstantsLibTest {

    @Test
    void canRetryCount() {
        assertEquals(5, ConstantsLib.CAN_RETRY_COUNT);
    }

    @Test
    void canTimeoutMs() {
        assertEquals(10, ConstantsLib.CAN_TIMEOUT_MS);
    }

    @Test
    void canLongTimeoutMs() {
        assertEquals(100, ConstantsLib.CAN_LONG_TIMEOUT_MS);
    }

    @Test
    void ntUpdateMs() {
        assertEquals(100.0, ConstantsLib.NT_UPDATE_MS, 1e-9);
    }

    @Test
    void sInvalid() {
        assertEquals("Invalid", ConstantsLib.S_INVALID);
    }

    @Test
    void canIdPhIsMutable() {
        int original = ConstantsLib.CAN_ID_PH;
        ConstantsLib.CAN_ID_PH = 42;

        assertEquals(42, ConstantsLib.CAN_ID_PH);

        ConstantsLib.CAN_ID_PH = original;
    }
}
