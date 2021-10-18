package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.service.WebhookService;
import com.navercorp.pinpoint.web.vo.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value={"/webhook", "/application/webhook"})
public class WebhookController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final static String APPLICATION_ID = "applicationId";
    public final static String SERVICE_NAME = "serviceName";
    public final static String ALARM_RULE_ID = "ruleId";

    private final WebhookService webhookService;

    @Value("${webhook.enable:false}")
    private boolean webhookEnable;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = Objects.requireNonNull(webhookService, "webhookService");
    }

    private Map<String, String> getErrorStringMap(String errorCode, String errorMessage) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("errorCode", errorCode);
        returnMap.put("errorMessage", errorMessage);
        return returnMap;
    }

    private Map<String, String> getResultStringMap(String result, String ruleId) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("result", result);
        returnMap.put("webhookId", ruleId);
        return returnMap;
    }

    private Map<String, String> getResultStringMap(String result) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("result", result);
        return returnMap;
    }

    @PostMapping()
    public Map<String, String> insertWebhook(@RequestBody Webhook webhook) {
        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhook.getUrl()) || !(StringUtils.hasText(webhook.getApplicationId())
                || StringUtils.hasText(webhook.getServiceName()))) {
            return getErrorStringMap("500", "there should be url, applicationId/serviceName to insert webhook");
        }
        String webhookId = webhookService.insertWebhook(webhook);
        return getResultStringMap("SUCCESS", webhookId);
    }

    @DeleteMapping()
    public Map<String, String> deleteWebhook(@RequestBody Webhook webhook) {
        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhook.getWebhookId())) {
            return getErrorStringMap("500", "there should be webhookId to delete webhook");
        }
        webhookService.deleteWebhook(webhook);
        return getResultStringMap("SUCCESS");
    }

    @GetMapping()
    public Object getWebhook(@RequestParam(value=APPLICATION_ID, required=false) String applicationId,
                             @RequestParam(value=SERVICE_NAME, required=false) String serviceName,
                             @RequestParam(value=ALARM_RULE_ID, required=false) String ruleId) {
        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook function is disabled");
        }

        if (!StringUtils.hasText(applicationId) && !StringUtils.hasText(serviceName) && !StringUtils.hasText(ruleId)) {
            return getErrorStringMap("500", "applicationId / serviceName / ruleId is needed to get webhooks");
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
    public Map<String, String> updateWebhook(@RequestBody Webhook webhook) {
        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook function is disabled");
        }
        
        if (!StringUtils.hasText(webhook.getWebhookId()) || !StringUtils.hasText(webhook.getUrl()) ||
                !(StringUtils.hasText(webhook.getApplicationId()) || StringUtils.hasText(webhook.getServiceName()))) {
            return getErrorStringMap("500", "there should be webhookId, url, applicationId/serviceName to update webhook");
        }
        webhookService.updateWebhook(webhook);
        return getResultStringMap("SUCCESS");
    }

    @ExceptionHandler(Exception.class)
    public Map<String, String> handleException(Exception e) {
        logger.warn(" Exception occurred while trying to CRUD Webhook information", e);

        Map<String, String> result = new HashMap<>();
        result.put("errorCode", "500");
        result.put("errorMessage", String.format("Exception occurred while trying to process Webhook information: %s", e.getMessage()));
        return result;
    }
}
