package com.team271.libtest;

import com.team271.libtest.subsystems.Infrastructure;
import com.team271.libtest.subsystems.Input.InputDriver;
import com.team271.libtest.subsystems.Input.InputOp;
import com.team271.libtest.subsystems.Superstructure;

/**
 * Static references to all subsystem instances. Set once during {@link Robot#robotInit()} and read
 * by any subsystem that needs cross-subsystem access.
 *
 * <p>Initialization order in Robot.robotInit() guarantees these are non-null after init completes.
 */
public final class Globals {
    public static InputDriver inputDriver;
    public static InputOp inputOp;
    public static Infrastructure infrastructure;
    public static Superstructure superstructure;

    private Globals() {}
}
