package com.team271.lib.auto;

import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.littletonrobotics.junction.Logger;

/**
 * Runs multiple {@link AutoMove}s in sequence, packaged as a single move. The first child runs
 * until it completes, then the second starts, and so on. The sequence completes when the last child
 * finishes.
 *
 * <p>This is essentially a mini {@link AutoMode} that can be nested inside an {@link
 * AutoMoveParallel} to create complex overlapping timing patterns.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * new AutoMoveSequence(
 *     new WaitMove(1.0),           // wait 1 second
 *     new LauncherSpinMove(6.0, ShotMode.HUB)  // then spin for 6 seconds
 * )
 * }</pre>
 */
@SuppressWarnings("NullAway.Init")
public class AutoMoveSequence extends AutoMove {

    private final List<AutoMove> moves;
    private int currentIdx = 0;
    @Nullable private AutoMove current;

    // Cached telemetry keys — computed in start() to avoid string concat every cycle
    private String telemetryKeyChildIndex;
    private String telemetryKeyChildName;

    public AutoMoveSequence(final AutoMove... argMoves) {
        super(0.0);
        moves = Arrays.asList(argMoves);
    }

    @Override
    public void start() {
        super.start();
        String prefix = "Auto/Moves/" + getName() + "/";
        telemetryKeyChildIndex = prefix + "ChildIndex";
        telemetryKeyChildName = prefix + "ChildName";
        currentIdx = 0;
        if (!moves.isEmpty()) {
            current = moves.get(0);
            current.start();
        } else {
            end();
        }
    }

    @Override
    public void onEnd() {
        if (current != null && current.isRunning() && !current.isComplete()) {
            current.end();
        }
    }

    private void advanceToNext() {
        currentIdx++;
        if (currentIdx < moves.size()) {
            current = moves.get(currentIdx);
            current.start();
        } else {
            current = null;
            end();
        }
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        super.robotPeriodicBefore(argTimestamp);

        Logger.recordOutput(telemetryKeyChildIndex, currentIdx);
        if (current != null) {
            Logger.recordOutput(telemetryKeyChildName, current.getName());
        }

        if (current != null && !current.isComplete()) {
            current.robotPeriodicBefore(argTimestamp);
        }
    }

    @Override
    public void autonomousPeriodic(final double argTimestamp) {
        if (current != null && current.canRun()) {
            current.autonomousPeriodic(argTimestamp);
        }
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
        if (current != null) {
            if (!current.isComplete()) {
                current.robotPeriodicAfter(argTimestamp);
            }
            if (current.isComplete()) {
                advanceToNext();
            }
        }
    }
}
