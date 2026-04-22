package com.team271.lib.hardware.sensors.encoders;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncoderFXTest {

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
    void constructorWithController() {
        CANDeviceID id = new CANDeviceID(20);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        assertNotNull(encoder);
    }

    @Test
    void getPositionReturnsZeroBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(21);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        assertEquals(0.0, encoder.getPosRotations(), 1e-6);
    }

    @Test
    void getVelocityReturnsZeroBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(22);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        assertEquals(0.0, encoder.getVelRPS(), 1e-6);
    }

    @Test
    void robotInitRegistersSignals() {
        CANDeviceID id = new CANDeviceID(23);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        assertDoesNotThrow(() -> encoder.robotInit(0.0));
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(24);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        assertDoesNotThrow(encoder::outputTelemetry);
    }

    @Test
    void resetSetsPositionToZero() {
        CANDeviceID id = new CANDeviceID(25);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        encoder.reset();
        assertEquals(0.0, encoder.getPosRotations(), 1e-6);
    }

    @Test
    void refreshDoesNotThrowAfterRobotInit() {
        CANDeviceID id = new CANDeviceID(26);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        encoder.robotInit(0.0);
        assertDoesNotThrow(encoder::refresh);
    }

    @Test
    void simMethodsDoNotThrowWithoutSimState() {
        CANDeviceID id = new CANDeviceID(27);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        assertDoesNotThrow(() -> encoder.setSimVelRotations(10.0));
        assertDoesNotThrow(() -> encoder.setSimPosRotations(5.0));
    }

    /* ========== Additional coverage tests ========== */

    @Test
    void refreshBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(28);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        assertDoesNotThrow(encoder::refresh);
    }

    @Test
    void refreshAfterRobotInitReturnsZero() {
        CANDeviceID id = new CANDeviceID(29);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);
        encoder.robotInit(0.0);
        encoder.refresh();

        assertEquals(0.0, encoder.getPosRotations(), 1e-9);
        assertEquals(0.0, encoder.getVelRPS(), 1e-9);
    }

    @Test
    void simulationInitDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(30);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);

        assertDoesNotThrow(() -> encoder.simulationInit(0.0));
    }

    @Test
    void simulationPeriodicDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(31);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);
        encoder.simulationInit(0.0);

        assertDoesNotThrow(() -> encoder.simulationPeriodic(0.0));
    }

    @Test
    void setPosRotationsUpdatesPosition() {
        CANDeviceID id = new CANDeviceID(32);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        EncoderFX encoder = new EncoderFX(null, "Enc", controller, 250.0);
        encoder.setPosRotations(2.5);
        assertEquals(2.5, encoder.getPosRotations(), 1e-9);
    }
}
