package com.team271.lib.util;

import edu.wpi.first.wpilibj.DriverStation;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public final class Util {
    public static final double kEpsilon = 1e-12;

    /*
     * Prevent this class from being instantiated.
     */
    private Util() {}

    /*
     * Limits the given input to the given magnitude.
     */
    public static double limit(final double argV, final double argMaxMagnitude) {
        return limit(argV, -argMaxMagnitude, argMaxMagnitude);
    }

    public static double limit(final double argV, final double argMin, final double argMax) {
        return Math.min(argMax, Math.max(argMin, argV));
    }

    public static boolean inRange(final double argV, final double argMaxMagnitude) {
        return inRange(argV, -argMaxMagnitude, argMaxMagnitude);
    }

    /*
     * Checks if the given input is within the range (min, max), both exclusive.
     */
    public static boolean inRange(final double argV, final double argMin, final double argMax) {
        return argV > argMin && argV < argMax;
    }

    public static double interpolate(final double argA, final double argB, final double argX) {
        final double t = limit(argX, 0.0, 1.0);
        return argA + (argB - argA) * t;
    }

    public static boolean epsilonEquals(
            final double argA, final double argB, final double argEpsilon) {
        return (argA - argEpsilon <= argB) && (argA + argEpsilon >= argB);
    }

    public static boolean epsilonEquals(final double argA, final double argB) {
        return epsilonEquals(argA, argB, kEpsilon);
    }

    public static boolean epsilonEquals(final int argA, final int argB, final int argEpsilon) {
        return (argA - argEpsilon <= argB) && (argA + argEpsilon >= argB);
    }

    public static boolean allCloseTo(
            final List<Double> argList, final double argValue, final double argEpsilon) {
        boolean result = true;
        for (Double valueIn : argList) {
            result &= epsilonEquals(valueIn, argValue, argEpsilon);
        }
        return result;
    }

    /*
     * Simple Deadzone and Rescale to -1.0 to 1.0
     * https://www.desmos.com/calculator/n1wwix0gjd
     */
    public static double handleDeadzone(final double argValue, final double argDeadband) {
        double tmpValue = Math.abs(argValue);
        double tmpDeadband = Math.abs(argDeadband);

        if (tmpDeadband >= 1.0) {
            return 0.0;
        }

        tmpValue = (tmpValue - tmpDeadband) / (1.0 - tmpDeadband);

        if (Math.abs(argValue) < tmpDeadband) {
            tmpValue = 0.0;
        } else if (argValue < -tmpDeadband) {
            tmpValue *= -1.0;
        }

        return tmpValue;
    }

    /*
     * Advanced Radial Deadzone and Rescale to -1.0 to 1.0
     * https://www.desmos.com/calculator/5olzcaocch
     */
    public static void handleDeadzone_Radial(
            final double argPOut[], // out: resulting stick x value
            final double argX, // in: initial stick x value
            final double argY, // in: initial stick x value
            final double argDeadZoneLow, // in: distance from zero to ignore
            final double argDeadZoneHigh // in: distance from unit circle to ignore
            ) {
        double mag = Math.sqrt(argX * argX + argY * argY);

        if (mag > argDeadZoneLow) {
            // scale such that output magnitude is in the range [0.0, 1.0]
            double legalRange = 1.0 - argDeadZoneHigh - argDeadZoneLow;
            double normalizedMag = Math.min(1.0, (mag - argDeadZoneLow) / legalRange);
            double scale = normalizedMag / mag;
            argPOut[0] = argX * scale;
            argPOut[1] = argY * scale;
        } else {
            // stick is in the inner dead zone
            argPOut[0] = 0.0;
            argPOut[1] = 0.0;
        }
    }

    /*
     * Convert -1.0 to 1.0 Tigger to 0.0 to 1.0
     */
    public static double convertTrigger(final double argValue) {
        return (argValue + 1.0) / 2.0;
    }

    public static double reMap(
            final double argInput,
            final double argInputStart,
            final double argInputEnd,
            final double argOutputStart,
            final double argOutputEnd) {
        double slope = 1.0 * (argOutputEnd - argOutputStart) / (argInputEnd - argInputStart);
        return argOutputStart + slope * (argInput - argInputStart);
    }

    /**
     * Returns the MAC address of the first non-loopback network interface with a hardware address.
     * On the roboRIO this is typically the ethernet port.
     *
     * @return the MAC address in "XX:XX:XX:XX:XX:XX" format, or empty string if unavailable
     */
    public static String getMACAddress() {
        try {
            Enumeration<NetworkInterface> nwInterface = NetworkInterface.getNetworkInterfaces();
            while (nwInterface != null && nwInterface.hasMoreElements()) {
                NetworkInterface nis = nwInterface.nextElement();
                if (nis == null || nis.isLoopback()) {
                    continue;
                }
                byte[] mac = nis.getHardwareAddress();
                if (mac != null) {
                    StringBuilder ret = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        ret.append(
                                String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    return ret.toString();
                }
            }
        } catch (SocketException e) {
            DriverStation.reportWarning("Util.getMACAddress: " + e.getMessage(), false);
        }

        return "";
    }
}
