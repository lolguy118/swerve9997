package com.team271.lib.auto;

import java.util.Arrays;
import java.util.List;
import org.littletonrobotics.junction.Logger;

/**
 * Runs multiple {@link AutoMove}s simultaneously. Completes when <b>all</b> children have
 * completed.
 *
 * <p>Each child move maintains its own timer and lifecycle. Timed children (e.g. {@link
 * AutoMoveTimed}) will self-terminate when their duration expires. The parallel container completes
 * once every child has finished.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * addMove(new AutoMoveParallel(
 *     new DrivePathMove(AutoPaths.HUB),
 *     new AutoMoveSequence(
 *         new WaitMove(1.0),
 *         new ShootMove(6.0, ShotMode.HUB)
 *     )
 * ));
 * }</pre>
 */
public class AutoMoveParallel extends AutoMove {

    private final List<AutoMove> moves;

    public AutoMoveParallel(final AutoMove... argMoves) {
        super(0.0);
        moves = Arrays.asList(argMoves);
    }

    @Override
    public void start() {
        super.start();
        for (AutoMove move : moves) {
            move.start();
        }
    }

    @Override
    public void onEnd() {
        for (AutoMove move : moves) {
            if (move.isRunning() && !move.isComplete()) {
                move.end();
            }
        }
    }

    /**
     * Delegates to all non-complete children every cycle, regardless of canRun(). This is
     * intentional -- timed and conditional moves check completion in robotPeriodicBefore, which
     * must fire even after the move's delay expires.
     */
    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        super.robotPeriodicBefore(argTimestamp);
        for (AutoMove move : moves) {
            if (!move.isComplete()) {
                move.robotPeriodicBefore(argTimestamp);
            }
        }

        int completedCount = 0;
        StringBuilder activeNames = new StringBuilder();
        for (AutoMove move : moves) {
            if (move.isComplete()) {
                completedCount++;
            } else {
                if (activeNames.length() > 0) {
                    activeNames.append(", ");
                }
                activeNames.append(move.getName());
            }
        }
        String prefix = "Auto/Moves/" + getName() + "/";
        Logger.recordOutput(prefix + "CompletedChildren", completedCount);
        Logger.recordOutput(prefix + "TotalChildren", moves.size());
        Logger.recordOutput(prefix + "ActiveChildren", activeNames.toString());
    }

    @Override
    public void autonomousPeriodic(final double argTimestamp) {
        boolean allComplete = true;
        for (AutoMove move : moves) {
            if (move.canRun()) {
                move.autonomousPeriodic(argTimestamp);
            }
            if (!move.isComplete()) {
                allComplete = false;
            }
        }
        if (allComplete) {
            end();
        }
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
        for (AutoMove move : moves) {
            if (!move.isComplete()) {
                move.robotPeriodicAfter(argTimestamp);
            }
        }
    }
}
