package com.team271.lib.auto;

import edu.wpi.first.wpilibj.Timer;
import java.util.ArrayList;
import java.util.List;

public abstract class AutoMode {
    protected boolean isRunning = false;
    protected boolean isComplete = false;

    protected final Timer elapsedTimer = new Timer();
    protected final double delay;

    protected double currentTime = 0.0;
    protected double lastTime = 0.0;

    private List<AutoMove> moves = new ArrayList<>();
    protected long currentMoveIdx = 0;
    protected AutoMove currentMove;

    protected AutoMode(double argDelay) {
        delay = argDelay;
    }

    protected AutoMode() {
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
        if (moves.isEmpty() == false) {
            currentMoveIdx = 0;
            currentMove = moves.get((int) currentMoveIdx);

            isRunning = true;

            elapsedTimer.start();

            if (currentMove != null) {
                currentMove.start();
            }
        }
    }

    public void end() {
        isRunning = false;

        elapsedTimer.stop();

        setCompleted();
    }

    public boolean isDelayDone() {
        /*
         * Check for Delay
         */
        return (delay < 0.01) || ((delay > 0.0) && (currentTime >= delay));
    }

    public void addMove(AutoMove argMove) {
        moves.add(argMove);
    }

    public void nextMove() {
        currentMoveIdx++;

        if (currentMoveIdx < moves.size()) {
            currentMove = moves.get((int) currentMoveIdx);
        } else {
            currentMove = null;
            end();
            return;
        }

        if (currentMove != null) {
            currentMove.start();
        }
    }

    /*
     * Robot
     */
    public void robotPeriodicBefore(double argTimestamp) {
        if (isRunning() == true) {
            lastTime = currentTime;
            currentTime = elapsedTimer.get();

            /*
             * Do Moves
             */
            if (currentMove != null) {
                currentMove.robotPeriodicBefore(argTimestamp);
            }
        }
    }

    public void robotPeriodicAfter(double argTimestamp) {
        if ((currentMove != null) && isRunning()) {
            currentMove.robotPeriodicAfter(argTimestamp);

            if (currentMove.isComplete()) {
                nextMove();

                if (currentMove == null) {
                    end();
                }
            }
        }
    }

    /*
     * Disabled
     */
    public void disabledInit(double argTimestamp) {
        // Default Method to override if needed
    }

    public void disabledPeriodic(double argTimestamp) {
        // Default Method to override if needed
    }

    public void disabledExit(double argTimestamp) {
        // Default Method to override if needed
    }

    /*
     * Auto
     */
    public void autonomousInit(double argTimestamp) {
        start();
    }

    public void autonomousPeriodic(double argTimestamp) {
        moves.forEach(t -> {
            if (t.canRun() == true) {
                t.autonomousPeriodic(argTimestamp);
            }
        });
    }

    public void autonomousExit(double argTimestamp) {
        end();
    }
}
