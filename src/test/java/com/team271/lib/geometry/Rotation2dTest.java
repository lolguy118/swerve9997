package com.team271.lib.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Rotation2dTest {

    private static final double kEps = 1e-9;

    /* --- Constructors --- */

    @Test
    void defaultConstructorIsZeroRadians() {
        Rotation2d r = new Rotation2d();

        assertEquals(1.0, r.cos(), kEps);
        assertEquals(0.0, r.sin(), kEps);
        assertEquals(0.0, r.getRadians(), kEps);
    }

    @Test
    void radiansConstructorWithNormalize() {
        Rotation2d r = new Rotation2d(Math.PI / 2.0, true);

        assertEquals(Math.PI / 2.0, r.getRadians(), kEps);
    }

    @Test
    void xyNormalizedConstructor() {
        Rotation2d r = new Rotation2d(3.0, 4.0, true);

        assertEquals(3.0 / 5.0, r.cos(), kEps);
        assertEquals(4.0 / 5.0, r.sin(), kEps);
    }

    @Test
    void xyNormalizedZeroMagnitudeDefaultsToIdentity() {
        Rotation2d r = new Rotation2d(0.0, 0.0, true);

        assertEquals(1.0, r.cos(), kEps);
        assertEquals(0.0, r.sin(), kEps);
    }

    @Test
    void copyConstructor() {
        Rotation2d original = Rotation2d.fromDegrees(45.0);
        Rotation2d copy = new Rotation2d(original);

        assertEquals(original.getRadians(), copy.getRadians(), kEps);
        assertEquals(original.cos(), copy.cos(), kEps);
        assertEquals(original.sin(), copy.sin(), kEps);
    }

    @Test
    void constructorFromTranslation2d() {
        Translation2d dir = new Translation2d(1.0, 1.0);
        Rotation2d r = new Rotation2d(dir, true);

        assertEquals(45.0, r.getDegrees(), kEps);
    }

    /* --- fromRadians / fromDegrees --- */

    @Test
    void fromRadians() {
        Rotation2d r = Rotation2d.fromRadians(Math.PI);

        assertEquals(Math.PI, r.getRadians(), kEps);
    }

    @Test
    void fromDegrees() {
        Rotation2d r = Rotation2d.fromDegrees(180.0);

        assertEquals(Math.PI, r.getRadians(), kEps);
    }

    /* --- cos / sin / tan --- */

    @Test
    void cosAndSinAt45Degrees() {
        Rotation2d r = Rotation2d.fromDegrees(45.0);
        double expected = Math.sqrt(2.0) / 2.0;

        assertEquals(expected, r.cos(), kEps);
        assertEquals(expected, r.sin(), kEps);
    }

    @Test
    void tanNormal() {
        Rotation2d r = Rotation2d.fromDegrees(45.0);

        assertEquals(1.0, r.tan(), kEps);
    }

    @Test
    void tanAt90DegreesReturnsPositiveInfinity() {
        Rotation2d r = new Rotation2d(0.0, 1.0, false);

        assertEquals(Double.POSITIVE_INFINITY, r.tan());
    }

    @Test
    void tanAtNegative90DegreesReturnsNegativeInfinity() {
        Rotation2d r = new Rotation2d(0.0, -1.0, false);

        assertEquals(Double.NEGATIVE_INFINITY, r.tan());
    }

    /* --- getRadians / getDegrees --- */

    @Test
    void getRadiansFromTrigValues() {
        Rotation2d r = new Rotation2d(0.0, 1.0, false);

        assertEquals(Math.PI / 2.0, r.getRadians(), kEps);
    }

    @Test
    void getDegrees() {
        Rotation2d r = Rotation2d.fromRadians(Math.PI / 2.0);

        assertEquals(90.0, r.getDegrees(), kEps);
    }

    /* --- rotateBy --- */

    @Test
    void rotateByAddsAngles() {
        Rotation2d a = Rotation2d.fromDegrees(30.0);
        Rotation2d b = Rotation2d.fromDegrees(60.0);
        Rotation2d result = a.rotateBy(b);

        assertEquals(90.0, result.getDegrees(), kEps);
    }

    /* --- normal --- */

    @Test
    void normalIs90DegreesCounterClockwise() {
        Rotation2d r = Rotation2d.fromDegrees(0.0);
        Rotation2d n = r.normal();

        assertEquals(-90.0, n.getDegrees(), kEps);
    }

    /* --- inverse --- */

    @Test
    void inverseNegatesAngle() {
        Rotation2d r = Rotation2d.fromDegrees(30.0);
        Rotation2d inv = r.inverse();

        assertEquals(-30.0, inv.getDegrees(), kEps);
    }

    @Test
    void rotateByInverseReturnsIdentity() {
        Rotation2d r = Rotation2d.fromDegrees(42.0);
        Rotation2d result = r.rotateBy(r.inverse());

        assertEquals(0.0, result.getRadians(), kEps);
    }

    /* --- isParallel --- */

    @Test
    void isParallelSameAngle() {
        Rotation2d a = Rotation2d.fromDegrees(45.0);
        Rotation2d b = Rotation2d.fromDegrees(45.0);

        assertTrue(a.isParallel(b));
    }

    @Test
    void isParallelOppositeAngle() {
        Rotation2d a = Rotation2d.fromDegrees(0.0);
        Rotation2d b = Rotation2d.fromDegrees(180.0);

        assertTrue(a.isParallel(b));
    }

    @Test
    void isParallelDifferentAngles() {
        Rotation2d a = Rotation2d.fromDegrees(0.0);
        Rotation2d b = Rotation2d.fromDegrees(45.0);

        assertFalse(a.isParallel(b));
    }

    /* --- toTranslation --- */

    @Test
    void toTranslationUnitVector() {
        Rotation2d r = Rotation2d.fromDegrees(0.0);
        Translation2d t = r.toTranslation();

        assertEquals(1.0, t.x(), kEps);
        assertEquals(0.0, t.y(), kEps);
    }

    /* --- interpolate --- */

    @Test
    void interpolateAtZero() {
        Rotation2d a = Rotation2d.fromDegrees(0.0);
        Rotation2d b = Rotation2d.fromDegrees(90.0);
        Rotation2d result = a.interpolate(b, 0.0);

        assertEquals(0.0, result.getDegrees(), kEps);
    }

    @Test
    void interpolateAtOne() {
        Rotation2d a = Rotation2d.fromDegrees(0.0);
        Rotation2d b = Rotation2d.fromDegrees(90.0);
        Rotation2d result = a.interpolate(b, 1.0);

        assertEquals(90.0, result.getDegrees(), kEps);
    }

    @Test
    void interpolateAtHalf() {
        Rotation2d a = Rotation2d.fromDegrees(0.0);
        Rotation2d b = Rotation2d.fromDegrees(90.0);
        Rotation2d result = a.interpolate(b, 0.5);

        assertEquals(45.0, result.getDegrees(), kEps);
    }

    /* --- distance --- */

    @Test
    void distanceBetweenRotations() {
        Rotation2d a = Rotation2d.fromDegrees(0.0);
        Rotation2d b = Rotation2d.fromDegrees(90.0);

        assertEquals(Math.PI / 2.0, a.distance(b), kEps);
    }

    /* --- equals / hashCode --- */

    @Test
    void equalsForSameAngle() {
        Rotation2d a = Rotation2d.fromDegrees(45.0);
        Rotation2d b = Rotation2d.fromDegrees(45.0);

        assertEquals(a, b);
    }

    @Test
    void notEqualsForDifferentAngle() {
        Rotation2d a = Rotation2d.fromDegrees(0.0);
        Rotation2d b = Rotation2d.fromDegrees(90.0);

        assertNotEquals(a, b);
    }

    @Test
    void notEqualsNonRotation() {
        assertNotEquals(Rotation2d.fromDegrees(0.0), "not a rotation");
    }

    @Test
    void hashCodeReturnsZero() {
        assertEquals(0, Rotation2d.fromDegrees(45.0).hashCode());
    }

    /* --- identity --- */

    @Test
    void identityIsZeroRotation() {
        Rotation2d id = Rotation2d.identity();

        assertEquals(0.0, id.getRadians(), kEps);
    }

    /* --- WrapRadians behavior --- */

    @Test
    void wrapRadiansNormalizesLargeAngle() {
        Rotation2d r = Rotation2d.fromRadians(3.0 * Math.PI);

        assertEquals(Math.PI, Math.abs(r.getRadians()), kEps);
    }

    @Test
    void wrapRadiansNormalizesNegativeAngle() {
        Rotation2d r = Rotation2d.fromRadians(-3.0 * Math.PI);

        assertEquals(Math.PI, Math.abs(r.getRadians()), kEps);
    }

    /* --- toString --- */

    @Test
    void toStringContainsDeg() {
        assertNotNull(Rotation2d.fromDegrees(90.0).toString());
        assertTrue(Rotation2d.fromDegrees(90.0).toString().contains("deg"));
    }

    /* --- Lazy computation and edge cases --- */

    @Test
    void fromRadiansOnlyThenGetCos() {
        Rotation2d r = Rotation2d.fromRadians(Math.PI / 4.0);
        assertEquals(Math.sqrt(2.0) / 2.0, r.cos(), kEps);
    }

    @Test
    void fromRadiansOnlyThenGetSin() {
        Rotation2d r = Rotation2d.fromRadians(Math.PI / 4.0);
        assertEquals(Math.sqrt(2.0) / 2.0, r.sin(), kEps);
    }

    @Test
    void fromXYOnlyThenGetRadians() {
        Rotation2d r = new Rotation2d(1, 0, false);
        assertEquals(0.0, r.getRadians(), kEps);
    }

    @Test
    void rotateByRadiansOnly() {
        Rotation2d a = Rotation2d.fromRadians(Math.PI / 6.0);
        Rotation2d b = Rotation2d.fromRadians(Math.PI / 3.0);
        Rotation2d result = a.rotateBy(b);
        assertEquals(Math.PI / 2.0, result.getRadians(), kEps);
    }

    @Test
    void normalRadiansOnly() {
        Rotation2d r = Rotation2d.fromRadians(Math.PI / 2.0);
        Rotation2d n = r.normal();
        assertEquals(0.0, n.getDegrees(), kEps);
    }

    @Test
    void inverseRadiansOnly() {
        Rotation2d r = Rotation2d.fromRadians(Math.PI / 4.0);
        Rotation2d inv = r.inverse();
        assertEquals(-45.0, inv.getDegrees(), kEps);
    }

    @Test
    void equalsNonRotation2d() {
        assertNotEquals(Rotation2d.fromDegrees(0.0), new Object());
    }

    @Test
    void isParallelFalseCase() {
        Rotation2d a = Rotation2d.fromDegrees(10.0);
        Rotation2d b = Rotation2d.fromDegrees(55.0);
        assertFalse(a.isParallel(b));
    }

    @Test
    void normalizeSmallMagnitude() {
        Rotation2d r = new Rotation2d(0.0, 0.0, true);
        // Zero-magnitude defaults to identity (cos=1, sin=0)
        assertEquals(1.0, r.cos(), kEps);
        assertEquals(0.0, r.sin(), kEps);
    }

    /* --- Additional branch coverage --- */

    @Test
    void constructorWithoutNormalize() {
        Rotation2d r = new Rotation2d(3.0 * Math.PI, false);
        // Without normalize, radians should be stored as-is
        assertEquals(3.0 * Math.PI, r.getRadians(), kEps);
    }

    @Test
    void getRotationReturnsSelf() {
        Rotation2d r = Rotation2d.fromDegrees(45.0);
        assertSame(r, r.getRotation());
    }

    @Test
    void isParallelWithRadiansOnlyInstances() {
        // fromRadians sets radians_ but leaves trig as NaN, so hasRadians() is true
        Rotation2d a = Rotation2d.fromRadians(Math.PI);
        Rotation2d b = Rotation2d.fromRadians(Math.PI);
        assertTrue(a.isParallel(b));
    }

    @Test
    void isParallelNonParallelRadiansOnly() {
        Rotation2d a = Rotation2d.fromRadians(Math.PI);
        Rotation2d b = Rotation2d.fromRadians(Math.PI / 2);
        assertFalse(a.isParallel(b));
    }

    @Test
    void normalFromRadiansOnly() {
        // fromRadians creates an instance without trig, exercising the else branch in normal()
        Rotation2d r = Rotation2d.fromRadians(Math.PI / 4);
        Rotation2d n = r.normal();
        assertNotNull(n);
        // normal subtracts PI/2 from radians
        assertEquals(Math.PI / 4 - Math.PI / 2, n.getRadians(), kEps);
    }

    @Test
    void equalsWithSameObject() {
        Rotation2d r = Rotation2d.fromDegrees(90);
        assertTrue(r.equals(r));
    }

    /* --- isParallel branch coverage --- */

    @Test
    void isParallelTrigOnlySameAngle() {
        // (x, y, false) sets cos/sin but radians=NaN → hasTrig()=true, hasRadians()=false
        Rotation2d a = new Rotation2d(1.0, 0.0, false);
        Rotation2d b = new Rotation2d(1.0, 0.0, false);
        assertTrue(a.isParallel(b));
    }

    @Test
    void isParallelTrigOnlyDifferentAngle() {
        Rotation2d a = new Rotation2d(1.0, 0.0, false);
        Rotation2d b = new Rotation2d(0.0, 1.0, false);
        assertFalse(a.isParallel(b));
    }

    @Test
    void isParallelMixedRadiansAndTrig() {
        Rotation2d a = Rotation2d.fromRadians(0.0); // radians only
        Rotation2d b = new Rotation2d(1.0, 0.0, false); // trig only
        assertTrue(a.isParallel(b));
    }

    @Test
    void isParallelOppositeRadiansOnly() {
        Rotation2d a = Rotation2d.fromRadians(0.0);
        Rotation2d b = Rotation2d.fromRadians(Math.PI);
        assertTrue(a.isParallel(b));
    }

    @Test
    void xyConstructorWithoutNormalize() {
        Rotation2d r = new Rotation2d(3.0, 4.0, false);
        assertEquals(3.0, r.cos(), kEps);
        assertEquals(4.0, r.sin(), kEps);
    }

    @Test
    void interpolateAboveOne() {
        Rotation2d a = Rotation2d.fromDegrees(0.0);
        Rotation2d b = Rotation2d.fromDegrees(90.0);
        Rotation2d result = a.interpolate(b, 1.5);
        assertEquals(90.0, result.getDegrees(), kEps);
    }

    @Test
    void interpolateBelowZero() {
        Rotation2d a = Rotation2d.fromDegrees(45.0);
        Rotation2d b = Rotation2d.fromDegrees(90.0);
        Rotation2d result = a.interpolate(b, -0.5);
        assertEquals(45.0, result.getDegrees(), kEps);
    }

    @Test
    void toCSVReturnsString() {
        String csv = Rotation2d.fromDegrees(90).toCSV();
        assertNotNull(csv);
        assertFalse(csv.isEmpty());
    }
}
