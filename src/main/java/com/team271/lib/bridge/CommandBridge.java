package com.team271.lib.bridge;

import com.team271.lib.Lifecycle;
import com.team271.lib.auto.AutoMode;
import com.team271.lib.auto.AutoMove;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Bridge between Team271's lifecycle-based architecture and WPILib's command-based framework.
 *
 * <p>Provides adapters for three interop scenarios:
 *
 * <ul>
 *   <li>{@link #asWPISubsystem} — wrap a Team271 {@link Lifecycle} as a WPILib {@link
 *       SubsystemBase} for command scheduling
 *   <li>{@link #asCommand} — wrap a Team271 {@link AutoMode} as a WPILib {@link Command}
 *   <li>{@link #asAutoMove} — wrap a WPILib {@link Command} as a Team271 {@link AutoMove}
 * </ul>
 *
 * <p>This is an optional bridge — teams can use the lifecycle pattern exclusively, the command
 * pattern exclusively, or mix both via these adapters. The bridge enables PathPlanner's
 * command-based API to work with lifecycle-based subsystems.
 */
public final class CommandBridge {

    private CommandBridge() {}

    /**
     * Wraps a Team271 {@link Lifecycle} as a WPILib {@link SubsystemBase}.
     *
     * <p>The wrapped subsystem's {@code robotPeriodicBefore()} and {@code robotPeriodicAfter()} are
     * called in the WPILib periodic method. Mode-specific hooks are NOT called — those remain
     * managed by Team271's SubsystemManager.
     *
     * @param argLifecycle the lifecycle to wrap
     * @param argName display name for the WPILib subsystem
     * @return a WPILib SubsystemBase wrapping the lifecycle
     */
    public static SubsystemBase asWPISubsystem(final Lifecycle argLifecycle, final String argName) {
        return new SubsystemBase() {
            {
                setName(argName);
            }

            @Override
            public void periodic() {
                final double ts = Timer.getFPGATimestamp();
                argLifecycle.robotPeriodicBefore(ts);
                argLifecycle.robotPeriodicAfter(ts);
            }
        };
    }

    /**
     * Wraps a Team271 {@link AutoMode} as a WPILib {@link Command}.
     *
     * <p>The auto mode's lifecycle (start, periodic, end) is driven by the command scheduler.
     *
     * @param argAutoMode the auto mode to wrap
     * @return a WPILib Command wrapping the auto mode
     */
    public static Command asCommand(final AutoMode argAutoMode) {
        return Commands.runOnce(
                        () -> {
                            argAutoMode.start();
                        })
                .andThen(
                        Commands.run(
                                () -> {
                                    final double ts = Timer.getFPGATimestamp();
                                    argAutoMode.robotPeriodicBefore(ts);
                                    argAutoMode.autonomousPeriodic(ts);
                                    argAutoMode.robotPeriodicAfter(ts);
                                }))
                .until(argAutoMode::isComplete)
                .finallyDo(
                        (final boolean argInterrupted) -> {
                            argAutoMode.end();
                        });
    }

    /**
     * Wraps a WPILib {@link Command} as a Team271 {@link AutoMove}.
     *
     * <p>The command is scheduled when the move starts and cancelled when the move ends. The move
     * reports complete when the command finishes.
     *
     * @param argCommand the WPILib command to wrap
     * @param argTimeoutSec timeout for the move (required per coding standard 4.9c)
     * @return an AutoMove wrapping the command
     */
    public static AutoMove asAutoMove(final Command argCommand, final double argTimeoutSec) {
        return new AutoMove("CmdBridge:" + argCommand.getName(), argTimeoutSec) {
            @Override
            public void onStart() {
                CommandScheduler.getInstance().schedule(argCommand);
            }

            @Override
            public void onEnd() {
                if (!argCommand.isFinished()) {
                    argCommand.cancel();
                }
            }

            @Override
            public void autonomousPeriodic(final double argTimestamp) {
                if (argCommand.isFinished()) {
                    setCompleted();
                }
            }
        };
    }
}
