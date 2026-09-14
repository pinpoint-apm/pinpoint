package com.navercorp.pinpoint.alarm.sender;

import java.util.List;

/**
 * Actually sends SMS messages. Implemented in each deployment module.
 */
public interface SmsDispatcher {

    void send(List<String> phoneNumbers, String message) throws Exception;
}
