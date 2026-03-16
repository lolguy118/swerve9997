package com.team271.lib.hardware.transmissions;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.transmissions.TransmissionBase.ShifterState;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ShifterPneumaticTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    /* --- Constructors --- */

    @Test
    void twoArgConstructorDoesNotThrow() {
        assertDoesNotThrow(() -> new ShifterPneumatic(0, 1));
    }

    @Test
    void threeArgConstructorDoesNotThrow() {
        assertDoesNotThrow(() -> new ShifterPneumatic(1, 4, 5));
    }

    /* --- Actuate --- */

    @Test
    void actuateGear1DoesNotThrow() {
        ShifterPneumatic shifter = new ShifterPneumatic(1, 6, 7);

        assertDoesNotThrow(() -> shifter.actuate(ShifterState.GEAR_1));
    }

    @Test
    void actuateGear2DoesNotThrow() {
        ShifterPneumatic shifter = new ShifterPneumatic(1, 8, 9);

        assertDoesNotThrow(() -> shifter.actuate(ShifterState.GEAR_2));
    }

    @Test
    void actuateGearNoneDoesNotThrow() {
        ShifterPneumatic shifter = new ShifterPneumatic(1, 10, 11);

        assertDoesNotThrow(() -> shifter.actuate(ShifterState.GEAR_NONE));
    }
}
