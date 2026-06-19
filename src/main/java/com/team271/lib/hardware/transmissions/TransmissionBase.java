package com.team271.lib.hardware.transmissions;

import static edu.wpi.first.units.Units.*;

import com.team271.lib.TObj;
import com.team271.lib.control.pid.PIDBase;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerBase.ControllerStatus;
import com.team271.lib.hardware.controllers.ControllerBase.MotorDirection;
import com.team271.lib.hardware.controllers.ControllerBase.NeutralState;
import com.team271.lib.hardware.controllers.ControllerSmart;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.sensors.encoders.CANCoderAdapter;
import com.team271.lib.hardware.sensors.encoders.EncoderAdapter;
import com.team271.lib.hardware.sensors.encoders.EncoderBase.EncoderDirection;
import com.team271.lib.hardware.sensors.encoders.EncoderCANCoder;
import com.team271.lib.hardware.sensors.encoders.EncoderFX;
import com.team271.lib.hardware.sensors.encoders.FXEncoderAdapter;
import com.team271.lib.hardware.sensors.switches.SwitchBase;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchTrigger;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchType;
import com.team271.lib.hardware.sensors.switches.SwitchFX;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.util.Elastic;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway.Init")
public abstract class TransmissionBase extends TObj {
    public enum ShifterState {
        GEAR_NONE,
        GEAR_1,
        GEAR_2
    }

    /*
     * Constants
     */
    private static final double CONFIG_ERROR_NOTIFICATION_THROTTLE_SEC = 2.0;
    private static final double KRAKENX44_NOMINAL_VOLTAGE = 12.0;
    private static final double KRAKENX44_STALL_TORQUE_NM = 4.05;
    private static final double KRAKENX44_STALL_CURRENT_A = 275.0;
    private static final double KRAKENX44_FREE_CURRENT_A = 1.4;
    private static final double KRAKENX44_FREE_SPEED_RPM = 7530.0;

    /*
     * Transmission
     */

    /*
     * Motors
     */
    protected ControllerSmart mLeader;

    /**
     * @deprecated The follower count is no longer capped (ADR-019). Construct with the leader only
     *     and call {@code addFollower(...)} on the concrete transmission; use {@link
     *     #getAllControllers()} as the single source of truth. These fields are still populated for
     *     the first three followers for backward compatibility.
     */
    @Deprecated protected ControllerSmart mFollower1;

    /**
     * @deprecated See {@link #mFollower1}.
     */
    @Deprecated protected ControllerSmart mFollower2;

    /**
     * @deprecated See {@link #mFollower1}.
     */
    @Deprecated protected ControllerSmart mFollower3;

    protected Set<ControllerSmart> mAllControllers = new LinkedHashSet<>();

    /*
     *
     * Sensors
     *
     */

    /*
     * Encoder — unified adapter (eliminates per-type conditional cascades)
     */
    protected EncoderAdapter mEncoder;
    protected GearRatio mGearRatio = GearRatio.IDENTITY;

    /*
     * Raw encoder references (kept for subclass access and backward compatibility)
     */
    protected EncoderFX mEncFX;
    protected EncoderCANCoder mEncCANCoder;

    /*
     * Limit Switches
     */
    protected static final double LIMIT_UPDATE_FREQ_HZ = 250.0;
    private double mLastConfigErrorNotificationTime = 0;
    protected SwitchBase mRevLimit;
    protected SwitchBase mFwdLimit;

    /*
     * Shifters
     */
    protected Shifter mShifter;
    protected ShifterState mShifterState = ShifterState.GEAR_NONE;

    protected double mSensorRatioGear1 = 1.0;
    protected double mSensorRatioGear2 = 1.0;

    /*
     * Control Loops
     */
    protected PIDBase mPid;

    /*
     *
     * Simulation
     *
     */
    protected DCMotor mSimDCMotor;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntIsBrakeMode = new NTEntry(table, "Is Brake Mode", false);

    final NTEntry ntOutputDuty = new NTEntry(table, "Output(DutyCycle)", 0.0);
    final NTEntry ntOutputVolt = new NTEntry(table, "Output(Voltage)", 0.0);
    final NTEntry ntOutputCL = new NTEntry(table, "Output(ClosedLoop)", 0.0);

