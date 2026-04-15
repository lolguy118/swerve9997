package com.team271.lib.hardware.transmissions;

import static edu.wpi.first.units.Units.*;

import com.team271.lib.TObj;
import com.team271.lib.control.pid.PIDBase;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerBase;
import com.team271.lib.hardware.controllers.ControllerBase.ControllerStatus;
import com.team271.lib.hardware.controllers.ControllerBase.MotorDirection;
import com.team271.lib.hardware.controllers.ControllerBase.NeutralState;
import com.team271.lib.hardware.controllers.ControllerSmart;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.sensors.encoders.*;
import com.team271.lib.hardware.sensors.encoders.EncoderAdapter;
import com.team271.lib.hardware.sensors.encoders.EncoderBase.EncoderDirection;
import com.team271.lib.hardware.sensors.switches.SwitchBase;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchTrigger;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchType;
import com.team271.lib.hardware.sensors.switches.SwitchFX;
import com.team271.lib.misc.Elastic;
import com.team271.lib.nt.NTEntry;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class TransmissionBase extends TObj {
    public enum ShifterState {
        GEAR_NONE,
        GEAR_1,
        GEAR_2
    }

    /*
     * Transmission
     */

    /*
     * Motors
     */
    protected ControllerSmart leader;
    protected ControllerSmart follower1;
    protected ControllerSmart follower2;
    protected ControllerSmart follower3;
    protected Set<ControllerSmart> allControllers = new LinkedHashSet<>();

    /*
     *
     * Sensors
     *
     */

    /*
     * Encoder — unified adapter (eliminates per-type conditional cascades)
     */
    protected EncoderAdapter encoder;
    protected GearRatio gearRatio = GearRatio.IDENTITY;

    /*
     * Raw encoder references (kept for subclass access and backward compatibility)
     */
    protected EncoderFX encFX;
    protected EncoderCANCoder encCANCoder;

    /*
     * Limit Switches
     */
    protected static final double LIMIT_UPDATE_FREQ_HZ = 250.0;
    private double lastConfigErrorNotificationTime = 0;
    protected SwitchBase revLimit;
    protected SwitchBase fwdLimit;

    /*
     * Shifters
     */
    protected Shifter shifter;
    protected ShifterState shifterState = ShifterState.GEAR_NONE;

    protected double sensorRatioGear1 = 1.0;
    protected double sensorRatioGear2 = 1.0;

    /*
     * Control Loops
     */
    protected PIDBase pid;

    /*
     *
     * Simulation
     *
     */
    protected DCMotor simDCMotor;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry isBrakeMode = new NTEntry(table, "Is Brake Mode", false);

    final NTEntry outputDuty = new NTEntry(table, "Output(DutyCycle)", 0.0);
    final NTEntry outputVolt = new NTEntry(table, "Output(Voltage)", 0.0);
    final NTEntry outputCL = new NTEntry(table, "Output(ClosedLoop)", 0.0);

    final NTEntry isGearOne = new NTEntry(table, "Is Gear 1", false);

    final NTEntry tSetpoint = new NTEntry(table, "Setpoint", 0.0);

    final NTEntry tError = new NTEntry(table, "Error", 0.0);

    final NTEntry tTol = new NTEntry(table, "Tol", 0.0);

    final NTEntry tMechPosFX = new NTEntry(table, "Mech Pos FX", 0.0);
    final NTEntry tMechPosEnc = new NTEntry(table, "Mech Pos Enc", 0.0);

    final NTEntry tMechVelFX = new NTEntry(table, "Mech Vel FX", 0.0);
    final NTEntry tMechVelEnc = new NTEntry(table, "Mech Vel Enc", 0.0);

    final NTEntry ntRotorToMechanism = new NTEntry(table, "Rotor To Mechanism", 1.0);
    final NTEntry ntSensorRelToMechanism = new NTEntry(table, "Sensor Rel To Mechanism", 1.0);

    /*
     *
     * Constructors
     *
     */
    protected TransmissionBase(final TObj argParent, final String argName) {
        super(argParent, "(Transmission)" + argName);
    }

    /*
     *
     * Core
     *
     */
    protected void create() {}

    /*
     *
     * Robot
     *
     */
    @Override
    public void robotInit(final double argTimestamp) {
        int tmpNumMotors = 0;

        for (ControllerSmart tmpMotor : allControllers) {
            tmpMotor.robotInit(argTimestamp);
            ++tmpNumMotors;
        }

        if (encFX != null) {
            encFX.robotInit(argTimestamp);
        }
        if (encCANCoder != null) {
            encCANCoder.robotInit(argTimestamp);
        }

        if (revLimit != null) {
            revLimit.robotInit(argTimestamp);
        }

        if (fwdLimit != null) {
            fwdLimit.robotInit(argTimestamp);
        }

        if (leader == null) {
            DriverStation.reportError(getName() + ": leader is null in robotInit()", false);
            return;
        }

        switch (leader.getMotor().getMotorType()) {
            case FALCON500:
                simDCMotor = DCMotor.getFalcon500Foc(tmpNumMotors);
                break;
            case KRAKENX60:
                simDCMotor = DCMotor.getKrakenX60Foc(tmpNumMotors);
                break;
            case KRAKENX44:
                simDCMotor =
                        new DCMotor(
                                12.0,
                                4.05,
                                275.0,
                                1.4,
                                RPM.of(7530).in(RadiansPerSecond),
                                tmpNumMotors);
                break;
            case CTRE_MINION:
                simDCMotor =
                        new DCMotor(
                                0.0, 0.0, 0.0, 0.0, RPM.of(0).in(RadiansPerSecond), tmpNumMotors);
                break;
            case NEO:
                simDCMotor = DCMotor.getNEO(tmpNumMotors);
                break;
            case NEO550:
                simDCMotor = DCMotor.getNeo550(tmpNumMotors);
                break;
            case NEO_VORTEX:
                simDCMotor = DCMotor.getNeoVortex(tmpNumMotors);
                break;
            default:
                simDCMotor =
                        new DCMotor(
                                0.0, 0.0, 0.0, 0.0, RPM.of(0).in(RadiansPerSecond), tmpNumMotors);
                break;
        }
    }

    /*
     *
     * Get Motors
     *
     */
    public Set<ControllerSmart> getAllControllers() {
        return allControllers;
    }

    public ControllerSmart getMotor(final CANDeviceID argMotor) {
        for (ControllerSmart tmpMotor : allControllers) {
            if (tmpMotor.isDevice(argMotor)) {
                return tmpMotor;
            }
        }

        return null;
    }

    public DCMotor getDCMotor() {
        return simDCMotor;
    }

    /*
     *
     * Config
     *
     */
    public void applyConfigs() {
        for (ControllerBase c : allControllers) {
            ControllerStatus status = c.applyConfig();
            if (status != ControllerStatus.OK) {
                String msg =
                        getName()
                                + ": config apply failed for "
                                + c.getName()
                                + " (status="
                                + status
                                + ")";
                DriverStation.reportWarning(msg, false);
                double now = Timer.getFPGATimestamp();
                if (now - lastConfigErrorNotificationTime > 2.0) {
                    lastConfigErrorNotificationTime = now;
                    Elastic.sendNotification(
                            new Elastic.Notification(
                                    Elastic.Notification.NotificationLevel.ERROR,
                                    "Config Apply Failed",
                                    msg));
                }
            }
        }
    }

    /*
     * Neutral Mode
     */
    public NeutralState getNeutralMode() {
        return leader.getNeutralMode();
    }

    public void setNeutralMode(final NeutralState argNeutralState) {
        allControllers.forEach(t -> t.setNeutralMode(argNeutralState));
    }

    /*
     * Direction
     */
    public void configDirection(final MotorDirection argDirection) {
        leader.setDirection(argDirection);
    }

    /*
     * Limit Switches
     */
    public void configLimitFwd(
            final SwitchType argSwitchType,
            final boolean argEnable,
            final SwitchTrigger argLimitType,
            final boolean argAutoZero,
            final double argAutoZeroValueInches) {

        if (argSwitchType == SwitchType.FX) {
            fwdLimit =
                    new SwitchFX(
                            this,
                            name + "(Fwd)",
                            (ControllerTalonFX) leader,
                            true,
                            argLimitType,
                            argAutoZero,
                            argAutoZeroValueInches,
                            LIMIT_UPDATE_FREQ_HZ);

            fwdLimit.setEnabled(argEnable);
        }
    }

    public void configLimitRev(
            final SwitchType argSwitchType,
            final boolean argEnable,
            final SwitchTrigger argLimitType,
            final boolean argAutoZero,
            final double argAutoZeroValueInches) {

        if (argSwitchType == SwitchType.FX) {
            revLimit =
                    new SwitchFX(
                            this,
                            name + "(Rev)",
                            (ControllerTalonFX) leader,
                            false,
                            argLimitType,
                            argAutoZero,
                            argAutoZeroValueInches,
                            LIMIT_UPDATE_FREQ_HZ);

            revLimit.setEnabled(argEnable);
        }
    }

    /*
     * Current Limits
     */
    public void configCurrentLimitStator(final boolean argEnable, final double argStatorCurrent) {
        /* Apply to all controllers (leader + followers) */
        allControllers.forEach(t -> t.setCurrentLimitStator(argEnable, argStatorCurrent));
    }

    public void configCurrentLimitSupply(final boolean argEnable, final double argSupplyCurrent) {
        /* Apply to all controllers (leader + followers) */
        allControllers.forEach(t -> t.setCurrentLimitSupply(argEnable, argSupplyCurrent));
    }

    public void configCurrentLimitSupply(
            final double argSupplyCurrentLimit,
            final double argTime,
            double argSupplyCurrentLowerLimit) {
        /* Apply to all controllers (leader + followers) */
        allControllers.forEach(
                t ->
                        t.setCurrentLimitSupply(
                                argSupplyCurrentLimit, argTime, argSupplyCurrentLowerLimit));
    }

    /*
     * Voltage Limits
     */
    public void configVoltagePeak(
            final double argFwdVoltage, final double argRevVoltage, final double argTimeFilter) {
        /* Apply to leader only */
        leader.setVoltagePeak(argFwdVoltage, argRevVoltage, argTimeFilter);
    }

    /*
     * Ramping Open Loop
     */
    public void configRampOpenLoopDuty(final double argRampRateSec) {
        /* Apply to leader only */
        leader.setRampOpenLoopDuty(argRampRateSec);
    }

    public void configRampOpenLoopVoltage(final double argRampRateSec) {
        /* Apply to leader only */
        leader.setRampOpenLoopVoltage(argRampRateSec);
    }

    /*
     * Ramping Closed Loop
     */
    public void configRampClosedLoopDuty(final double argRampRateSec) {
        /* Apply to leader only */
        leader.setRampClosedLoopDuty(argRampRateSec);
    }

    public void configRampClosedLoopVoltage(final double argRampRateSec) {
        /* Apply to leader only */
        leader.setRampClosedLoopVoltage(argRampRateSec);
    }

    /*
     * PID
     */
    public void configPIDFSlot(
            final int argSlot,
            final double argP,
            final double argI,
            final double argD,
            final double argV,
            final double argS) {
        leader.setPIDFSlot(argSlot, argP, argI, argD, argV, argS);
    }

    /*
     *
     * Encoders
     *
     */
    public void addEncoderFX(final double argUpdateFreqHz) {
        encFX = new EncoderFX(this, name, (ControllerTalonFX) leader, argUpdateFreqHz);
        /* Only set as active adapter if no CANCoder is present (CANCoder takes priority) */
        if (encCANCoder == null) {
            encoder = new FXEncoderAdapter(encFX, gearRatio);
        }
    }

    public EncoderFX getEncoderFX() {
        return encFX;
    }

    public void addCANCoder(
            final CANDeviceID argCANIDEnc,
            final EncoderDirection argEncoderDir,
            final double argUpdateFreqHz) {
        encCANCoder =
                new EncoderCANCoderComp(this, name, argCANIDEnc, argEncoderDir, argUpdateFreqHz);
        /* CANCoder always becomes the active adapter when present */
        encoder = new CANCoderAdapter(encCANCoder, gearRatio);
    }

    public EncoderCANCoder getEncoderCANCoder() {
        return encCANCoder;
    }

    /** Returns the active encoder adapter, or null if no encoder is configured. */
    public EncoderAdapter getEncoder() {
        return encoder;
    }

    public void resetEncoders() {
        if (encFX != null) {
            encFX.reset();
        }
        if (encCANCoder != null) {
            encCANCoder.reset();
        }
    }

    /*
     * Position - Rotations
     */
    public void setPosRotations(final double argPositionRotations) {
        if (encoder != null) {
            encoder.setPositionRotations(argPositionRotations);
        }
    }

    public double getPosAbsRotations() {
        if (encCANCoder != null) {
            return encCANCoder.getPosAbsRotations();
        }
        return 0.0;
    }

    public double getPosRotations() {
        if (encoder != null) {
            return encoder.getPositionRotations();
        }
        return 0.0;
    }

    /*
     * Position - Mechanism Output Units
     */
    public double getPosFX() {
        if (encFX != null) {
            return gearRatio.rotorToOutput(encFX.getPosRotations());
        }
        return 0.0;
    }

    public double getPos() {
        if (encoder != null) {
            return encoder.getPosition();
        }
        return 0.0;
    }

    public double getPosAbs() {
        if (encoder != null) {
            return encoder.getAbsolutePosition();
        }
        return 0.0;
    }

    /*
     * Gear Ratio Configuration
     */
    public void setRotorToMechanism(final double argRotorToMechanism) {
        gearRatio =
                new GearRatio(
                        argRotorToMechanism,
                        gearRatio.getSensorRelToMechanism(),
                        gearRatio.getSensorAbsToMechanism(),
                        gearRatio.getMechanismToUnits());
        if (encoder != null) {
            encoder.updateGearRatio(gearRatio);
        }
    }

    public void setSensorToMechanism(final double argSensorRelToMechanism) {
        gearRatio =
                new GearRatio(
                        gearRatio.getRotorToMechanism(),
                        argSensorRelToMechanism,
                        gearRatio.getSensorAbsToMechanism(),
                        gearRatio.getMechanismToUnits());
        if (encoder != null) {
            encoder.updateGearRatio(gearRatio);
        }
    }

    public void setSensorAbsToMechanism(final double argSensorAbsToMechanism) {
        gearRatio =
                new GearRatio(
                        gearRatio.getRotorToMechanism(),
                        gearRatio.getSensorRelToMechanism(),
                        argSensorAbsToMechanism,
                        gearRatio.getMechanismToUnits());
        if (encoder != null) {
            encoder.updateGearRatio(gearRatio);
        }
    }

    public void setMechanismToUnits(final double argMechanismToUnits) {
        gearRatio =
                new GearRatio(
                        gearRatio.getRotorToMechanism(),
                        gearRatio.getSensorRelToMechanism(),
                        gearRatio.getSensorAbsToMechanism(),
                        argMechanismToUnits);
        if (encoder != null) {
            encoder.updateGearRatio(gearRatio);
        }
    }

    /** Sets the full gear ratio. Propagates to the encoder adapter. */
    public void setGearRatio(final GearRatio argGearRatio) {
        gearRatio = argGearRatio;
        if (encoder != null) {
            encoder.updateGearRatio(gearRatio);
        }
    }

    public GearRatio getGearRatio() {
        return gearRatio;
    }

    /*
     * Velocity - Rotations Per Second (RPS)
     */
    public double getVelFXRPS() {
        if (encFX != null) {
            return encFX.getVelRPS();
        }
        return 0.0;
    }

    public double getVelRPS() {
        if (encoder != null) {
            return encoder.getVelocityRPS();
        }
        return 0.0;
    }

    /*
     * Velocity - Mechanism Output Units
     */
    public double getVelFX() {
        if (encFX != null) {
            return gearRatio.rotorToOutput(encFX.getVelRPS());
        }
        return 0.0;
    }

    public double getVel() {
        if (encoder != null) {
            return encoder.getVelocity();
        }
        return 0.0;
    }

    /*
     *
     * Limit Switches
     *
     */
    public boolean getRevLimit() {
        if (revLimit != null) {
            return revLimit.getTriggered();
        }

        return false;
    }

    public boolean getFwdLimit() {
        if (fwdLimit != null) {
            return fwdLimit.getTriggered();
        }

        return false;
    }

    /*
     *
     * Shifters
     *
     */
    /**
     * Set a shifter actuator. Use {@link ShifterPneumatic} for pneumatic shifting, or implement the
     * {@link Shifter} interface for other mechanisms.
     */
    public void setShifter(final Shifter argShifter) {
        shifter = argShifter;
    }

    /** Set a shifter with per-gear sensor ratios. */
    public void setShifter(
            final Shifter argShifter, final double argSensorRatio1, final double argSensorRatio2) {
        sensorRatioGear1 = argSensorRatio1;
        sensorRatioGear2 = argSensorRatio2;

        setShifter(argShifter);
    }

    /**
     * Convenience method: create a pneumatic shifter from solenoid channels. Equivalent to {@code
     * setShifter(new ShifterPneumatic(chGear1, chGear2))}.
     */
    public void addShifter(final int pneumaticHubCanId, final int chGear1, final int chGear2) {
        if (chGear1 != 99 && chGear2 != 99) {
            setShifter(new ShifterPneumatic(pneumaticHubCanId, chGear1, chGear2));
        }
    }

    /** Convenience method: create a pneumatic shifter with per-gear sensor ratios. */
    public void addShifter(
            final int pneumaticHubCanId,
            final int chGear1,
            final double argSensorRatio1,
            final int chGear2,
            final double argSensorRatio2) {
        sensorRatioGear1 = argSensorRatio1;
        sensorRatioGear2 = argSensorRatio2;

        addShifter(pneumaticHubCanId, chGear1, chGear2);
    }

    public ShifterState shift(final ShifterState argShiftTo) {
        /*
         * - Actuate shifter mechanism
         * - Update gear state
         */
        if (shifterState != argShiftTo) {
            if (shifter != null) {
                shifter.actuate(argShiftTo);
            }
            shifterState = argShiftTo;
        }

        return shifterState;
    }

    /*
     *
     * Closed Loop
     *
     */

    /* PID Values */
    public void setPSlot(final int argSlot, final double argSetPSlot0) {
        leader.setPSlot(argSlot, argSetPSlot0);
    }

    public double getPSlot(final int argSlot) {
        return leader.getPSlot(argSlot);
    }

    public void setISlot(final int argSlot, final double argSetISlot0) {
        leader.setISlot(argSlot, argSetISlot0);
    }

    public double getISlot(final int argSlot) {
        return leader.getISlot(argSlot);
    }

    public void setDSlot(final int argSlot, final double argSetDSlot0) {
        leader.setDSlot(argSlot, argSetDSlot0);
    }

    public double getDSlot(final int argSlot) {
        return leader.getDSlot(argSlot);
    }

    public double getVSlot(final int argSlot) {
        return leader.getVSlot(argSlot);
    }

    public double getSSlot(final int argSlot) {
        return leader.getSSlot(argSlot);
    }

    public void setPIDFSlot(
            final int argSlot,
            final double argP,
            final double argI,
            final double argD,
            final double argV,
            final double argS) {
        leader.setPIDFSlot(argSlot, argP, argI, argD, argV, argS);
    }

    /* Controller Values */
    public void setSetpoint(final double argSetpoint) {}

    public double getSetpoint() {
        return 0;
    }

    public double getCLError() {
        if (leader == null) {
            return 0;
        }
        if (encCANCoder != null) {
            return leader.getCLError()
                    * gearRatio.getSensorRelToMechanism()
                    * gearRatio.getMechanismToUnits();
        } else if (encFX != null) {
            return leader.getCLError()
                    * gearRatio.getRotorToMechanism()
                    * gearRatio.getMechanismToUnits();
        }
        return 0;
    }

    public double getTolerance() {
        return 0;
    }

    /*
     *
     * Outputs
     *
     */
    public void stop() {
        allControllers.forEach(t -> t.stop());
    }

    /*
     * Open Loop
     */
    /* Duty Cycle */
    public double getOutputDuty() {
        return leader.getOutputDuty();
    }

    public void setOutputDuty(final double argOutDuty) {
        if (leader != null) {
            leader.setOutputDuty(argOutDuty);
        }
    }

    /* Voltage */
    public double getOutputVoltage() {
        double tmpVoltage = 0.0;

        for (ControllerBase tmpMotor : allControllers) {
            tmpVoltage += tmpMotor.getOutputVoltage();
        }

        if (!allControllers.isEmpty()) {
            tmpVoltage = tmpVoltage / allControllers.size();
        }

        return tmpVoltage;
    }

    public void setOutputVoltage(final double argOutputVolts) {
        if (leader != null) {
            leader.setOutputVoltage(argOutputVolts);
        }
    }

    /* Torque Current */
    public void setOutputTorqueCurrent(final double argTorqueCurrent) {
        // Override in TransmissionFX to delegate to ControllerTalonFX
    }

    /*
     * Closed Loop (CL)
     */
    public double getCLOutput() {
        return leader.getCLOutput();
    }

    public void setOutputPosition(final double argPositionRot, final double argFFVolt) {
        leader.setOutputPosition(argPositionRot, argFFVolt);
    }

    public void setOutputVelocity(final double argRPS, final double argFFVolt) {
        leader.setOutputVelocity(argRPS, argFFVolt);
    }

    /*
     *
     * Robot Loops
     *
     */
    public void robotPeriodicBefore(final double argTimestamp) {
        for (ControllerBase tmpMotor : allControllers) {
            tmpMotor.robotPeriodicBefore(argTimestamp);
        }
    }

    public void robotPeriodicAfter(final double argTimestamp) {
        // Unused
    }

    /*
     *
     * Simulation
     *
     */
    public void setSimVelRotations(final double argVelRotations) {
        if (encCANCoder != null) {
            encCANCoder.setSimVelRotations(argVelRotations);
        }

        for (ControllerBase tmpMotor : allControllers) {
            tmpMotor.setSimVelRotations(argVelRotations);
        }
    }

    public void setSimPosRotations(final double argPositionRotations) {
        if (encCANCoder != null) {
            encCANCoder.setSimPosRotations(argPositionRotations);
        }

        for (ControllerBase tmpMotor : allControllers) {
            tmpMotor.setSimPosRotations(argPositionRotations);
        }
    }

    public void simulationInit(final double argTimestamp) {
        for (ControllerBase tmpMotor : allControllers) {
            tmpMotor.simulationInit(argTimestamp);
        }

        if (encFX != null) {
            encFX.simulationInit(argTimestamp);
        }
        if (encCANCoder != null) {
            encCANCoder.simulationInit(argTimestamp);
        }

        if (revLimit != null) {
            revLimit.simulationInit(argTimestamp);
        }

        if (fwdLimit != null) {
            fwdLimit.simulationInit(argTimestamp);
        }
    }

    public void simulationPeriodic(final double argTimestamp) {
        for (ControllerBase tmpMotor : allControllers) {
            tmpMotor.simulationPeriodic(argTimestamp);
        }

        if (encFX != null) {
            encFX.simulationPeriodic(argTimestamp);
        }
        if (encCANCoder != null) {
            encCANCoder.simulationPeriodic(argTimestamp);
        }

        if (revLimit != null) {
            revLimit.simulationPeriodic(argTimestamp);
        }

        if (fwdLimit != null) {
            fwdLimit.simulationPeriodic(argTimestamp);
        }
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        for (ControllerBase tmpMotor : allControllers) {
            tmpMotor.outputTelemetry();
        }

        if (encFX != null) {
            encFX.outputTelemetry();
        }
        if (encCANCoder != null) {
            encCANCoder.outputTelemetry();
        }

        if (pid != null) {
            pid.outputTelemetry();
        }

        isBrakeMode.publish(getNeutralMode() == NeutralState.BRAKE);

        outputDuty.publish(getOutputDuty());
        outputVolt.publish(getOutputVoltage());
        outputCL.publish(getCLOutput());

        isGearOne.publish(shifterState == ShifterState.GEAR_1);

        if (revLimit != null) {
            revLimit.outputTelemetry();
        }

        if (fwdLimit != null) {
            fwdLimit.outputTelemetry();
        }

        tSetpoint.publish(getSetpoint());

        tError.publish(getCLError());
        tTol.publish(getTolerance());

        tMechPosFX.publish(getPosFX());
        tMechPosEnc.publish(getPos());

        tMechVelFX.publish(getVelFX());
        tMechVelEnc.publish(getVel());

        ntRotorToMechanism.publish(gearRatio.getRotorToMechanism());
        ntSensorRelToMechanism.publish(gearRatio.getSensorRelToMechanism());
    }
}
