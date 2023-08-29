package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.sender.MailSender;
import com.navercorp.pinpoint.batch.alarm.sender.SpringSmtpMailSender;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookPayloadFactory;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSender;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSenderEmptyImpl;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSenderImpl;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.webhook.WebhookModule;
import com.navercorp.pinpoint.web.webhook.service.WebhookService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

/**
 * JavaMailSenderImpl Properties
 * <a href="https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html">...</a>
 */
@Configuration
@Import({
        AlarmSenderProperties.class,
        MailSenderAutoConfiguration.class
})
public class AlarmSenderConfiguration {


    @Bean
    public MailSender springMailSender(AlarmSenderProperties alarmSenderProperties,
                                       UserGroupService userGroupService,
                                       JavaMailSenderImpl mailSender) {
        return new SpringSmtpMailSender(alarmSenderProperties, userGroupService, mailSender);
    }


    @Bean
    @ConditionalOnProperty(name = WebhookModule.NAME, havingValue = "true", matchIfMissing = true)
    public WebhookSender webhookSender(AlarmSenderProperties alarmSenderProperties,
                                       UserService userService,
                                       RestTemplate restTemplate,
                                       WebhookService webhookService) {
        String pinpointUrl = alarmSenderProperties.getPinpointUrl();
        String batchEnv = alarmSenderProperties.getBatchEnv();

        WebhookPayloadFactory webhookPayloadFactory = new WebhookPayloadFactory(pinpointUrl, batchEnv);
        return new WebhookSenderImpl(webhookPayloadFactory, userService, restTemplate, webhookService);
    }

    @Bean("webhookSender")
    @ConditionalOnProperty(name = WebhookModule.NAME, havingValue = "false")
    public WebhookSender webhookSenderEmpty() {
        return new WebhookSenderEmptyImpl();
    }


}
