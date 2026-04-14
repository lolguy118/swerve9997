package com.team271.lib.auto;

import edu.wpi.first.wpilibj.Timer;
import org.littletonrobotics.junction.Logger;

public abstract class AutoMove {
    protected boolean isRunning = false;
    protected boolean isComplete = false;

    protected final Timer elapsedTimer = new Timer();
    protected final double delay;
    protected final String name;

    protected double currentTime = 0.0;
    protected double lastTime = 0.0;

    protected AutoMove(final String argName, final double argDelay) {
        name = argName;
        delay = argDelay;
    }

    protected AutoMove(double argDelay) {
        this(null, argDelay);
    }

    protected AutoMove() {
        this(0.0);
    }

    /** Returns the move name for telemetry. Defaults to the class simple name if not set. */
    public String getName() {
        return (name != null) ? name : getClass().getSimpleName();
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setCompleted() {
        isComplete = true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void start() {
        isRunning = true;

        elapsedTimer.start();

        onStart();
    }

    public void end() {
        onEnd();

        isRunning = false;

        elapsedTimer.stop();

        setCompleted();

        String prefix = "Auto/Moves/" + getName() + "/";
        Logger.recordOutput(prefix + "Running", false);
        Logger.recordOutput(prefix + "Complete", true);
    }

    /**
     * Returns true while the elapsed time is within the allowed duration. Once currentTime exceeds
     * the time limit, returns false and canRun() will stop.
     *
     * <p>Note: AutoMode.isDelayDone() has OPPOSITE semantics (true AFTER delay elapses).
     */
    public boolean isWithinTimeLimit() {
        return (delay < 0.01) || ((delay > 0.0) && (currentTime <= delay));
    }

    public boolean canRun() {
        return isRunning() && isWithinTimeLimit() && !isComplete();
    }

    /*
     * Move
     */
    public void onStart() {
        // Default Method to override if needed
    }

    public void onEnd() {
        // Default Method to override if needed
    }

    /*
     * Robot
     */
    public void robotPeriodicBefore(double argTimestamp) {
        if (isRunning() == true) {
            lastTime = currentTime;
            currentTime = elapsedTimer.get();

            String prefix = "Auto/Moves/" + getName() + "/";
            Logger.recordOutput(prefix + "Running", true);
            Logger.recordOutput(prefix + "ElapsedTime", currentTime);
        }
    }

    public void robotPeriodicAfter(double argTimestamp) {
        // Default Method to override if needed
    }

    /*
     * Auto
     */
    public void autonomousPeriodic(double argTimestamp) {
        // Default Method to override if needed
    }
}
