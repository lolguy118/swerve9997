// Copyright (c) 2023-2026 Gold87 and other Elastic contributors
// This software can be modified and/or shared under the terms
// defined by the Elastic license:
// https://github.com/Gold872/elastic_dashboard/blob/main/LICENSE

package com.team271.lib.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.networktables.StringTopic;

public final class Elastic {
    private static final StringTopic notificationTopic =
            NetworkTableInstance.getDefault().getStringTopic("/Elastic/RobotNotifications");
    private static final StringPublisher notificationPublisher =
            notificationTopic.publish(
                    PubSubOption.sendAll(true), PubSubOption.keepDuplicates(true));
    private static final StringTopic selectedTabTopic =
            NetworkTableInstance.getDefault().getStringTopic("/Elastic/SelectedTab");
    private static final StringPublisher selectedTabPublisher =
            selectedTabTopic.publish(PubSubOption.keepDuplicates(true));
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Elastic() {
        // Utility class; not instantiable.
    }

    /**
     * Represents the possible levels of notifications for the Elastic dashboard. These levels are
     * used to indicate the severity or type of notification.
     */
    public enum NotificationLevel {
        /** Informational Message */
        INFO,
        /** Warning message */
        WARNING,
        /** Error message */
        ERROR
    }

    /**
     * Sends an notification to the Elastic dashboard. The notification is serialized as a JSON
     * string before being published.
     *
     * @param argNotification the {@link Notification} object containing notification details
     */
    public static void sendNotification(final Notification argNotification) {
        try {
            notificationPublisher.set(objectMapper.writeValueAsString(argNotification));
        } catch (JsonProcessingException e) {
            edu.wpi.first.wpilibj.DriverStation.reportError(
                    "Elastic notification failed: " + e.getMessage(), false);
        }
    }

    /**
     * Selects the tab of the dashboard with the given name. If no tab matches the name, this will
     * have no effect on the widgets or tabs in view.
     *
     * <p>If the given name is a number, Elastic will select the tab whose index equals the number
     * provided.
     *
     * @param argTabName the name of the tab to select
     */
    public static void selectTab(final String argTabName) {
        selectedTabPublisher.set(argTabName);
    }

    /**
     * Selects the tab of the dashboard at the given index. If this index is greater than or equal
     * to the number of tabs, this will have no effect.
     *
     * @param argTabIndex the index of the tab to select.
     */
    public static void selectTab(final int argTabIndex) {
        selectTab(Integer.toString(argTabIndex));
    }

    /**
     * Represents an notification object to be sent to the Elastic dashboard. This object holds
     * properties such as level, title, description, display time, and dimensions to control how the
     * notification is displayed on the dashboard.
     */
    public static class Notification {
        @JsonProperty("level")
        private NotificationLevel level;

        @JsonProperty("title")
        private String title;

        @JsonProperty("description")
        private String description;

        @JsonProperty("displayTime")
        private int displayTimeMillis;

        @JsonProperty("width")
        private double width;

        @JsonProperty("height")
        private double height;

        /**
         * Creates a new Notification with all default parameters. This constructor is intended to
         * be used with the chainable decorator methods
         *
         * <p>Title and description fields are empty.
         */
        public Notification() {
            this(NotificationLevel.INFO, "", "");
        }

        /**
         * Creates a new Notification with all properties specified.
         *
         * @param argLevel the level of the notification (e.g., INFO, WARNING, ERROR)
         * @param argTitle the title text of the notification
         * @param argDescription the descriptive text of the notification
         * @param argDisplayTimeMillis the time in milliseconds for which the notification is
         *     displayed
         * @param argWidth the width of the notification display area
         * @param argHeight the height of the notification display area, inferred if below zero
         */
        public Notification(
                final NotificationLevel argLevel,
                final String argTitle,
                final String argDescription,
                final int argDisplayTimeMillis,
                final double argWidth,
                final double argHeight) {
            this.level = argLevel;
            this.title = argTitle;
            this.displayTimeMillis = argDisplayTimeMillis;
            this.description = argDescription;
            this.height = argHeight;
            this.width = argWidth;
        }

        /**
         * Creates a new Notification with default display time and dimensions.
         *
         * @param argLevel the level of the notification
         * @param argTitle the title text of the notification
         * @param argDescription the descriptive text of the notification
         */
        public Notification(
                final NotificationLevel argLevel,
                final String argTitle,
                final String argDescription) {
            this(argLevel, argTitle, argDescription, 3000, 350, -1);
        }

        /**
         * Creates a new Notification with a specified display time and default dimensions.
         *
         * @param argLevel the level of the notification
         * @param argTitle the title text of the notification
         * @param argDescription the descriptive text of the notification
         * @param argDisplayTimeMillis the display time in milliseconds
         */
        public Notification(
                final NotificationLevel argLevel,
                final String argTitle,
                final String argDescription,
                final int argDisplayTimeMillis) {
            this(argLevel, argTitle, argDescription, argDisplayTimeMillis, 350, -1);
        }

