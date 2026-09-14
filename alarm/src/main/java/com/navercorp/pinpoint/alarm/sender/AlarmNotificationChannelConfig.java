package com.navercorp.pinpoint.alarm.sender;

/**
 * Parsed notification channel configuration.
 *
 * @param format webhook payload format, such as {@code SLACK}
 * @param title custom notification title template
 * @param template custom notification body template
 */
public record AlarmNotificationChannelConfig(String format,
                                             String title,
                                             String template) {

    private static final AlarmNotificationChannelConfig EMPTY =
            new AlarmNotificationChannelConfig(null, null, null);

    public static AlarmNotificationChannelConfig empty() {
        return EMPTY;
    }
}
