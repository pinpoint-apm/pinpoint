package com.navercorp.pinpoint.batch.alarm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailParseException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Objects;

public class AlarmSenderProperties {

    private final InternetAddress senderEmailAddress;

    private final String pinpointUrl;

    private final String batchEnv;

    public AlarmSenderProperties(@Value("${spring.mail.properties.mail.smtp.from}") String senderEmailAddress,
                                 @Value("${pinpoint.url}") String pinpointUrl,
                                 @Value("${batch.server.env}") String batchEnv) {
        Objects.requireNonNull(senderEmailAddress, "senderEmailAddress");
        try {
            this.senderEmailAddress = new InternetAddress(senderEmailAddress);
        } catch (AddressException e) {
            throw new MailParseException(e);
        }

        this.pinpointUrl = Objects.requireNonNull(pinpointUrl, "pinpointUrl");
        this.batchEnv = Objects.requireNonNull(batchEnv, "batchEnv");
    }

    public String getPinpointUrl() {
        return pinpointUrl;
    }

    public InternetAddress getSenderEmailAddress() {
        return senderEmailAddress;
    }


    public String getBatchEnv() {
        return batchEnv;
    }


    @Override
    public String toString() {
        return "AlarmSenderProperties{" +
                ", batchEnv='" + batchEnv + '\'' +
                ", senderEmailAddress='" + senderEmailAddress + '\'' +
                ", pinpointUrl='" + pinpointUrl + '\'' +
                '}';
    }
}
