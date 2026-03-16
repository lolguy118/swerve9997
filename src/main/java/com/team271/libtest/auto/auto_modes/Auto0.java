package com.team271.libtest.auto.auto_modes;

import com.team271.lib.auto.AutoMode;

/**
 * Default "do nothing" autonomous mode. Safe fallback that immediately completes without adding any
 * moves.
 */
public class Auto0 extends AutoMode {

    public Auto0() {
        super(0.0);
    }
}
