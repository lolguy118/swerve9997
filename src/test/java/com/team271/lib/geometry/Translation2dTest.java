package com.team271.lib.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Translation2dTest {

    private static final double kEps = 1e-9;

    /* --- Constructors --- */

    @Test
    void defaultConstructorIsZero() {
        Translation2d t = new Translation2d();

        assertEquals(0.0, t.x(), kEps);
        assertEquals(0.0, t.y(), kEps);
    }

    @Test
    void xyConstructor() {
        Translation2d t = new Translation2d(3.0, 4.0);

        assertEquals(3.0, t.x(), kEps);
        assertEquals(4.0, t.y(), kEps);
    }

    @Test
    void copyConstructor() {
        Translation2d original = new Translation2d(1.5, -2.5);
        Translation2d copy = new Translation2d(original);

        assertEquals(original.x(), copy.x(), kEps);
        assertEquals(original.y(), copy.y(), kEps);
    }

    @Test
    void startEndConstructorComputesDifference() {
        Translation2d start = new Translation2d(1.0, 2.0);
        Translation2d end = new Translation2d(4.0, 6.0);
        Translation2d diff = new Translation2d(start, end);

        assertEquals(3.0, diff.x(), kEps);
        assertEquals(4.0, diff.y(), kEps);
    }

    /* --- identity --- */

    @Test
    void identityIsZero() {
        Translation2d id = Translation2d.identity();

        assertEquals(0.0, id.x(), kEps);
        assertEquals(0.0, id.y(), kEps);
    }

    /* --- norm / norm2 --- */

    @Test
    void norm345Triangle() {
        Translation2d t = new Translation2d(3.0, 4.0);

        assertEquals(5.0, t.norm(), kEps);
    }

    @Test
    void norm2() {
        Translation2d t = new Translation2d(3.0, 4.0);

        assertEquals(25.0, t.norm2(), kEps);
    }

    /* --- translateBy --- */

    @Test
    void translateByAddsComponents() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(3.0, -1.0);
        Translation2d result = a.translateBy(b);

        assertEquals(4.0, result.x(), kEps);
        assertEquals(1.0, result.y(), kEps);
    }

    /* --- rotateBy --- */

    @Test
    void rotateBy90Degrees() {
        Translation2d t = new Translation2d(1.0, 0.0);
        Rotation2d rot = Rotation2d.fromDegrees(90.0);
        Translation2d result = t.rotateBy(rot);

        assertEquals(0.0, result.x(), kEps);
        assertEquals(1.0, result.y(), kEps);
    }

    /* --- direction --- */

    @Test
    void directionReturnsAngle() {
        Translation2d t = new Translation2d(1.0, 1.0);
        Rotation2d dir = t.direction();

        assertEquals(45.0, dir.getDegrees(), kEps);
    }

    /* --- inverse --- */

    @Test
    void inverseNegatesComponents() {
        Translation2d t = new Translation2d(3.0, -4.0);
        Translation2d inv = t.inverse();

        assertEquals(-3.0, inv.x(), kEps);
        assertEquals(4.0, inv.y(), kEps);
    }

    /* --- interpolate --- */

    @Test
    void interpolateAtZeroReturnsSelf() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(5.0, 6.0);
        Translation2d result = a.interpolate(b, 0.0);

        assertEquals(a.x(), result.x(), kEps);
        assertEquals(a.y(), result.y(), kEps);
    }

    @Test
    void interpolateAtOneReturnsOther() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(5.0, 6.0);
        Translation2d result = a.interpolate(b, 1.0);

        assertEquals(b.x(), result.x(), kEps);
        assertEquals(b.y(), result.y(), kEps);
    }

    @Test
    void interpolateAtHalf() {
        Translation2d a = new Translation2d(0.0, 0.0);
        Translation2d b = new Translation2d(10.0, 20.0);
        Translation2d result = a.interpolate(b, 0.5);

        assertEquals(5.0, result.x(), kEps);
        assertEquals(10.0, result.y(), kEps);
    }

    @Test
    void interpolateClampsBelowZero() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(5.0, 6.0);
        Translation2d result = a.interpolate(b, -0.5);

        assertEquals(a.x(), result.x(), kEps);
        assertEquals(a.y(), result.y(), kEps);
    }

    @Test
    void interpolateClampsAboveOne() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(5.0, 6.0);
        Translation2d result = a.interpolate(b, 1.5);

        assertEquals(b.x(), result.x(), kEps);
        assertEquals(b.y(), result.y(), kEps);
    }

    /* --- extrapolate --- */

    @Test
    void extrapolateBeyondOne() {
        Translation2d a = new Translation2d(0.0, 0.0);
        Translation2d b = new Translation2d(10.0, 0.0);
        Translation2d result = a.extrapolate(b, 2.0);

        assertEquals(20.0, result.x(), kEps);
        assertEquals(0.0, result.y(), kEps);
    }

    /* --- scale --- */

    @Test
    void scaleMultipliesBothComponents() {
        Translation2d t = new Translation2d(2.0, -3.0);
        Translation2d scaled = t.scale(4.0);

        assertEquals(8.0, scaled.x(), kEps);
        assertEquals(-12.0, scaled.y(), kEps);
    }

    /* --- epsilonEquals --- */

    @Test
    void epsilonEqualsTrue() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(1.0 + 1e-10, 2.0 - 1e-10);

        assertTrue(a.epsilonEquals(b, 1e-9));
    }

    @Test
    void epsilonEqualsFalse() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(1.1, 2.0);

        assertFalse(a.epsilonEquals(b, 1e-9));
    }

    /* --- dot --- */

    @Test
    void dotProduct() {
        Translation2d a = new Translation2d(3.0, 4.0);
        Translation2d b = new Translation2d(2.0, 1.0);

        assertEquals(10.0, Translation2d.dot(a, b), kEps);
    }

    /* --- getAngle --- */

    @Test
    void getAnglePerpendicularVectors() {
        Translation2d a = new Translation2d(1.0, 0.0);
        Translation2d b = new Translation2d(0.0, 1.0);
        Rotation2d angle = Translation2d.getAngle(a, b);

        assertEquals(90.0, angle.getDegrees(), kEps);
    }

    @Test
    void getAngleZeroVectorReturnsIdentity() {
        Translation2d a = new Translation2d(0.0, 0.0);
        Translation2d b = new Translation2d(1.0, 0.0);
        Rotation2d angle = Translation2d.getAngle(a, b);

        assertEquals(0.0, angle.getRadians(), kEps);
    }

    /* --- cross --- */

    @Test
    void crossProduct() {
        Translation2d a = new Translation2d(1.0, 0.0);
        Translation2d b = new Translation2d(0.0, 1.0);

        assertEquals(1.0, Translation2d.cross(a, b), kEps);
    }

    /* --- distance --- */

    @Test
    void distanceBetweenPoints() {
        Translation2d a = new Translation2d(0.0, 0.0);
        Translation2d b = new Translation2d(3.0, 4.0);

        assertEquals(5.0, a.distance(b), kEps);
    }

    /* --- equals / hashCode --- */

    @Test
    void equalsForSameValues() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(1.0, 2.0);

        assertEquals(a, b);
    }

    @Test
    void notEqualsForDifferentValues() {
        Translation2d a = new Translation2d(1.0, 2.0);
        Translation2d b = new Translation2d(3.0, 4.0);

        assertNotEquals(a, b);
    }

    @Test
    void notEqualsNonTranslation() {
        Translation2d a = new Translation2d(1.0, 2.0);

        assertNotEquals(a, "not a translation");
    }

    @Test
    void hashCodeReturnsZero() {
        assertEquals(0, new Translation2d(1.0, 2.0).hashCode());
    }

    /* --- toString / toCSV --- */

    @Test
    void toStringFormat() {
        Translation2d t = new Translation2d(1.0, 2.0);

        assertNotNull(t.toString());
        assertTrue(t.toString().contains("1.000"));
    }

    @Test
    void toCSVFormat() {
        Translation2d t = new Translation2d(1.0, 2.0);
        String csv = t.toCSV();

        assertTrue(csv.contains(","));
        assertTrue(csv.contains("1.000"));
        assertTrue(csv.contains("2.000"));
    }

    /* --- getTranslation --- */

    @Test
    void getTranslationReturnsSelf() {
        Translation2d t = new Translation2d(1.0, 2.0);

        assertSame(t, t.getTranslation());
    }
}
