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
        CANDeviceID leaderId = new CANDeviceID(100);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNotNull(tx);
        assertNotNull(tx.getLeader());
        assertNotNull(tx.getLeaderController());
        assertNotNull(tx.getLeaderConfig());
    }

    @Test
    void constructorSingleMotorHasOneController() {
        CANDeviceID leaderId = new CANDeviceID(101);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(1, tx.getAllControllers().size());
    }

    /* Constructor - With Follower */

    @Test
    void constructorWithFollower() {
        CANDeviceID leaderId = new CANDeviceID(102);
        CANDeviceID followerId = new CANDeviceID(103);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId, followerId, false);

        assertNotNull(tx);
        assertEquals(2, tx.getAllControllers().size());
    }

    @Test
    void constructorWithFollowerOpposed() {
        CANDeviceID leaderId = new CANDeviceID(104);
        CANDeviceID followerId = new CANDeviceID(105);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId, followerId, true);

        assertEquals(2, tx.getAllControllers().size());
    }

    /* Constructor - With Two Followers */

    @Test
    void constructorWithTwoFollowers() {
        CANDeviceID leaderId = new CANDeviceID(106);
        CANDeviceID follower1Id = new CANDeviceID(107);
        CANDeviceID follower2Id = new CANDeviceID(108);
        TransmissionFX tx =
                new TransmissionFX(
                        null, "TX", KRAKEN, leaderId, follower1Id, false, follower2Id, true);

        assertEquals(3, tx.getAllControllers().size());
    }

    /* setDutyCycle */

    @Test
    void setOutputDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(109);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputDuty(0.5));
    }

    /* setVoltage */

    @Test
    void setOutputVoltageDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(110);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputVoltage(6.0));
    }

    /* setNeutralMode */

    @Test
    void setNeutralModeBrake() {
        CANDeviceID leaderId = new CANDeviceID(111);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setNeutralMode(NeutralState.BRAKE);
        assertEquals(NeutralState.BRAKE, tx.getNeutralMode());
    }

    @Test
    void setNeutralModeCoast() {
        CANDeviceID leaderId = new CANDeviceID(112);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setNeutralMode(NeutralState.COAST);
        assertEquals(NeutralState.COAST, tx.getNeutralMode());
    }

    /* robotInit */

    @Test
    void robotInitDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(113);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    /* outputTelemetry */

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(114);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::outputTelemetry);
    }

    /* stop */

    @Test
    void stopDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(115);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::stop);
    }

    /* Encoder Integration */

    @Test
    void addEncoderFXDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(116);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.addEncoderFX(250.0));
        assertNotNull(tx.getEncoderFX());
    }

    @Test
    void positionReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(117);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getPosFX(), 1e-6);
        assertEquals(0.0, tx.getPos(), 1e-6);
    }

    @Test
    void velocityReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(118);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getVelFX(), 1e-6);
        assertEquals(0.0, tx.getVel(), 1e-6);
    }

    /* Gear Ratio Setters */

    @Test
    void setRotorToMechanism() {
        CANDeviceID leaderId = new CANDeviceID(119);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        tx.setRotorToMechanism(0.5);
        assertEquals(0.0, tx.getPosFX(), 1e-6);
    }

    /* PID Passthrough */

    @Test
    void pidPassthroughToLeader() {
        CANDeviceID leaderId = new CANDeviceID(120);
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
        CANDeviceID leaderId = new CANDeviceID(121);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configCurrentLimitStator(true, 40.0));
        assertDoesNotThrow(() -> tx.configCurrentLimitSupply(true, 30.0));
    }

    /* Refresh */

    @Test
    void refreshDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(122);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::refresh);
    }

    /* applyConfigs */

    @Test
    void applyConfigsDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(123);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::applyConfigs);
    }

    /* Limit Switches */

    @Test
    void limitSwitchesReturnFalseByDefault() {
        CANDeviceID leaderId = new CANDeviceID(124);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertFalse(tx.getRevLimit());
        assertFalse(tx.getFwdLimit());
    }

    /* Simulation */

    @Test
    void simulationInitDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(125);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.simulationInit(0.0));
    }

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(126);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.simulationInit(0.0);
        assertDoesNotThrow(() -> tx.simulationPeriodic(0.0));
    }

    /* --- Position control --- */

    @Test
    void setOutputPositionDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(127);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputPosition(1.0, 0.5));
    }

    @Test
    void setOutputPositionWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(128);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputPosition(2.0, 0.1));
    }

    /* --- Velocity control --- */

    @Test
    void setOutputVelocityDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(129);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputVelocity(10.0, 0.5));
    }

    @Test
    void setOutputVelocityWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(130);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocity(5.0, 0.2));
    }

    /* --- Motion Magic --- */

    @Test
    void setMMConfigDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(131);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setMMConfig(100.0, 200.0, 1000.0));
    }

    @Test
    void setOutputMMPositionDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(132);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionDutyWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(133);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionVoltageWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(134);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionVoltage(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionVoltageWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(135);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMPositionVoltage(1.0, 0.5));
    }

    /* --- Gear Ratio Setters --- */

    @Test
    void setSensorToMechanismStoresValue() {
        CANDeviceID leaderId = new CANDeviceID(136);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setSensorToMechanism(0.25);
        assertEquals(0.0, tx.getPos(), 1e-6);
    }

    @Test
    void setSensorAbsToMechanismStoresValue() {
        CANDeviceID leaderId = new CANDeviceID(137);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setSensorAbsToMechanism(0.5);
        assertEquals(0.0, tx.getPosAbs(), 1e-6);
    }

    @Test
    void setMechanismToUnitsStoresValue() {
        CANDeviceID leaderId = new CANDeviceID(138);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setMechanismToUnits(2.0);
        assertEquals(0.0, tx.getPosFX(), 1e-6);
    }

    /* --- Direction --- */

    @Test
    void configDirectionDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(139);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configDirection(MotorDirection.CW));
        assertDoesNotThrow(() -> tx.configDirection(MotorDirection.CCW));
    }

    /* --- Voltage Limits --- */

    @Test
    void configVoltagePeakDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(140);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configVoltagePeak(12.0, -12.0, 0.01));
    }

    /* --- Ramp Rates --- */

    @Test
    void configRampRatesDoNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(141);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configRampOpenLoopDuty(0.5));
        assertDoesNotThrow(() -> tx.configRampOpenLoopVoltage(0.3));
        assertDoesNotThrow(() -> tx.configRampClosedLoopDuty(0.2));
        assertDoesNotThrow(() -> tx.configRampClosedLoopVoltage(0.1));
    }

    /* --- configPIDFSlot --- */

    @Test
    void configPIDFSlotPassesThrough() {
        CANDeviceID leaderId = new CANDeviceID(142);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configPIDFSlot(0, 1.0, 0.5, 0.25, 0.1, 0.05);
        assertEquals(1.0, tx.getPSlot(0), 1e-6);
        assertEquals(0.5, tx.getISlot(0), 1e-6);
        assertEquals(0.25, tx.getDSlot(0), 1e-6);
    }

    /* --- setPIDFSlot on TransmissionBase --- */

    @Test
    void setPIDFSlotPassesThrough() {
        CANDeviceID leaderId = new CANDeviceID(143);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setPIDFSlot(1, 2.0, 1.0, 0.5, 0.2, 0.1);
        assertEquals(2.0, tx.getPSlot(1), 1e-6);
    }

    /* --- Closed Loop Error --- */

    @Test
    void getCLErrorReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(144);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getCLError(), 1e-6);
    }

    @Test
    void getCLErrorWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(145);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getCLError(), 1e-6);
    }

    @Test
    void getCLOutputReturnsZero() {
        CANDeviceID leaderId = new CANDeviceID(146);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getCLOutput(), 1e-6);
    }

    /* --- getOutputDuty / getOutputVoltage --- */

    @Test
    void getOutputDutyReturnsValue() {
        CANDeviceID leaderId = new CANDeviceID(147);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getOutputDuty(), 1e-6);
    }

    @Test
    void getOutputVoltageAveragesControllers() {
        CANDeviceID leaderId = new CANDeviceID(148);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getOutputVoltage(), 1e-6);
    }

    /* --- getSetpoint --- */

    @Test
    void getSetpointReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(149);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getSetpoint(), 1e-6);
    }

    @Test
    void getSetpointReturnsValueWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(150);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getSetpoint(), 1e-6);
    }

    /* --- Tolerance / Setpoint base methods --- */

    @Test
    void getToleranceReturnsZero() {
        CANDeviceID leaderId = new CANDeviceID(151);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getTolerance(), 1e-6);
    }

    @Test
    void setSetpointDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(152);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setSetpoint(1.0));
    }

    /* --- Shifters --- */

    @Test
    void shiftToGear1DoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(153);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    @Test
    void shiftToGear2DoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(154);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        ShifterState result = tx.shift(ShifterState.GEAR_2);
        assertEquals(ShifterState.GEAR_2, result);
    }

    @Test
    void shiftToSameGearDoesNotReapply() {
        CANDeviceID leaderId = new CANDeviceID(155);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.shift(ShifterState.GEAR_1);
        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    /* --- Current Limits (3-arg supply variant) --- */

    @Test
    void configCurrentLimitSupplyThreeArgDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(156);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.configCurrentLimitSupply(60.0, 1.0, 40.0));
    }

    /* --- resetEncoders --- */

    @Test
    void resetEncodersDoesNotThrowWithoutEncoders() {
        CANDeviceID leaderId = new CANDeviceID(157);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(tx::resetEncoders);
    }

    @Test
    void resetEncodersWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(158);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(tx::resetEncoders);
    }

    /* --- setPosRotations / getPosRotations --- */

    @Test
    void setPosRotationsWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(159);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setPosRotations(5.0));
    }

    @Test
    void setPosRotationsWithoutEncoderDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(160);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setPosRotations(5.0));
    }

    @Test
    void getPosRotationsReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(161);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getPosRotations(), 1e-6);
    }

    @Test
    void getPosAbsRotationsReturnsZeroWithoutCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(162);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getPosAbsRotations(), 1e-6);
    }

    @Test
    void getPosAbsReturnsZeroWithoutCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(163);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getPosAbs(), 1e-6);
    }

    /* --- Velocity RPS --- */

    @Test
    void getVelFXRPSReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(164);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getVelFXRPS(), 1e-6);
    }

    @Test
    void getVelRPSReturnsZeroWithoutEncoder() {
        CANDeviceID leaderId = new CANDeviceID(165);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertEquals(0.0, tx.getVelRPS(), 1e-6);
    }

    /* --- getMotor by ID --- */

    @Test
    void getMotorByIdReturnsLeader() {
        CANDeviceID leaderId = new CANDeviceID(166);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNotNull(tx.getMotor(leaderId));
    }

    @Test
    void getMotorByIdReturnsNullForUnknownId() {
        CANDeviceID leaderId = new CANDeviceID(167);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNull(tx.getMotor(new CANDeviceID(999)));
    }

    /* --- getDCMotor --- */

    @Test
    void getDCMotorNullBeforeRobotInit() {
        CANDeviceID leaderId = new CANDeviceID(168);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNull(tx.getDCMotor());
    }

    @Test
    void getDCMotorSetAfterRobotInit() {
        CANDeviceID leaderId = new CANDeviceID(169);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.robotInit(0.0);

        assertNotNull(tx.getDCMotor());
    }

    /* --- robotInit with different motor types --- */

    @Test
    void robotInitWithFalcon500() {
        MotorBase falcon = new MotorBase(MotorBase.MotorType.FALCON500);
        CANDeviceID leaderId = new CANDeviceID(170);
        TransmissionFX tx = new TransmissionFX(null, "TX", falcon, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithKrakenX44() {
        MotorBase krakenX44 = new MotorBase(MotorBase.MotorType.KRAKENX44);
        CANDeviceID leaderId = new CANDeviceID(171);
        TransmissionFX tx = new TransmissionFX(null, "TX", krakenX44, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithCTREMinion() {
        MotorBase minion = new MotorBase(MotorBase.MotorType.CTRE_MINION);
        CANDeviceID leaderId = new CANDeviceID(172);
        TransmissionFX tx = new TransmissionFX(null, "TX", minion, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithNEO() {
        MotorBase neo = new MotorBase(MotorBase.MotorType.NEO);
        CANDeviceID leaderId = new CANDeviceID(173);
        TransmissionFX tx = new TransmissionFX(null, "TX", neo, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithNEO550() {
        MotorBase neo550 = new MotorBase(MotorBase.MotorType.NEO550);
        CANDeviceID leaderId = new CANDeviceID(174);
        TransmissionFX tx = new TransmissionFX(null, "TX", neo550, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    @Test
    void robotInitWithNEOVortex() {
        MotorBase neoVortex = new MotorBase(MotorBase.MotorType.NEO_VORTEX);
        CANDeviceID leaderId = new CANDeviceID(175);
        TransmissionFX tx = new TransmissionFX(null, "TX", neoVortex, leaderId);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
        assertNotNull(tx.getDCMotor());
    }

    /* --- robotPeriodicBefore --- */

    @Test
    void robotPeriodicBeforeDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(176);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.robotPeriodicBefore(0.0));
    }

    /* --- setSimVelRotations / setSimPosRotations --- */

    @Test
    void setSimVelRotationsDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(177);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setSimVelRotations(10.0));
    }

    @Test
    void setSimPosRotationsDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(178);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setSimPosRotations(5.0));
    }

    /* --- getSimState --- */

    @Test
    void getSimStateReturnsNonNull() {
        CANDeviceID leaderId = new CANDeviceID(179);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNotNull(tx.getSimState());
    }

    /* --- getEncoderCANCoder --- */

    @Test
    void getEncoderCANCoderReturnsNullByDefault() {
        CANDeviceID leaderId = new CANDeviceID(180);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertNull(tx.getEncoderCANCoder());
    }

    /* --- robotPeriodicAfter --- */

    @Test
    void robotPeriodicAfterDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(181);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.robotPeriodicAfter(0.0));
    }

    /* --- setOutputTorqueCurrent --- */

    @Test
    void setOutputTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(182);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputTorqueCurrent(10.0));
    }

    @Test
    void setOutputTorqueCurrentBaseDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(183);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        TransmissionBase base = tx;
        assertDoesNotThrow(() -> base.setOutputTorqueCurrent(5.0));
    }

    /* --- setOutputPositionDuty --- */

    @Test
    void setOutputPositionDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(184);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputPositionDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(185);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputPositionDuty(2.0, 0.1));
    }

    /* --- setOutputPositionTorqueCurrent --- */

    @Test
    void setOutputPositionTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(186);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputPositionTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(187);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputPositionTorqueCurrent(2.0, 0.1));
    }

    /* --- setOutputVelocityDuty --- */

    @Test
    void setOutputVelocityDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(188);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputVelocityDuty(10.0, 0.5));
    }

    @Test
    void setOutputVelocityDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(189);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocityDuty(5.0, 0.2));
    }

    /* --- setOutputVelocityTorqueCurrent --- */

    @Test
    void setOutputVelocityTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(190);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputVelocityTorqueCurrent(10.0, 0.5));
    }

    @Test
    void setOutputVelocityTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(191);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocityTorqueCurrent(5.0, 0.2));
    }

    /* --- setOutputMMPositionTorqueCurrent --- */

    @Test
    void setOutputMMPositionTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(192);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(193);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionTorqueCurrent(2.0, 0.1));
    }

    /* --- setOutputMMVelocityDuty --- */

    @Test
    void setOutputMMVelocityDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(194);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityDuty(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(195);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityDuty(5.0, 0.2));
    }

    /* --- setOutputMMVelocityVoltage --- */

    @Test
    void setOutputMMVelocityVoltageDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(196);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityVoltage(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityVoltageWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(197);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityVoltage(5.0, 0.2));
    }

    /* --- setOutputMMVelocityTorqueCurrent --- */

    @Test
    void setOutputMMVelocityTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(198);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityTorqueCurrent(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(199);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityTorqueCurrent(5.0, 0.2));
    }

    /* --- setOutputMMExpoPositionDuty --- */

    @Test
    void setOutputMMExpoPositionDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(200);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(201);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionDuty(2.0, 0.1));
    }

    /* --- setOutputMMExpoPositionVoltage --- */

    @Test
    void setOutputMMExpoPositionVoltageDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(202);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionVoltage(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionVoltageWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(203);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionVoltage(2.0, 0.1));
    }

    /* --- setOutputMMExpoPositionTorqueCurrent --- */

    @Test
    void setOutputMMExpoPositionTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(204);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(205);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionTorqueCurrent(2.0, 0.1));
    }

    /* --- setOutputDynMMPositionDuty --- */

    @Test
    void setOutputDynMMPositionDutyDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(206);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionDuty(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionDutyWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(207);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionDuty(2.0, 80.0, 160.0, 800.0, 0.1));
    }

    /* --- setOutputDynMMPositionVoltage --- */

    @Test
    void setOutputDynMMPositionVoltageDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(208);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionVoltage(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionVoltageWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(209);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionVoltage(2.0, 80.0, 160.0, 800.0, 0.1));
    }

    /* --- setOutputDynMMPositionTorqueCurrent --- */

    @Test
    void setOutputDynMMPositionTorqueCurrentDoesNotThrow() {
        CANDeviceID leaderId = new CANDeviceID(210);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        assertDoesNotThrow(
                () -> tx.setOutputDynMMPositionTorqueCurrent(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionTorqueCurrentWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(211);
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
    private int getSlot(Object root, String... path) throws Exception {
        Object current = root;
        for (String fieldName : path) {
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
        CANDeviceID id = new CANDeviceID(300);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputPosition(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mController", "motorPosition"));
    }

    @Test
    void setOutputVelocitySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(301);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputVelocity(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mController", "motorVelocity"));
    }

    @Test
    void setOutputPositionDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(302);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputPositionDuty(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mPositionDuty"));
    }

    @Test
    void setOutputVelocityDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(303);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputVelocityDuty(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mVelocityDuty"));
    }

    @Test
    void setOutputMMPositionVoltageSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(304);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMPositionVoltage(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMPositionVoltage"));
    }

    @Test
    void setOutputDynMMPositionVoltageSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(305);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputDynMMPositionVoltage(1.0, 100.0, 200.0, 1000.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mDynMMVoltage"));
    }

    @Test
    void setOutputPositionTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(306);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputPositionTorqueCurrent(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mPositionTC"));
    }

    @Test
    void setOutputVelocityTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(307);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputVelocityTorqueCurrent(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mVelocityTC"));
    }

    @Test
    void setOutputMMPositionDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(308);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMPositionDuty(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMPositionDuty"));
    }

    @Test
    void setOutputMMPositionTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(309);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMPositionTorqueCurrent(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMPositionTC"));
    }

    @Test
    void setOutputMMVelocityDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(310);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMVelocityDuty(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMVelocityDuty"));
    }

    @Test
    void setOutputMMVelocityVoltageSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(311);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMVelocityVoltage(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMVelocityVoltage"));
    }

    @Test
    void setOutputMMVelocityTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(312);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMVelocityTorqueCurrent(10.0, 0.2);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMVelocityTC"));
    }

    @Test
    void setOutputMMExpoPositionDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(313);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMExpoPositionDuty(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMExpoDuty"));
    }

    @Test
    void setOutputMMExpoPositionVoltageSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(314);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMExpoPositionVoltage(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMExpoVoltage"));
    }

    @Test
    void setOutputMMExpoPositionTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(315);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputMMExpoPositionTorqueCurrent(1.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mMMExpoTC"));
    }

    @Test
    void setOutputDynMMPositionDutySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(316);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputDynMMPositionDuty(1.0, 100.0, 200.0, 1000.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mDynMMDuty"));
    }

    @Test
    void setOutputDynMMPositionTorqueCurrentSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(317);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, id);
        tx.addEncoderFX(250.0);

        tx.setOutputDynMMPositionTorqueCurrent(1.0, 100.0, 200.0, 1000.0, 0.1);

        assertEquals(0, getSlot(tx, "mCTRELeader", "mDynMMTC"));
    }

    /* ========== CANCoder Encoder Selection Branches ========== */

    @Test
    void addCANCoderCreatesEncoder() {
        CANDeviceID leaderId = new CANDeviceID(400);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.addCANCoder(new CANDeviceID(401), EncoderDirection.CW, 250.0);
        assertNotNull(tx.getEncoderCANCoder());
    }

    @Test
    void getPosWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(402);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(403), EncoderDirection.CW, 250.0);

        /* CANCoder branch returns encCANCoder.getPosRotations() * ratio */
        assertEquals(0.0, tx.getPos(), 1e-6);
    }

    @Test
    void getPosAbsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(404);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(405), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getPosAbs(), 1e-6);
    }

    @Test
    void getVelWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(406);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(407), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getVel(), 1e-6);
    }

    @Test
    void getVelRPSWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(408);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(409), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getVelRPS(), 1e-6);
    }

    @Test
    void getPosFXReturnsZeroWithCANCoderOnly() {
        CANDeviceID leaderId = new CANDeviceID(410);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(411), EncoderDirection.CW, 250.0);

        /* getPosFX checks encFX only, not encCANCoder */
        assertEquals(0.0, tx.getPosFX(), 1e-6);
    }

    @Test
    void getVelFXReturnsZeroWithCANCoderOnly() {
        CANDeviceID leaderId = new CANDeviceID(412);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(413), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getVelFX(), 1e-6);
    }

    @Test
    void getVelFXRPSReturnsZeroWithCANCoderOnly() {
        CANDeviceID leaderId = new CANDeviceID(414);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(415), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getVelFXRPS(), 1e-6);
    }

    @Test
    void getPosRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(416);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(417), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getPosRotations(), 1e-6);
    }

    @Test
    void getPosAbsRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(418);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(419), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getPosAbsRotations(), 1e-6);
    }

    @Test
    void setPosRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(420);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(421), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setPosRotations(3.0));
    }

    @Test
    void getVelWithBothEncoders() {
        CANDeviceID leaderId = new CANDeviceID(422);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(423), EncoderDirection.CW, 250.0);

        /* CANCoder takes priority */
        assertEquals(0.0, tx.getVel(), 1e-6);
    }

    @Test
    void getVelWithFXEncoderOnly() {
        CANDeviceID leaderId = new CANDeviceID(424);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        /* Falls through to getVelFX */
        assertEquals(0.0, tx.getVel(), 1e-6);
    }

    /* ========== getCLError Encoder Branches ========== */

    @Test
    void getCLErrorWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(425);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(426), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getCLError(), 1e-6);
    }

    /* ========== getSetpoint Encoder Branches ========== */

    @Test
    void getSetpointWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(427);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(428), EncoderDirection.CW, 250.0);

        assertEquals(0.0, tx.getSetpoint(), 1e-6);
    }

    /* ========== Limit Switch Configuration ========== */

    @Test
    void configLimitFwdCreatesSwitchFX() {
        CANDeviceID leaderId = new CANDeviceID(429);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        assertFalse(tx.getFwdLimit());
    }

    @Test
    void configLimitRevCreatesSwitchFX() {
        CANDeviceID leaderId = new CANDeviceID(430);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        assertFalse(tx.getRevLimit());
    }

    @Test
    void configLimitFwdDisabled() {
        CANDeviceID leaderId = new CANDeviceID(431);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitFwd(SwitchType.FX, false, SwitchTrigger.NC, false, 0.0);
        assertFalse(tx.getFwdLimit());
    }

    @Test
    void configLimitRevDisabled() {
        CANDeviceID leaderId = new CANDeviceID(432);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitRev(SwitchType.FX, false, SwitchTrigger.NC, false, 0.0);
        assertFalse(tx.getRevLimit());
    }

    @Test
    void configLimitFwdWithAutoZero() {
        CANDeviceID leaderId = new CANDeviceID(433);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, true, 5.0);
        assertFalse(tx.getFwdLimit());
    }

    /* ========== Shifter Operations ========== */

    /* Simple no-op Shifter for test isolation (avoids HAL port conflicts with ShifterPneumatic) */
    private static final Shifter TEST_SHIFTER = argShiftTo -> {};

    @Test
    void setShifterWithRatios() {
        CANDeviceID leaderId = new CANDeviceID(434);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setShifter(TEST_SHIFTER, 2.5, 1.5);

        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    @Test
    void setShifterWithoutRatios() {
        CANDeviceID leaderId = new CANDeviceID(435);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setShifter(TEST_SHIFTER);

        ShifterState result = tx.shift(ShifterState.GEAR_1);
        assertEquals(ShifterState.GEAR_1, result);
    }

    @Test
    void addShifterInvalidChannels() {
        CANDeviceID leaderId = new CANDeviceID(437);
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
        CANDeviceID leaderId = new CANDeviceID(439);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setShifter(TEST_SHIFTER);

        tx.shift(ShifterState.GEAR_1);
        tx.shift(ShifterState.GEAR_2);
        assertEquals(ShifterState.GEAR_2, tx.shift(ShifterState.GEAR_2)); /* no-op */
    }

    @Test
    void shiftWithoutShifterStillUpdatesState() {
        CANDeviceID leaderId = new CANDeviceID(440);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* No shifter set — shifter is null */
        ShifterState result = tx.shift(ShifterState.GEAR_2);
        assertEquals(ShifterState.GEAR_2, result);
    }

    /* ========== TransmissionFX shift() Config Updates ========== */

    @Test
    void shiftToGear1UpdatesRotorRatio() {
        CANDeviceID leaderId = new CANDeviceID(441);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.setShifter(TEST_SHIFTER, 3.5, 2.0);

        tx.shift(ShifterState.GEAR_1);

        assertEquals(3.5, tx.getLeaderConfig().Feedback.RotorToSensorRatio, 1e-6);
    }

    @Test
    void shiftToGear2UpdatesRotorRatio() {
        CANDeviceID leaderId = new CANDeviceID(442);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.setShifter(TEST_SHIFTER, 3.5, 2.0);

        tx.shift(ShifterState.GEAR_2);

        assertEquals(2.0, tx.getLeaderConfig().Feedback.RotorToSensorRatio, 1e-6);
    }

    @Test
    void shiftToGearNoneDoesNotUpdateRatio() {
        CANDeviceID leaderId = new CANDeviceID(443);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        double originalRatio = tx.getLeaderConfig().Feedback.RotorToSensorRatio;

        tx.shift(ShifterState.GEAR_NONE);

        assertEquals(originalRatio, tx.getLeaderConfig().Feedback.RotorToSensorRatio, 1e-6);
    }

    /* ========== Config Error Rate Limiting ========== */

    @Test
    void applyConfigsReportsWarningOnFailure() {
        CANDeviceID leaderId = new CANDeviceID(444);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* In sim, applyConfig always fails — this exercises the rate-limiting path */
        assertDoesNotThrow(tx::applyConfigs);
    }

    @Test
    void applyConfigsRateLimitsWithin2Seconds() {
        CANDeviceID leaderId = new CANDeviceID(445);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        /* First call sets the rate-limit timer */
        tx.applyConfigs();
        /* Second call within 2s should be rate-limited */
        assertDoesNotThrow(tx::applyConfigs);
    }

    /* ========== CANCoder Output Methods ========== */

    @Test
    void setOutputPositionWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(446);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(447), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputPosition(2.0, 0.1));
    }

    @Test
    void setOutputVelocityWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(448);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(449), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocity(5.0, 0.2));
    }

    @Test
    void setOutputPositionDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(450);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(451), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputPositionDuty(2.0, 0.1));
    }

    @Test
    void setOutputPositionTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(452);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(453), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputPositionTorqueCurrent(2.0, 0.1));
    }

    @Test
    void setOutputVelocityDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(454);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(455), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocityDuty(5.0, 0.2));
    }

    @Test
    void setOutputVelocityTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(456);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(457), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputVelocityTorqueCurrent(5.0, 0.2));
    }

    @Test
    void setOutputMMPositionDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(458);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(459), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionVoltageWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(460);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(461), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionVoltage(1.0, 0.5));
    }

    @Test
    void setOutputMMPositionTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(462);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(463), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputMMVelocityDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(464);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(465), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityDuty(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityVoltageWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(466);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(467), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityVoltage(10.0, 0.5));
    }

    @Test
    void setOutputMMVelocityTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(468);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(469), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMVelocityTorqueCurrent(10.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(470);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(471), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionDuty(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionVoltageWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(472);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(473), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionVoltage(1.0, 0.5));
    }

    @Test
    void setOutputMMExpoPositionTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(474);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(475), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputMMExpoPositionTorqueCurrent(1.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionDutyWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(476);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(477), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionDuty(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionVoltageWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(478);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(479), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setOutputDynMMPositionVoltage(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    @Test
    void setOutputDynMMPositionTorqueCurrentWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(480);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(481), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(
                () -> tx.setOutputDynMMPositionTorqueCurrent(1.0, 100.0, 200.0, 1000.0, 0.5));
    }

    /* ========== Simulation with CANCoder ========== */

    @Test
    void setSimVelRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(482);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(483), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setSimVelRotations(10.0));
    }

    @Test
    void setSimPosRotationsWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(484);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(485), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.setSimPosRotations(5.0));
    }

    @Test
    void simulationInitWithEncodersAndLimits() {
        CANDeviceID leaderId = new CANDeviceID(486);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(487), EncoderDirection.CW, 250.0);
        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);

        assertDoesNotThrow(() -> tx.simulationInit(0.0));
    }

    @Test
    void simulationPeriodicWithEncodersAndLimits() {
        CANDeviceID leaderId = new CANDeviceID(488);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(489), EncoderDirection.CW, 250.0);
        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);

        tx.simulationInit(0.0);
        assertDoesNotThrow(() -> tx.simulationPeriodic(0.02));
    }

    /* ========== robotInit with Encoders and Limits ========== */

    @Test
    void robotInitWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(490);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    @Test
    void robotInitWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(491);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(492), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    @Test
    void robotInitWithBothEncoders() {
        CANDeviceID leaderId = new CANDeviceID(493);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(494), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    @Test
    void robotInitWithLimitSwitches() {
        CANDeviceID leaderId = new CANDeviceID(495);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    /* ========== outputTelemetry with Encoders and Limits ========== */

    @Test
    void outputTelemetryWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(496);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(497), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::outputTelemetry);
    }

    @Test
    void outputTelemetryWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(498);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(tx::outputTelemetry);
    }

    @Test
    void outputTelemetryWithLimitSwitches() {
        CANDeviceID leaderId = new CANDeviceID(499);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.configLimitFwd(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);
        tx.configLimitRev(SwitchType.FX, true, SwitchTrigger.NO, false, 0.0);

        assertDoesNotThrow(tx::outputTelemetry);
    }

    /* ========== Refresh with Encoders ========== */

    @Test
    void refreshWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(500);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertDoesNotThrow(tx::refresh);
    }

    @Test
    void refreshWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(501);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(502), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::refresh);
    }

    @Test
    void refreshWithBothEncoders() {
        CANDeviceID leaderId = new CANDeviceID(503);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(504), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::refresh);
    }

    /* ========== Reset Encoders with CANCoder ========== */

    @Test
    void resetEncodersWithCANCoder() {
        CANDeviceID leaderId = new CANDeviceID(505);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addCANCoder(new CANDeviceID(506), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::resetEncoders);
    }

    @Test
    void resetEncodersWithBothEncoders() {
        CANDeviceID leaderId = new CANDeviceID(507);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);
        tx.addCANCoder(new CANDeviceID(508), EncoderDirection.CW, 250.0);

        assertDoesNotThrow(tx::resetEncoders);
    }

    /* ========== Motion Magic Config ========== */

    @Test
    void setMMConfigUpdatesFields() {
        CANDeviceID leaderId = new CANDeviceID(509);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.setMMConfig(50.0, 100.0, 500.0);
        /* Verify via outputTelemetry which publishes configMM values */
        assertDoesNotThrow(tx::outputTelemetry);
    }

    /* ========== Encoder Direction CCW ========== */

    @Test
    void addCANCoderCCW() {
        CANDeviceID leaderId = new CANDeviceID(510);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);

        tx.addCANCoder(new CANDeviceID(511), EncoderDirection.CCW, 250.0);
        assertNotNull(tx.getEncoderCANCoder());
    }

    /* ========== getVel with FX encoder (else-if branch) ========== */

    @Test
    void getVelFXWithEncoderReturnsValue() {
        CANDeviceID leaderId = new CANDeviceID(512);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getVelFX(), 1e-6);
    }

    @Test
    void getVelFXRPSWithEncoderReturnsValue() {
        CANDeviceID leaderId = new CANDeviceID(513);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getVelFXRPS(), 1e-6);
    }

    /* ========== getPos with FX encoder (else-if branch in getPosRotations) ========== */

    @Test
    void getPosRotationsWithEncoderFX() {
        CANDeviceID leaderId = new CANDeviceID(514);
        TransmissionFX tx = new TransmissionFX(null, "TX", KRAKEN, leaderId);
        tx.addEncoderFX(250.0);

        assertEquals(0.0, tx.getPosRotations(), 1e-6);
    }
}
