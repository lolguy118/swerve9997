package com.team271.lib.sysid;

public class LoggerGeneral extends Logger {

    public double getMotorVoltage() {
        return motorVoltage;
    }

    public void log(
            final double argTimestamp,
            final double argVoltage,
            final double argMeasuredPosition,
            final double argMeasuredVelocity) {
        updateData(argTimestamp);
        if (data.size() < DATA_VECTOR_SIZE) {
            data.add(argTimestamp);
            data.add(argVoltage);
            data.add(argMeasuredPosition);
            data.add(argMeasuredVelocity);
        }
    }

    @Override
    public boolean isWrongMechanism() {
        return !(mechanism.equals("")
                || mechanism.equals("Arm")
                || mechanism.equals("Elevator")
                || mechanism.equals("Simple"));
    }
}
