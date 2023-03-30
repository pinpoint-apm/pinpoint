package com.navercorp.pinpoint.web.webhook.controller;

import com.navercorp.pinpoint.web.response.Response;
import com.navercorp.pinpoint.web.response.SuccessResponse;
import com.navercorp.pinpoint.web.webhook.model.WebhookSendInfo;
import com.navercorp.pinpoint.web.webhook.model.WebhookSendInfoResponse;
import com.navercorp.pinpoint.web.webhook.service.WebhookSendInfoService;
import com.navercorp.pinpoint.web.webhook.service.WebhookService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Objects;

@RestController
@RequestMapping(value={"/webhookSendInfo", "/application/webhookSendInfo"})
public class WebhookSendInfoController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public final static String WEBHOOK_ID = "webhookId";
    public final static String RULE_ID = "ruleId";

    private final WebhookSendInfoService webhookSendInfoService;

    @Value("${webhook.enable:false}")
    private boolean webhookEnable;

    public WebhookSendInfoController(WebhookService webhookService, WebhookSendInfoService webhookSendInfoService) {
        this.webhookSendInfoService = Objects.requireNonNull(webhookSendInfoService, "webhookSendInfoService");
    }


    @PostMapping()
    public Response insertWebhookSendInfo(@RequestBody WebhookSendInfo webhookSendInfo) {
        if (!webhookEnable) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhookSendInfo.getRuleId()) || !StringUtils.hasText(webhookSendInfo.getWebhookId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there should be ruleId and webhookId to insert webhookSendInfo");
        }
        String webhookSendInfoId = webhookSendInfoService.insertWebhookSendInfo(webhookSendInfo);
        return new WebhookSendInfoResponse("SUCCESS", webhookSendInfoId);
    }

    @DeleteMapping()
    public Response deleteWebhookSendInfo(@RequestBody WebhookSendInfo webhookSendInfo) {
        if (!webhookEnable) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhookSendInfo.getWebhookSendInfoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there should be webhookSendInfoId to delete webhook");
        }
        webhookSendInfoService.deleteWebhookSendInfo(webhookSendInfo);
        return SuccessResponse.ok();
    }

    @GetMapping()
    public Object getWebhookSendInfo(@RequestParam(value=WEBHOOK_ID, required=false) String webhookId,
                             @RequestParam(value=RULE_ID, required=false) String ruleId) {
        if (!webhookEnable) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhookId) && !StringUtils.hasText(ruleId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either webhookId or ruleId is needed to get webhook send information");
        }

        if (StringUtils.hasText(webhookId)) {
            return webhookSendInfoService.selectWebhookSendInfoByWebhookId(webhookId);
        }

        return webhookSendInfoService.selectWebhookSendInfoByRuleId(ruleId);
    }

    @PutMapping()
    public Response updateWebhookSendInfo(@RequestBody WebhookSendInfo webhookSendInfo) {
        if (!webhookEnable) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "webhook function is disabled");
        }

        if (!StringUtils.hasText(webhookSendInfo.getWebhookSendInfoId()) ||
                !StringUtils.hasText(webhookSendInfo.getWebhookId()) || !StringUtils.hasText(webhookSendInfo.getRuleId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There should be webhookSendInfoId, webhookId and ruleId to update webhook send information");
        }
        webhookSendInfoService.updateWebhookSendInfo(webhookSendInfo);
        return SuccessResponse.ok();
    }
}
