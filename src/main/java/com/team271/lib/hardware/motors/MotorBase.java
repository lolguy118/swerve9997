package com.team271.lib.hardware.motors;

import static edu.wpi.first.units.Units.*;

import com.team271.lib.ConstantsLib;
import edu.wpi.first.math.system.plant.DCMotor;

public class MotorBase {
    public enum MotorType {
        FALCON500,
        KRAKENX60,
        KRAKENX44,
        CTRE_MINION,
        NEO,
        NEO550,
        NEO_VORTEX
    }

    public enum MotorControlType {
        BRUSHED,
        BRUSHLESS
    }

    protected final DCMotor motor;
    protected final MotorType motorType;
    protected final MotorControlType motorControlType;

    /*
     *
     * Constructors
     *
     */
    public MotorBase(final MotorType argMotorType) {
        /*
         * Store Motor Type
         */
        motorType = argMotorType;

        switch (motorType) {
            case FALCON500:
                motor = DCMotor.getFalcon500Foc(1);

                motorControlType = MotorControlType.BRUSHLESS;
                break;

            case KRAKENX60:
                motor = DCMotor.getKrakenX60Foc(1);

                motorControlType = MotorControlType.BRUSHLESS;
                break;

            case KRAKENX44:
                motor = new DCMotor(12.0, 4.05, 275.0, 1.4, RPM.of(7530).in(RadiansPerSecond), 1);

                motorControlType = MotorControlType.BRUSHLESS;
                break;

            case CTRE_MINION:
                motor = new DCMotor(0.0, 0.0, 0.0, 0.0, RPM.of(0).in(RadiansPerSecond), 0);

                motorControlType = MotorControlType.BRUSHLESS;
                break;

            case NEO:
                motor = DCMotor.getNEO(1);

                motorControlType = MotorControlType.BRUSHLESS;
                break;

            case NEO550:
                motor = DCMotor.getNeo550(1);

                motorControlType = MotorControlType.BRUSHLESS;
                break;

            case NEO_VORTEX:
                motor = DCMotor.getNeoVortex(1);

                motorControlType = MotorControlType.BRUSHLESS;
                break;

            default:
                motor = new DCMotor(0.0, 0.0, 0.0, 0.0, RPM.of(0).in(RadiansPerSecond), 0);

                motorControlType = MotorControlType.BRUSHLESS;
        }
    }

    public MotorType getMotorType() {
        return motorType;
    }

    public String getMotorName() {
        switch (motorType) {
            case FALCON500:
                return "Falcon 500";
            case KRAKENX60:
                return "Kraken X60";
            case KRAKENX44:
                return "Kraken X44";
            case CTRE_MINION:
                return "CTRE Minion";
            case NEO:
                return "Neo";
            case NEO550:
                return "Neo 550";
            case NEO_VORTEX:
                return "Neo Vortex";
            default:
                return ConstantsLib.S_INVALID;
        }
    }

    public MotorControlType getControlType() {
        return motorControlType;
    }

    public String getControlName() {
        switch (motorControlType) {
            case BRUSHLESS:
                return "Brushless";
            case BRUSHED:
                return "Brushed";
            default:
                return ConstantsLib.S_INVALID;
        }
    }

    public double getFreeSpeed() {
        return RadiansPerSecond.of(motor.freeSpeedRadPerSec).in(RPM);
    }
}
