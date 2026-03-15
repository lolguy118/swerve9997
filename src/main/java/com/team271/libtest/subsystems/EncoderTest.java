package com.team271.libtest.subsystems;

import com.team271.libtest.Constants;
import com.team271.libtest.Constants.ControlMode;
import com.team271.libtest.subsystems.Input.InputDriver;
import com.team271.libtest.subsystems.Input.InputOp;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.sensors.encoders.EncoderBase.EncoderDirection;
import com.team271.lib.hardware.sensors.encoders.EncoderCANCoder;
import com.team271.lib.subsystem.Subsystem;

public class EncoderTest extends Subsystem {
    /*
     * Singleton
     */
    private static EncoderTest mInstance;

    public static EncoderTest getInstance(final TObj argParent) {
        if (mInstance == null) {
            mInstance = new EncoderTest(argParent);
        }

        return mInstance;
    }

    public static EncoderTest getInstance() {
        return mInstance;
    }

    /*
     * Other Singletons
     */
    protected final InputDriver mInputDriver = InputDriver.getInstance();
    protected final InputOp mInputOp = InputOp.getInstance();

    /*
     * Constants
     */
    public static final class EncoderTestConstants {
        static final double kEncUpdateFreq = 250.0;

        // Calibration
        //static final double kRefLowPos = -15.7 - 75.05859375;
        static final double kRefHighPos = 91.6;

        static final double kMagnetOffset = -0.894775;

        static final double kScale = 360.0;
    }

    /*
     * Variables
     */
    protected ControlMode controlMode = ControlMode.MANUAL;

    /*
     * EncoderTest
     */
    protected EncoderCANCoder encCANCoder = new EncoderCANCoder(this, "EncoderTest", new CANDeviceID(Constants.CAN_ID_CANCODER_SHOULDER, Constants.CAN_BUS_NAME), EncoderDirection.CW, EncoderTestConstants.kEncUpdateFreq);

    /*
     *
     * Telemetry (NT)
     * 
     */

    /*
     * 
     * Constructors
     * 
     */
    public EncoderTest(final TObj argParent) {
        super(argParent, "EncoderTest");
    }

    /*
     * 
     * Robot States
     * 
     */
    @Override
    public void robotInit(final double timestamp) {
        /*
         * CANCoder
         */
        encCANCoder.setMagnetSensor(1.0);
        encCANCoder.setMagnetOffset(EncoderTestConstants.kMagnetOffset);
        encCANCoder.applyConfig();


        /*
         * Transmission Init
         */
        encCANCoder.robotInit(timestamp);
    }

    /*
     * 
     * Transmissions
     * 
     */
    /*
     * 
     * EncoderTest
     * 
     */

    /*
     *
     * Robot States
     * 
     */
    @Override
    public void robotPeriodicBefore(final double timestamp) {
        encCANCoder.refresh();
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
    }

    private double simAngleRad = 0;
    private double simVelRadPerSec = 0;
    private static final double SIM_DT = 0.020;

    @Override
    public void simulationInit(final double argTimestamp) {
        simAngleRad = 0;
        simVelRadPerSec = 0;
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        /* Simple constant-velocity rotation for encoder testing: 0.5 RPS */
        simVelRadPerSec = 0.5 * 2.0 * Math.PI;
        simAngleRad += simVelRadPerSec * SIM_DT;

        double simPosRotations = simAngleRad / (2.0 * Math.PI);
        double simVelRPS = simVelRadPerSec / (2.0 * Math.PI);

        encCANCoder.setSimPosRotations(simPosRotations);
        encCANCoder.setSimVelRotations(simVelRPS);

        encCANCoder.simulationPeriodic(argTimestamp);
    }

    /*
     * 
     * Telemetry
     * 
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        encCANCoder.outputTelemetry();
    }
}
