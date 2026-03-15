package com.team271.lib.sysid;

public class LoggerGeneral extends Logger {

    public double getMotorVoltage() {
        return motorVoltage;
    }

    public void log(
            final double argTimestamp,
            final double voltage,
            final double measuredPosition,
            final double measuredVelocity) {
        updateData(argTimestamp);
        if (data.size() < DATA_VECTOR_SIZE) {
            data.add(argTimestamp);
            data.add(voltage);
            data.add(measuredPosition);
            data.add(measuredVelocity);
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