        /**
         * Creates a new Notification with specified dimensions and default display time. If the
         * height is below zero, it is automatically inferred based on screen size.
         *
         * @param argLevel the level of the notification
         * @param argTitle the title text of the notification
         * @param argDescription the descriptive text of the notification
         * @param argWidth the width of the notification display area
         * @param argHeight the height of the notification display area, inferred if below zero
         */
        public Notification(
                final NotificationLevel argLevel,
                final String argTitle,
                final String argDescription,
                final double argWidth,
                final double argHeight) {
            this(argLevel, argTitle, argDescription, 3000, argWidth, argHeight);
        }

        /**
         * Updates the level of this notification
         *
         * @param argLevel the level to set the notification to
         */
        public void setLevel(final NotificationLevel argLevel) {
            this.level = argLevel;
        }

        /**
         * Returns the level of this notification.
         *
         * @return the level of this notification
         */
        public NotificationLevel getLevel() {
            return level;
        }

        /**
         * Updates the title of this notification
         *
         * @param argTitle the title to set the notification to
         */
        public void setTitle(final String argTitle) {
            this.title = argTitle;
        }

        /**
         * Gets the title of this notification
         *
         * @return the title of this notification
         */
        public String getTitle() {
            return title;
        }

        /**
         * Updates the description of this notification
         *
         * @param argDescription the description to set the notification to
         */
        public void setDescription(final String argDescription) {
            this.description = argDescription;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Updates the display time of the notification
         *
         * @param argSeconds the number of seconds to display the notification for
         */
        public void setDisplayTimeSeconds(final double argSeconds) {
            setDisplayTimeMillis((int) Math.round(argSeconds * 1000));
        }

        /**
         * Updates the display time of the notification in milliseconds
         *
         * @param argDisplayTimeMillis the number of milliseconds to display the notification for
         */
        public void setDisplayTimeMillis(final int argDisplayTimeMillis) {
            this.displayTimeMillis = argDisplayTimeMillis;
        }

        /**
         * Gets the display time of the notification in milliseconds
         *
         * @return the number of milliseconds the notification is displayed for
         */
        public int getDisplayTimeMillis() {
            return displayTimeMillis;
        }

        /**
         * Updates the width of the notification
         *
         * @param argWidth the width to set the notification to
         */
        public void setWidth(final double argWidth) {
            this.width = argWidth;
        }

        /**
         * Gets the width of the notification
         *
         * @return the width of the notification
         */
        public double getWidth() {
            return width;
        }

        /**
         * Updates the height of the notification
         *
         * <p>If the height is set to -1, the height will be determined automatically by the
         * dashboard
         *
         * @param argHeight the height to set the notification to
         */
        public void setHeight(final double argHeight) {
            this.height = argHeight;
        }

        /**
         * Gets the height of the notification
         *
         * @return the height of the notification
         */
        public double getHeight() {
            return height;
        }

        /**
         * Modifies the notification's level and returns itself to allow for method chaining
         *
         * @param argLevel the level to set the notification to
         * @return the current notification
         */
        public Notification withLevel(final NotificationLevel argLevel) {
            this.level = argLevel;
            return this;
        }

        /**
         * Modifies the notification's title and returns itself to allow for method chaining
         *
         * @param argTitle the title to set the notification to
         * @return the current notification
         */
        public Notification withTitle(final String argTitle) {
            setTitle(argTitle);
            return this;
        }

        /**
         * Modifies the notification's description and returns itself to allow for method chaining
         *
         * @param argDescription the description to set the notification to
         * @return the current notification
         */
        public Notification withDescription(final String argDescription) {
            setDescription(argDescription);
            return this;
        }

        /**
         * Modifies the notification's display time and returns itself to allow for method chaining
         *
         * @param argSeconds the number of seconds to display the notification for
         * @return the current notification
         */
        public Notification withDisplaySeconds(final double argSeconds) {
            return withDisplayMilliseconds((int) Math.round(argSeconds * 1000));
        }

        /**
         * Modifies the notification's display time and returns itself to allow for method chaining
         *
         * @param argDisplayTimeMillis the number of milliseconds to display the notification for
         * @return the current notification
         */
        public Notification withDisplayMilliseconds(final int argDisplayTimeMillis) {
            setDisplayTimeMillis(argDisplayTimeMillis);
            return this;
        }

        /**
         * Modifies the notification's width and returns itself to allow for method chaining
         *
         * @param argWidth the width to set the notification to
         * @return the current notification
         */
        public Notification withWidth(final double argWidth) {
            setWidth(argWidth);
            return this;
        }

        /**
         * Modifies the notification's height and returns itself to allow for method chaining
         *
         * @param argHeight the height to set the notification to
         * @return the current notification
         */
        public Notification withHeight(final double argHeight) {
            setHeight(argHeight);
            return this;
        }

        /**
         * Modifies the notification's height and returns itself to allow for method chaining
         *
         * <p>This will set the height to -1 to have it automatically determined by the dashboard
         *
         * @return the current notification
         */
        public Notification withAutomaticHeight() {
            setHeight(-1);
            return this;
        }

        /**
         * Modifies the notification to disable the auto dismiss behavior
         *
         * <p>This sets the display time to 0 milliseconds
         *
         * <p>The auto dismiss behavior can be re-enabled by setting the display time to a number
         * greater than 0
         *
         * @return the current notification
         */
        public Notification withNoAutoDismiss() {
            setDisplayTimeMillis(0);
            return this;
        }
    }
}
