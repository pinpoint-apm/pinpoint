package com.navercorp.pinpoint.alarm.sender;

import java.util.List;

/**
 * Actually sends emails. Implemented in each deployment module
 * (e.g. using JavaMailSender or an internal mail API).
 */
public interface EmailDispatcher {

    void send(List<String> recipients, String subject, String body) throws Exception;
}
