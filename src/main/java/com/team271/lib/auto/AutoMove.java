package com.team271.lib.auto;

import edu.wpi.first.wpilibj.Timer;

public abstract class AutoMove {
    protected boolean isRunning = false;
    protected boolean isComplete = false;

    protected final Timer elapsedTimer = new Timer();
    protected final double delay;

    protected double currentTime = 0.0;
    protected double lastTime = 0.0;

    protected AutoMove(double argDelay) {
        delay = argDelay;
    }

    protected AutoMove() {
        this(0.0);
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
    }

    /**
     * Returns true while the elapsed time is within the allowed duration.
     * Once currentTime exceeds the time limit, returns false and canRun() will stop.
     *
     * Note: AutoMode.isDelayDone() has OPPOSITE semantics (true AFTER delay elapses).
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
