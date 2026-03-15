package com.team271.lib.hardware;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource") // CTRE CANBus is AutoCloseable but HAL manages lifecycle in sim
class CANDeviceIDTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    /* --- Constructors & Getters --- */

    @Test
    void constructorStoresDeviceNumberAndBus() {
        CANDeviceID id = new CANDeviceID(5, "drivetrain");

        assertEquals(5, id.getDeviceNumber());
        assertEquals("drivetrain", id.getBus());
    }

    @Test
    void defaultConstructorUsesEmptyBus() {
        CANDeviceID id = new CANDeviceID(3);

        assertEquals(3, id.getDeviceNumber());
        assertEquals("", id.getBus());
    }

    @Test
    void zeroDeviceNumber() {
        CANDeviceID id = new CANDeviceID(0, "rio");

        assertEquals(0, id.getDeviceNumber());
    }

    /* --- getCANBus (CTRE CANBus object) --- */

    @Test
    void getCANBusReturnsCachedInstance() {
        CANDeviceID id = new CANDeviceID(1, "subsystems");

        assertSame(
                id.getCANBus(),
                id.getCANBus(),
                "getCANBus() should return the same cached instance");
    }

    @Test
    void getCANBusHasCorrectName() {
        CANDeviceID id = new CANDeviceID(1, "drivetrain");

        assertEquals("drivetrain", id.getCANBus().getName());
    }

    @Test
    void getCANBusEmptyBusReturnsEmptyName() {
        CANDeviceID id = new CANDeviceID(1);

        assertEquals("", id.getCANBus().getName());
    }

    /* --- isSameBus --- */

    @Test
    void isSameBusMatchesSameName() {
        CANDeviceID a = new CANDeviceID(1, "drivetrain");
        CANDeviceID b = new CANDeviceID(2, "drivetrain");

        assertTrue(a.isSameBus(b));
    }

    @Test
    void isSameBusRejectsDifferentName() {
        CANDeviceID a = new CANDeviceID(1, "drivetrain");
        CANDeviceID b = new CANDeviceID(1, "subsystems");

        assertFalse(a.isSameBus(b));
    }

    @Test
    void isSameBusSymmetric() {
        CANDeviceID a = new CANDeviceID(1, "drivetrain");
        CANDeviceID b = new CANDeviceID(2, "drivetrain");

        assertTrue(a.isSameBus(b));
        assertTrue(b.isSameBus(a));
    }

    @Test
    void isSameBusBothEmptyBus() {
        CANDeviceID a = new CANDeviceID(1);
        CANDeviceID b = new CANDeviceID(2);

        assertTrue(a.isSameBus(b));
    }

    /* --- equals --- */

    @Test
    void equalsSelfReference() {
        CANDeviceID id = new CANDeviceID(1, "rio");

        assertEquals(id, id);
    }

    @Test
    void equalsSameDeviceSameBus() {
        CANDeviceID a = new CANDeviceID(5, "drivetrain");
        CANDeviceID b = new CANDeviceID(5, "drivetrain");

        assertEquals(a, b);
    }

    @Test
    void equalsSymmetric() {
        CANDeviceID a = new CANDeviceID(5, "drivetrain");
        CANDeviceID b = new CANDeviceID(5, "drivetrain");

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void notEqualDifferentDevice() {
        CANDeviceID a = new CANDeviceID(5, "drivetrain");
        CANDeviceID b = new CANDeviceID(6, "drivetrain");

        assertNotEquals(a, b);
    }

    @Test
    void notEqualDifferentBus() {
        CANDeviceID a = new CANDeviceID(5, "drivetrain");
        CANDeviceID b = new CANDeviceID(5, "subsystems");

        assertNotEquals(a, b);
    }

    @Test
    void sameDeviceNumberDifferentBusNotEqual() {
        CANDeviceID drive1 = new CANDeviceID(1, "drivetrain");
        CANDeviceID subs1 = new CANDeviceID(1, "subsystems");

        assertNotEquals(drive1, subs1);
        assertFalse(drive1.isSameBus(subs1));
    }

    @Test
    void equalsHandlesNull() {
        CANDeviceID id = new CANDeviceID(1, "rio");

        assertNotEquals(null, id);
    }

    @Test
    void equalsHandlesWrongType() {
        CANDeviceID id = new CANDeviceID(1, "rio");

        assertNotEquals("not a CANDeviceID", id);
    }

    @Test
    void equalsHandlesWrongTypeInteger() {
        CANDeviceID id = new CANDeviceID(1, "rio");

        assertNotEquals(Integer.valueOf(1), id);
    }

    /* --- hashCode --- */

    @Test
    void hashCodeConsistentWithEquals() {
        CANDeviceID a = new CANDeviceID(5, "drivetrain");
        CANDeviceID b = new CANDeviceID(5, "drivetrain");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeDiffersForDifferentDevice() {
        CANDeviceID a = new CANDeviceID(1, "drivetrain");
        CANDeviceID b = new CANDeviceID(2, "drivetrain");

        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeDiffersForDifferentBus() {
        CANDeviceID a = new CANDeviceID(1, "drivetrain");
        CANDeviceID b = new CANDeviceID(1, "subsystems");

        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeStableAcrossCalls() {
        CANDeviceID id = new CANDeviceID(5, "271");
        int h1 = id.hashCode();
        int h2 = id.hashCode();

        assertEquals(h1, h2);
    }

    /* --- toString --- */

    @Test
    void toStringContainsBusAndId() {
        CANDeviceID id = new CANDeviceID(5, "271");

        String s = id.toString();
        assertTrue(s.contains("271"), "toString should contain bus name");
        assertTrue(s.contains("5"), "toString should contain device number");
    }

    @Test
    void toStringEmptyBus() {
        CANDeviceID id = new CANDeviceID(3);

        String s = id.toString();
        assertTrue(s.contains("3"), "toString should contain device number");
    }

    /* --- HashMap key behavior --- */

    @Test
    void worksAsHashMapKey() {
        java.util.HashMap<CANDeviceID, String> map = new java.util.HashMap<>();

        CANDeviceID key1 = new CANDeviceID(1, "drivetrain");
        CANDeviceID key2 = new CANDeviceID(1, "subsystems");
        map.put(key1, "drive");
        map.put(key2, "arm");

        /* Look up with fresh instances (same values, different objects) */
        assertEquals("drive", map.get(new CANDeviceID(1, "drivetrain")));
        assertEquals("arm", map.get(new CANDeviceID(1, "subsystems")));
        assertNull(map.get(new CANDeviceID(1, "rio")));
    }
}
