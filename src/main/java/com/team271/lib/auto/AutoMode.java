package com.team271.lib.auto;

import com.team271.lib.ConstantsLib;
import com.team271.lib.util.Elastic;
import edu.wpi.first.wpilibj.Timer;
import java.util.ArrayList;
import java.util.List;
import org.littletonrobotics.junction.Logger;

public abstract class AutoMode {
    protected boolean isRunning = false;
    protected boolean isComplete = false;

    protected final Timer elapsedTimer = new Timer();
    protected final double delay;

    protected double currentTime = 0.0;
    protected double lastTime = 0.0;

    private List<AutoMove> moves = new ArrayList<>();
    protected int currentMoveIdx = 0;
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
        if (!moves.isEmpty()) {
            currentMoveIdx = 0;
            currentMove = moves.get(currentMoveIdx);

            isRunning = true;

            elapsedTimer.start();

            if (currentMove != null) {
                currentMove.start();
            }

            Elastic.sendNotification(
                    new Elastic.Notification(
                            Elastic.NotificationLevel.INFO, "Auto", "Auto Started"));
        }
    }

    public void end() {
        isRunning = false;

        elapsedTimer.stop();

        setCompleted();

        Logger.recordOutput("Auto/Running", false);
        Logger.recordOutput("Auto/Complete", true);
        Elastic.sendNotification(
                new Elastic.Notification(Elastic.NotificationLevel.INFO, "Auto", "Auto Complete"));
    }

    public boolean isDelayDone() {
        /*
         * Check for Delay
         */
        return (delay < ConstantsLib.DELAY_THRESHOLD_SEC)
                || ((delay > 0.0) && (currentTime >= delay));
    }

    public void addMove(AutoMove argMove) {
        moves.add(argMove);
    }

    public void nextMove() {
        currentMoveIdx++;

        if (currentMoveIdx < moves.size()) {
            currentMove = moves.get(currentMoveIdx);
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
        if (isRunning()) {
            lastTime = currentTime;
            currentTime = elapsedTimer.get();

            Logger.recordOutput("Auto/Running", true);
            Logger.recordOutput("Auto/CurrentTime", currentTime);
            Logger.recordOutput("Auto/MoveIndex", currentMoveIdx);
            if (currentMove != null) {
                Logger.recordOutput("Auto/CurrentMoveName", currentMove.getName());
            }

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
        if (currentMove != null && currentMove.canRun()) {
            currentMove.autonomousPeriodic(argTimestamp);
        }
    }

    public void autonomousExit(double argTimestamp) {
        end();
    }
}
