package com.navercorp.pinpoint.alarm.sender;

public class AlarmSendException extends RuntimeException {

    public AlarmSendException(String message) {
        super(message);
    }

    public AlarmSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
