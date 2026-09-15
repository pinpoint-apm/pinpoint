/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.batch.config;

import com.navercorp.pinpoint.alarm.batch.sender.DefaultEmailDispatcher;
import com.navercorp.pinpoint.alarm.sender.AlarmMessageFormatter;
import com.navercorp.pinpoint.alarm.sender.EmailAlarmSender;
import com.navercorp.pinpoint.alarm.sender.WebhookAlarmSender;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.common.server.webhook.WebhookDnsResolver;
import com.navercorp.pinpoint.common.server.webhook.WebhookHostPolicy;
import com.navercorp.pinpoint.user.service.UserGroupService;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Wires the senders the outbox dispatcher delivers through.
 *
 * <p>The webhook client enforces the host policy and refuses redirects, because a webhook URL
 * is typed in by an operator. A deployment whose own notification back-end legitimately lives
 * on a private address gives that transport its own client rather than relaxing this one.
 *
 * <p>A sender is declared only when the transport behind it is configured, where a blank
 * setting counts as unconfigured rather than as an empty address. There is no
 * stand-in that accepts a notification and drops it: a channel with no sender for its method
 * type is logged and recorded against the alarm's history, where the rule's owner can see it,
 * and the rule's other channels still go out. SMS has no sender here at all, because a gateway
 * is bought rather than installed -- a deployment with one contributes its own.
 */
@Configuration(proxyBeanMethods = false)
public class AlarmNotificationConfiguration {

    private static final Logger logger = LogManager.getLogger(AlarmNotificationConfiguration.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Host names allowed to resolve into private ranges. Empty by default, which blocks every
     * private range; deployments whose webhook targets live on an internal network list them here.
     */
    @Bean
    public WebhookHostPolicy webhookHostPolicy(
            @Value("${pinpoint.webhook.allowed-private-hosts:}") List<String> allowedHosts,
            @Value("${pinpoint.webhook.allowed-private-host-suffixes:}") List<String> allowedHostSuffixes) {
        WebhookHostPolicy hostPolicy = new WebhookHostPolicy(allowedHosts, allowedHostSuffixes);
        // the entries name internal hosts, so only the counts go out at INFO
        logger.info("Install WebhookHostPolicy. allowedHosts={}, allowedHostSuffixes={}",
                hostPolicy.allowedHostCount(), hostPolicy.allowedHostSuffixCount());
        logger.debug("Install {}", hostPolicy);
        return hostPolicy;
    }

    /**
     * The client webhook deliveries go through. The host check runs in the DNS resolver rather
     * than before the call, so it sees the addresses the connection will actually use and a host
     * that passed validation at registration cannot be re-pointed at an internal one.
     */
    @Bean(destroyMethod = "close")
    public CloseableHttpClient webhookHttpClient(WebhookHostPolicy webhookHostPolicy) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(CONNECT_TIMEOUT))
                .setResponseTimeout(Timeout.of(READ_TIMEOUT))
                .build();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(CONNECT_TIMEOUT))
                .setSocketTimeout(Timeout.of(READ_TIMEOUT))
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(new WebhookDnsResolver(SystemDefaultDnsResolver.INSTANCE, webhookHostPolicy))
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        // redirects stay disabled: an allowed host must not be able to hand out a 30x to an internal address
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build();
    }

    /**
     * Kept distinct from any client a deployment adds for its own transports, so the host
     * policy and the redirect restriction apply only to operator-supplied webhook URLs.
     */
    @Bean
    public RestTemplate webhookRestTemplate(
            @Qualifier("webhookHttpClient") CloseableHttpClient webhookHttpClient) {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(webhookHttpClient));
    }

    @Bean
    public TemplateEngine alarmTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(true);
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Bean
    public AlarmMessageFormatter alarmMessageFormatter(
            TemplateEngine alarmTemplateEngine,
            @Value("${alarm.email.logo:classpath:templates/alarm/pinpoint-logo-base64.txt}") Resource logoResource,
            @Value("${alarm.pinpoint.web.url:}") String pinpointWebUrl,
            // The page an alarm links back to. Shared code cannot know it: the path is a
            // deployment's own route.
            @Value("${alarm.pinpoint.web.alarm-path:/config/alarm}") String alarmPagePath,
            AlarmDataSourceRegistry dataSourceRegistry) {
        return new AlarmMessageFormatter(alarmTemplateEngine,
                resolveLogoDataUri(logoResource), pinpointWebUrl, alarmPagePath, dataSourceRegistry);
    }

    /**
     * Logo for the alarm email: a base64 text resource, operator-provided via
     * {@code alarm.email.logo} (for example {@code file:/etc/pinpoint/logo-base64.txt}) and
     * defaulting to the bundled one. A missing resource falls back to {@code ""} and the
     * template renders brand text instead; a resource that exists but cannot be read fails
     * startup, because that is a deployment mistake rather than an absent option.
     */
    private static String resolveLogoDataUri(Resource logoResource) {
        if (!logoResource.exists()) {
            logger.warn("alarm logo resource not found, falling back to brand text: {}", logoResource);
            return "";
        }
        try {
            String logo = logoResource.getContentAsString(StandardCharsets.UTF_8).trim();
            return AlarmMessageFormatter.toLogoPngBase64(logo);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read alarm logo: " + logoResource, e);
        }
    }

    @Bean
    public WebhookAlarmSender webhookAlarmSender(@Qualifier("webhookRestTemplate") RestTemplate webhookRestTemplate,
                                                 AlarmMessageFormatter alarmMessageFormatter) {
        return new WebhookAlarmSender(webhookRestTemplate, alarmMessageFormatter);
    }

    /**
     * Declared only once a From address is configured.
     *
     * <p>An empty one is not a working default: {@code setFrom("")} throws nothing and simply
     * leaves the header off, so the deployment starts and every email is refused by the relay
     * instead -- five retries per notification before it is given up on. Leaving the sender out
     * is the louder failure: an email channel then has no sender for its method type, which is
     * logged and recorded against the alarm's history where its owner can see it, while
     * webhook channels on the same rule are unaffected.
     */
    @Bean
    // Blank counts as unset: @ConditionalOnProperty would take an empty value as present and
    // build the bean anyway, which is how a deployment that deliberately leaves the address
    // out ends up failing startup on a missing JavaMailSender instead of just having no email.
    @ConditionalOnExpression("!'${alarm.email.sender:}'.isBlank()")
    public EmailAlarmSender emailAlarmSender(
            UserGroupService userGroupService,
            JavaMailSender javaMailSender,
            @Value("${alarm.email.sender}") String senderAddress,
            AlarmMessageFormatter alarmMessageFormatter) {
        return new EmailAlarmSender(
                userGroupService::selectEmailOfMember,
                new DefaultEmailDispatcher(javaMailSender, senderAddress),
                alarmMessageFormatter);
    }

}
