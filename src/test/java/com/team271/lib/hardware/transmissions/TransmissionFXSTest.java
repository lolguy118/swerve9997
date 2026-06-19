package com.team271.lib.hardware.transmissions;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerBase.NeutralState;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransmissionFXSTest {

    private static final MotorBase MINION = new MotorBase(MotorBase.MotorType.CTRE_MINION);

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
         * Closing devices unregisters their Phoenix 6 sim state; leaving them open across tests can
         * SIGSEGV the JVM when the sim library dereferences freed handles.
         */
        CTREManager.resetForTesting();
    }

    /* Constructor */

    @Test
    void constructorSingleMotor() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(10));

        assertNotNull(tx);
        assertNotNull(tx.getLeader());
        assertNotNull(tx.getLeaderController());
        assertEquals(1, tx.getAllControllers().size());
    }

    /* ADR-019: variable-arity followers */

    @Test
    void addFollowerRegistersWithAllControllers() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(11));

        tx.addFollower(new CANDeviceID(12), false);

        assertEquals(2, tx.getAllControllers().size());
    }

    @Test
    void addFollowerSupportsMoreThanFourMotors() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(13));

        /* leader + 5 followers = 6 motors, exceeding the legacy 4-motor cap */
        tx.addFollower(new CANDeviceID(14), false);
        tx.addFollower(new CANDeviceID(15), true);
        tx.addFollower(new CANDeviceID(16), false);
        tx.addFollower(new CANDeviceID(17), true);
        tx.addFollower(new CANDeviceID(18), false);

        assertEquals(6, tx.getAllControllers().size());
    }

    @Test
    void addFollowerNullIdThrows() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(19));

        assertThrows(IllegalArgumentException.class, () -> tx.addFollower(null, false));
    }

    @Test
    void addFollowerDuplicateIdThrows() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(20));

        /* colliding with the leader's own CAN ID must be rejected (no duplicate device) */
        assertThrows(
                IllegalArgumentException.class, () -> tx.addFollower(new CANDeviceID(20), false));
    }

    /* Inherited base behavior */

    @Test
    void setNeutralModeBrake() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(21));

        tx.setNeutralMode(NeutralState.BRAKE);
        assertEquals(NeutralState.BRAKE, tx.getNeutralMode());
    }

    @Test
    void robotInitDoesNotThrow() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(22));

        assertDoesNotThrow(() -> tx.robotInit(0.0));
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(23));

        assertDoesNotThrow(tx::outputTelemetry);
    }

    @Test
    void stopDoesNotThrow() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(24));

        assertDoesNotThrow(tx::stop);
    }

    @Test
    void setOutputDutyDoesNotThrow() {
        TransmissionFXS tx = new TransmissionFXS(null, "TX", MINION, new CANDeviceID(25));

        assertDoesNotThrow(() -> tx.setOutputDuty(0.5));
    }
}
