package com.team271.libtest;

import com.team271.libtest.subsystems.EncoderTest;
import com.team271.libtest.subsystems.Infrastructure;
import com.team271.libtest.subsystems.Input.InputDriver;
import com.team271.libtest.subsystems.Input.InputOp;
import com.team271.libtest.subsystems.Superstructure;
import com.team271.libtest.subsystems.TransmissionTest;

public class Globals {

    public static InputDriver controllerDriver;
    public static InputOp controllerOperator;

    public static Infrastructure infrastructure;

    public static EncoderTest encoderTest;

    public static TransmissionTest transmissionTest;

    public static Superstructure superstructure;

    private Globals() {}
}
