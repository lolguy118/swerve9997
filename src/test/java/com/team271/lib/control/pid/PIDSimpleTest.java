package com.team271.lib.control.pid;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PIDSimpleTest {

    private PIDSimple pid;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() {
        pid = new PIDSimple(null, "Test", 1.0, 0.0, 0.0, 0.05);
    }

    /* --- Constructor --- */

    @Test
    void constructorSetsGains() {
        assertEquals(1.0, pid.getP());
        assertEquals(0.0, pid.getI());
        assertEquals(0.0, pid.getD());
    }

    @Test
    void constructorSetsTolerance() {
        PIDSimple custom = new PIDSimple(null, "Tol", 0.5, 0.1, 0.2, 0.1);
        assertEquals(0.5, custom.getP());
        assertEquals(0.1, custom.getI());
        assertEquals(0.2, custom.getD());
    }

    /* --- Proportional only --- */

    @Test
    void proportionalPositiveError() {
        // setpoint=10, measurement=0, error=10, output=kP*error=1.0*10=10 clamped to 1.0
        double output = pid.calc(0.0, 10.0, 0.0);
        assertEquals(1.0, output, 1e-9);
    }

    @Test
    void proportionalNegativeError() {
        // setpoint=-10, measurement=0, error=-10, output=-10 clamped to -1.0
        double output = pid.calc(0.0, -10.0, 0.0);
        assertEquals(-1.0, output, 1e-9);
    }

    @Test
    void proportionalZeroError() {
        double output = pid.calc(5.0, 5.0, 0.0);
        assertEquals(0.0, output, 1e-9);
    }

    @Test
    void proportionalSmallError() {
        // kP=1, error=0.5, output=0.5 (within [-1, 1])
        double output = pid.calc(0.0, 0.5, 0.0);
        assertEquals(0.5, output, 1e-9);
    }

    /* --- Output range clamping --- */

    @Test
    void outputClampedToDefaultRange() {
        // Default range is [-1.0, 1.0]
        // kP=1, error=100 -> output=100, clamped to 1.0
        double output = pid.calc(0.0, 100.0, 0.0);
        assertEquals(1.0, output, 1e-9);
    }

    @Test
    void outputClampedToCustomRange() {
        pid.setOutputRange(-0.5, 0.5);
        double output = pid.calc(0.0, 100.0, 0.0);
        assertEquals(0.5, output, 1e-9);
    }

    @Test
    void defaultOutputRangeIsNegOneToOne() {
        // Verify positive clamp
        assertEquals(1.0, pid.calc(0.0, 999.0, 0.0), 1e-9);
        pid = new PIDSimple(null, "Test2", 1.0, 0.0, 0.0, 0.05);
        // Verify negative clamp
        assertEquals(-1.0, pid.calc(0.0, -999.0, 0.0), 1e-9);
    }

    /* --- Integral --- */

    @Test
    void integralAccumulatesOverTime() {
        PIDSimple iPid = new PIDSimple(null, "I", 0.0, 1.0, 0.0, 0.05);

        // First call initializes timestamp, dt is clamped to 1E-6 so integral is ~0
        iPid.calc(0.0, 1.0, 0.0);

        // Second call: dt=0.1s, error=1.0, totalError ~= 0 + 1.0 * 0.1 = ~0.1
        double output2 = iPid.calc(0.0, 1.0, 0.1);

        // Third call: dt=0.1s, totalError ~= 0.1 + 1.0 * 0.1 = ~0.2
        double output3 = iPid.calc(0.0, 1.0, 0.2);

        // The integral should grow between calls
        assertTrue(output3 > output2, "Integral should accumulate over time");
        assertEquals(output3, output2 + 0.1, 1e-6);
    }

    @Test
    void integralIZoneResetsTotalError() {
        PIDSimple iPid = new PIDSimple(null, "IZ", 0.0, 1.0, 0.0, 0.05);
        iPid.setIZone(0.5);

        // error=1.0 > iZone=0.5, totalError should reset to 0
        iPid.calc(0.0, 1.0, 0.0);
        double output = iPid.calc(0.0, 1.0, 0.1);
        assertEquals(0.0, output, 1e-9);

        // error=0.3 < iZone=0.5, integral should accumulate
        output = iPid.calc(0.0, 0.3, 0.2);
        // totalError = 0 + 0.3 * 0.1 = 0.03
        assertEquals(0.03, output, 1e-6);
    }

    @Test
    void integralClampedByIMinIMax() {
        PIDSimple iPid = new PIDSimple(null, "IC", 0.0, 1.0, 0.0, 0.05);
        iPid.setIntegratorRange(-0.05, 0.05);

        // Accumulate for many cycles -- should clamp
        iPid.calc(0.0, 1.0, 0.0);
        for (int i = 1; i <= 100; i++) {
            iPid.calc(0.0, 1.0, i * 0.1);
        }
        // totalError is clamped to iMax/kI = 0.05/1.0 = 0.05
        // output = kI * 0.05 = 0.05
        double output = iPid.calc(0.0, 1.0, 10.1);
        assertEquals(0.05, output, 1e-6);
    }

    @Test
    void integralZeroKiSkipsAccumulation() {
        // kI=0 means the else-if branch in calc() is skipped
        PIDSimple pOnly = new PIDSimple(null, "POnly", 1.0, 0.0, 0.0, 0.05);
        pOnly.calc(0.0, 0.5, 0.0);
        pOnly.calc(0.0, 0.5, 0.1);
        double output = pOnly.calc(0.0, 0.5, 0.2);
        // Output should be pure P: kP * error = 1.0 * 0.5 = 0.5
        assertEquals(0.5, output, 1e-9);
    }

    /* --- Derivative --- */

    @Test
    void derivativeOutputProportionalToErrorChange() {
        PIDSimple dPid = new PIDSimple(null, "D", 0.0, 0.0, 1.0, 0.05);

        // First call: error=1.0, prevError=0, dt~=1e-6
        dPid.calc(0.0, 1.0, 0.0);

        // Second call: error=2.0, prevError=1.0, dt=0.1
        // velError = (2.0 - 1.0) / 0.1 = 10.0
        // output = kD * 10.0 = 10.0, clamped to 1.0
        double output = dPid.calc(0.0, 2.0, 0.1);
        assertEquals(1.0, output, 1e-9);
    }

    @Test
    void derivativeSmallChange() {
        PIDSimple dPid = new PIDSimple(null, "DS", 0.0, 0.0, 0.1, 0.05);
        dPid.setOutputRange(-10.0, 10.0);

        dPid.calc(0.0, 1.0, 0.0);

        // error changes from 1.0 to 1.5, dt=0.1
        // velError = (1.5 - 1.0) / 0.1 = 5.0
        // output = kD * velError = 0.1 * 5.0 = 0.5
        double output = dPid.calc(0.0, 1.5, 0.1);
        assertEquals(0.5, output, 1e-6);
    }

    /* --- Combined PID --- */

    @Test
    void pidCombinedOutput() {
        PIDSimple combo = new PIDSimple(null, "Combo", 0.5, 0.0, 0.0, 0.05);
        combo.setOutputRange(-10.0, 10.0);

        // P only: error=2.0, output = 0.5 * 2.0 = 1.0
        double output = combo.calc(0.0, 2.0, 0.0);
        assertEquals(1.0, output, 1e-9);
    }

    /* --- Continuous input --- */

    @Test
    void continuousInputWrapsError() {
        pid.enableContinuousInput(0.0, 360.0);
        assertTrue(pid.isContinuousInputEnabled());

        // Measurement=10, Setpoint=350
        // Without wrapping: error = 350-10 = 340
        // With wrapping: error bound = 180, error = inputModulus(340, -180, 180) = -20
        // output = kP * (-20) = -20, clamped to -1.0
        double output = pid.calc(10.0, 350.0, 0.0);
        assertTrue(output < 0.0, "Should wrap to negative direction");
    }

    @Test
    void continuousInputDisabledByDefault() {
        assertFalse(pid.isContinuousInputEnabled());

        // Without wrapping: error = 350-10 = 340, output = 340, clamped to 1.0
        double output = pid.calc(10.0, 350.0, 0.0);
        assertEquals(1.0, output, 1e-9);
    }

    @Test
    void disableContinuousInput() {
        pid.enableContinuousInput(0.0, 360.0);
        assertTrue(pid.isContinuousInputEnabled());

        pid.disableContinuousInput();
        assertFalse(pid.isContinuousInputEnabled());
    }

    /* --- atSetpoint --- */

    @Test
    void atSetpointTrueWhenWithinTolerance() {
        // tolerance = 0.05 (set in constructor)
        pid.calc(5.0, 5.02, 0.0);
        assertTrue(pid.atSetpoint());
    }

    @Test
    void atSetpointFalseBeforeFirstCall() {
        // lastInputMeasurement is NaN before calc is called
        assertFalse(pid.atSetpoint());
    }

    @Test
    void atSetpointFalseWhenOutsideTolerance() {
        pid.calc(0.0, 1.0, 0.0);
        assertFalse(pid.atSetpoint());
    }

    /* --- Reset --- */

    @Test
    void resetClearsAllState() {
        pid.calc(0.0, 5.0, 0.0);
        pid.calc(0.0, 5.0, 0.1);

        pid.reset();

        // After reset, atSetpoint should return false (lastInputMeasurement is NaN)
        assertFalse(pid.atSetpoint());

        // After reset, a fresh calc with zero error should give zero output
        double output = pid.calc(5.0, 5.0, 1.0);
        assertEquals(0.0, output, 1e-9);
    }

    /* --- Timestamp handling --- */

    @Test
    void firstCallInitializesTimestamp() {
        // First call should not throw and should produce valid output
        double output = pid.calc(0.0, 0.5, 100.0);
        assertEquals(0.5, output, 1e-9);
    }

    @Test
    void dtClampedToMinimum() {
        // Two calls with same timestamp -- dt would be 0, but clamped to 1E-6
        pid.calc(0.0, 1.0, 5.0);
        double output = pid.calc(0.0, 1.0, 5.0);
        // Should not throw or produce NaN/Inf
        assertFalse(Double.isNaN(output));
        assertFalse(Double.isInfinite(output));
    }

    /* --- PIDSimple-specific --- */

    @Test
    void calcStoresSetpoint() {
        pid.calc(0.0, 7.5, 0.0);
        assertEquals(7.5, pid.getSetpoint(), 1e-9);
    }

    @Test
    void setSetpointManually() {
        pid.setSetpoint(3.14);
        assertEquals(3.14, pid.getSetpoint(), 1e-9);
    }

    /* --- Setter methods --- */

    @Test
    void setPIDUpdatesGains() {
        pid.setPID(2.0, 3.0, 4.0);
        assertEquals(2.0, pid.getP());
        assertEquals(3.0, pid.getI());
        assertEquals(4.0, pid.getD());
    }

    @Test
    void setIndividualGains() {
        pid.setP(10.0);
        assertEquals(10.0, pid.getP());

        pid.setI(20.0);
        assertEquals(20.0, pid.getI());

        pid.setD(30.0);
        assertEquals(30.0, pid.getD());
    }

    @Test
    void setIZoneRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> pid.setIZone(-1.0));
    }

    @Test
    void setIZoneAcceptsZero() {
        assertDoesNotThrow(() -> pid.setIZone(0.0));
    }

    @Test
    void setIZoneAcceptsInfinity() {
        assertDoesNotThrow(() -> pid.setIZone(Double.POSITIVE_INFINITY));
    }

    /* --- PIDBase coverage --- */

    @Test
    void outputTelemetryCallsCheckTuning() {
        pid.calc(0.0, 1.0, 0.0);
        assertDoesNotThrow(pid::outputTelemetry);
    }

    @Test
    void calcWithContinuousInputEnabled() {
        pid.enableContinuousInput(0, 360);
        // measurement=350, setpoint=10, wrapped error should be small positive
        double output = pid.calc(350.0, 10.0, 0.0);
        assertTrue(output > 0.0, "Should wrap to positive direction via shortest path");
    }

    @Test
    void calcWithPDeadband() {
        pid.setPDeadband(0.5);
        // error = 0.3, which is < deadband 0.5 → P term should be 0
        double output = pid.calc(0.0, 0.3, 0.0);
        assertEquals(0.0, output, 1e-9);
    }

    @Test
    void calcWithIZone() {
        PIDSimple iPid = new PIDSimple(null, "IZ2", 0.0, 1.0, 0.0, 0.05);
        iPid.setIZone(1.0);

        // error = 2.0, which is > iZone = 1.0 → integral resets to 0
        iPid.calc(0.0, 2.0, 0.0);
        double output = iPid.calc(0.0, 2.0, 0.1);
        assertEquals(0.0, output, 1e-9, "Integral should not accumulate when error > iZone");
    }

    @Test
    void atSetpointWithVelTolerance() {
        pid.setTolerance(1.0, 100.0);
        // First call to initialize
        pid.calc(0.0, 0.5, 0.0);
        // Second call with same error → velError = 0
        pid.calc(0.0, 0.5, 0.1);
        assertTrue(pid.atSetpoint(), "Should be at setpoint with pos and vel within tolerance");
    }

    /* --- PIDBase: setPDeadband negative --- */

    @Test
    void setPDeadbandRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> pid.setPDeadband(-1.0));
    }

    /* --- PIDBase: setTolerance two-arg --- */

    @Test
    void setToleranceWithVelToleranceChecksBoth() {
        pid.setTolerance(0.1, 5.0);
        // error = 0.05 < posTolerance 0.1, velError will be ~0 < velTolerance 5.0
        pid.calc(0.0, 0.05, 0.0);
        pid.calc(0.0, 0.05, 0.1);
        assertTrue(pid.atSetpoint());
    }

    /* --- PIDBase: setIntegratorRange --- */

    @Test
    void setIntegratorRangeClampsOutput() {
        PIDSimple iPid = new PIDSimple(null, "IR", 0.0, 1.0, 0.0, 0.05);
        iPid.setIntegratorRange(-0.1, 0.1);
        // Accumulate integral over many cycles
        iPid.calc(0.0, 1.0, 0.0);
        for (int i = 1; i <= 50; i++) {
            iPid.calc(0.0, 1.0, i * 0.1);
        }
        double output = iPid.calc(0.0, 1.0, 5.1);
        assertTrue(output <= 0.1 + 1e-6, "Output should be clamped by integrator range");
    }

    /* --- checkTuning via NT --- */

    private void setNT(String tablePath, String key, double val) {
        NetworkTableInstance.getDefault()
                .getTable(tablePath)
                .getDoubleTopic(key)
                .publish()
                .set(val);
    }

    @Test
    void checkTuningUpdatesP() {
        PIDSimple tunePid = new PIDSimple(null, "TuneP", 1.0, 0.0, 0.0, 0.05);
        setNT("PID(PIDSimple)TuneP", "Tune P", 5.0);
        tunePid.outputTelemetry();
        assertEquals(5.0, tunePid.getP(), 1e-9);
    }

    @Test
    void checkTuningUpdatesI() {
        PIDSimple tunePid = new PIDSimple(null, "TuneI", 0.0, 1.0, 0.0, 0.05);
        setNT("PID(PIDSimple)TuneI", "Tune I", 3.0);
        tunePid.outputTelemetry();
        assertEquals(3.0, tunePid.getI(), 1e-9);
    }

    @Test
    void checkTuningUpdatesD() {
        PIDSimple tunePid = new PIDSimple(null, "TuneD", 0.0, 0.0, 1.0, 0.05);
        setNT("PID(PIDSimple)TuneD", "Tune D", 2.0);
        tunePid.outputTelemetry();
        assertEquals(2.0, tunePid.getD(), 1e-9);
    }

    @Test
    void checkTuningUpdatesTolerance() {
        PIDSimple tunePid = new PIDSimple(null, "TuneTol", 1.0, 0.0, 0.0, 0.05);
        setNT("PID(PIDSimple)TuneTol", "Tune Pos Tol", 0.5);
        tunePid.outputTelemetry();
        tunePid.calc(0.0, 0.3, 0.0);
        assertTrue(tunePid.atSetpoint());
    }

    @Test
    void checkTuningUpdatesPDeadband() {
        PIDSimple tunePid = new PIDSimple(null, "TuneDB", 1.0, 0.0, 0.0, 0.05);
        setNT("PID(PIDSimple)TuneDB", "Tune P Deadband", 1.0);
        tunePid.outputTelemetry();
        double output = tunePid.calc(0.0, 0.5, 0.0);
        assertEquals(0.0, output, 1e-9);
    }

    @Test
    void checkTuningUpdatesIZone() {
        PIDSimple tunePid = new PIDSimple(null, "TuneIZ", 0.0, 1.0, 0.0, 0.05);
        setNT("PID(PIDSimple)TuneIZ", "Tune I Zone", 0.5);
        tunePid.outputTelemetry();
        tunePid.calc(0.0, 2.0, 0.0);
        double output = tunePid.calc(0.0, 2.0, 0.1);
        assertEquals(0.0, output, 1e-9);
    }

    @Test
    void checkTuningUpdatesOutputRange() {
        PIDSimple tunePid = new PIDSimple(null, "TuneOR", 1.0, 0.0, 0.0, 0.05);
        setNT("PID(PIDSimple)TuneOR", "Tune Output Min", -0.5);
        setNT("PID(PIDSimple)TuneOR", "Tune Output Max", 0.5);
        tunePid.outputTelemetry();
        double output = tunePid.calc(0.0, 10.0, 0.0);
        assertEquals(0.5, output, 1e-9);
    }
}
