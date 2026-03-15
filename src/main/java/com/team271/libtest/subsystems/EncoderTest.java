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
    protected EncoderCANCoder encCANCoder = new EncoderCANCoder(this, "EncoderTest", new CANDeviceID(1, Constants.CAN_BUS_NAME), EncoderDirection.CW, EncoderTestConstants.kEncUpdateFreq);

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

    double tmpSimPos = 0;
    double tmpSimVel = 0;

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        encCANCoder.setSimPosRotations(tmpSimPos);
        encCANCoder.setSimVelRotations(tmpSimVel);

        encCANCoder.simulationPeriodic(argTimestamp);

        ++tmpSimPos;
        ++tmpSimVel;
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
