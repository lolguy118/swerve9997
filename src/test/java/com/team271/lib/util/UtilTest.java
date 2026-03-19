package com.team271.lib.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class UtilTest {

    // ── limit(double, double) ──

    @Test
    void limitSymmetric_withinRange() {
        assertEquals(0.5, Util.limit(0.5, 1.0));
    }

    @Test
    void limitSymmetric_aboveMax() {
        assertEquals(1.0, Util.limit(1.5, 1.0));
    }

    @Test
    void limitSymmetric_belowMin() {
        assertEquals(-1.0, Util.limit(-1.5, 1.0));
    }

    @Test
    void limitSymmetric_zero() {
        assertEquals(0.0, Util.limit(0.0, 1.0));
    }

    // ── limit(double, double, double) ──

    @Test
    void limitRange_withinRange() {
        assertEquals(5.0, Util.limit(5.0, 2.0, 8.0));
    }

    @Test
    void limitRange_clampHigh() {
        assertEquals(8.0, Util.limit(10.0, 2.0, 8.0));
    }

    @Test
    void limitRange_clampLow() {
        assertEquals(2.0, Util.limit(0.0, 2.0, 8.0));
    }

    @Test
    void limitRange_atBoundary() {
        assertEquals(2.0, Util.limit(2.0, 2.0, 8.0));
        assertEquals(8.0, Util.limit(8.0, 2.0, 8.0));
    }

    // ── inRange ──

    @Test
    void inRangeSymmetric_inside() {
        assertTrue(Util.inRange(0.5, 1.0));
    }

    @Test
    void inRangeSymmetric_outside() {
        assertFalse(Util.inRange(1.5, 1.0));
    }

    @Test
    void inRangeSymmetric_atBoundary() {
        assertFalse(Util.inRange(1.0, 1.0));
        assertFalse(Util.inRange(-1.0, 1.0));
    }

    @Test
    void inRange_inside() {
        assertTrue(Util.inRange(5.0, 2.0, 8.0));
    }

    @Test
    void inRange_outsideHigh() {
        assertFalse(Util.inRange(9.0, 2.0, 8.0));
    }

    @Test
    void inRange_atBoundaryExclusive() {
        assertFalse(Util.inRange(2.0, 2.0, 8.0));
        assertFalse(Util.inRange(8.0, 2.0, 8.0));
    }

    // ── interpolate ──

    @Test
    void interpolate_midpoint() {
        assertEquals(5.0, Util.interpolate(0.0, 10.0, 0.5));
    }

    @Test
    void interpolate_atZero() {
        assertEquals(0.0, Util.interpolate(0.0, 10.0, 0.0));
    }

    @Test
    void interpolate_atOne() {
        assertEquals(10.0, Util.interpolate(0.0, 10.0, 1.0));
    }

    @Test
    void interpolate_clampsAboveOne() {
        assertEquals(10.0, Util.interpolate(0.0, 10.0, 1.5));
    }

    @Test
    void interpolate_clampsBelowZero() {
        assertEquals(0.0, Util.interpolate(0.0, 10.0, -0.5));
    }

    // ── joinStrings ──

    @Test
    void joinStrings_multipleElements() {
        assertEquals("a, b, c", Util.joinStrings(", ", Arrays.asList("a", "b", "c")));
    }

    @Test
    void joinStrings_singleElement() {
        assertEquals("a", Util.joinStrings(", ", Collections.singletonList("a")));
    }

    @Test
    void joinStrings_emptyList() {
        assertEquals("", Util.joinStrings(", ", Collections.emptyList()));
    }

    @Test
    void joinStrings_nonStringObjects() {
        assertEquals("1-2-3", Util.joinStrings("-", Arrays.asList(1, 2, 3)));
    }

    // ── epsilonEquals ──

    @Test
    void epsilonEquals_same() {
        assertTrue(Util.epsilonEquals(1.0, 1.0));
    }

    @Test
    void epsilonEquals_withinEpsilon() {
        assertTrue(Util.epsilonEquals(1.0, 1.0 + 1e-13));
    }

    @Test
    void epsilonEquals_outsideEpsilon() {
        assertFalse(Util.epsilonEquals(1.0, 1.1));
    }

    @Test
    void epsilonEquals_customEpsilon() {
        assertTrue(Util.epsilonEquals(1.0, 1.05, 0.1));
        assertFalse(Util.epsilonEquals(1.0, 1.2, 0.1));
    }

    @Test
    void epsilonEquals_intOverload() {
        assertTrue(Util.epsilonEquals(10, 11, 1));
        assertFalse(Util.epsilonEquals(10, 12, 1));
    }

    // ── allCloseTo ──

    @Test
    void allCloseTo_allClose() {
        assertTrue(Util.allCloseTo(Arrays.asList(1.0, 1.01, 0.99), 1.0, 0.05));
    }

    @Test
    void allCloseTo_oneFar() {
        assertFalse(Util.allCloseTo(Arrays.asList(1.0, 1.01, 2.0), 1.0, 0.05));
    }

    @Test
    void allCloseTo_emptyList() {
        assertTrue(Util.allCloseTo(Collections.emptyList(), 1.0, 0.05));
    }

    // ── handleDeadzone ──

    @Test
    void handleDeadzone_insideDeadzone() {
        assertEquals(0.0, Util.handleDeadzone(0.05, 0.1));
    }

    @Test
    void handleDeadzone_outsideDeadzone() {
        double result = Util.handleDeadzone(0.55, 0.1);
        assertTrue(result > 0.0);
        assertTrue(result <= 1.0);
    }

    @Test
    void handleDeadzone_negative() {
        double result = Util.handleDeadzone(-0.55, 0.1);
        assertTrue(result < 0.0);
    }

    @Test
    void handleDeadzone_fullPositive() {
        assertEquals(1.0, Util.handleDeadzone(1.0, 0.0), 1e-9);
    }

    @Test
    void handleDeadzone_deadbandAtOne() {
        assertEquals(0.0, Util.handleDeadzone(0.5, 1.0));
    }

    @Test
    void handleDeadzone_zero() {
        assertEquals(0.0, Util.handleDeadzone(0.0, 0.1));
    }

    // ── handleDeadzone_Radial ──

    @Test
    void handleDeadzoneRadial_insideDeadzone() {
        double[] out = new double[2];
        Util.handleDeadzone_Radial(out, 0.05, 0.05, 0.1, 0.0);
        assertEquals(0.0, out[0]);
        assertEquals(0.0, out[1]);
    }

    @Test
    void handleDeadzoneRadial_outsideDeadzone() {
        double[] out = new double[2];
        Util.handleDeadzone_Radial(out, 0.5, 0.5, 0.1, 0.0);
        assertTrue(out[0] > 0.0);
        assertTrue(out[1] > 0.0);
    }

    @Test
    void handleDeadzoneRadial_fullMagnitude() {
        double[] out = new double[2];
        Util.handleDeadzone_Radial(out, 1.0, 0.0, 0.0, 0.0);
        assertEquals(1.0, out[0], 1e-9);
        assertEquals(0.0, out[1], 1e-9);
    }

    // ── convertTrigger ──

    @Test
    void convertTrigger_minusOne() {
        assertEquals(0.0, Util.convertTrigger(-1.0), 1e-9);
    }

    @Test
    void convertTrigger_plusOne() {
        assertEquals(1.0, Util.convertTrigger(1.0), 1e-9);
    }

    @Test
    void convertTrigger_zero() {
        assertEquals(0.5, Util.convertTrigger(0.0), 1e-9);
    }

    // ── reMap ──

    @Test
    void reMap_identity() {
        assertEquals(0.5, Util.reMap(0.5, 0.0, 1.0, 0.0, 1.0), 1e-9);
    }

    @Test
    void reMap_scaleUp() {
        assertEquals(50.0, Util.reMap(0.5, 0.0, 1.0, 0.0, 100.0), 1e-9);
    }

    @Test
    void reMap_invertedRange() {
        assertEquals(50.0, Util.reMap(0.5, 0.0, 1.0, 100.0, 0.0), 1e-9);
    }

    @Test
    void reMap_offsetInput() {
        assertEquals(150.0, Util.reMap(15.0, 10.0, 20.0, 100.0, 200.0), 1e-9);
    }

    // ── kEpsilon ──

    @Test
    void kEpsilon_isSmallPositive() {
        assertTrue(Util.kEpsilon > 0);
        assertTrue(Util.kEpsilon < 1e-6);
    }

    // ── getMACAddress ──

    @Test
    void getMACAddress_returnsString() {
        String mac = Util.getMACAddress();
        assertNotNull(mac);
    }

    // ── epsilonEquals edge cases ──

    @Test
    void epsilonEquals_firstConditionFalse() {
        assertFalse(Util.epsilonEquals(10.0, 5.0, 1.0));
    }

    @Test
    void epsilonEquals_secondConditionFalse() {
        assertFalse(Util.epsilonEquals(5.0, 10.0, 1.0));
    }

    @Test
    void epsilonEquals_intFirstConditionFalse() {
        assertFalse(Util.epsilonEquals(10, 5, 1));
    }

    @Test
    void epsilonEquals_intSecondConditionFalse() {
        assertFalse(Util.epsilonEquals(5, 10, 1));
    }

    // ── allCloseTo: short-circuit ──

    @Test
    void allCloseTo_firstFarRestClose() {
        assertFalse(Util.allCloseTo(Arrays.asList(100.0, 1.0, 1.0), 1.0, 0.05));
    }

    // ── inRange: outsideLow ──

    @Test
    void inRange_outsideLow() {
        assertFalse(Util.inRange(1.0, 2.0, 8.0));
    }

    // ── handleDeadzone: negative value past deadzone ──

    @Test
    void handleDeadzone_negativeFullValue() {
        double result = Util.handleDeadzone(-1.0, 0.0);
        assertEquals(-1.0, result, 1e-9);
    }

    // ── handleDeadzone: deadband above one ──

    @Test
    void handleDeadzone_deadbandAboveOne() {
        assertEquals(0.0, Util.handleDeadzone(0.5, 1.5));
    }

    // ── interpolate: negative clamps to a ──

    @Test
    void interpolate_negativeClampsToA() {
        assertEquals(2.0, Util.interpolate(2.0, 8.0, -1.0), 1e-9);
    }
}
