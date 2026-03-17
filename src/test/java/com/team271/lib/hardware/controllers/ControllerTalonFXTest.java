package com.team271.lib.hardware.controllers;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerBase.ControllerStatus;
import com.team271.lib.hardware.controllers.ControllerBase.MotorDirection;
import com.team271.lib.hardware.controllers.ControllerBase.NeutralState;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class ControllerTalonFXTest {

    private static final MotorBase KRAKEN = new MotorBase(MotorBase.MotorType.KRAKENX60);

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void resetCTREManager() throws Exception {
        clearStaticField("buses");
        clearStaticField("devicesByBus");
        clearStaticField("devices");
        clearStaticField("signalsAll");
        setStaticField("signalsAllArray", null);
        setStaticField("prevRefreshTime", null);
        setStaticField("lastRefreshTime", null);
    }

    private void clearStaticField(String fieldName) throws Exception {
        Field f = CTREManager.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        Object collection = f.get(null);
        if (collection instanceof java.util.Map) {
            ((java.util.Map<?, ?>) collection).clear();
        } else if (collection instanceof java.util.List) {
            ((java.util.List<?>) collection).clear();
        }
    }

    private void setStaticField(String fieldName, Object value) throws Exception {
        Field f = CTREManager.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    /* Constructor */

    @Test
    void constructorCreatesTalonFX() {
        CANDeviceID id = new CANDeviceID(1);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertNotNull(controller.getTalonFX());
    }

    @Test
    void getConfigIsNotNull() {
        CANDeviceID id = new CANDeviceID(2);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertNotNull(controller.getConfig());
    }

    @Test
    void motorIsStored() {
        CANDeviceID id = new CANDeviceID(3);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertSame(KRAKEN, controller.getMotor());
    }

    /* applyConfig */

    @Test
    void applyConfigDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(4);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(controller::applyConfig);
    }

    /* Neutral Mode */

    @Test
    void setNeutralModeBrake() {
        CANDeviceID id = new CANDeviceID(5);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setNeutralMode(NeutralState.BRAKE);
        assertEquals(NeutralState.BRAKE, controller.getNeutralMode());
    }

    @Test
    void setNeutralModeCoast() {
        CANDeviceID id = new CANDeviceID(6);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setNeutralMode(NeutralState.COAST);
        assertEquals(NeutralState.COAST, controller.getNeutralMode());
    }

    /* Direction */

    @Test
    void setDirectionCW() {
        CANDeviceID id = new CANDeviceID(7);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setDirection(MotorDirection.CW);
        assertEquals(MotorDirection.CW, controller.getDirection());
    }

    @Test
    void setDirectionCCW() {
        CANDeviceID id = new CANDeviceID(8);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setDirection(MotorDirection.CCW);
        assertEquals(MotorDirection.CCW, controller.getDirection());
    }

    /* Duty Cycle */

    @Test
    void setOutputDutyDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(9);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setOutputDuty(0.5));
    }

    @Test
    void getOutputDutyReturnsZeroBeforeSignalInit() {
        CANDeviceID id = new CANDeviceID(10);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(0.0, controller.getOutputDuty(), 1e-6);
    }

    /* Voltage */

    @Test
    void setOutputVoltageDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(11);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setOutputVoltage(6.0));
    }

    @Test
    void getOutputVoltageReturnsZeroBeforeSignalInit() {
        CANDeviceID id = new CANDeviceID(12);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(0.0, controller.getOutputVoltage(), 1e-6);
    }

    /* Follower Constructor */

    @Test
    void followerConstructorSameBus() {
        CANDeviceID leaderId = new CANDeviceID(13);
        CANDeviceID followerId = new CANDeviceID(14);
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower =
                new ControllerTalonFX(null, "Follower", followerId, KRAKEN, leader, false);

        assertNotNull(follower.getTalonFX());
        assertEquals(leaderId, follower.getFollowingID());
    }

    @Test
    void followerConstructorOpposeLeader() {
        CANDeviceID leaderId = new CANDeviceID(15);
        CANDeviceID followerId = new CANDeviceID(16);
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower =
                new ControllerTalonFX(null, "Follower", followerId, KRAKEN, leader, true);

        assertTrue(follower.getOpposeLeader());
    }

    @Test
    void followDifferentBusReturnsError() {
        CANDeviceID leaderId = new CANDeviceID(17, "canivore");
        CANDeviceID followerId = new CANDeviceID(18, "rio");
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower = new ControllerTalonFX(null, "Follower", followerId, KRAKEN);

        ControllerStatus status = follower.follow(leader, false);
        assertEquals(ControllerStatus.ERROR_INVALID_BUS, status);
    }

    /* setControlUpdateFrequency */

    @Test
    void setControlUpdateFrequencyWithTimesync() {
        CANDeviceID id = new CANDeviceID(19);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setControlUpdateFrequency(250.0, true));
        assertEquals(250.0, controller.getConfig().MotorOutput.ControlTimesyncFreqHz, 1e-6);
    }

    @Test
    void setControlUpdateFrequencyWithoutTimesync() {
        CANDeviceID id = new CANDeviceID(50);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setControlUpdateFrequency(100.0, false));
        assertEquals(0.0, controller.getConfig().MotorOutput.ControlTimesyncFreqHz, 1e-6);
    }

    /* Simulation Methods */

    @Test
    void setSimVelRotationsDoesNotThrowWithoutSimState() {
        CANDeviceID id = new CANDeviceID(51);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setSimVelRotations(100.0));
    }

    @Test
    void setSimPosRotationsDoesNotThrowWithoutSimState() {
        CANDeviceID id = new CANDeviceID(52);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setSimPosRotations(5.0));
    }

    @Test
    void simulationInitCreatesSimState() {
        CANDeviceID id = new CANDeviceID(53);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertNull(controller.getSimState());
        controller.simulationInit(0.0);
        assertNotNull(controller.getSimState());
    }

    @Test
    void setSimMethodsWorkAfterSimInit() {
        CANDeviceID id = new CANDeviceID(54);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.simulationInit(0.0);
        assertDoesNotThrow(() -> controller.setSimVelRotations(10.0));
        assertDoesNotThrow(() -> controller.setSimPosRotations(2.5));
    }

    /* Current Limits */

    @Test
    void setCurrentLimitStator() {
        CANDeviceID id = new CANDeviceID(55);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setCurrentLimitStator(true, 40.0);
        assertTrue(controller.getCurrentLimitStatorEnable());
        assertEquals(40.0, controller.getCurrentLimitStator(), 1e-6);
    }

    @Test
    void setCurrentLimitSupply() {
        CANDeviceID id = new CANDeviceID(56);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setCurrentLimitSupply(true, 30.0);
        assertTrue(controller.getCurrentLimitSupplyEnable());
        assertEquals(30.0, controller.getCurrentLimitSupply(), 1e-6);
    }

    /* PID Slots */

    @Test
    void pidSlot0() {
        CANDeviceID id = new CANDeviceID(57);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setPSlot(0, 1.0);
        controller.setISlot(0, 0.1);
        controller.setDSlot(0, 0.01);

        assertEquals(1.0, controller.getPSlot(0), 1e-6);
        assertEquals(0.1, controller.getISlot(0), 1e-6);
        assertEquals(0.01, controller.getDSlot(0), 1e-6);
    }

    @Test
    void pidSlot1() {
        CANDeviceID id = new CANDeviceID(58);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setPSlot(1, 2.0);
        assertEquals(2.0, controller.getPSlot(1), 1e-6);
    }

    @Test
    void pidSlot2() {
        CANDeviceID id = new CANDeviceID(59);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setPSlot(2, 3.0);
        assertEquals(3.0, controller.getPSlot(2), 1e-6);
    }

    @Test
    void pidInvalidSlotReturnsZero() {
        CANDeviceID id = new CANDeviceID(60);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(0.0, controller.getPSlot(5), 1e-6);
    }

    @Test
    void setPIDFSlot() {
        CANDeviceID id = new CANDeviceID(61);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setPIDFSlot(0, 1.0, 0.5, 0.25, 0.1, 0.05);
        assertEquals(1.0, controller.getPSlot(0), 1e-6);
        assertEquals(0.5, controller.getISlot(0), 1e-6);
        assertEquals(0.25, controller.getDSlot(0), 1e-6);
    }

    /* Voltage Peak */

    @Test
    void setVoltagePeak() {
        CANDeviceID id = new CANDeviceID(62);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setVoltagePeak(12.0, -12.0, 0.01);
        assertEquals(12.0, controller.getVoltagePeakFwd(), 1e-6);
        assertEquals(-12.0, controller.getVoltagePeakRev(), 1e-6);
        assertEquals(0.01, controller.getVoltagePeakTime(), 1e-6);
    }

    /* Ramp Rates */

    @Test
    void setRampRates() {
        CANDeviceID id = new CANDeviceID(63);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setRampOpenLoopDuty(0.5);
        assertEquals(0.5, controller.getRampOpenLoopDuty(), 1e-6);

        controller.setRampOpenLoopVoltage(0.3);
        assertEquals(0.3, controller.getRampOpenLoopVoltage(), 1e-6);

        controller.setRampClosedLoopDuty(0.2);
        assertEquals(0.2, controller.getRampClosedLoopDuty(), 1e-6);

        controller.setRampClosedLoopVoltage(0.1);
        assertEquals(0.1, controller.getRampClosedLoopVoltage(), 1e-6);
    }

    /* Closed Loop */

    @Test
    void closedLoopReturnsZeroBeforeSignalInit() {
        CANDeviceID id = new CANDeviceID(64);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(0.0, controller.getCLError(), 1e-6);
        assertEquals(0.0, controller.getCLOutput(), 1e-6);
    }

    /* Stop */

    @Test
    void stopDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(65);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(controller::stop);
    }

    /* outputTelemetry */

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(66);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(controller::outputTelemetry);
    }

    /* robotInit */

    @Test
    void robotInitDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(67);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.robotInit(0.0));
    }

    /* Device ID */

    @Test
    void deviceIdAccessors() {
        CANDeviceID id = new CANDeviceID(68, "canivore");
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(68, controller.getIDNum());
        assertEquals("canivore", controller.getBus());
        assertTrue(controller.isDevice(id));
    }

    /* --- ControllerBase: getControllerStatus / follow on non-follower --- */

    @Test
    void getFollowingIDNullForNonFollower() {
        CANDeviceID id = new CANDeviceID(69);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertNull(controller.getFollowingID());
    }

    @Test
    void getFollowingIDNumReturnsNegativeOneForNonFollower() {
        CANDeviceID id = new CANDeviceID(70);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(-1, controller.getFollowingIDNum());
    }

    @Test
    void getFollowingBusReturnsEmptyForNonFollower() {
        CANDeviceID id = new CANDeviceID(71);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals("", controller.getFollowingBus());
    }

    @Test
    void getOpposeLeaderFalseByDefault() {
        CANDeviceID id = new CANDeviceID(72);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertFalse(controller.getOpposeLeader());
    }

    @Test
    void isConnectedAccessible() {
        CANDeviceID id = new CANDeviceID(73);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        // In sim, isConnected may be true or false depending on HAL state
        assertDoesNotThrow(controller::isConnected);
    }

    @Test
    void isConfiguredFalseBeforeApply() {
        CANDeviceID id = new CANDeviceID(74);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertFalse(controller.isConfigured());
    }

    @Test
    void getIDReturnsDeviceID() {
        CANDeviceID id = new CANDeviceID(75);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(id, controller.getID());
    }

    /* --- robotPeriodicBefore --- */

    @Test
    void robotPeriodicBeforeDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(76);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.robotPeriodicBefore(0.0));
    }

    /* --- robotPeriodicAfter (base class, no-op) --- */

    @Test
    void robotPeriodicAfterDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(77);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.robotPeriodicAfter(0.0));
    }

    /* --- Torque Current output --- */

    @Test
    void setOutputTorqueCurrentDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(78);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setOutputTorqueCurrent(10.0));
    }

    @Test
    void getOutputTorqueCurrentReturnsZeroBeforeSignalInit() {
        CANDeviceID id = new CANDeviceID(79);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(0.0, controller.getOutputTorqueCurrent(), 1e-6);
    }

    /* --- Position / Velocity control --- */

    @Test
    void setOutputPositionDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(80);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setOutputPosition(5.0, 0.5));
    }

    @Test
    void setOutputVelocityDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(81);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setOutputVelocity(10.0, 0.2));
    }

    /* --- Torque ramp rates --- */

    @Test
    void setRampOpenLoopTorque() {
        CANDeviceID id = new CANDeviceID(82);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setRampOpenLoopTorque(0.4);
        assertEquals(0.4, controller.getRampOpenLoopTorque(), 1e-6);
    }

    @Test
    void setRampClosedLoopTorque() {
        CANDeviceID id = new CANDeviceID(83);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setRampClosedLoopTorque(0.3);
        assertEquals(0.3, controller.getRampClosedLoopTorque(), 1e-6);
    }

    /* --- Tolerance --- */

    @Test
    void setToleranceDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(84);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setTolerance(0.5));
        assertEquals(0.0, controller.getTolerance(), 1e-6);
    }

    /* --- Supply current limit (3-arg variant) --- */

    @Test
    void setCurrentLimitSupplyThreeArg() {
        CANDeviceID id = new CANDeviceID(85);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setCurrentLimitSupply(60.0, 1.0, 40.0);
        assertEquals(60.0, controller.getCurrentLimitSupply(), 1e-6);
        assertEquals(1.0, controller.getCurrentLimitSupplyTime(), 1e-6);
        assertEquals(40.0, controller.getCurrentLimitSupplyLowerLimit(), 1e-6);
    }

    /* --- getConfigMM --- */

    @Test
    void getConfigMMNotNull() {
        CANDeviceID id = new CANDeviceID(86);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertNotNull(controller.getConfigMM());
    }

    /* --- Follow same bus succeeds --- */

    @Test
    void followSameBusReturnsOK() {
        CANDeviceID leaderId = new CANDeviceID(87);
        CANDeviceID followerId = new CANDeviceID(88);
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower = new ControllerTalonFX(null, "Follower", followerId, KRAKEN);

        ControllerStatus status = follower.follow(leader, false);
        assertEquals(ControllerStatus.OK, status);
        assertEquals(leaderId, follower.getFollowingID());
        assertEquals(87, follower.getFollowingIDNum());
        assertNotNull(follower.getFollowingBus());
    }

    /* --- simulationPeriodic --- */

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(89);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.simulationInit(0.0);
        assertDoesNotThrow(() -> controller.simulationPeriodic(0.0));
    }

    @Test
    void simulationPeriodicWithoutSimInitDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(90);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.simulationPeriodic(0.0));
    }

    /* --- PID slot I and D for slots 1 and 2 --- */

    @Test
    void pidSlot1IAndD() {
        CANDeviceID id = new CANDeviceID(91);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setISlot(1, 0.2);
        controller.setDSlot(1, 0.02);
        assertEquals(0.2, controller.getISlot(1), 1e-6);
        assertEquals(0.02, controller.getDSlot(1), 1e-6);
    }

    @Test
    void pidSlot2IAndD() {
        CANDeviceID id = new CANDeviceID(92);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setISlot(2, 0.3);
        controller.setDSlot(2, 0.03);
        assertEquals(0.3, controller.getISlot(2), 1e-6);
        assertEquals(0.03, controller.getDSlot(2), 1e-6);
    }

    @Test
    void pidInvalidSlotIAndDReturnsZero() {
        CANDeviceID id = new CANDeviceID(93);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setISlot(5, 1.0);
        controller.setDSlot(5, 1.0);
        assertEquals(0.0, controller.getISlot(5), 1e-6);
        assertEquals(0.0, controller.getDSlot(5), 1e-6);
    }

    /* --- setPIDFSlot for slot 1 and 2 --- */

    @Test
    void setPIDFSlotSlot1() {
        CANDeviceID id = new CANDeviceID(94);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setPIDFSlot(1, 1.0, 0.5, 0.25, 0.1, 0.05);
        assertEquals(1.0, controller.getPSlot(1), 1e-6);
        assertEquals(0.5, controller.getISlot(1), 1e-6);
        assertEquals(0.25, controller.getDSlot(1), 1e-6);
    }

    @Test
    void setPIDFSlotSlot2() {
        CANDeviceID id = new CANDeviceID(95);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setPIDFSlot(2, 3.0, 1.5, 0.75, 0.3, 0.15);
        assertEquals(3.0, controller.getPSlot(2), 1e-6);
        assertEquals(1.5, controller.getISlot(2), 1e-6);
        assertEquals(0.75, controller.getDSlot(2), 1e-6);
    }

    @Test
    void setPIDFSlotInvalidSlotDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(96);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.setPIDFSlot(5, 1.0, 0.5, 0.25, 0.1, 0.05));
    }

    /* --- Direction with simState --- */

    @Test
    void setDirectionCWWithSimState() {
        CANDeviceID id = new CANDeviceID(97);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);
        controller.simulationInit(0.0);

        controller.setDirection(MotorDirection.CW);
        assertEquals(MotorDirection.CW, controller.getDirection());
    }

    @Test
    void setDirectionCCWWithSimState() {
        CANDeviceID id = new CANDeviceID(98);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);
        controller.simulationInit(0.0);

        controller.setDirection(MotorDirection.CCW);
        assertEquals(MotorDirection.CCW, controller.getDirection());
    }

    /* --- Follow with simState initialized --- */

    @Test
    void followWithSimStateOpposeLeader() {
        CANDeviceID leaderId = new CANDeviceID(200);
        CANDeviceID followerId = new CANDeviceID(201);
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower = new ControllerTalonFX(null, "Follower", followerId, KRAKEN);

        follower.simulationInit(0.0);
        ControllerStatus status = follower.follow(leader, true);
        assertEquals(ControllerStatus.OK, status);
        assertTrue(follower.getOpposeLeader());
    }

    @Test
    void followWithSimStateAligned() {
        CANDeviceID leaderId = new CANDeviceID(202);
        CANDeviceID followerId = new CANDeviceID(203);
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower = new ControllerTalonFX(null, "Follower", followerId, KRAKEN);

        follower.simulationInit(0.0);
        ControllerStatus status = follower.follow(leader, false);
        assertEquals(ControllerStatus.OK, status);
        assertFalse(follower.getOpposeLeader());
    }

    /* Slot Assignment Regression */

    @Test
    void setOutputPositionSetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(300);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setOutputPosition(5.0, 0.5);

        Field posField = ControllerTalonFX.class.getDeclaredField("motorPosition");
        posField.setAccessible(true);
        Object pos = posField.get(controller);
        Field slotField = pos.getClass().getField("Slot");
        assertEquals(0, slotField.getInt(pos));
    }

    @Test
    void setOutputVelocitySetsSlotToZero() throws Exception {
        CANDeviceID id = new CANDeviceID(301);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        controller.setOutputVelocity(10.0, 0.2);

        Field velField = ControllerTalonFX.class.getDeclaredField("motorVelocity");
        velField.setAccessible(true);
        Object vel = velField.get(controller);
        Field slotField = vel.getClass().getField("Slot");
        assertEquals(0, slotField.getInt(vel));
    }

    /* ========== Coverage: ControllerBase branches ========== */

    @Test
    void isDeviceReturnsTrueForMatchingId() {
        CANDeviceID id = new CANDeviceID(400);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);
        assertTrue(controller.isDevice(id));
    }

    @Test
    void isDeviceReturnsFalseForDifferentId() {
        CANDeviceID id = new CANDeviceID(401);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);
        assertFalse(controller.isDevice(new CANDeviceID(999)));
    }

    @Test
    void getFollowingIDNumReturnsNegativeOneWhenNull() {
        CANDeviceID id = new CANDeviceID(402);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);
        assertEquals(-1, controller.getFollowingIDNum());
    }

    @Test
    void getFollowingBusReturnsEmptyWhenNull() {
        CANDeviceID id = new CANDeviceID(403);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);
        assertEquals("", controller.getFollowingBus());
    }

    @Test
    void getFollowingIDNumAfterFollow() {
        CANDeviceID leaderId = new CANDeviceID(404);
        CANDeviceID followerId = new CANDeviceID(405);
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower = new ControllerTalonFX(null, "Follower", followerId, KRAKEN);

        follower.follow(leader, false);
        assertEquals(leaderId.getDeviceNumber(), follower.getFollowingIDNum());
        assertEquals(leaderId.getBus(), follower.getFollowingBus());
    }

    @Test
    void outputTelemetryWithFollowerExercisesFollowingBranch() {
        CANDeviceID leaderId = new CANDeviceID(406);
        CANDeviceID followerId = new CANDeviceID(407);
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower = new ControllerTalonFX(null, "Follower", followerId, KRAKEN);

        follower.follow(leader, true);
        assertDoesNotThrow(follower::outputTelemetry);
    }

    @Test
    void robotInitRegistersSignals() {
        CANDeviceID id = new CANDeviceID(408);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertDoesNotThrow(() -> controller.robotInit(0.0));
    }

    @Test
    void outputTelemetryAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(409);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);
        controller.robotInit(0.0);

        assertDoesNotThrow(controller::outputTelemetry);
    }

    @Test
    void applyConfigReturnsErrorInSim() {
        CANDeviceID id = new CANDeviceID(410);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        ControllerStatus result = controller.applyConfig();
        assertEquals(ControllerStatus.ERROR, result);
        assertFalse(controller.isConfigured());
    }

    @Test
    void getSupplyVoltageReturnsZeroBeforeInit() {
        CANDeviceID id = new CANDeviceID(411);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(0.0, controller.getSupplyVoltage(), 1e-6);
    }

    @Test
    void getSupplyCurrentReturnsZeroBeforeInit() {
        CANDeviceID id = new CANDeviceID(412);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertEquals(0.0, controller.getSupplyCurrent(), 1e-6);
    }

    @Test
    void getFollowingIDReturnsNullBeforeFollow() {
        CANDeviceID id = new CANDeviceID(413);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Test", id, KRAKEN);

        assertNull(controller.getFollowingID());
    }

    @Test
    void getFollowingIDReturnsLeaderIdAfterFollow() {
        CANDeviceID leaderId = new CANDeviceID(414);
        CANDeviceID followerId = new CANDeviceID(415);
        ControllerTalonFX leader = new ControllerTalonFX(null, "Leader", leaderId, KRAKEN);
        ControllerTalonFX follower = new ControllerTalonFX(null, "Follower", followerId, KRAKEN);

        follower.follow(leader, false);
        assertEquals(leaderId, follower.getFollowingID());
    }
}
