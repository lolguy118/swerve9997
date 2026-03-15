package com.team271.lib.auto;

public class AutoMoveTimed extends AutoMove {
    protected final double length;
    protected final double timeout;

    public AutoMoveTimed(double argLength, double argDelay, double argTimeout) {
        super(argDelay);

        length = argLength;
        timeout = argTimeout;
    }

    public AutoMoveTimed(double argLength, double argDelay) {
        this(argLength, argDelay, 0.0);
    }

    public AutoMoveTimed(double argLength) {
        this(argLength, 0.0, 0.0);
    }

    public boolean isTimedout() {
        return (currentTime > timeout);
    }

    /*
     * Robot
     */
    @Override
    public void robotPeriodicBefore(double argTimestamp) {
        super.robotPeriodicBefore(argTimestamp);

        if (isRunning() == true) {
            /*
             * Check for Move Timeout
             */
            if ((timeout > 0.01) && (currentTime > timeout)) {
                end();
            }
            /*
             * Check for Move End
             */
            else if (currentTime > length) {
                end();
            }
        }
    }
}
