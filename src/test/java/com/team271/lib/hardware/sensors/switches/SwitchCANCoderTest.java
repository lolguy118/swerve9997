package com.team271.lib.hardware.sensors.switches;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchTrigger;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SwitchCANCoderTest {

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
        CTREManager.resetForTesting();
    }

    @Test
    void constructorFwdLimitNO() {
        CANDeviceID id = new CANDeviceID(24);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertNotNull(sw);
    }

    @Test
    void constructorRevLimitNC() {
        CANDeviceID id = new CANDeviceID(25);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NC, true, 1.0, 250.0);

        assertNotNull(sw);
    }

    @Test
    void setEnabledFwd() {
        CANDeviceID id = new CANDeviceID(26);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setEnabled(false));
        assertFalse(controller.getConfig().HardwareLimitSwitch.ForwardLimitEnable);
    }

    @Test
    void setEnabledRev() {
        CANDeviceID id = new CANDeviceID(27);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setEnabled(false));
        assertFalse(controller.getConfig().HardwareLimitSwitch.ReverseLimitEnable);
    }

    @Test
    void setTriggerTypeFwdNC() {
        CANDeviceID id = new CANDeviceID(28);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setTriggerType(SwitchTrigger.NC));
    }

    @Test
    void getTriggeredReturnsFalseBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(29);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertFalse(sw.getTriggered());
    }

    @Test
    void getTriggeredRevReturnsFalseBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(30);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        assertFalse(sw.getTriggered());
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(31);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(sw::outputTelemetry);
    }

    @Test
    void autoSetConfiguredByConstructor() {
        CANDeviceID id = new CANDeviceID(32);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, true, 5.0, 250.0);

        assertTrue(sw.getAutoSet());
        assertEquals(5.0, sw.getAutoSetPos(), 1e-6);
    }

    @Test
    void robotInitRegistersSignals() {
        CANDeviceID id = new CANDeviceID(33);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.robotInit(0.0));
    }

    /* ========== Additional coverage tests ========== */

    @Test
    void setAutoSetFwd() {
        CANDeviceID id = new CANDeviceID(34);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setAutoSet(true);
        assertTrue(sw.getAutoSet());
    }

    @Test
    void setAutoSetPosFwd() {
        CANDeviceID id = new CANDeviceID(35);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setAutoSetPos(7.5);
        assertEquals(7.5, sw.getAutoSetPos(), 1e-6);
    }

    @Test
    void setEnabledTrueFwd() {
        CANDeviceID id = new CANDeviceID(36);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setEnabled(true);
        assertTrue(controller.getConfig().HardwareLimitSwitch.ForwardLimitEnable);
    }

    @Test
    void setEnabledTrueRev() {
        CANDeviceID id = new CANDeviceID(37);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setEnabled(true);
        assertTrue(controller.getConfig().HardwareLimitSwitch.ReverseLimitEnable);
    }

    @Test
    void robotInitRevRegistersSignals() {
        CANDeviceID id = new CANDeviceID(14);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.robotInit(0.0));
    }

    @Test
    void getTriggeredFwdAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(15);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.robotInit(0.0);
        assertFalse(sw.getTriggered());
    }

    @Test
    void getTriggeredRevAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(16);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.robotInit(0.0);
        assertFalse(sw.getTriggered());
    }

    @Test
    void outputTelemetryAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(17);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.robotInit(0.0);
        assertDoesNotThrow(sw::outputTelemetry);
    }
}
