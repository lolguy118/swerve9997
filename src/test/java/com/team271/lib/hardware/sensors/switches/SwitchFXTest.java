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

class SwitchFXTest {

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
        CANDeviceID id = new CANDeviceID(75);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertNotNull(sw);
    }

    @Test
    void constructorRevLimitNC() {
        CANDeviceID id = new CANDeviceID(76);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NC, true, 1.0, 250.0);

        assertNotNull(sw);
    }

    @Test
    void setEnabledFwd() {
        CANDeviceID id = new CANDeviceID(77);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setEnabled(false));
        assertFalse(controller.getConfig().HardwareLimitSwitch.ForwardLimitEnable);
    }

    @Test
    void setEnabledRev() {
        CANDeviceID id = new CANDeviceID(78);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setEnabled(false));
        assertFalse(controller.getConfig().HardwareLimitSwitch.ReverseLimitEnable);
    }

    @Test
    void setTriggerTypeFwdNC() {
        CANDeviceID id = new CANDeviceID(79);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setTriggerType(SwitchTrigger.NC));
    }

    @Test
    void setTriggerTypeRevNO() {
        CANDeviceID id = new CANDeviceID(80);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NC, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setTriggerType(SwitchTrigger.NO));
    }

    @Test
    void getTriggeredReturnsFalseBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(81);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertFalse(sw.getTriggered());
    }

    @Test
    void getTriggeredRevReturnsFalseBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(82);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        assertFalse(sw.getTriggered());
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(83);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(sw::outputTelemetry);
    }

    @Test
    void autoSetConfiguredByConstructor() {
        CANDeviceID id = new CANDeviceID(84);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, true, 5.0, 250.0);

        assertTrue(sw.getAutoSet());
        assertEquals(5.0, sw.getAutoSetPos(), 1e-6);
    }

    @Test
    void robotInitRegistersSignals() {
        CANDeviceID id = new CANDeviceID(85);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.robotInit(0.0));
    }

    /* ========== Additional coverage tests ========== */

    @Test
    void setAutoSetFwd() {
        CANDeviceID id = new CANDeviceID(86);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setAutoSet(true);
        assertTrue(sw.getAutoSet());
        sw.setAutoSet(false);
        assertFalse(sw.getAutoSet());
    }

    @Test
    void setAutoSetPosFwd() {
        CANDeviceID id = new CANDeviceID(87);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setAutoSetPos(10.0);
        assertEquals(10.0, sw.getAutoSetPos(), 1e-6);
    }

    @Test
    void setAutoSetRev() {
        CANDeviceID id = new CANDeviceID(88);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setAutoSet(true);
        assertTrue(sw.getAutoSet());
    }

    @Test
    void setAutoSetPosRev() {
        CANDeviceID id = new CANDeviceID(89);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setAutoSetPos(3.0);
        assertEquals(3.0, sw.getAutoSetPos(), 1e-6);
    }

    @Test
    void setEnabledTrueFwd() {
        CANDeviceID id = new CANDeviceID(90);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setEnabled(true);
        assertTrue(controller.getConfig().HardwareLimitSwitch.ForwardLimitEnable);
    }

    @Test
    void setEnabledTrueRev() {
        CANDeviceID id = new CANDeviceID(91);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.setEnabled(true);
        assertTrue(controller.getConfig().HardwareLimitSwitch.ReverseLimitEnable);
    }

    @Test
    void robotInitRevRegistersSignals() {
        CANDeviceID id = new CANDeviceID(92);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.robotInit(0.0));
    }

    @Test
    void getTriggeredFwdAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(93);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.robotInit(0.0);
        assertFalse(sw.getTriggered());
    }

    @Test
    void getTriggeredRevAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(94);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.robotInit(0.0);
        assertFalse(sw.getTriggered());
    }

    @Test
    void outputTelemetryAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(95);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        sw.robotInit(0.0);
        assertDoesNotThrow(sw::outputTelemetry);
    }

    @Test
    void autoZeroDisabledByDefault() {
        CANDeviceID id = new CANDeviceID(96);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchFX sw =
                new SwitchFX(null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertFalse(sw.getAutoSet(), "Auto-zero should be disabled by default (safe default)");
    }
}
