package com.navercorp.pinpoint.web.webhook.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.web.webhook.model.Webhook;
import com.navercorp.pinpoint.web.webhook.model.WebhookResponse;
import com.navercorp.pinpoint.web.webhook.service.WebhookService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value={"/api/webhook", "/api/application/webhook"})
public class WebhookController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public final static String APPLICATION_ID = "applicationId";
    public final static String SERVICE_NAME = "serviceName";
    public final static String ALARM_RULE_ID = "ruleId";

    private final WebhookService webhookService;


    public WebhookController(WebhookService webhookService) {
        this.webhookService = Objects.requireNonNull(webhookService, "webhookService");
    }

    @PostMapping()
    public WebhookResponse insertWebhook(@RequestBody Webhook webhook) {

        if (!StringUtils.hasText(webhook.getUrl()) || !(StringUtils.hasText(webhook.getApplicationId())
                || StringUtils.hasText(webhook.getServiceName()))) {
            logger.info("Missing arguments: webhook.url, applicationId/serviceName");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing arguments: webhook.url and applicationId/serviceName");
        }

        try {
            validateURL(webhook);
        } catch (MalformedURLException | URISyntaxException e) {
            logger.info("Malformed argument: webhook.url");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed argument: webhook.url");
        }

        String webhookId = webhookService.insertWebhook(webhook);
        return new WebhookResponse(Result.SUCCESS, webhookId);
    }

    @DeleteMapping()
    public Response deleteWebhook(@RequestBody Webhook webhook) {

        if (!StringUtils.hasText(webhook.getWebhookId())) {
            logger.info("Missing argument: webhookId");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing argument: webhook.id");
        }
        webhookService.deleteWebhook(webhook);
        return SimpleResponse.ok();
    }

    @GetMapping()
    public List<Webhook> getWebhook(@RequestParam(value=APPLICATION_ID, required=false) String applicationId,
                                    @RequestParam(value=SERVICE_NAME, required=false) String serviceName,
                                    @RequestParam(value=ALARM_RULE_ID, required=false) String ruleId) {

        if (!StringUtils.hasText(applicationId) && !StringUtils.hasText(serviceName) && !StringUtils.hasText(ruleId)) {
            logger.info("Missing argument: applicationId/serviceName/ruleId");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing argument: applicationId / serviceName / ruleId");
        }

        if (StringUtils.hasText(ruleId)) {
            return webhookService.selectWebhookByRuleId(ruleId);
        }

        if (StringUtils.hasText(applicationId)) {
            return webhookService.selectWebhookByApplicationId(applicationId);
        }

        return webhookService.selectWebhookByServiceName(serviceName);
    }

    @PutMapping()
    public Response updateWebhook(@RequestBody Webhook webhook) {

        if (!StringUtils.hasText(webhook.getWebhookId()) || !StringUtils.hasText(webhook.getUrl()) ||
                !(StringUtils.hasText(webhook.getApplicationId()) || StringUtils.hasText(webhook.getServiceName()))) {
            logger.info("Missing arguments: webhook.id, webhook.url, applicationId/serviceName");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing arguments: webhook.id, webhook.url, applicationId/serviceName");
        }

        try {
            validateURL(webhook);
        } catch (MalformedURLException | URISyntaxException e) {
            logger.info("Malformed argument: webhook.url");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed argument: webhook.url");
        }

        webhookService.updateWebhook(webhook);
        return SimpleResponse.ok();
    }

    private void validateURL(Webhook webhook) throws MalformedURLException, URISyntaxException {
        URL u = new URL(webhook.getUrl());
        webhook.setUrl(u.toURI().toString());
    }
}
