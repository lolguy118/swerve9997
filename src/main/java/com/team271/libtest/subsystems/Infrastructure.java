package com.team271.libtest.subsystems;

//import au.grapplerobotics.CanBridge;
//import au.grapplerobotics.MitoCANdria;

import com.team271.lib.TObj;
import com.team271.lib.subsystem.Subsystem;

import edu.wpi.first.net.WebServer;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.BatterySim;

/**
 * Subsystem to ensure the compressor never runs while the superstructure moves
 */
public class Infrastructure extends Subsystem {
    private static Infrastructure mInstance;

    private boolean mIsTeleop = false;

    //private MitoCANdria mito = null;

    public static Infrastructure getInstance(final TObj argParent) {
        if (mInstance == null) {
            mInstance = new Infrastructure(argParent);
        }

        return mInstance;
    }

    public Infrastructure(final TObj argParent) {
        super(argParent, "Infrastructure");

        RobotController.setEnabled3V3(false);
        RobotController.setEnabled5V(false); // Needed for DIO +5 Rail
        RobotController.setEnabled6V(false);

        RobotController.setBrownoutVoltage(6.3);

        /*
         * MitoCANdria Setup
         */
        /*
        try {
            CanBridge.runTCP();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

        try {
            mito = new MitoCANdria(1);

            // Get and print USB1 current
            mito.getChannelCurrent(MitoCANdria.MITOCANDRIA_CHANNEL_USB1)
                    .ifPresentOrElse(
                            current -> System.out.println("USB1 current: " + current + " A"),
                            () -> System.out.println("Couldn't get USB1 current"));

            // Get and print 5VA voltage
            mito.getChannelVoltage(MitoCANdria.MITOCANDRIA_CHANNEL_5VA)
                    .ifPresentOrElse(
                            voltage -> System.out.println("5VA voltage: " + voltage + " V"),
                            () -> System.out.println("Couldn't get 5VA voltage"));

            // Enable USB2 channel
            mito.setChannelEnabled(MitoCANdria.MITOCANDRIA_CHANNEL_5VA, false);
            System.out.println("5VA channel enabled");

            // Set ADJ channel voltage
            mito.setChannelVoltage(MitoCANdria.MITOCANDRIA_CHANNEL_ADJ, 3.3);
            System.out.println("ADJ channel voltage set to 3.3V");

            // Get and print ADJ channel setpoint
            mito.getChannelVoltageSetpoint(MitoCANdria.MITOCANDRIA_CHANNEL_ADJ)
                    .ifPresentOrElse(
                            setpoint -> System.out.println("ADJ channel setpoint: " + setpoint + " V"),
                            () -> System.out.println("Couldn't get ADJ channel setpoint"));

            // Check if 5VB channel is enabled
            mito.getChannelEnabled(MitoCANdria.MITOCANDRIA_CHANNEL_5VB)
                    .ifPresentOrElse(
                            enabled -> System.out.println("5VB channel enabled: " + (enabled == 1)),
                            () -> System.out.println("Couldn't check if 5VB channel is enabled"));

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
*/
        /*
         * Elastic Setup
         */
        try {
            WebServer.start(5800, Filesystem.getDeployDirectory().getPath());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

    }

    public synchronized void setIsTeleop(final boolean isTeleop) {
        mIsTeleop = isTeleop;
    }

    public synchronized boolean isTeleop() {
        return mIsTeleop;
    }

    public double getBatteryVoltage(double... argCurrents)
    {
        return BatterySim.calculateLoadedBatteryVoltage(12.0, 0.016, argCurrents);
    }
}
