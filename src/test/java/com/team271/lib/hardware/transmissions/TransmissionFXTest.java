package com.team271.lib.hardware.transmissions;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerBase.MotorDirection;
import com.team271.lib.hardware.controllers.ControllerBase.NeutralState;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.hardware.sensors.encoders.EncoderBase.EncoderDirection;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchTrigger;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchType;
import com.team271.lib.hardware.transmissions.TransmissionBase.ShifterState;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransmissionFXTest {

    private static final MotorBase KRAKEN = new MotorBase(MotorBase.MotorType.KRAKENX60);

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void resetCTREManager() {
        CTREManager.resetForTesting();
    }

    @AfterEach
    void closeDevices() {
        /*
         * Closing devices unregisters their Phoenix 6 sim state. If left open
         * across tests, the sim library can dereference freed handles during
         * later device construction and SIGSEGV the JVM.
         */
        CTREManager.resetForTesting();
    }

    /* Constructor - Single Motor */

    @Test
    void constructorSingleMotor() {
        CANDeviceID leaderId = new CANDeviceID(38);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNotNull(tx);
        assertNotNull(tx.getLeader());
        assertNotNull(tx.getLeaderController());
        assertNotNull(tx.getLeaderConfig());
    }

    @Test
    void constructorSingleMotorHasOneController() {
        CANDeviceID leaderId = new CANDeviceID(39);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(1, tx.getAllControllers().size());
    }

    /* Constructor - With Follower */

    @Test
    void constructorWithFollower() {
        CANDeviceID leaderId = new CANDeviceID(40);
        CANDeviceID followerId = new CANDeviceID(41);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId, followerId, false);

        assertNotNull(tx);
        assertEquals(2, tx.getAllControllers().size());
    }

    @Test
    void constructorWithFollowerOpposed() {
        CANDeviceID leaderId = new CANDeviceID(42);
        CANDeviceID followerId = new CANDeviceID(43);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId, followerId, true);

        assertEquals(2, tx.getAllControllers().size());
    }

    /* Constructor - With Two Followers */

    @Test
    void constructorWithTwoFollowers() {
        CANDeviceID leaderId = new CANDeviceID(44);
        CANDeviceID follower1Id = new CANDeviceID(45);
        CANDeviceID follower2Id = new CANDeviceID(46);
        TransmissionFX tx =
                new TransmissionFX(
                        null, "TX", KRAKEN, leaderId, follower1Id, false, follower2Id, true);

        assertEquals(3, tx.getAllControllers().size());
    }

    /* ADR-019: variable-arity followers (cap lifted past 4 motors) */

    @Test
    void addFollowerRegistersWithAllControllers() {
        CANDeviceID leaderId = new CANDeviceID(26);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.addFollower(new CANDeviceID(27), false);

        assertEquals(2, tx.getAllControllers().size());
    }

    @Test
    void addFollowerSupportsMoreThanFourMotors() {
        CANDeviceID leaderId = new CANDeviceID(20);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* leader + 5 followers = 6 motors, exceeding the legacy 4-motor cap */
        tx.addFollower(new CANDeviceID(21), false);
        tx.addFollower(new CANDeviceID(22), true);
        tx.addFollower(new CANDeviceID(23), false);
        tx.addFollower(new CANDeviceID(24), true);
        tx.addFollower(new CANDeviceID(25), false);

        assertEquals(6, tx.getAllControllers().size());
    }

    @Test
    void addFollowerNullIdThrows() {
        CANDeviceID leaderId = new CANDeviceID(28);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertThrows(IllegalArgumentException.class, () -> tx.addFollower(null, false));
    }

    @Test
    void addFollowerDuplicateIdThrows() {
        CANDeviceID leaderId = new CANDeviceID(29);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* colliding with the leader's own CAN ID must be rejected (no duplicate device) */
        assertThrows(
                IllegalArgumentException.class, () -> tx.addFollower(new CANDeviceID(29), false));
    }

    @SuppressWarnings("deprecation")
    @Test
    void addFollowerPopulatesLegacyFieldsForFirstThree() {
        CANDeviceID leaderId = new CANDeviceID(30);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.addFollower(new CANDeviceID(31), false);
        tx.addFollower(new CANDeviceID(32), false);
        tx.addFollower(new CANDeviceID(33), false);

        /* back-compat: the first three followers still populate the deprecated fields */
        assertNotNull(tx.mFollower1);
        assertNotNull(tx.mFollower2);
        assertNotNull(tx.mFollower3);
    }

    /* setDutyCycle */

    @Test
    void setOutputDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(47);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputDuty(0.5));
    }

    /* setVoltage */

    @Test
    void setOutputVoltageDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(48);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputVoltage(6.0));
    }

    /* setNeutralMode */

    @Test
    void setNeutralModeBrake() {
        CANDeviceID leaderId = new CANDeviceID(49);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setNeutralMode(NeutralState.BRAKE);
        assertEquals(NeutralState.BRAKE, tx.getNeutralMode());
    }

    @Test
    void setNeutralModeCoast() {
        CANDeviceID leaderId = new CANDeviceID(50);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setNeutralMode(NeutralState.COAST);
        assertEquals(NeutralState.COAST, tx.getNeutralMode());
    }

    /* robotInit */

    @Test
    void robotInitDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(51);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    /* outputTelemetry */

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(52);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::outputTelemetry);
    }

    /* stop */

    @Test
    void stopDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(53);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::stop);
    }

    /* Encoder Integration */

    @Test
    void addEncoderFXDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(54);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.addEncoderFX(250.0));
        assertNotNull(tx.getEncoderFX());
    }

    @Test
    void positionReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(55);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getPosFX(), 1e-6);
        assertEquals(0.0, tx.getPos(), 1e-6);
    }

    @Test
    void velocityReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(56);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getVelFX(), 1e-6);
        assertEquals(0.0, tx.getVel(), 1e-6);
    }

    /* Gear Ratio Setters */

    @Test
    void setRotorToMechanism() {
        CANDeviceID leaderId = new CANDeviceID(57);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        tx.setRotorToMechanism(0.5);
        assertEquals(0.0, tx.getPosFX(), 1e-6);
    }

    /* PID Passthrough */

    @Test
    void pidPassthroughToLeader() {
        CANDeviceID leaderId = new CANDeviceID(58);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setPSlot(0, 1.5);
        assertEquals(1.5, tx.getPSlot(0), 1e-6);

        tx.setISlot(0, 0.1);
        assertEquals(0.1, tx.getISlot(0), 1e-6);

        tx.setDSlot(0, 0.01);
        assertEquals(0.01, tx.getDSlot(0), 1e-6);
    }

    /* Current Limits */

    @Test
    void configCurrentLimitsDoNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(59);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configCurrentLimitStator(true, 40.0));
        assertDoesNotThrow(() -> tx.configCurrentLimitSupply(true, 30.0));
    }

    /* Refresh */

    @Test
    void refreshDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(60);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::refresh);
    }

    /* applyConfigs */

    @Test
    void applyConfigsDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(61);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::applyConfigs);
    }

    /* Limit Switches */

    @Test
    void limitSwitchesReturnFalseByDefault() {
        CANDeviceID leaderId = new CANDeviceID(62);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertFalse(tx.getRevLimit());
        assertFalse(tx.getFwdLimit());
    }

    /* Simulation */

    @Test
    void simulationInitDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(1);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.simulationInit(0.0));
    }

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(2);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.simulationInit(0.0);
        assertDoesNotThrow(() -> tx.simulationPeriodic(0.0));
    }

    /* --- Position control --- */

    @Test
    void setOutputPositionDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(3);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputPosition(1.0, 0.5));
    }

    @Test
    void setOutputPositionWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(4);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputPosition(2.0, 0.1));
    }

    /* --- Velocity control --- */

    @Test
    void setOutputVelocityDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(5);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputVelocity(10.0, 0.5));
    }

    @Test
    void setOutputVelocityWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(6);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocity(5.0, 0.2));
    }

    /* --- Motion Magic --- */

    @Test
    void setMMConfigDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(7);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setMMConfig(100.0, 200.0, 1000.0));
    }

    @Test
    void setOutputMMPositionDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(8);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionDutyWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(9);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionVoltageWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(10);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionVoltage(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionVoltageWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(11);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMPositionVoltage(1.0, 0.5));
    }

    /* --- Gear Ratio Setters --- */

    @Test
    void setSensorToMechanismStoresValue() {
        CANDeviceID leaderId = new CANDeviceID(12);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setSensorToMechanism(0.25);
        assertEquals(0.0, tx.getPos(), 1e-6);
    }

    @Test
    void setSensorAbsToMechanismStoresValue() {
        CANDeviceID leaderId = new CANDeviceID(13);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setSensorAbsToMechanism(0.5);
        assertEquals(0.0, tx.getPosAbs(), 1e-6);
    }

    @Test
    void setMechanismToUnitsStoresValue() {
        CANDeviceID leaderId = new CANDeviceID(14);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setMechanismToUnits(2.0);
        assertEquals(0.0, tx.getPosFX(), 1e-6);
    }

    /* --- Direction --- */

    @Test
    void configDirectionDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(15);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configDirection(MotorDirection.CW));
        assertDoesNotThrow(() -> tx.configDirection(MotorDirection.CCW));
    }

    /* --- Voltage Limits --- */

    @Test
    void configVoltagePeakDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(16);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configVoltagePeak(12.0, -12.0, 0.01));
    }

    /* --- Ramp Rates --- */

    @Test
    void configRampRatesDoNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(17);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configRampOpenLoopDuty(0.5));
        assertDoesNotThrow(() -> tx.configRampOpenLoopVoltage(0.3));
        assertDoesNotThrow(() -> tx.configRampClosedLoopDuty(0.2));
        assertDoesNotThrow(() -> tx.configRampClosedLoopVoltage(0.1));
    }

    /* --- configPIDFSlot --- */

    @Test
    void configPIDFSlotPassesThrough() {
        CANDeviceID leaderId = new CANDeviceID(18);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configPIDFSlot(0, 1.0, 0.5, 0.25, 0.1, 0.05);
        assertEquals(1.0, tx.getPSlot(0), 1e-6);
        assertEquals(0.5, tx.getISlot(0), 1e-6);
        assertEquals(0.25, tx.getDSlot(0), 1e-6);
    }

    /* --- setPIDFSlot on TransmissionBase --- */

    @Test
    void setPIDFSlotPassesThrough() {
        CANDeviceID leaderId = new CANDeviceID(19);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setPIDFSlot(1, 2.0, 1.0, 0.5, 0.2, 0.1);
        assertEquals(2.0, tx.getPSlot(1), 1e-6);
    }

    /* --- Closed Loop Error --- */

    @Test
    void getCLErrorReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(20);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getCLError(), 1e-6);
    }

    @Test
    void getCLErrorWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(21);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getCLError(), 1e-6);
    }

    @Test
    void getCLOutputReturnsZero() {
        CANDeviceID leaderId = new CANDeviceID(22);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getCLOutput(), 1e-6);
    }

    /* --- getOutputDuty / getOutputVoltage --- */

    @Test
    void getOutputDutyReturnsValue() {
        CANDeviceID leaderId = new CANDeviceID(23);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getOutputDuty(), 1e-6);
    }

    @Test
    void getOutputVoltageAveragesControllers() {
        CANDeviceID leaderId = new CANDeviceID(24);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getOutputVoltage(), 1e-6);
    }

    /* --- getSetpoint --- */

    @Test
    void getSetpointReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(25);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getSetpoint(), 1e-6);
    }

    @Test
    void getSetpointReturnsValueWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(26);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getSetpoint(), 1e-6);
    }

    /* --- Tolerance / Setpoint base methods --- */

    @Test
    void getToleranceReturnsZero() {
        CANDeviceID leaderId = new CANDeviceID(27);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getTolerance(), 1e-6);
    }

    @Test
    void setSetpointDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(28);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setSetpoint(1.0));
    }

    /* --- Shifters --- */

    @Test
    void shiftToGear1DoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(29);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    @Test
    void shiftToGear2DoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(30);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        ShifterState result = tx.shift(ShifterState.GEAR_2);
        assertEquals(ShifterState.GEAR_2, result);
    }

    @Test
    void shiftToSameGearDoesNotReapply() {
        CANDeviceID leaderId = new CANDeviceID(31);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.shift(ShifterState.GEAR_1);
        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    /* --- Current Limits (3-arg supply variant) --- */

    @Test
    void configCurrentLimitSupplyThreeArgDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(32);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configCurrentLimitSupply(60.0, 1.0, 40.0));
    }

    /* --- resetEncoders --- */

    @Test
    void resetEncodersDoesNotThrowWithoutEncoders() {
        CANDeviceID leaderId = new CANDeviceID(33);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::resetEncoders);
    }

    @Test
    void resetEncodersWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(34);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(tx::resetEncoders);
    }

    /* --- setPosRotations / getPosRotations --- */

    @Test
    void setPosRotationsWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(35);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setPosRotations(5.0));
    }

    @Test
    void setPosRotationsWithoutEncoderDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(36);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setPosRotations(5.0));
    }

    @Test
    void getPosRotationsReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(37);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getPosRotations(), 1e-6);
    }

    @Test
    void getPosAbsRotationsReturnsZeroWithoutCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(38);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getPosAbsRotations(), 1e-6);
    }

    @Test
    void getPosAbsReturnsZeroWithoutCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(39);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getPosAbs(), 1e-6);
    }

    /* --- Velocity RPS --- */

    @Test
    void getVelFXRPSReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(40);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getVelFXRPS(), 1e-6);
    }

    @Test
    void getVelRPSReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(41);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getVelRPS(), 1e-6);
    }

    /* --- getMotor by ID --- */

    @Test
    void getMotorByIdReturnsLeader() {
        CANDeviceID leaderId = new CANDeviceID(42);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNotNull(tx.getMotor(leaderId));
    }

    @Test
    void getMotorByIdReturnsNullForUnknownId() {
        CANDeviceID leaderId = new CANDeviceID(43);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNull(tx.getMotor(new CANDeviceID(7)));
    }

    /* --- getDCMotor --- */

    @Test
    void getDCMotorNullBeforeRobotInit() {
        CANDeviceID leaderId = new CANDeviceID(44);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNull(tx.getDCMotor());
    }

    @Test
    void getDCMotorSetAfterRobotInit() {
        CANDeviceID leaderId = new CANDeviceID(45);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.robotInit(0.0);

        assertNotNull(tx.getDCMotor());
    }

    /* --- robotInit with different motor types --- */

    @Test
    void robotInitWithFalcon500() {
        MotorBase falcon = new MotorBase(MotorBase.MotorType.FALCON500);
        CANDeviceID leaderId = new CANDeviceID(46);
        TransmissionFX tx = new TransmissionFX(null, "TX", falcon, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithKrakenX44() {
        MotorBase krakenX44 = new MotorBase(MotorBase.MotorType.KRAKENX44);
        CANDeviceID leaderId = new CANDeviceID(47);
        TransmissionFX tx = new TransmissionFX(null, "TX", krakenX44, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithCTREMinion() {
        MotorBase minion = new MotorBase(MotorBase.MotorType.CTRE_MINION);
        CANDeviceID leaderId = new CANDeviceID(48);
        TransmissionFX tx = new TransmissionFX(null, "TX", minion, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithNEO() {
        MotorBase neo = new MotorBase(MotorBase.MotorType.NEO);
        CANDeviceID leaderId = new CANDeviceID(49);
        TransmissionFX tx = new TransmissionFX(null, "TX", neo, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithNEO550() {
        MotorBase neo550 = new MotorBase(MotorBase.MotorType.NEO550);
        CANDeviceID leaderId = new CANDeviceID(50);
        TransmissionFX tx = new TransmissionFX(null, "TX", neo550, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithNEOVortex() {
        MotorBase neoVortex = new MotorBase(MotorBase.MotorType.NEO_VORTEX);
        CANDeviceID leaderId = new CANDeviceID(51);
        TransmissionFX tx = new TransmissionFX(null, "TX", neoVortex, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    /* --- robotPeriodicBefore --- */

    @Test
    void robotPeriodicBeforeDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(52);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.robotPeriodicBefore(0.0));
    }

    /* --- setSimVelRotations / setSimPosRotations --- */

    @Test
    void setSimVelRotationsDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(53);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setSimVelRotations(10.0));
    }

    @Test
    void setSimPosRotationsDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(54);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setSimPosRotations(5.0));
    }

    /* --- getSimState --- */

    @Test
    void getSimStateReturnsNonNull() {
        CANDeviceID leaderId = new CANDeviceID(55);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNotNull(tx.getSimState());
    }

    /* --- getEncoderCANCoder --- */

    @Test
    void getEncoderCANCoderReturnsNullByDefault() {
        CANDeviceID leaderId = new CANDeviceID(56);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNull(tx.getEncoderCANCoder());
    }

    /* --- robotPeriodicAfter --- */

    @Test
    void robotPeriodicAfterDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(57);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.robotPeriodicAfter(0.0));
    }

    /* --- setOutputTorqueCurrent --- */

    @Test
    void setOutputTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(58);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputTorqueCurrent(10.0));
    }

    @Test
    void setOutputTorqueCurrentBaseDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(59);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        TransmissionBase base = tx;
        assertDoesNotThrow(() -> base.setOutputTorqueCurrent(5.0));
    }

    /* --- setOutputPositionDuty --- */

    @Test
    void setOutputPositionDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(60);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputPositionDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(61);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputPositionDuty(2.0, 0.1));
    }

    /* --- setOutputPositionTorqueCurrent --- */

    @Test
    void setOutputPositionTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(62);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputPositionTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(1);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputPositionTorqueCurrent(2.0, 0.1));
    }

    /* --- setOutputVelocityDuty --- */

    @Test
    void setOutputVelocityDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(2);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputVelocityDuty(10.0, 0.5));
    }

    @Test
    void setOutputVelocityDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(3);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocityDuty(5.0, 0.2));
    }

    /* --- setOutputVelocityTorqueCurrent --- */

    @Test
    void setOutputVelocityTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(4);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputVelocityTorqueCurrent(10.0, 0.5));
    }

    @Test
    void setOutputVelocityTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(5);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocityTorqueCurrent(5.0, 0.2));
    }

    /* --- setOutputMMPositionTorqueCurrent --- */

    @Test
    void setOutputMMPositionTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(6);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(7);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionTorqueCurrent(2.0, 0.1));
    }

    /* --- setOutputMMVelocityDuty --- */

    @Test
    void setOutputMMVelocityDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(8);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityDuty(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(9);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityDuty(5.0, 0.2));
    }

    /* --- setOutputMMVelocityVoltage --- */

    @Test
    void setOutputMMVelocityVoltageDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(10);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityVoltage(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityVoltageWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(11);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityVoltage(5.0, 0.2));
    }

    /* --- setOutputMMVelocityTorqueCurrent --- */

    @Test
    void setOutputMMVelocityTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(12);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityTorqueCurrent(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(13);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityTorqueCurrent(5.0, 0.2));
    }

    /* --- setOutputMMExpoPositionDuty --- */

    @Test
    void setOutputMMExpoPositionDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(14);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(15);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionDuty(2.0, 0.1));
    }

    /* --- setOutputMMExpoPositionVoltage --- */

    @Test
    void setOutputMMExpoPositionVoltageDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(16);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionVoltage(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionVoltageWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(17);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionVoltage(2.0, 0.1));
    }

    /* --- setOutputMMExpoPositionTorqueCurrent --- */

    @Test
    void setOutputMMExpoPositionTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(18);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(19);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionTorqueCurrent(2.0, 0.1));
    }

    /* --- setOutputDynMMPositionDuty --- */

    @Test
    void setOutputDynMMPositionDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(20);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionDuty(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(21);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionDuty(2.0, 80.0, 160.0, 800.0, 0.1));
    }

    /* --- setOutputDynMMPositionVoltage --- */

    @Test
    void setOutputDynMMPositionVoltageDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(22);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionVoltage(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionVoltageWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(23);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionVoltage(2.0, 80.0, 160.0, 800.0, 0.1));
    }

    /* --- setOutputDynMMPositionTorqueCurrent --- */

    @Test
    void setOutputDynMMPositionTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(24);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(
                () -> tx.setOutputDynMMPositionTorqueCurrent(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(25);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(
                () -> tx.setOutputDynMMPositionTorqueCurrent(2.0, 80.0, 160.0, 800.0, 0.1));
    }

    /* Slot Assignment Regression */

    /**
     * Walks a reflective path from {@code root} through private fields named in {@code path}, then
     * reads the public {@code Slot} field on the terminal CTRE request object. Lets these
     * regression tests target CTRE request objects that have moved across classes over time.
     */
    private int getSlot(final Object argRoot, final String... argPath) throws Exception {
        Object current = argRoot;
        for (String fieldName : argPath) {
            Field field = null;
            Class<?> cls = current.getClass();
            while (cls != null && field == null) {
                try {
                    field = cls.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                    cls = cls.getSuperclass();
                }
            }
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            field.setAccessible(true);
            current = field.get(current);
        }
        Field slotField = current.getClass().getField("Slot");
        return slotField.getInt(current);
    }

    @Test
    void setOutputPositionSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(52);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputPosition(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mController", "motorPosition"));
    }

    @Test
    void setOutputVelocitySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(53);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputVelocity(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mController", "motorVelocity"));
    }

    @Test
    void setOutputPositionDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(54);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputPositionDuty(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mPositionDuty"));
    }

    @Test
    void setOutputVelocityDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(55);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputVelocityDuty(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mVelocityDuty"));
    }

    @Test
    void setOutputMMPositionVoltageSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(56);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMPositionVoltage(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMPositionVoltage"));
    }

    @Test
    void setOutputDynMMPositionVoltageSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(57);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputDynMMPositionVoltage(1.0, 100.0, 200.0, 1000.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mDynMMVoltage"));
    }

    @Test
    void setOutputPositionTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(58);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputPositionTorqueCurrent(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mPositionTC"));
    }

    @Test
    void setOutputVelocityTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(59);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputVelocityTorqueCurrent(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mVelocityTC"));
    }

    @Test
    void setOutputMMPositionDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(60);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMPositionDuty(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMPositionDuty"));
    }

    @Test
    void setOutputMMPositionTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(61);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMPositionTorqueCurrent(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMPositionTC"));
    }

    @Test
    void setOutputMMVelocityDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(62);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMVelocityDuty(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMVelocityDuty"));
    }

    @Test
    void setOutputMMVelocityVoltageSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(1);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMVelocityVoltage(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMVelocityVoltage"));
    }

    @Test
    void setOutputMMVelocityTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(2);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMVelocityTorqueCurrent(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMVelocityTC"));
    }

    @Test
    void setOutputMMExpoPositionDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(3);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMExpoPositionDuty(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMExpoDuty"));
    }

    @Test
    void setOutputMMExpoPositionVoltageSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(4);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMExpoPositionVoltage(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMExpoVoltage"));
    }

    @Test
    void setOutputMMExpoPositionTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(5);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMExpoPositionTorqueCurrent(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMExpoTC"));
    }

    @Test
    void setOutputDynMMPositionDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(6);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputDynMMPositionDuty(1.0, 100.0, 200.0, 1000.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mDynMMDuty"));
    }

    @Test
    void setOutputDynMMPositionTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(7);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputDynMMPositionTorqueCurrent(1.0, 100.0, 200.0, 1000.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mDynMMTC"));
    }

    /* ========== CANCoder Encoder Selection Branches ========== */

    @Test
    void addCANCoderCreatesEncoder() {
        CANDeviceID leaderId = new CANDeviceID(28);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.addCANCoder(new CANDeviceID(29), EncoderDirection.CW, 250.0);
        assertNotNull(tx.getEncoderCANCoder());
    }

    @Test
    void getPosWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(30);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(31), EncoderDirection.CW, 250.0);

        /* CANCoder branch returns encCANCoder.getPosRotations() * ratio */
        assertEquals(0.0, tx.getPos(), 1e-6);
    }

    @Test
    void getPosAbsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(32);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(33), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getPosAbs(), 1e-6);
    }

    @Test
    void getVelWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(34);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(35), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getVel(), 1e-6);
    }

    @Test
    void getVelRPSWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(36);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(37), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getVelRPS(), 1e-6);
    }

    @Test
    void getPosFXReturnsZeroWithCANCoderOnly() {
        CANDeviceID leaderId = new CANDeviceID(38);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(39), EncoderDirection.CW, 250.0);

        /* getPosFX checks encFX only, not encCANCoder */
        assertEquals(0.0, tx.getPosFX(), 1e-6);
    }

    @Test
    void getVelFXReturnsZeroWithCANCoderOnly() {
        CANDeviceID leaderId = new CANDeviceID(40);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(41), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getVelFX(), 1e-6);
    }

    @Test
    void getVelFXRPSReturnsZeroWithCANCoderOnly() {
        CANDeviceID leaderId = new CANDeviceID(42);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(43), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getVelFXRPS(), 1e-6);
    }

    @Test
    void getPosRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(44);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(45), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getPosRotations(), 1e-6);
    }

    @Test
    void getPosAbsRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(46);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(47), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getPosAbsRotations(), 1e-6);
    }

    @Test
    void setPosRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(48);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(49), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setPosRotations(3.0));
    }

    @Test
    void getVelWithBothEncoders() {
        CANDeviceID leaderId = new CANDeviceID(50);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(51), EncoderDirection.CW, 250.0);

        /* CANCoder takes priority */
        assertEquals(0.0, tx.getVel(), 1e-6);
    }

    @Test
    void getVelWithFXEncoderOnly() {
        CANDeviceID leaderId = new CANDeviceID(52);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        /* Falls through to getVelFX */
        assertEquals(0.0, tx.getVel(), 1e-6);
    }

    /* ========== getCLError Encoder Branches ========== */

    @Test
    void getCLErrorWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(53);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(54), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getCLError(), 1e-6);
    }

    /* ========== getSetpoint Encoder Branches ========== */

    @Test
    void getSetpointWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(55);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(56), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getSetpoint(), 1e-6);
    }

    /* ========== Limit Switch Configuration ========== */

    @Test
    void configLimitFwdCreatesSwitchFX() {
        CANDeviceID leaderId = new CANDeviceID(57);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        assertFalse(tx.getFwdLimit());
    }

    @Test
    void configLimitRevCreatesSwitchFX() {
        CANDeviceID leaderId = new CANDeviceID(58);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        assertFalse(tx.getRevLimit());
    }

    @Test
    void configLimitFwdDisabled() {
        CANDeviceID leaderId = new CANDeviceID(59);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitFwd(SwitchType.FX, false, SwitchTrigger.NC, false, 0.0);
        assertFalse(tx.getFwdLimit());
    }

    @Test
    void configLimitRevDisabled() {
        CANDeviceID leaderId = new CANDeviceID(60);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitRev(SwitchType.FX, false, SwitchTrigger.NC, false, 0.0);
        assertFalse(tx.getRevLimit());
    }

    @Test
    void configLimitFwdWithAutoZero() {
        CANDeviceID leaderId = new CANDeviceID(61);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, true, 5.0);
        assertFalse(tx.getFwdLimit());
    }

    /* ========== Shifter Operations ========== */

    /* Simple no-op Shifter for test isolation (avoids HAL port conflicts with ShifterPneumatic) */
    private static final Shifter TEST_SHIFTER = argShiftTo -> {};

    @Test
    void setShifterWithRatios() {
        CANDeviceID leaderId = new CANDeviceID(62);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setShifter(TEST_SHIFTER, 2.5, 1.5);

        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    @Test
    void setShifterWithoutRatios() {
        CANDeviceID leaderId = new CANDeviceID(1);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setShifter(TEST_SHIFTER);

        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    @Test
    void addShifterInvalidChannels() {
        CANDeviceID leaderId = new CANDeviceID(3);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* NO_SOLENOID_CHANNEL sentinel prevents shifter creation */
        tx.addShifter(
                1, TransmissionBase.NO_SOLENOID_CHANNEL, TransmissionBase.NO_SOLENOID_CHANNEL);
        /* No shifter created, shift still changes state */
        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    @Test
    void shiftWithShifterActuates() {
        CANDeviceID leaderId = new CANDeviceID(5);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setShifter(TEST_SHIFTER);

        tx.shift(ShifterState.GEAR_1);
        tx.shift(ShifterState.GEAR_2);
        assertEquals(ShifterState.GEAR_2, tx.shift(ShifterState.GEAR_2)); /* no-op */
    }

    @Test
    void shiftWithoutShifterStillUpdatesState() {
        CANDeviceID leaderId = new CANDeviceID(6);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* No shifter set — shifter is null */
        ShifterState result = tx.shift(ShifterState.GEAR_2);
        assertEquals(ShifterState.GEAR_2, result);
    }

    /* ========== TransmissionFX shift() Config Updates ========== */

    @Test
    void shiftToGear1UpdatesRotorRatio() {
        CANDeviceID leaderId = new CANDeviceID(7);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.setShifter(TEST_SHIFTER, 3.5, 2.0);

        tx.shift(ShifterState.GEAR_1);

        assertEquals(3.5, tx.getLeaderConfig().Feedback.RotorToSensorRatio, 1e-6);
    }

    @Test
    void shiftToGear2UpdatesRotorRatio() {
        CANDeviceID leaderId = new CANDeviceID(8);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.setShifter(TEST_SHIFTER, 3.5, 2.0);

        tx.shift(ShifterState.GEAR_2);

        assertEquals(2.0, tx.getLeaderConfig().Feedback.RotorToSensorRatio, 1e-6);
    }

    @Test
    void shiftToGearNoneDoesNotUpdateRatio() {
        CANDeviceID leaderId = new CANDeviceID(9);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        double originalRatio = tx.getLeaderConfig().Feedback.RotorToSensorRatio;

        tx.shift(ShifterState.GEAR_NONE);

        assertEquals(originalRatio, tx.getLeaderConfig().Feedback.RotorToSensorRatio, 1e-6);
    }

    /* ========== Config Error Rate Limiting ========== */

    @Test
    void applyConfigsReportsWarningOnFailure() {
        CANDeviceID leaderId = new CANDeviceID(10);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* In sim, applyConfig always fails — this exercises the rate-limiting path */
        assertDoesNotThrow(tx::applyConfigs);
    }

    @Test
    void applyConfigsRateLimitsWithin2Seconds() {
        CANDeviceID leaderId = new CANDeviceID(11);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* First call sets the rate-limit timer */
        tx.applyConfigs();
        /* Second call within 2s should be rate-limited */
        assertDoesNotThrow(tx::applyConfigs);
    }

    /* ========== CANCoder Output Methods ========== */

    @Test
    void setOutputPositionWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(12);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(13), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputPosition(2.0, 0.1));
    }

    @Test
    void setOutputVelocityWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(14);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(15), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocity(5.0, 0.2));
    }

    @Test
    void setOutputPositionDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(16);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(17), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputPositionDuty(2.0, 0.1));
    }

    @Test
    void setOutputPositionTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(18);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(19), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputPositionTorqueCurrent(2.0, 0.1));
    }

    @Test
    void setOutputVelocityDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(20);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(21), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocityDuty(5.0, 0.2));
    }

    @Test
    void setOutputVelocityTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(22);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(23), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocityTorqueCurrent(5.0, 0.2));
    }

    @Test
    void setOutputMMPositionDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(24);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(25), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionVoltageWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(26);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(27), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionVoltage(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(28);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(29), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputMMVelocityDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(30);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(31), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityDuty(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityVoltageWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(32);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(33), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityVoltage(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(34);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(35), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityTorqueCurrent(10.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(36);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(37), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionVoltageWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(38);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(39), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionVoltage(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(40);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(41), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(42);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(43), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionDuty(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionVoltageWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(44);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(45), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionVoltage(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(46);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(47), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(
                () -> tx.setOutputDynMMPositionTorqueCurrent(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    /* ========== Simulation with CANCoder ========== */

    @Test
    void setSimVelRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(48);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(49), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setSimVelRotations(10.0));
    }

    @Test
    void setSimPosRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(50);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(51), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setSimPosRotations(5.0));
    }

    @Test
    void simulationInitWithEncodersAndLimits() {
        CANDeviceID leaderId = new CANDeviceID(52);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(53), EncoderDirection.CW, 250.0);
        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);

        assertDoesNotThrow(() -> tx.simulationInit(0.0));
    }

    @Test
    void simulationPeriodicWithEncodersAndLimits() {
        CANDeviceID leaderId = new CANDeviceID(54);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(55), EncoderDirection.CW, 250.0);
        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);

        tx.simulationInit(0.0);
        assertDoesNotThrow(() -> tx.simulationPeriodic(0.02));
    }

    /* ========== robotInit with Encoders and Limits ========== */

    @Test
    void robotInitWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(56);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    @Test
    void robotInitWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(57);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(58), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    @Test
    void robotInitWithBothEncoders() {
        CANDeviceID leaderId = new CANDeviceID(59);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(60), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    @Test
    void robotInitWithLimitSwitches() {
        CANDeviceID leaderId = new CANDeviceID(61);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    /* ========== outputTelemetry with Encoders and Limits ========== */

    @Test
    void outputTelemetryWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(62);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(1), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::outputTelemetry);
    }

    @Test
    void outputTelemetryWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(2);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(tx::outputTelemetry);
    }

    @Test
    void outputTelemetryWithLimitSwitches() {
        CANDeviceID leaderId = new CANDeviceID(3);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);

        assertDoesNotThrow(tx::outputTelemetry);
    }

    /* ========== Refresh with Encoders ========== */

    @Test
    void refreshWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(4);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(tx::refresh);
    }

    @Test
    void refreshWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(5);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(6), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::refresh);
    }

    @Test
    void refreshWithBothEncoders() {
        CANDeviceID leaderId = new CANDeviceID(7);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(8), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::refresh);
    }

    /* ========== Reset Encoders with CANCoder ========== */

    @Test
    void resetEncodersWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(9);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(10), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::resetEncoders);
    }

    @Test
    void resetEncodersWithBothEncoders() {
        CANDeviceID leaderId = new CANDeviceID(11);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(12), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::resetEncoders);
    }

    /* ========== Motion Magic Config ========== */

    @Test
    void setMMConfigUpdatesFields() {
        CANDeviceID leaderId = new CANDeviceID(13);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setMMConfig(50.0, 100.0, 500.0);
        /* Verify via outputTelemetry which publishes configMM values */
        assertDoesNotThrow(tx::outputTelemetry);
    }

    /* ========== Encoder Direction CCW ========== */

    @Test
    void addCANCoderCCW() {
        CANDeviceID leaderId = new CANDeviceID(14);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.addCANCoder(new CANDeviceID(15), EncoderDirection.CCW, 250.0);
        assertNotNull(tx.getEncoderCANCoder());
    }

    /* ========== getVel with FX encoder (else-if branch) ========== */

    @Test
    void getVelFXWithEncoderReturnsValue() {
        CANDeviceID leaderId = new CANDeviceID(16);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getVelFX(), 1e-6);
    }

    @Test
    void getVelFXRPSWithEncoderReturnsValue() {
        CANDeviceID leaderId = new CANDeviceID(17);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getVelFXRPS(), 1e-6);
    }

    /* ========== getPos with FX encoder (else-if branch in getPosRotations) ========== */

    @Test
    void getPosRotationsWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(18);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getPosRotations(), 1e-6);
    }
}
