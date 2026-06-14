package com.team271.lib.auto;

import com.team271.lib.util.Elastic;
import java.util.function.BooleanSupplier;

/**
 * Completes when a {@link BooleanSupplier} condition returns true, or when the timeout expires.
 * Enables event-driven patterns like "wait until launcher at speed" without manual polling in
 * {@link #autonomousPeriodic}.
 *
 * <p>The timeout is mandatory per CODE-SAF-012 (all waiting operations must have timeouts). On
 * timeout, an Elastic notification is sent to alert the driver.
 *
 * <p>The condition is checked in {@link #robotPeriodicBefore} so it runs every cycle regardless of
 * the {@link #canRun()} state - the same pattern {@link AutoMoveTimed} uses for duration checks.
 *
 * <p>Example - wait until the launcher is at speed (up to 3 seconds):
 *
 * <pre>{@code
 * new AutoMoveConditional("LauncherReady", Launcher::isAtMaxVelocity, 3.0)
 * }</pre>
 */
public class AutoMoveConditional extends AutoMove {

    private final BooleanSupplier condition;
    private final double timeoutSec;

    /**
     * @param argName display name for telemetry (or null for class simple name)
     * @param argCondition the condition to wait for - move completes when this returns true
     * @param argTimeoutSec maximum wait time in seconds (required, must be &gt; 0)
     */
    public AutoMoveConditional(
            final String argName, final BooleanSupplier argCondition, final double argTimeoutSec) {
        super(argName, 0.0);
        condition = argCondition;
        timeoutSec = argTimeoutSec;
    }

    /**
     * @param argCondition the condition to wait for - move completes when this returns true
     * @param argTimeoutSec maximum wait time in seconds (required, must be &gt; 0)
     */
    public AutoMoveConditional(final BooleanSupplier argCondition, final double argTimeoutSec) {
        this(null, argCondition, argTimeoutSec);
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        super.robotPeriodicBefore(argTimestamp);

        if (isRunning()) {
            if (condition.getAsBoolean()) {
                end();
            } else if (currentTime >= timeoutSec) {
                Elastic.sendNotification(
                        new Elastic.Notification(
                                Elastic.NotificationLevel.WARNING,
                                "Auto",
                                "AutoMoveConditional timed out: " + getName()));
                end();
            }
        }
    }
}
