package com.team271.lib.hardware.controllers;

/**
 * Library-owned gravity compensation type, independent of any vendor API.
 *
 * <p>Maps to CTRE's {@code GravityTypeValue} in TalonFX implementations. Future REV implementations
 * would apply equivalent feedforward logic in software.
 *
 * @see ControllerSmart#setGravityType(int, GravityType)
 */
public enum GravityType {
    /**
     * Gravity compensation using cosine of the mechanism angle. Appropriate for arms and pivots
     * where gravity torque varies with position.
     */
    ARM_COSINE,

    /**
     * Constant gravity compensation. Appropriate for elevators and linear mechanisms where gravity
     * force is constant regardless of position.
     */
    ELEVATOR_STATIC
}
