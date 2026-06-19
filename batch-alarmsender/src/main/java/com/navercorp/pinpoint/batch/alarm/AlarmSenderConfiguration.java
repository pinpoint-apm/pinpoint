package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.sender.MailSender;
import com.navercorp.pinpoint.batch.alarm.sender.SpringSmtpMailSender;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookDnsResolver;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookPayloadFactory;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSender;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSenderEmptyImpl;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSenderImpl;
import com.navercorp.pinpoint.user.service.UserGroupService;
import com.navercorp.pinpoint.user.service.UserService;
import com.navercorp.pinpoint.web.webhook.WebhookModule;
import com.navercorp.pinpoint.web.webhook.service.WebhookService;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

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

    private static final int CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int READ_TIMEOUT_MILLIS = 6000;


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
                                       @Qualifier("webhookRestTemplate") RestTemplate webhookRestTemplate,
                                       WebhookService webhookService) {
        String pinpointUrl = alarmSenderProperties.getPinpointUrl();
        String batchEnv = alarmSenderProperties.getBatchEnv();

        WebhookPayloadFactory webhookPayloadFactory = new WebhookPayloadFactory(pinpointUrl, batchEnv);
        return new WebhookSenderImpl(webhookPayloadFactory, userService, webhookRestTemplate, webhookService);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = WebhookModule.NAME, havingValue = "true", matchIfMissing = true)
    public CloseableHttpClient webhookHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .setResponseTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .build();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .setSocketTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(new WebhookDnsResolver(SystemDefaultDnsResolver.INSTANCE))
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = WebhookModule.NAME, havingValue = "true", matchIfMissing = true)
    public RestTemplate webhookRestTemplate(@Qualifier("restTemplate") RestTemplate restTemplate,
                                            @Qualifier("webhookHttpClient") CloseableHttpClient webhookHttpClient) {
        RestTemplate webhookRestTemplate = new RestTemplate(restTemplate.getMessageConverters());
        webhookRestTemplate.setErrorHandler(restTemplate.getErrorHandler());
        webhookRestTemplate.setInterceptors(restTemplate.getInterceptors());
        webhookRestTemplate.setUriTemplateHandler(restTemplate.getUriTemplateHandler());
        webhookRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(webhookHttpClient));
        return webhookRestTemplate;
    }

    @Bean("webhookSender")
    @ConditionalOnProperty(name = WebhookModule.NAME, havingValue = "false")
    public WebhookSender webhookSenderEmpty() {
        return new WebhookSenderEmptyImpl();
    }

}
