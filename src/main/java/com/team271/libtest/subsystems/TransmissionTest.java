package com.team271.libtest.subsystems;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;

import com.team271.libtest.Constants;
import com.team271.libtest.Constants.ControlMode;
import com.team271.libtest.subsystems.Input.InputDriver;
import com.team271.libtest.subsystems.Input.InputOp;

import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerBase.MotorDirection;
import com.team271.lib.hardware.controllers.ControllerBase.NeutralState;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.hardware.motors.MotorBase.MotorType;
import com.team271.lib.hardware.sensors.encoders.EncoderBase.EncoderDirection;
import com.team271.lib.hardware.transmissions.TransmissionFX;
import com.team271.lib.subsystem.Subsystem;

public class TransmissionTest extends Subsystem {
    /*
     * Singleton
     */
    private static TransmissionTest mInstance;

    public static TransmissionTest getInstance(final TObj argParent) {
        if (mInstance == null) {
            mInstance = new TransmissionTest(argParent);
        }

        return mInstance;
    }

    public static TransmissionTest getInstance() {
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
    public static final class TransmissionTestConstants {
        static final boolean kSatorLimitEnabled = false;
        static final int kCurrentSatorLimit = 80;

        static final boolean kSupplyLimitEnabled = false;
        static final int kCurrentSupplyLimit = 50;

        static final double kMotorUpdateFreq = 250.0;
        static final double kEncUpdateFreq = 250.0;

        static final double kRatio = (1.0 / 9.0) * (1.0 / 5.0) * (16.0 / 60.0) * (28.0 / 80.0);

        static final double kArmLengthMeters = Inches.of(27).in(Meters);
        static final double kArmMassKg = Pounds.of(42.5).in(Kilograms);

        static final double kMinPos = Degrees.of(-75).in(Radians);
        static final double kMaxPos = Degrees.of(255).in(Radians);

        // Calibration
        //static final double kRefLowPos = -15.7 - 75.05859375;
        static final double kRefHighPos = 91.6;

        static final double kMagnetOffset = -0.894775;

        static final double kRotorToMechanism = 1.0;
        static final double kSensorToMechanism = 1.0;
        static final double kMechanismToUnits = 1.0;
    }

    /*
     * Variables
     */
    protected ControlMode controlMode = ControlMode.MANUAL;

    /*
     * TransmissionTest
     */
    private final TransmissionFX transmission = new TransmissionFX(this,
            "TransmissionTest",
            new MotorBase(MotorType.KRAKENX60),
            new CANDeviceID(Constants.CAN_ID_SHOULDER_LEAD, Constants.CAN_BUS_NAME),
            new CANDeviceID(Constants.CAN_ID_SHOULDER_FOLLOWER, Constants.CAN_BUS_NAME), true);


    /*
     * Simulation
     */
    private SingleJointedArmSim m_armSim;

    // Create a Mechanism2d display of an Arm with a fixed ArmTower and moving Arm.
    private final Mechanism2d m_mech2d = new Mechanism2d(68, 90);
    private final MechanismRoot2d m_armPivot = m_mech2d.getRoot("ArmPivot", (68/2)-8.525000000, 10.960000000);
    private final MechanismLigament2d m_armTower = m_armPivot.append(new MechanismLigament2d("ArmTower", 20, -90));
    private final MechanismLigament2d m_arm = m_armPivot.append( new MechanismLigament2d(
                "Arm",
                30,
                Radians.of(0.0).in(Degrees),
                6,
                new Color8Bit(Color.kYellow)));

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
    public TransmissionTest(final TObj argParent) {
        super(argParent, "TransmissionTest");
    }

    /*
     * 
     * Robot States
     * 
     */
    @Override
    public void robotInit(final double timestamp) {
        /*
         * Shoulder Config
         */
        transmission.configDirection(MotorDirection.CW);
        transmission.setNeutralMode(NeutralState.BRAKE);

        transmission.configCurrentLimitStator(TransmissionTestConstants.kSatorLimitEnabled, TransmissionTestConstants.kCurrentSatorLimit);

        transmission.configCurrentLimitSupply(TransmissionTestConstants.kSupplyLimitEnabled, TransmissionTestConstants.kCurrentSupplyLimit);

        transmission.applyConfigs();

        /*
         * CANCoder
         */
        transmission.addCANCoder(new CANDeviceID(Constants.CAN_ID_CANCODER_SHOULDER, Constants.CAN_BUS_NAME),
                EncoderDirection.CCW, TransmissionTestConstants.kEncUpdateFreq);

        transmission.getEncoderCANCoder().setMagnetSensor(1.0);
        transmission.getEncoderCANCoder().setMagnetOffset(TransmissionTestConstants.kMagnetOffset);
        transmission.getEncoderCANCoder().applyConfig();

        transmission.setRotorToMechanism(TransmissionTestConstants.kRotorToMechanism);
        transmission.setSensorToMechanism(TransmissionTestConstants.kSensorToMechanism);
        transmission.setMechanismToUnits(TransmissionTestConstants.kMechanismToUnits);

        /*
         * Transmission Init
         */
        transmission.robotInit(timestamp);

        /*
         * Simulation
         */
        /*
        m_armSim = new SingleJointedArmSim(
                transmission.getDCMotor(),
                1.0 / TransmissionTestConstants.kRatio,
                SingleJointedArmSim.estimateMOI(TransmissionTestConstants.kArmLengthMeters,
                        TransmissionTestConstants.kArmMassKg),
                TransmissionTestConstants.kArmLengthMeters,
                TransmissionTestConstants.kMinPos,
                TransmissionTestConstants.kMaxPos,
                true,
                0);
                */
                m_armSim = new SingleJointedArmSim(
                        transmission.getDCMotor(),
                        1.0 / TransmissionTestConstants.kRatio,
                        0.021,
                        TransmissionTestConstants.kArmLengthMeters,
                        TransmissionTestConstants.kMinPos,
                        TransmissionTestConstants.kMaxPos,
                        true,
                        0);

        // Put Mechanism 2d to SmartDashboard
        SmartDashboard.putData("Arm Sim", m_mech2d);
        m_armTower.setColor(new Color8Bit(Color.kBlue));
    }

    /*
     * 
     * Transmissions
     * 
     */
    /*
     * 
     * TransmissionTest
     * 
     */

    /*
     *
     * Robot States
     * 
     */
    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        transmission.robotPeriodicBefore(argTimestamp);
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
    }

