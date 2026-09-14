package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleChannel;
import com.navercorp.pinpoint.service.web.vo.ServiceConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/alarm/channel")
@Validated
public class AlarmChannelController {

    private final AlarmChannelService channelService;
    private final AlarmNotificationChannelApiMapper channelApiMapper;

    public AlarmChannelController(AlarmChannelService channelService,
                                  AlarmNotificationChannelApiMapper channelApiMapper) {
        this.channelService = Objects.requireNonNull(channelService, "channelService");
        this.channelApiMapper = Objects.requireNonNull(channelApiMapper, "channelApiMapper");
    }

    /**
     * Fills in the serviceName of the {@link AlarmApplication} the read endpoints bind,
     * so that reads take it from the same place the writes do -- the header.
     * <p>
     * A {@code ?serviceName=} parameter binds onto the attribute and wins over this
     * value. That is left alone on purpose: reads are open across services, and the web
     * app ships as one version, so no client sends it by accident.
     */
    @ModelAttribute
    AlarmApplication alarmApplication(
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName) {
        AlarmApplication application = new AlarmApplication();
        application.setServiceName(serviceName);
        return application;
    }

    @GetMapping
    public List<AlarmNotificationChannelResponse> getChannels(
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName) {
        return channelApiMapper.toResponses(channelService.getChannelsByServiceName(serviceName));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public AlarmNotificationChannelResponse createChannel(
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            // Does not store app name in alarm channels, but only used form permission check.
            // TODO: replace permission checker and remove these when service feature is released!!
            @RequestParam @NotBlank String applicationName,
            @RequestBody @Valid AlarmNotificationChannelRequest request) {
        return channelApiMapper.toResponse(
                channelService.createChannel(channelApiMapper.toModel(serviceName, request)));
    }

    @GetMapping("/{id}")
    public AlarmNotificationChannelResponse getChannel(
            @PathVariable Long id,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName) {
        return channelApiMapper.toResponse(channelService.getChannel(serviceName, id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void updateChannel(
            @PathVariable Long id,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            // Does not store app name in alarm channels, but only used form permission check.
            // TODO: replace permission checker and remove these when service feature is released!!
            @RequestParam @NotBlank String applicationName,
            @RequestBody @Valid AlarmNotificationChannelRequest request) {
        var channel = channelApiMapper.toModel(serviceName, request);
        channel.setId(id);
        channelService.updateChannel(serviceName, channel);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void deleteChannel(
            @PathVariable Long id,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            // Does not store app name in alarm channels, but only used form permission check.
            // TODO: replace permission checker and remove these when service feature is released!!
            @RequestParam @NotBlank String applicationName) {
        channelService.deleteChannel(serviceName, id);
    }

    // ---- Rule-Channel mapping ----

    @GetMapping("/rule/{ruleId}")
    public List<AlarmNotificationChannelResponse> getChannelsByRule(
            @PathVariable Long ruleId,
            @Valid @ModelAttribute AlarmApplication application) {
        return channelApiMapper.toResponses(
                channelService.getChannelsByRuleId(ruleId, application));
    }

    @PostMapping("/rule/{ruleId}/{channelId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void linkRuleChannel(@PathVariable Long ruleId,
                                 @PathVariable Long channelId,
                                 @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
                                 @NotBlank
                                 @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
                                 String serviceName,
                                 @RequestParam("applicationName") @NotBlank String applicationName,
                                 @RequestBody(required = false) AlarmRuleChannel ruleChannel) {
        if (ruleChannel == null) {
            ruleChannel = new AlarmRuleChannel();
        }
        ruleChannel.setRuleId(ruleId);
        ruleChannel.setChannelId(channelId);
        channelService.linkRuleChannel(serviceName, applicationName, ruleChannel);
    }

    @DeleteMapping("/rule/{ruleId}/{channelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void unlinkRuleChannel(@PathVariable Long ruleId,
                                   @PathVariable Long channelId,
                                   @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
                                   @NotBlank
                                   @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
                                   String serviceName,
                                   @RequestParam("applicationName") @NotBlank String applicationName) {
        channelService.unlinkRuleChannel(serviceName, applicationName, ruleId, channelId);
    }

    @GetMapping("/template/{templateId}")
    public List<AlarmNotificationChannelResponse> getChannelsByTemplate(
            @PathVariable Long templateId,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName) {
        return channelApiMapper.toResponses(
                channelService.getChannelsByTemplateId(serviceName, templateId));
    }

    @PostMapping("/template/{templateId}/{channelId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void linkTemplateChannel(@PathVariable Long templateId,
                                    @PathVariable Long channelId,
                                    @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
                                    @NotBlank
                                    @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
                                    String serviceName,
                                    @RequestParam(value = "applicationName", required = false, defaultValue = "") String applicationName) {
        channelService.linkTemplateChannel(serviceName, templateId, channelId);
    }

    @DeleteMapping("/template/{templateId}/{channelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void unlinkTemplateChannel(@PathVariable Long templateId,
                                      @PathVariable Long channelId,
                                      @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
                                      @NotBlank
                                      @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
                                      String serviceName,
                                      @RequestParam(value = "applicationName", required = false, defaultValue = "") String applicationName) {
        channelService.unlinkTemplateChannel(serviceName, templateId, channelId);
    }
}
