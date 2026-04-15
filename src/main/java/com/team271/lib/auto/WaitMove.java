package com.team271.lib.auto;

/**
 * A named alias for {@link AutoMoveTimed} that represents a pure delay with no subsystem commands.
 *
 * <p>Usage in auto mode composition:
 *
 * <pre>{@code
 * addMove(new WaitMove(0.5));  // wait 500ms before next move
 * }</pre>
 */
public class WaitMove extends AutoMoveTimed {
    public WaitMove(final double argDurationSec) {
        super(argDurationSec);
    }

    @Override
    public String toString() {
        return "WaitMove(" + length + "s)";
    }
}
