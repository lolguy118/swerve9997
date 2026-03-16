package com.team271.lib;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TRobotTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @Test
    void constructorCreatesInstance() {
        TRobot robot = new TRobot();

        assertNotNull(robot);
    }

    @Test
    void getNameReturnsRobot() {
        TRobot robot = new TRobot();

        assertEquals("Robot", robot.getName());
    }

    @Test
    void getTableIsNotNull() {
        TRobot robot = new TRobot();

        assertNotNull(robot.getTable());
    }

    @Test
    void tablePathIsSlashRobot() {
        TRobot robot = new TRobot();

        assertEquals("/Robot", robot.getTable().getPath());
    }
}
