package com.team271.lib.control;

import com.team271.lib.nt.LoggedNTInput;
import com.team271.lib.nt.NTTable;
import org.littletonrobotics.junction.Logger;

public class Balance {
    private static final int STATE_APPROACH = 0;
    private static final int STATE_CLIMB = 1;
    private static final int STATE_HOLD = 2;
    private static final int STATE_DONE = 3;

    /** Small correction output applied during the HOLD state to fine-tune balance. */
    private static final double HOLD_CORRECTION_OUTPUT = 0.1;

    private final boolean isFwd;
    private int state;
    private int debounceCount;
    private double robotSpeedSlow;
    private double robotSpeedFast;
    private double onChargeStationDegree;
    private double levelDegree;
    private double debounceTime;

    private final NTTable table = new NTTable("Balance");

    private final LoggedNTInput tuneSpeedSlow;
    private final LoggedNTInput tuneSpeedFast;
    private final LoggedNTInput tuneOnChargeDeg;
    private final LoggedNTInput tuneLevelDeg;
    private final LoggedNTInput tuneDebounceTime;

    public Balance(final boolean argIsFwd) {
        isFwd = argIsFwd;

        tuneSpeedSlow = new LoggedNTInput(table, "Tune Speed Slow", 0.2);
        tuneSpeedFast = new LoggedNTInput(table, "Tune Speed Fast", 0.6);
        tuneOnChargeDeg = new LoggedNTInput(table, "Tune On Charge Deg", 13.0);
        tuneLevelDeg = new LoggedNTInput(table, "Tune Level Deg", 6.0);
        tuneDebounceTime = new LoggedNTInput(table, "Tune Debounce Time", 0.1);
    }

    public void init() {
        state = STATE_APPROACH;
        debounceCount = 0;

        /*
         * CONFIG
         */
        // Speed the robot drove while scoring/approaching station, default = 0.4
        robotSpeedFast = 0.6;

        // Speed the robot drives while balancing itself on the charge station.
        // Should be roughly half the fast speed, to make the robot more accurate,
        // default = 0.2
        robotSpeedSlow = 0.2;

        // Angle where the robot knows it is on the charge station, default = 13.0
        onChargeStationDegree = 13.0;

        // Angle where the robot can assume it is level on the charging station
        // Used for exiting the drive forward sequence as well as for auto balancing,
        // default = 6.0
        levelDegree = 6.0;

        // Amount of time a sensor condition needs to be met before changing states in
        // seconds
        // Reduces the impact of sensor noise, but too high can make the auto run
        // slower, default = 0.2
        debounceTime = 0.1;

        if (isFwd) {
            onChargeStationDegree = onChargeStationDegree * -1;
            levelDegree = levelDegree * -1;
        }
    }

    public int secondsToTicks(final double time) {
        return (int) (time * 50);
    }

    /**
     * Drives onto and engages the charge station. Direction is determined by the constructor's
     * isFwd parameter.
     *
     * @param argTilt current robot tilt in degrees
     * @return motor output from -1.0 to 1.0
     */
    public double autoBalanceRoutine(final double argTilt) {
        /* Direction multiplier: forward approach sees negative tilt, reverse sees positive */
        double dirSign = isFwd ? -1.0 : 1.0;

        switch (state) {
            case STATE_APPROACH:
                if ((argTilt * dirSign) > onChargeStationDegree * dirSign) {
                    debounceCount++;
                }
                if (debounceCount > secondsToTicks(debounceTime)) {
                    state = STATE_CLIMB;
                    debounceCount = 0;
                    return robotSpeedSlow;
                }
                return robotSpeedFast;

            case STATE_CLIMB:
                if ((argTilt * dirSign) < levelDegree * dirSign) {
                    debounceCount++;
                }
                if (debounceCount > secondsToTicks(debounceTime)) {
                    state = STATE_HOLD;
                    debounceCount = 0;
                    return 0;
                }
                return robotSpeedSlow;

            case STATE_HOLD:
                if (Math.abs(argTilt) <= Math.abs(levelDegree) / 2) {
                    debounceCount++;
                }
                if (debounceCount > secondsToTicks(debounceTime)) {
                    state = STATE_DONE;
                    debounceCount = 0;
                    return 0;
                }
                if (argTilt >= levelDegree) {
                    return isFwd ? -HOLD_CORRECTION_OUTPUT : HOLD_CORRECTION_OUTPUT;
                } else if (argTilt <= -levelDegree) {
                    return isFwd ? HOLD_CORRECTION_OUTPUT : -HOLD_CORRECTION_OUTPUT;
                }
                return 0;

            default:
                return 0;
        }
    }

    protected void checkTuning() {
        if (tuneSpeedSlow.hasChanged()) {
            robotSpeedSlow = tuneSpeedSlow.getDbl();
        }
        if (tuneSpeedFast.hasChanged()) {
            robotSpeedFast = tuneSpeedFast.getDbl();
        }
        if (tuneOnChargeDeg.hasChanged()) {
            onChargeStationDegree = isFwd ? -tuneOnChargeDeg.getDbl() : tuneOnChargeDeg.getDbl();
        }
        if (tuneLevelDeg.hasChanged()) {
            levelDegree = isFwd ? -tuneLevelDeg.getDbl() : tuneLevelDeg.getDbl();
        }
        if (tuneDebounceTime.hasChanged()) {
            debounceTime = tuneDebounceTime.getDbl();
        }
    }

    public void outputTelemetry() {
        checkTuning();

        Logger.recordOutput("Balance/State", state);
        Logger.recordOutput("Balance/DebounceCount", debounceCount);
        Logger.recordOutput("Balance/RobotSpeedSlow", robotSpeedSlow);
        Logger.recordOutput("Balance/RobotSpeedFast", robotSpeedFast);
        Logger.recordOutput("Balance/OnChargeStationDegree", onChargeStationDegree);
        Logger.recordOutput("Balance/LevelDegree", levelDegree);
    }
}
