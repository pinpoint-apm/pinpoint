package com.nhn.pinpoint.web.alarm;


public class DefaultAlarmEvent implements AlarmEvent {

    private final long startEventTimeMillis;

    public DefaultAlarmEvent(long startEventTimeMillis) {
        this.startEventTimeMillis = startEventTimeMillis;
    }

    @Override
    public long getEventStartTimeMillis() {
        return startEventTimeMillis;
    }
    

}