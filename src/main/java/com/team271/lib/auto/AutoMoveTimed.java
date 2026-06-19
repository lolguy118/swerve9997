package com.team271.lib.auto;

import com.team271.lib.ConstantsLib;

public class AutoMoveTimed extends AutoMove {
    protected final double length;
    protected final double timeout;

    public AutoMoveTimed(final double argLength, final double argDelay, final double argTimeout) {
        super(argDelay);

        length = argLength;
        timeout = argTimeout;
    }

    public AutoMoveTimed(final double argLength, final double argDelay) {
        this(argLength, argDelay, 0.0);
    }

    public AutoMoveTimed(final double argLength) {
        this(argLength, 0.0, 0.0);
    }

    public boolean isTimedout() {
        return (currentTime > timeout);
    }

    /*
     * Robot
     */
    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        super.robotPeriodicBefore(argTimestamp);

        if (isRunning() == true) {
            /*
             * Check for Move Timeout
             */
            if ((timeout > ConstantsLib.DELAY_THRESHOLD_SEC) && (currentTime > timeout)) {
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