    @Override
    public void teleopPeriodic(final double argTimestamp) {
        transmission.setOutputVoltage(mInputOp.getLeftY() * 12.0);
    }

    double tmpSimPos = 0;
    double tmpSimVel = 0;

    /*
     *
     * Simulation
     * 
     */
    @Override
    public void simulationInit(final double argTimestamp) {
        transmission.simulationInit(argTimestamp);
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        transmission.simulationPeriodic(argTimestamp);

        var tmpMotorVoltage = transmission.getSimState().getMotorVoltageMeasure();

        // In this method, we update our simulation of what our arm is doing
        // First, we set our "inputs" (voltages)
        m_armSim.setInput(tmpMotorVoltage.in(Volts));

        // Next, we update it. The standard loop time is 20ms.
        m_armSim.update(0.02);

        // Finally, we set our simulated encoder's readings and simulated battery voltage
        transmission.setSimPosRotations(Radians.of(m_armSim.getAngleRads()).in(Rotations) / TransmissionTestConstants.kRatio);
        transmission.setSimVelRotations(RadiansPerSecond.of(m_armSim.getVelocityRadPerSec()).in(RotationsPerSecond) / TransmissionTestConstants.kRatio);

        // SimBattery estimates loaded battery voltages
        RoboRioSim.setVInVoltage(
            BatterySim.calculateDefaultBatteryLoadedVoltage(m_armSim.getCurrentDrawAmps()));

        // Update the Mechanism Arm angle based on the simulated arm angle
        m_arm.setAngle(Radians.of(m_armSim.getAngleRads()).in(Degrees));
    }

    /*
     * 
     * Telemetry
     * 
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        transmission.outputTelemetry();
    }
}
