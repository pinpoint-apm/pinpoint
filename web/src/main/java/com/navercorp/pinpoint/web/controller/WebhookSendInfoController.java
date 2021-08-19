package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.service.WebhookSendInfoService;
import com.navercorp.pinpoint.web.service.WebhookService;
import com.navercorp.pinpoint.web.vo.WebhookSendInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value={"/webhookSendInfo", "/application/webhookSendInfo"})
public class WebhookSendInfoController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final static String WEBHOOK_ID = "webhookId";
    public final static String RULE_ID = "ruleId";

    private final WebhookSendInfoService webhookSendInfoService;

    @Value("${webhook.enable:false}")
    private boolean webhookEnable;

    public WebhookSendInfoController(WebhookService webhookService, WebhookSendInfoService webhookSendInfoService) {
        this.webhookSendInfoService = Objects.requireNonNull(webhookSendInfoService, "webhookSendInfoService");
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
        returnMap.put("webhookSendInfoId", ruleId);
        return returnMap;
    }

    private Map<String, String> getResultStringMap(String result) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("result", result);
        return returnMap;
    }

    @PostMapping()
    public Map<String, String> insertWebhookSendInfo(@RequestBody WebhookSendInfo webhookSendInfo) {
        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhookSendInfo.getRuleId()) || !StringUtils.hasText(webhookSendInfo.getWebhookId())) {
            return getErrorStringMap("500", "there should be ruleId and webhookIdto insert webhookSendInfo");
        }
        String webhookSendInfoId = webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);
        return getResultStringMap("SUCCESS", webhookSendInfoId);
    }

    @DeleteMapping()
    public Map<String, String> deleteWebhookSendInfo(@RequestBody WebhookSendInfo webhookSendInfo) {
        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhookSendInfo.getWebhookSendInfoId())) {
            return getErrorStringMap("500", "there should be webhookSendInfoId to delete webhook");
        }
        webhookSendInfoService.deleteWebhookSendInfo(webhookSendInfo);
        return getResultStringMap("SUCCESS");
    }

    @GetMapping()
    public Object getWebhookSendInfo(@RequestParam(value=WEBHOOK_ID, required=false) String webhookId,
                             @RequestParam(value=RULE_ID, required=false) String ruleId) {
        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhookId) && !StringUtils.hasText(ruleId)) {
            return getErrorStringMap("500", "Either webhookId or ruleId is needed to get webhook send information");
        }

        if (StringUtils.hasText(webhookId)) {
            return webhookSendInfoService.selectWebhookSendInfoByWebhookId(webhookId);
        }

        return webhookSendInfoService.selectWebhookSendInfoByRuleId(ruleId);
    }

    @PutMapping()
    public Map<String, String> updateWebhookSendInfo(@RequestBody WebhookSendInfo webhookSendInfo) {
        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhookSendInfo.getWebhookSendInfoId()) ||
                !StringUtils.hasText(webhookSendInfo.getWebhookId()) || !StringUtils.hasText(webhookSendInfo.getRuleId())) {
            return getErrorStringMap("500", "There should be webhookSendInfoId, webhookId and ruleId to update webhook send information");
        }
        webhookSendInfoService.updateWebhookSendInfo(webhookSendInfo);
        return getResultStringMap("SUCCESS");
    }

    @ExceptionHandler(Exception.class)
    public Map<String, String> handleException(Exception e) {
        logger.error(" Exception occurred while trying to CRUD WebhookSendInfo", e);

        Map<String, String> result = new HashMap<>();
        result.put("errorCode", "500");
        result.put("errorMessage", "Exception occurred while trying to process WebhookSendInfo");
        return result;
    }
}
