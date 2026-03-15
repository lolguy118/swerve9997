package com.team271.lib.control.pid;

//
// import edu.wpi.first.networktables.NetworkTable;
// import edu.wpi.first.networktables.NetworkTableInstance;
// import edu.wpi.first.wpilibj.Encoder;
// import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
//
// public class PIDSimpleTest implements AutoCloseable {
//    public static final int kMotorPort = 0;
//    public static final int kEncoderAChannel = 0;
//    public static final int kEncoderBChannel = 1;
//
//    public static final double kElevatorKp = 5;
//    public static final double kElevatorKi = 0;
//    public static final double kElevatorKd = 0;
//
//    public static final double kElevatorGearing = 10.0;
//    public static final double kElevatorDrumRadius = Units.inches_to_meters(2.0);
//    public static final double kCarriageMass = 4.0; // kg
//
//    // distance per pulse = (distance per revolution) / (pulses per revolution)
//    // = (Pi * D) / ppr
//    public static final double kElevatorEncoderDistPerPulse = 2.0 * Math.PI * kElevatorDrumRadius
// / 4096;
//
//    // This gearbox represents a gearbox containing 4 Vex 775pro motors.
//    //private final DCMotor m_elevatorGearbox = DCMotor.getVex775Pro(4);
//
//    PIDSimple pidsTest;// = new PIDSimple("", kElevatorKp, kElevatorKi, kElevatorKd, 0.1);
//
//    private final Encoder m_encoder = new Encoder(kEncoderAChannel, kEncoderBChannel);
//    private final PWMSparkMax m_motor = new PWMSparkMax(kMotorPort);
//
//    // Simulation classes help us simulate what's going on, including gravity.
//    //private final EncoderSim m_encoderSim = new EncoderSim(m_encoder);
//    //private final PWMSim m_motorSim = new PWMSim(m_motor);
//
//    double lastTime = 0;
//    double goal = 0;
//    double output = 0;
//    double distance = 0;
//
//
//    NetworkTableInstance inst;
//    NetworkTable table;
//    //NTDoubleEntry ntGoal;
//
//    /* Subsystem constructor. */
//    public PIDSimpleTest() {
//        m_encoder.setDistancePerPulse(kElevatorEncoderDistPerPulse);
//
//        inst = NetworkTableInstance.getDefault();
//        table = inst.getTable("Sim");
//        //ntGoal = new NTDoubleEntry(table, "Goal");
//
//        //ntGoal.setup(goal);
//        //pidsTest.enableTelemetry();
//    }
//
//    /* Advance the simulation. */
//    public void simulationPeriodic(double argTimestamp) {
//        if ((argTimestamp - lastTime) > 5) {
//            //goal = ntGoal.publish(goal);
//            pidsTest.setSetpoint(goal);
//            lastTime = argTimestamp;
//        }
//
//        output = pidsTest.calc(distance, goal, argTimestamp);
//
//        distance += 0.5 * output;
//    }
//
//    /* Update telemetry, including the mechanism visualization. */
//    public void updateTelemetry() {
//        // Update elevator visualization with position
//        // m_elevatorMech2d.setLength(m_encoder.getDistance());
//        pidsTest.outputTelemetry(0);
//    }
//
//    @Override
//    public void close() {
//        m_encoder.close();
//        m_motor.close();
//    }
// }
//
