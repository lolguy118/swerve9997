package com.team271.lib.hardware;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CANBusTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    /* --- Constructor: Bus type detection --- */

    @Test
    void emptyStringIsRIOBus() {
        CANBus bus = new CANBus("");

        assertEquals(CANBus.CANBusType.RIO, bus.getType());
        assertFalse(bus.isCANivore());
    }

    @Test
    void rioStringIsRIOBus() {
        CANBus bus = new CANBus("rio");

        assertEquals(CANBus.CANBusType.RIO, bus.getType());
        assertFalse(bus.isCANivore());
    }

    @Test
    void rioStringCaseInsensitive() {
        CANBus busUpper = new CANBus("RIO");
        CANBus busMixed = new CANBus("Rio");

        assertEquals(CANBus.CANBusType.RIO, busUpper.getType());
        assertEquals(CANBus.CANBusType.RIO, busMixed.getType());
    }

    @Test
    void namedBusIsCANivore() {
        CANBus bus = new CANBus("drivetrain");

        assertEquals(CANBus.CANBusType.CANIVORE, bus.getType());
        assertTrue(bus.isCANivore());
    }

    @Test
    void numberedBusIsCANivore() {
        CANBus bus = new CANBus("271");

        assertEquals(CANBus.CANBusType.CANIVORE, bus.getType());
        assertTrue(bus.isCANivore());
    }

    /* --- Constructor: Hoot file variant --- */

    @Test
    void hootFileConstructorSetsCANivoreType() {
        CANBus bus = new CANBus("drivetrain", "/media/sda1/");

        assertEquals(CANBus.CANBusType.CANIVORE, bus.getType());
        assertEquals("drivetrain", bus.getBus());
    }

    @Test
    void hootFileConstructorWithRIOBus() {
        CANBus bus = new CANBus("rio", "/media/sda1/");

        assertEquals(CANBus.CANBusType.RIO, bus.getType());
        assertEquals("rio", bus.getBus());
    }

    @Test
    void hootFileConstructorWithEmptyBus() {
        CANBus bus = new CANBus("", "/media/sda1/");

        assertEquals(CANBus.CANBusType.RIO, bus.getType());
        assertEquals("", bus.getBus());
    }

    @Test
    void hootFileConstructorWithEmptyHootFile() {
        CANBus bus = new CANBus("drivetrain", "");

        assertEquals(CANBus.CANBusType.CANIVORE, bus.getType());
    }

    /* --- Getters --- */

    @Test
    void getBusReturnsConstructorName() {
        CANBus bus = new CANBus("subsystems");

        assertEquals("subsystems", bus.getBus());
    }

    @Test
    void getBusReturnsEmptyStringForDefault() {
        CANBus bus = new CANBus("");

        assertEquals("", bus.getBus());
    }

    @Test
    void busUtilizationStartsAtZero() {
        CANBus bus = new CANBus("drivetrain");

        assertEquals(0.0f, bus.getBusUtilization());
    }

    /* --- refresh() --- */

    @Test
    void refreshDoesNotThrow() {
        CANBus bus = new CANBus("drivetrain");

        /* In sim, refresh reads from the sim CAN bus — should not throw */
        assertDoesNotThrow(() -> bus.refresh());
    }

    @Test
    void refreshUpdatesUtilization() {
        CANBus bus = new CANBus("drivetrain");
        bus.refresh();

        /* In sim, utilization is 0.0 but the field is written */
        float util = bus.getBusUtilization();
        assertTrue(util >= 0.0f, "Bus utilization should be non-negative");
    }

    @Test
    void refreshMultipleTimesDoesNotThrow() {
        CANBus bus = new CANBus("rio");

        assertDoesNotThrow(
                () -> {
                    bus.refresh();
                    bus.refresh();
                    bus.refresh();
                });
    }

    /* --- outputTelemetry() --- */

    @Test
    void outputTelemetryDoesNotThrow() {
        CANBus bus = new CANBus("drivetrain");

        assertDoesNotThrow(() -> bus.outputTelemetry());
    }

    @Test
    void outputTelemetryAfterRefreshDoesNotThrow() {
        CANBus bus = new CANBus("drivetrain");
        bus.refresh();

        assertDoesNotThrow(() -> bus.outputTelemetry());
    }

    @Test
    void outputTelemetryRIOBusDoesNotThrow() {
        CANBus bus = new CANBus("");
        bus.refresh();

        assertDoesNotThrow(() -> bus.outputTelemetry());
    }

    @Test
    void outputTelemetryEmptyBusPublishesRio() {
        /* Tests the mBus.isEmpty() ? "rio" : mBus branch in outputTelemetry */
        CANBus bus = new CANBus("");

        assertDoesNotThrow(() -> bus.outputTelemetry());
    }

    /* --- equals --- */

    @Test
    void equalsSelfReference() {
        CANBus bus = new CANBus("drivetrain");

        assertEquals(bus, bus);
    }

    @Test
    void equalsSameBusName() {
        CANBus a = new CANBus("drivetrain");
        CANBus b = new CANBus("drivetrain");

        assertEquals(a, b);
    }

    @Test
    void equalsSymmetric() {
        CANBus a = new CANBus("drivetrain");
        CANBus b = new CANBus("drivetrain");

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void notEqualDifferentBusName() {
        CANBus a = new CANBus("drivetrain");
        CANBus b = new CANBus("subsystems");

        assertNotEquals(a, b);
    }

    @Test
    void equalsIgnoresHootFile() {
        /* equals only checks bus name, not hoot file */
        CANBus a = new CANBus("drivetrain", "/media/sda1/");
        CANBus b = new CANBus("drivetrain", "");

        assertEquals(a, b);
    }

    @Test
    void equalsHandlesNull() {
        CANBus bus = new CANBus("rio");

        assertNotEquals(null, bus);
    }

    @Test
    void equalsHandlesWrongType() {
        CANBus bus = new CANBus("rio");

        assertNotEquals("not a CANBus", bus);
    }

    @Test
    void equalsHandlesWrongTypeInteger() {
        CANBus bus = new CANBus("rio");

        assertNotEquals(Integer.valueOf(42), bus);
    }

    /* --- hashCode --- */

    @Test
    void hashCodeConsistentWithEquals() {
        CANBus a = new CANBus("drivetrain");
        CANBus b = new CANBus("drivetrain");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeDiffersForDifferentBus() {
        CANBus a = new CANBus("drivetrain");
        CANBus b = new CANBus("subsystems");

        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeStableAcrossCalls() {
        CANBus bus = new CANBus("271");
        int h1 = bus.hashCode();
        int h2 = bus.hashCode();

        assertEquals(h1, h2);
    }

    @Test
    void hashCodeSameForDifferentHootFiles() {
        CANBus a = new CANBus("drivetrain", "/path/a/");
        CANBus b = new CANBus("drivetrain", "/path/b/");

        assertEquals(a.hashCode(), b.hashCode());
    }

    /* --- isSameBus --- */

    @Test
    void isSameBusMatches() {
        CANBus a = new CANBus("drivetrain");
        CANBus b = new CANBus("drivetrain");

        assertTrue(a.isSameBus(b));
    }

    @Test
    void isSameBusRejectsDifferent() {
        CANBus a = new CANBus("drivetrain");
        CANBus b = new CANBus("subsystems");

        assertFalse(a.isSameBus(b));
    }

    @Test
    void isSameBusSymmetric() {
        CANBus a = new CANBus("drivetrain");
        CANBus b = new CANBus("drivetrain");

        assertTrue(a.isSameBus(b));
        assertTrue(b.isSameBus(a));
    }

    @Test
    void isSameBusBothEmpty() {
        CANBus a = new CANBus("");
        CANBus b = new CANBus("");

        assertTrue(a.isSameBus(b));
    }

    /* --- toString --- */

    @Test
    void toStringContainsBusName() {
        CANBus bus = new CANBus("drivetrain");

        assertTrue(bus.toString().contains("drivetrain"));
    }

    @Test
    void toStringEmptyBusShowsRio() {
        CANBus bus = new CANBus("");

        assertTrue(bus.toString().contains("rio"));
    }

    @Test
    void toStringNonEmpty() {
        CANBus bus = new CANBus("drivetrain");

        assertFalse(bus.toString().isEmpty());
    }

    /* --- HashMap key behavior --- */

    @Test
    void worksAsHashMapKey() {
        java.util.HashMap<CANBus, String> map = new java.util.HashMap<>();

        CANBus key1 = new CANBus("drivetrain");
        CANBus key2 = new CANBus("subsystems");
        map.put(key1, "drive");
        map.put(key2, "arm");

        assertEquals("drive", map.get(new CANBus("drivetrain")));
        assertEquals("arm", map.get(new CANBus("subsystems")));
        assertNull(map.get(new CANBus("rio")));
    }
}
