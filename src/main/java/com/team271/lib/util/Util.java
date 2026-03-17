package com.team271.lib.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class Util {
    public static final double kEpsilon = 1e-12;

    /*
     * Prevent this class from being instantiated.
     */
    private Util() {}

    /*
     * Limits the given input to the given magnitude.
     */
    public static double limit(double v, double maxMagnitude) {
        return limit(v, -maxMagnitude, maxMagnitude);
    }

    public static double limit(double v, double min, double max) {
        return Math.min(max, Math.max(min, v));
    }

    public static boolean inRange(double v, double maxMagnitude) {
        return inRange(v, -maxMagnitude, maxMagnitude);
    }

    /*
     * Checks if the given input is within the range (min, max), both exclusive.
     */
    public static boolean inRange(double v, double min, double max) {
        return v > min && v < max;
    }

    public static double interpolate(double a, double b, double x) {
        x = limit(x, 0.0, 1.0);
        return a + (b - a) * x;
    }

    public static String joinStrings(final String delim, final List<?> strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); ++i) {
            sb.append(strings.get(i).toString());
            if (i < strings.size() - 1) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    public static boolean epsilonEquals(double a, double b, double epsilon) {
        return (a - epsilon <= b) && (a + epsilon >= b);
    }

    public static boolean epsilonEquals(double a, double b) {
        return epsilonEquals(a, b, kEpsilon);
    }

    public static boolean epsilonEquals(int a, int b, int epsilon) {
        return (a - epsilon <= b) && (a + epsilon >= b);
    }

    public static boolean allCloseTo(final List<Double> list, double value, double epsilon) {
        boolean result = true;
        for (Double value_in : list) {
            result &= epsilonEquals(value_in, value, epsilon);
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
            double pOut[], // out: resulting stick x value
            double x, // in: initial stick x value
            double y, // in: initial stick x value
            double deadZoneLow, // in: distance from zero to ignore
            double deadZoneHigh // in: distance from unit circle to ignore
            ) {
        double mag = Math.sqrt(x * x + y * y);

        if (mag > deadZoneLow) {
            // scale such that output magnitude is in the range [0.0f, 1.0f]
            double legalRange = 1.0f - deadZoneHigh - deadZoneLow;
            double normalizedMag = Math.min(1.0f, (mag - deadZoneLow) / legalRange);
            double scale = normalizedMag / mag;
            pOut[0] = x * scale;
            pOut[1] = y * scale;
        } else {
            // stick is in the inner dead zone
            pOut[0] = 0.0f;
            pOut[1] = 0.0f;
        }
    }

    /*
     * Convert -1.0 to 1.0 Tigger to 0.0 to 1.0
     */
    public static double convertTrigger(final double argValue) {
        return (argValue + 1.0) / 2.0;
    }

    public static double reMap(
            final double input,
            final double input_start,
            final double input_end,
            final double output_start,
            final double output_end) {
        double tmpOutput = 0.0;

        double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
        tmpOutput = output_start + slope * (input - input_start);

        return tmpOutput;
    }

    /**
     * @return the MAC address of the robot
     */
    public static String getMACAddress() {
        try {
            Enumeration<NetworkInterface> nwInterface = NetworkInterface.getNetworkInterfaces();
            StringBuilder ret = new StringBuilder();
            while (nwInterface.hasMoreElements()) {
                NetworkInterface nis = nwInterface.nextElement();
                if (nis != null && "eth0".equals(nis.getDisplayName())) {
                    byte[] mac = nis.getHardwareAddress();
                    if (mac != null) {
                        for (int i = 0; i < mac.length; i++) {
                            ret.append(
                                    String.format(
                                            "%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                        }
                        return ret.toString();
                    }
                }
            }
        } catch (SocketException | NullPointerException e) {
            e.printStackTrace();
        }

        return "";
    }
}
