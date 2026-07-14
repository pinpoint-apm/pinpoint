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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
                                       JavaMailSenderImpl mailSender,
                                       @Value("classpath:/templates/alarm/pinpoint-logo-base64.txt") Resource logoResource) throws IOException {
        String logoBase64 = logoResource.getContentAsString(StandardCharsets.UTF_8);
        return new SpringSmtpMailSender(alarmSenderProperties, userGroupService, mailSender, logoBase64);
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
