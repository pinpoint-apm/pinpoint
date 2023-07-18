package com.navercorp.pinpoint.batch.alarm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({
        "classpath:applicationContext-batch-sender.xml"
})
public class AlarmSenderConfiguration {

    @Bean
    public AlarmSenderProperties alarmSenderProperties() {
        return new AlarmSenderProperties();
    }
}
