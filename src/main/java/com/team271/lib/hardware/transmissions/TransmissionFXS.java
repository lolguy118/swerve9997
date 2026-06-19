package com.team271.lib.hardware.transmissions;

import com.ctre.phoenix6.hardware.TalonFXS;
import com.team271.lib.TObj;
import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.controllers.ControllerBase.ControllerStatus;
import com.team271.lib.hardware.controllers.ControllerTalonFXS;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * TalonFXS-backed transmission — the {@link ControllerTalonFXS} peer of {@link TransmissionFX}.
 *
 * <p>Mirrors {@link TransmissionFX}'s structure — a leader plus any number of followers added one
 * at a time with {@link #addFollower} ([ADR-019]) — but is scoped to the control surface that
 * {@code ControllerTalonFXS} supports: configuration (current/voltage limits, ramp rates, PID
 * slots), neutral mode, following, and the basic open-/closed-loop outputs inherited from {@link
 * TransmissionBase}. The TalonFX-only Motion Magic matrix and the {@code CTREMotor} vendor-neutral
 * wrapper are intentionally absent, because {@code CTREMotor} wraps {@code ControllerTalonFX} only.
 *
 * <p>Construct with the leader, then add followers as needed:
 *
 * <pre>{@code
 * TransmissionFXS tx = new TransmissionFXS(this, "Feeder", minion, new CANDeviceID(10));
 * tx.addFollower(new CANDeviceID(11), false);
 * }</pre>
 */
public class TransmissionFXS extends TransmissionBase {

    /*
     *
     * Constructors
     *
     */
    public TransmissionFXS(
            final TObj argParent,
            final String argName,
            final MotorBase argMotor,
            final CANDeviceID argCANIDMaster) {
        super(argParent, "(FXS)" + argName);

        mLeader = new ControllerTalonFXS(this, "(FXS)" + argName, argCANIDMaster, argMotor);
        mAllControllers.add(mLeader);
    }

    /*
     *
     * Get Motors
     *
     */
    public ControllerTalonFXS getLeaderController() {
        return ((ControllerTalonFXS) mLeader);
    }

    /** Passthrough — returns the raw CTRE TalonFXS leader device (ADR-005). */
    public TalonFXS getLeader() {
        return getLeaderController().getTalonFXS();
    }

    /*
     *
     * Followers
     *
     */

    /**
     * Adds a follower motor to this transmission. There is no follower-count limit (ADR-019): each
     * follower is constructed once here as a {@link ControllerTalonFXS}, set to follow the leader,
     * and registered with {@link #getAllControllers()} via {@link #registerFollower}. Its
     * control-request objects are pre-allocated at construction, so no allocation happens in a
     * periodic loop (CODE-GEN-004).
     *
     * @param argCANIDFollower the follower's CAN device ID (non-null, not already in use)
     * @param argOpposeLeader true if the follower spins opposite the leader
     */
    public void addFollower(final CANDeviceID argCANIDFollower, final boolean argOpposeLeader) {
        if (argCANIDFollower == null) {
            throw new IllegalArgumentException(
                    getName() + ": addFollower requires a non-null CAN ID");
        }
        /*
         * Reject a CAN ID already used by the leader or an existing follower — two CTRE device
         * objects on one CAN ID is a safety violation (.claude/rules/safety.md).
         */
        if (getMotor(argCANIDFollower) != null) {
            throw new IllegalArgumentException(
                    getName()
                            + ": addFollower CAN ID "
                            + argCANIDFollower
                            + " is already in use by this transmission");
        }
        final int followerNum = mAllControllers.size(); // leader occupies index 0
        final ControllerTalonFXS follower =
                new ControllerTalonFXS(
                        this,
                        getName() + "(F" + followerNum + ")",
                        argCANIDFollower,
                        getLeaderController().getMotor());
        final ControllerStatus followStatus =
                follower.follow(getLeaderController(), argOpposeLeader);
        if (followStatus != ControllerStatus.OK) {
            /* e.g. ERROR_INVALID_BUS on a cross-bus follower: the Follower control is never sent,
             * so the motor would sit idle. Surface it instead of failing silently. */
            DriverStation.reportError(
                    getName()
                            + ": addFollower follow() failed for "
                            + argCANIDFollower
                            + " (status="
                            + followStatus
                            + ")",
                    false);
        }
        registerFollower(follower);
    }
}
