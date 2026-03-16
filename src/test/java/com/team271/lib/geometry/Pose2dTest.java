package com.team271.lib.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Pose2dTest {

    private static final double kEps = 1e-9;

    /* --- Constructors --- */

    @Test
    void defaultConstructorIsIdentity() {
        Pose2d p = new Pose2d();

        assertEquals(0.0, p.getTranslation().x(), kEps);
        assertEquals(0.0, p.getTranslation().y(), kEps);
        assertEquals(0.0, p.getRotation().getRadians(), kEps);
    }

    @Test
    void xyRotationConstructor() {
        Rotation2d rot = Rotation2d.fromDegrees(90.0);
        Pose2d p = new Pose2d(1.0, 2.0, rot);

        assertEquals(1.0, p.getTranslation().x(), kEps);
        assertEquals(2.0, p.getTranslation().y(), kEps);
        assertEquals(90.0, p.getRotation().getDegrees(), kEps);
    }

    @Test
    void translationRotationConstructor() {
        Translation2d t = new Translation2d(3.0, 4.0);
        Rotation2d r = Rotation2d.fromDegrees(45.0);
        Pose2d p = new Pose2d(t, r);

        assertEquals(3.0, p.getTranslation().x(), kEps);
        assertEquals(45.0, p.getRotation().getDegrees(), kEps);
    }

    @Test
    void copyConstructor() {
        Pose2d original = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(30.0));
        Pose2d copy = new Pose2d(original);

        assertEquals(original.getTranslation().x(), copy.getTranslation().x(), kEps);
        assertEquals(original.getTranslation().y(), copy.getTranslation().y(), kEps);
        assertEquals(original.getRotation().getDegrees(), copy.getRotation().getDegrees(), kEps);
    }

    /* --- fromTranslation / fromRotation --- */

    @Test
    void fromTranslation() {
        Translation2d t = new Translation2d(5.0, 6.0);
        Pose2d p = Pose2d.fromTranslation(t);

        assertEquals(5.0, p.getTranslation().x(), kEps);
        assertEquals(0.0, p.getRotation().getRadians(), kEps);
    }

    @Test
    void fromRotation() {
        Rotation2d r = Rotation2d.fromDegrees(90.0);
        Pose2d p = Pose2d.fromRotation(r);

        assertEquals(0.0, p.getTranslation().x(), kEps);
        assertEquals(90.0, p.getRotation().getDegrees(), kEps);
    }

    /* --- exp / log roundtrip --- */

    @Test
    void expLogRoundtripPureStraight() {
        Twist2d twist = new Twist2d(1.0, 0.0, 0.0);
        Pose2d pose = Pose2d.exp(twist);
        Twist2d recovered = Pose2d.log(pose);

        assertEquals(twist.dx, recovered.dx, kEps);
        assertEquals(twist.dy, recovered.dy, kEps);
        assertEquals(twist.dtheta, recovered.dtheta, kEps);
    }

    @Test
    void expLogRoundtripWithRotation() {
        Twist2d twist = new Twist2d(1.0, 0.0, Math.PI / 4.0);
        Pose2d pose = Pose2d.exp(twist);
        Twist2d recovered = Pose2d.log(pose);

        assertEquals(twist.dx, recovered.dx, kEps);
        assertEquals(twist.dy, recovered.dy, kEps);
        assertEquals(twist.dtheta, recovered.dtheta, kEps);
    }

    @Test
    void expLogRoundtripWithDy() {
        Twist2d twist = new Twist2d(1.0, 2.0, Math.PI / 3.0);
        Pose2d pose = Pose2d.exp(twist);
        Twist2d recovered = Pose2d.log(pose);

        assertEquals(twist.dx, recovered.dx, kEps);
        assertEquals(twist.dy, recovered.dy, kEps);
        assertEquals(twist.dtheta, recovered.dtheta, kEps);
    }

    /* --- transformBy --- */

    @Test
    void transformByTranslation() {
        Pose2d a = new Pose2d(1.0, 0.0, new Rotation2d());
        Pose2d b = new Pose2d(2.0, 0.0, new Rotation2d());
        Pose2d result = a.transformBy(b);

        assertEquals(3.0, result.getTranslation().x(), kEps);
        assertEquals(0.0, result.getTranslation().y(), kEps);
    }

    @Test
    void transformByWithRotation() {
        Pose2d a = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(90.0));
        Pose2d b = new Pose2d(1.0, 0.0, new Rotation2d());
        Pose2d result = a.transformBy(b);

        assertEquals(0.0, result.getTranslation().x(), kEps);
        assertEquals(1.0, result.getTranslation().y(), kEps);
    }

    /* --- inverse --- */

    @Test
    void inverseRoundtrip() {
        Pose2d p = new Pose2d(3.0, 4.0, Rotation2d.fromDegrees(45.0));
        Pose2d result = p.transformBy(p.inverse());

        assertEquals(0.0, result.getTranslation().x(), kEps);
        assertEquals(0.0, result.getTranslation().y(), kEps);
        assertEquals(0.0, result.getRotation().getRadians(), kEps);
    }

    /* --- normal --- */

    @Test
    void normalRotatesBy90() {
        Pose2d p = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(0.0));
        Pose2d n = p.normal();

        assertEquals(1.0, n.getTranslation().x(), kEps);
        assertEquals(2.0, n.getTranslation().y(), kEps);
        assertEquals(-90.0, n.getRotation().getDegrees(), kEps);
    }

    /* --- intersection --- */

    @Test
    void intersectionCrossingLines() {
        Pose2d a = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(45.0));
        Pose2d b = new Pose2d(10.0, 0.0, Rotation2d.fromDegrees(135.0));
        Translation2d pt = a.intersection(b);

        assertEquals(5.0, pt.x(), kEps);
        assertEquals(5.0, pt.y(), kEps);
    }

    @Test
    void intersectionParallelLinesReturnsInfinity() {
        Pose2d a = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(0.0, 5.0, Rotation2d.fromDegrees(0.0));
        Translation2d pt = a.intersection(b);

        assertEquals(Double.POSITIVE_INFINITY, pt.x());
        assertEquals(Double.POSITIVE_INFINITY, pt.y());
    }

    /* --- isColinear --- */

    @Test
    void isColinearSameLine() {
        Pose2d a = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(5.0, 0.0, Rotation2d.fromDegrees(0.0));

        assertTrue(a.isColinear(b));
    }

    @Test
    void isColinearDifferentLines() {
        Pose2d a = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(0.0, 5.0, Rotation2d.fromDegrees(0.0));

        assertFalse(a.isColinear(b));
    }

    /* --- epsilonEquals --- */

    @Test
    void epsilonEqualsTrue() {
        Pose2d a = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(1.0 + 1e-10, 2.0 - 1e-10, Rotation2d.fromDegrees(0.0));

        assertTrue(a.epsilonEquals(b, 1e-9));
    }

    @Test
    void epsilonEqualsFalse() {
        Pose2d a = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(2.0, 2.0, Rotation2d.fromDegrees(0.0));

        assertFalse(a.epsilonEquals(b, 1e-9));
    }

    /* --- interpolate --- */

    @Test
    void interpolateAtZero() {
        Pose2d a = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(10.0, 0.0, Rotation2d.fromDegrees(90.0));
        Pose2d result = a.interpolate(b, 0.0);

        assertEquals(0.0, result.getTranslation().x(), kEps);
        assertEquals(0.0, result.getRotation().getDegrees(), kEps);
    }

    @Test
    void interpolateAtOne() {
        Pose2d a = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(10.0, 0.0, Rotation2d.fromDegrees(90.0));
        Pose2d result = a.interpolate(b, 1.0);

        assertEquals(10.0, result.getTranslation().x(), kEps);
        assertEquals(90.0, result.getRotation().getDegrees(), kEps);
    }

    @Test
    void interpolateAtHalf() {
        Pose2d a = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(10.0, 0.0, Rotation2d.fromDegrees(0.0));
        Pose2d result = a.interpolate(b, 0.5);

        assertEquals(5.0, result.getTranslation().x(), kEps);
    }

    /* --- mirror --- */

    @Test
    void mirrorFlipsYAndRotation() {
        Pose2d p = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(30.0));
        Pose2d m = p.mirror();

        assertEquals(1.0, m.getTranslation().x(), kEps);
        assertEquals(-2.0, m.getTranslation().y(), kEps);
        assertEquals(-30.0, m.getRotation().getDegrees(), kEps);
    }

    /* --- distance --- */

    @Test
    void distanceFromSelfIsZero() {
        Pose2d p = new Pose2d(3.0, 4.0, Rotation2d.fromDegrees(45.0));

        assertEquals(0.0, p.distance(p), kEps);
    }

    @Test
    void distancePureTranslation() {
        Pose2d a = new Pose2d(0.0, 0.0, new Rotation2d());
        Pose2d b = new Pose2d(3.0, 4.0, new Rotation2d());

        assertEquals(5.0, a.distance(b), kEps);
    }

    /* --- equals / hashCode --- */

    @Test
    void equalsForSameValues() {
        Pose2d a = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(45.0));
        Pose2d b = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(45.0));

        assertEquals(a, b);
    }

    @Test
    void notEqualsForDifferentValues() {
        Pose2d a = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(0.0));
        Pose2d b = new Pose2d(3.0, 4.0, Rotation2d.fromDegrees(90.0));

        assertNotEquals(a, b);
    }

    @Test
    void notEqualsNonPose() {
        assertNotEquals(new Pose2d(), "not a pose");
    }

    @Test
    void hashCodeReturnsZero() {
        assertEquals(0, new Pose2d(1.0, 2.0, new Rotation2d()).hashCode());
    }

    /* --- identity --- */

    @Test
    void identityIsOrigin() {
        Pose2d id = Pose2d.identity();

        assertEquals(0.0, id.getTranslation().x(), kEps);
        assertEquals(0.0, id.getTranslation().y(), kEps);
        assertEquals(0.0, id.getRotation().getRadians(), kEps);
    }

    /* --- getPose --- */

    @Test
    void getPoseReturnsSelf() {
        Pose2d p = new Pose2d(1.0, 2.0, new Rotation2d());

        assertSame(p, p.getPose());
    }

    /* --- toString / toCSV --- */

    @Test
    void toStringContainsTAndR() {
        Pose2d p = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(45.0));
        String s = p.toString();

        assertTrue(s.contains("T:"));
        assertTrue(s.contains("R:"));
    }

    @Test
    void toCSVContainsCommas() {
        Pose2d p = new Pose2d(1.0, 2.0, Rotation2d.fromDegrees(45.0));
        String csv = p.toCSV();

        assertNotNull(csv);
        assertTrue(csv.contains(","));
    }
}