    final NTEntry ntIsGearOne = new NTEntry(table, "Is Gear 1", false);

    final NTEntry ntSetpoint = new NTEntry(table, "Setpoint", 0.0);

    final NTEntry ntError = new NTEntry(table, "Error", 0.0);

    final NTEntry ntTol = new NTEntry(table, "Tol", 0.0);

    final NTEntry ntMechPosFX = new NTEntry(table, "Mech Pos FX", 0.0);
    final NTEntry ntMechPosEnc = new NTEntry(table, "Mech Pos Enc", 0.0);

    final NTEntry ntMechVelFX = new NTEntry(table, "Mech Vel FX", 0.0);
    final NTEntry ntMechVelEnc = new NTEntry(table, "Mech Vel Enc", 0.0);

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

        for (ControllerSmart tmpMotor : mAllControllers) {
            tmpMotor.robotInit(argTimestamp);
            ++tmpNumMotors;
        }

        if (mEncFX != null) {
            mEncFX.robotInit(argTimestamp);
        }
        if (mEncCANCoder != null) {
            mEncCANCoder.robotInit(argTimestamp);
        }

        if (mRevLimit != null) {
            mRevLimit.robotInit(argTimestamp);
        }

        if (mFwdLimit != null) {
            mFwdLimit.robotInit(argTimestamp);
        }

        if (mLeader == null) {
            DriverStation.reportError(getName() + ": leader is null in robotInit()", false);
            return;
        }

        switch (mLeader.getMotor().getMotorType()) {
            case FALCON500:
                mSimDCMotor = DCMotor.getFalcon500Foc(tmpNumMotors);
                break;
            case KRAKENX60:
                mSimDCMotor = DCMotor.getKrakenX60Foc(tmpNumMotors);
                break;
            case KRAKENX44:
                mSimDCMotor =
                        new DCMotor(
                                KRAKENX44_NOMINAL_VOLTAGE,
                                KRAKENX44_STALL_TORQUE_NM,
                                KRAKENX44_STALL_CURRENT_A,
                                KRAKENX44_FREE_CURRENT_A,
                                RPM.of(KRAKENX44_FREE_SPEED_RPM).in(RadiansPerSecond),
                                tmpNumMotors);
                break;
            case CTRE_MINION:
                // CTRE Minion specs not yet published; default to KrakenX60 for simulation
                mSimDCMotor = DCMotor.getKrakenX60Foc(tmpNumMotors);
                DriverStation.reportWarning(
                        getName() + ": CTRE_MINION sim uses KrakenX60 approximation", false);
                break;
            case NEO:
                mSimDCMotor = DCMotor.getNEO(tmpNumMotors);
                break;
            case NEO550:
                mSimDCMotor = DCMotor.getNeo550(tmpNumMotors);
                break;
            case NEO_VORTEX:
                mSimDCMotor = DCMotor.getNeoVortex(tmpNumMotors);
                break;
            default:
                // Unknown motor type — default to KrakenX60 to avoid divide-by-zero in sim
                mSimDCMotor = DCMotor.getKrakenX60Foc(tmpNumMotors);
                DriverStation.reportWarning(
                        getName()
                                + ": unknown motor type "
                                + mLeader.getMotor().getMotorType()
                                + " — sim uses KrakenX60 approximation",
                        false);
                break;
        }
    }

    /*
     *
     * Get Motors
     *
     */
    public Set<ControllerSmart> getAllControllers() {
        return mAllControllers;
    }

    /**
     * Registers an already-constructed follower controller with this transmission. The follower
     * joins {@link #getAllControllers()} — the single source of truth for every per-motor operation
     * (config, current limits, neutral mode, stop, telemetry, simulation). For the first three
     * followers it also populates the deprecated {@code mFollower1..3} fields for backward
     * compatibility. There is no follower-count limit (ADR-019).
     *
     * <p>Concrete transmissions construct their vendor-specific follower controller and pass it
     * here; the controller's control-request objects are pre-allocated at construction, so no
     * allocation happens in a periodic loop (CODE-GEN-004).
     *
     * @param argFollower the follower controller to register (non-null)
     */
    protected void registerFollower(final ControllerSmart argFollower) {
        if (argFollower == null) {
            throw new IllegalArgumentException(
                    getName() + ": registerFollower requires a non-null follower");
        }
        mAllControllers.add(argFollower);
        if (mFollower1 == null) {
            mFollower1 = argFollower;
        } else if (mFollower2 == null) {
            mFollower2 = argFollower;
        } else if (mFollower3 == null) {
            mFollower3 = argFollower;
        }
    }

    @Nullable
    public ControllerSmart getMotor(final CANDeviceID argMotor) {
        for (ControllerSmart tmpMotor : mAllControllers) {
            if (argMotor.equals(tmpMotor.getID())) {
                return tmpMotor;
            }
        }

        return null;
    }

    @Nullable
    public DCMotor getDCMotor() {
        return mSimDCMotor;
    }

    /*
     *
     * Config
     *
     */
    public void applyConfigs() {
        for (ControllerSmart c : mAllControllers) {
            ControllerStatus status = c.applyConfig();
            if (status != ControllerStatus.OK) {
                double now = Timer.getFPGATimestamp();
                if (now - mLastConfigErrorNotificationTime
                        > CONFIG_ERROR_NOTIFICATION_THROTTLE_SEC) {
                    mLastConfigErrorNotificationTime = now;
                    String msg =
                            getName()
                                    + ": config apply failed for "
                                    + c.getName()
                                    + " (status="
                                    + status
                                    + ")";
                    DriverStation.reportWarning(msg, false);
                    Elastic.sendNotification(
                            new Elastic.Notification(
                                    Elastic.NotificationLevel.ERROR, "Config Apply Failed", msg));
                }
            }
        }
    }

    /*
     * Neutral Mode
     */
    public NeutralState getNeutralMode() {
        return mLeader.getNeutralMode();
    }

    public void setNeutralMode(final NeutralState argNeutralState) {
        mAllControllers.forEach(controller -> controller.setNeutralMode(argNeutralState));
    }

    /*
     * Direction
     */
    public void configDirection(final MotorDirection argDirection) {
        mLeader.setDirection(argDirection);
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
            if (!(mLeader instanceof ControllerTalonFX)) {
                DriverStation.reportError(
                        getName()
                                + ": configLimitFwd requires ControllerTalonFX leader, got "
                                + mLeader.getClass().getSimpleName(),
                        false);
                return;
            }
            mFwdLimit =
                    new SwitchFX(
                            this,
                            name + "(Fwd)",
                            (ControllerTalonFX) mLeader,
                            true,
                            argLimitType,
                            argAutoZero,
                            argAutoZeroValueInches,
                            LIMIT_UPDATE_FREQ_HZ);

            mFwdLimit.setEnabled(argEnable);
        }
    }

    public void configLimitRev(
            final SwitchType argSwitchType,
            final boolean argEnable,
            final SwitchTrigger argLimitType,
            final boolean argAutoZero,
            final double argAutoZeroValueInches) {

        if (argSwitchType == SwitchType.FX) {
            if (!(mLeader instanceof ControllerTalonFX)) {
                DriverStation.reportError(
                        getName()
                                + ": configLimitRev requires ControllerTalonFX leader, got "
                                + mLeader.getClass().getSimpleName(),
                        false);
                return;
            }
            mRevLimit =
                    new SwitchFX(
                            this,
                            name + "(Rev)",
                            (ControllerTalonFX) mLeader,
                            false,
                            argLimitType,
                            argAutoZero,
                            argAutoZeroValueInches,
                            LIMIT_UPDATE_FREQ_HZ);

            mRevLimit.setEnabled(argEnable);
        }
    }

    /*
     * Current Limits
     */
    public void configCurrentLimitStator(final boolean argEnable, final double argStatorCurrent) {
        /* Apply to all controllers (leader + followers) */
        mAllControllers.forEach(
                controller -> controller.setCurrentLimitStator(argEnable, argStatorCurrent));
    }

    public void configCurrentLimitSupply(final boolean argEnable, final double argSupplyCurrent) {
        /* Apply to all controllers (leader + followers) */
        mAllControllers.forEach(
                controller -> controller.setCurrentLimitSupply(argEnable, argSupplyCurrent));
    }

    public void configCurrentLimitSupply(
            final double argSupplyCurrentLimit,
            final double argTime,
            final double argSupplyCurrentLowerLimit) {
        /* Apply to all controllers (leader + followers) */
        mAllControllers.forEach(
                controller ->
                        controller.setCurrentLimitSupply(
                                argSupplyCurrentLimit, argTime, argSupplyCurrentLowerLimit));
    }

    /*
     * Voltage Limits
     */
    public void configVoltagePeak(
            final double argFwdVoltage, final double argRevVoltage, final double argTimeFilter) {
        /* Apply to leader only */
        mLeader.setVoltagePeak(argFwdVoltage, argRevVoltage, argTimeFilter);
    }

    /*
     * Ramping Open Loop
     */
    public void configRampOpenLoopDuty(final double argRampRateSec) {
        /* Apply to leader only */
        mLeader.setRampOpenLoopDuty(argRampRateSec);
    }

    public void configRampOpenLoopVoltage(final double argRampRateSec) {
        /* Apply to leader only */
        mLeader.setRampOpenLoopVoltage(argRampRateSec);
    }

    /*
     * Ramping Closed Loop
     */
    public void configRampClosedLoopDuty(final double argRampRateSec) {
        /* Apply to leader only */
        mLeader.setRampClosedLoopDuty(argRampRateSec);
    }

    public void configRampClosedLoopVoltage(final double argRampRateSec) {
        /* Apply to leader only */
        mLeader.setRampClosedLoopVoltage(argRampRateSec);
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
        mLeader.setPIDFSlot(argSlot, argP, argI, argD, argV, argS);
    }

    /*
     *
     * Encoders
     *
     */
    public void addEncoderFX(final double argUpdateFreqHz) {
        if (!(mLeader instanceof ControllerTalonFX)) {
            DriverStation.reportError(
                    getName()
                            + ": addEncoderFX requires ControllerTalonFX leader, got "
                            + mLeader.getClass().getSimpleName(),
                    false);
            return;
        }
        mEncFX = new EncoderFX(this, name, (ControllerTalonFX) mLeader, argUpdateFreqHz);
        /* Only set as active adapter if no CANCoder is present (CANCoder takes priority) */
        if (mEncCANCoder == null) {
            mEncoder = new FXEncoderAdapter(mEncFX, mGearRatio);
        }
    }

    public EncoderFX getEncoderFX() {
        return mEncFX;
    }

    public void addCANCoder(
            final CANDeviceID argCANIDEnc,
            final EncoderDirection argEncoderDir,
            final double argUpdateFreqHz) {
        mEncCANCoder = new EncoderCANCoder(this, name, argCANIDEnc, argEncoderDir, argUpdateFreqHz);
        /* CANCoder always becomes the active adapter when present */
        mEncoder = new CANCoderAdapter(mEncCANCoder, mGearRatio);
    }

    public EncoderCANCoder getEncoderCANCoder() {
        return mEncCANCoder;
    }

    /** Returns the active encoder adapter, or null if no encoder is configured. */
    public EncoderAdapter getEncoder() {
        return mEncoder;
    }

    public void resetEncoders() {
        if (mEncFX != null) {
            mEncFX.reset();
        }
        if (mEncCANCoder != null) {
            mEncCANCoder.reset();
        }
    }

    /*
     * Position - Rotations
     */
    public void setPosRotations(final double argPositionRotations) {
        if (mEncoder != null) {
            mEncoder.setPositionRotations(argPositionRotations);
        }
    }

    public double getPosAbsRotations() {
        if (mEncCANCoder != null) {
            return mEncCANCoder.getPosAbsRotations();
        }
        return 0.0;
    }

    public double getPosRotations() {
        if (mEncoder != null) {
            return mEncoder.getPositionRotations();
        }
        return 0.0;
    }

    /*
     * Position - Mechanism Output Units
     */
    public double getPosFX() {
        if (mEncFX != null) {
            return mGearRatio.rotorToOutput(mEncFX.getPosRotations());
        }
        return 0.0;
    }

    public double getPos() {
        if (mEncoder != null) {
            return mEncoder.getPosition();
        }
        return 0.0;
    }

    public double getPosAbs() {
        if (mEncoder != null) {
            return mEncoder.getAbsolutePosition();
        }
        return 0.0;
    }

    /*
     * Gear Ratio Configuration
     */
    public void setRotorToMechanism(final double argRotorToMechanism) {
        mGearRatio =
                new GearRatio(
                        argRotorToMechanism,
                        mGearRatio.getSensorRelToMechanism(),
                        mGearRatio.getSensorAbsToMechanism(),
                        mGearRatio.getMechanismToUnits());
        if (mEncoder != null) {
            mEncoder.updateGearRatio(mGearRatio);
        }
    }

    public void setSensorToMechanism(final double argSensorRelToMechanism) {
        mGearRatio =
                new GearRatio(
                        mGearRatio.getRotorToMechanism(),
                        argSensorRelToMechanism,
                        mGearRatio.getSensorAbsToMechanism(),
                        mGearRatio.getMechanismToUnits());
        if (mEncoder != null) {
            mEncoder.updateGearRatio(mGearRatio);
        }
    }

    public void setSensorAbsToMechanism(final double argSensorAbsToMechanism) {
        mGearRatio =
                new GearRatio(
                        mGearRatio.getRotorToMechanism(),
                        mGearRatio.getSensorRelToMechanism(),
                        argSensorAbsToMechanism,
                        mGearRatio.getMechanismToUnits());
        if (mEncoder != null) {
            mEncoder.updateGearRatio(mGearRatio);
        }
    }

    public void setMechanismToUnits(final double argMechanismToUnits) {
        mGearRatio =
                new GearRatio(
                        mGearRatio.getRotorToMechanism(),
                        mGearRatio.getSensorRelToMechanism(),
                        mGearRatio.getSensorAbsToMechanism(),
                        argMechanismToUnits);
        if (mEncoder != null) {
            mEncoder.updateGearRatio(mGearRatio);
        }
    }

    /** Sets the full gear ratio. Propagates to the encoder adapter. */
    public void setGearRatio(final GearRatio argGearRatio) {
        mGearRatio = argGearRatio;
        if (mEncoder != null) {
            mEncoder.updateGearRatio(mGearRatio);
        }
    }

    public GearRatio getGearRatio() {
        return mGearRatio;
    }

    /*
     * Velocity - Rotations Per Second (RPS)
     */
    public double getVelFXRPS() {
        if (mEncFX != null) {
            return mEncFX.getVelRPS();
        }
        return 0.0;
    }

    public double getVelRPS() {
        if (mEncoder != null) {
            return mEncoder.getVelocityRPS();
        }
        return 0.0;
    }

    /*
     * Velocity - Mechanism Output Units
     */
    public double getVelFX() {
        if (mEncFX != null) {
            return mGearRatio.rotorToOutput(mEncFX.getVelRPS());
        }
        return 0.0;
    }

    public double getVel() {
        if (mEncoder != null) {
            return mEncoder.getVelocity();
        }
        return 0.0;
    }

    /*
     *
     * Limit Switches
     *
     */
    public boolean getRevLimit() {
        if (mRevLimit != null) {
            return mRevLimit.getTriggered();
        }

        return false;
    }

    public boolean getFwdLimit() {
        if (mFwdLimit != null) {
            return mFwdLimit.getTriggered();
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
        mShifter = argShifter;
    }

    /** Set a shifter with per-gear sensor ratios. */
    public void setShifter(
            final Shifter argShifter, final double argSensorRatio1, final double argSensorRatio2) {
        mSensorRatioGear1 = argSensorRatio1;
        mSensorRatioGear2 = argSensorRatio2;

        setShifter(argShifter);
    }

    /**
     * Convenience method: create a pneumatic shifter from solenoid channels. Equivalent to {@code
     * setShifter(new ShifterPneumatic(chGear1, chGear2))}.
     */
    /** Sentinel value indicating no solenoid channel is configured. */
    public static final int NO_SOLENOID_CHANNEL = -1;

    public void addShifter(
            final int argPneumaticHubCanId, final int argChGear1, final int argChGear2) {
        if (argChGear1 != NO_SOLENOID_CHANNEL && argChGear2 != NO_SOLENOID_CHANNEL) {
            setShifter(new ShifterPneumatic(argPneumaticHubCanId, argChGear1, argChGear2));
        }
    }

    /** Convenience method: create a pneumatic shifter with per-gear sensor ratios. */
    public void addShifter(
            final int argPneumaticHubCanId,
            final int argChGear1,
            final double argSensorRatio1,
            final int argChGear2,
            final double argSensorRatio2) {
        mSensorRatioGear1 = argSensorRatio1;
        mSensorRatioGear2 = argSensorRatio2;

        addShifter(argPneumaticHubCanId, argChGear1, argChGear2);
    }

    public ShifterState shift(final ShifterState argShiftTo) {
        /*
         * GEAR_NONE is the uninitialized state, not a valid shift target.
         */
        if (argShiftTo == ShifterState.GEAR_NONE) {
            DriverStation.reportWarning(
                    getName() + ": shift(GEAR_NONE) ignored — not a valid shift target", false);
            return mShifterState;
        }

        /*
         * - Actuate shifter mechanism
         * - Update gear state
         */
        if (mShifterState != argShiftTo) {
            if (mShifter != null) {
                mShifter.actuate(argShiftTo);
            }
            mShifterState = argShiftTo;
        }

        return mShifterState;
    }

    /*
     *
     * Closed Loop
     *
     */

    /* PID Values */
    public void setPSlot(final int argSlot, final double argSetPSlot0) {
        mLeader.setPSlot(argSlot, argSetPSlot0);
    }

    public double getPSlot(final int argSlot) {
        return mLeader.getPSlot(argSlot);
    }

    public void setISlot(final int argSlot, final double argSetISlot0) {
        mLeader.setISlot(argSlot, argSetISlot0);
    }

    public double getISlot(final int argSlot) {
        return mLeader.getISlot(argSlot);
    }

    public void setDSlot(final int argSlot, final double argSetDSlot0) {
        mLeader.setDSlot(argSlot, argSetDSlot0);
    }

    public double getDSlot(final int argSlot) {
        return mLeader.getDSlot(argSlot);
    }

    public double getVSlot(final int argSlot) {
        return mLeader.getVSlot(argSlot);
    }

    public double getSSlot(final int argSlot) {
        return mLeader.getSSlot(argSlot);
    }

    public void setPIDFSlot(
            final int argSlot,
            final double argP,
            final double argI,
            final double argD,
            final double argV,
            final double argS) {
        mLeader.setPIDFSlot(argSlot, argP, argI, argD, argV, argS);
    }

    /* Controller Values */
    public void setSetpoint(final double argSetpoint) {}

    public double getSetpoint() {
        return 0;
    }

    public double getCLError() {
        if (mLeader == null) {
            return 0;
        }
        if (mEncCANCoder != null) {
            return mLeader.getCLError()
                    * mGearRatio.getSensorRelToMechanism()
                    * mGearRatio.getMechanismToUnits();
        } else if (mEncFX != null) {
            return mLeader.getCLError()
                    * mGearRatio.getRotorToMechanism()
                    * mGearRatio.getMechanismToUnits();
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
        mAllControllers.forEach(controller -> controller.stop());
    }

    /*
     * Open Loop
     */
    /* Duty Cycle */
    public double getOutputDuty() {
        return mLeader.getOutputDuty();
    }

    public void setOutputDuty(final double argOutDuty) {
        if (mLeader != null) {
            mLeader.setOutputDuty(argOutDuty);
        }
    }

    /* Voltage */
    public double getOutputVoltage() {
        double tmpVoltage = 0.0;

        for (ControllerSmart tmpMotor : mAllControllers) {
            tmpVoltage += tmpMotor.getOutputVoltage();
        }

        if (!mAllControllers.isEmpty()) {
            tmpVoltage = tmpVoltage / mAllControllers.size();
        }

        return tmpVoltage;
    }

    public void setOutputVoltage(final double argOutputVolts) {
        if (mLeader != null) {
            mLeader.setOutputVoltage(argOutputVolts);
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
        return mLeader.getCLOutput();
    }

    public void setOutputPosition(final double argPositionRot, final double argFFVolt) {
        mLeader.setOutputPosition(argPositionRot, argFFVolt);
    }

    public void setOutputVelocity(final double argRPS, final double argFFVolt) {
        mLeader.setOutputVelocity(argRPS, argFFVolt);
    }

    /*
     *
     * Robot Loops
     *
     */
    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        for (ControllerSmart tmpMotor : mAllControllers) {
            tmpMotor.robotPeriodicBefore(argTimestamp);
        }
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
        // Unused
    }

    /*
     *
     * Simulation
     *
     */
    public void setSimVelRotations(final double argVelRotations) {
        if (mEncCANCoder != null) {
            mEncCANCoder.setSimVelRotations(argVelRotations);
        }

        for (ControllerSmart tmpMotor : mAllControllers) {
            tmpMotor.setSimVelRotations(argVelRotations);
        }
    }

    public void setSimPosRotations(final double argPositionRotations) {
        if (mEncCANCoder != null) {
            mEncCANCoder.setSimPosRotations(argPositionRotations);
        }

        for (ControllerSmart tmpMotor : mAllControllers) {
            tmpMotor.setSimPosRotations(argPositionRotations);
        }
    }

    @Override
    public void simulationInit(final double argTimestamp) {
        for (ControllerSmart tmpMotor : mAllControllers) {
            tmpMotor.simulationInit(argTimestamp);
        }

        if (mEncFX != null) {
            mEncFX.simulationInit(argTimestamp);
        }
        if (mEncCANCoder != null) {
            mEncCANCoder.simulationInit(argTimestamp);
        }

        if (mRevLimit != null) {
            mRevLimit.simulationInit(argTimestamp);
        }

        if (mFwdLimit != null) {
            mFwdLimit.simulationInit(argTimestamp);
        }
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        for (ControllerSmart tmpMotor : mAllControllers) {
            tmpMotor.simulationPeriodic(argTimestamp);
        }

        if (mEncFX != null) {
            mEncFX.simulationPeriodic(argTimestamp);
        }
        if (mEncCANCoder != null) {
            mEncCANCoder.simulationPeriodic(argTimestamp);
        }

        if (mRevLimit != null) {
            mRevLimit.simulationPeriodic(argTimestamp);
        }

        if (mFwdLimit != null) {
            mFwdLimit.simulationPeriodic(argTimestamp);
        }
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        for (ControllerSmart tmpMotor : mAllControllers) {
            tmpMotor.outputTelemetry();
        }

        if (mEncFX != null) {
            mEncFX.outputTelemetry();
        }
        if (mEncCANCoder != null) {
            mEncCANCoder.outputTelemetry();
        }

        if (mPid != null) {
            mPid.outputTelemetry();
        }

        ntIsBrakeMode.publish(getNeutralMode() == NeutralState.BRAKE);

        ntOutputDuty.publish(getOutputDuty());
        ntOutputVolt.publish(getOutputVoltage());
        ntOutputCL.publish(getCLOutput());

        ntIsGearOne.publish(mShifterState == ShifterState.GEAR_1);

        if (mRevLimit != null) {
            mRevLimit.outputTelemetry();
        }

        if (mFwdLimit != null) {
            mFwdLimit.outputTelemetry();
        }

        ntSetpoint.publish(getSetpoint());

        ntError.publish(getCLError());
        ntTol.publish(getTolerance());

        ntMechPosFX.publish(getPosFX());
        ntMechPosEnc.publish(getPos());

        ntMechVelFX.publish(getVelFX());
        ntMechVelEnc.publish(getVel());

        ntRotorToMechanism.publish(mGearRatio.getRotorToMechanism());
        ntSensorRelToMechanism.publish(mGearRatio.getSensorRelToMechanism());
    }
}
