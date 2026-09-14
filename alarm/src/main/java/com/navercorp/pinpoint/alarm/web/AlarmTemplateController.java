package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.service.web.vo.ServiceConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/alarm/template")
@Validated
public class AlarmTemplateController {

    private final AlarmTemplateService templateService;

    public AlarmTemplateController(AlarmTemplateService templateService) {
        this.templateService = Objects.requireNonNull(templateService, "templateService");
    }

    @GetMapping
    public List<AlarmTemplate> getTemplates(
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            @RequestParam(value = "dataSource", required = false) String dataSource) {
        return templateService.getTemplates(serviceName, dataSource);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public AlarmTemplate createTemplate(
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            @RequestParam(value = "applicationName", required = false, defaultValue = "") String applicationName,
            @RequestBody @Valid AlarmTemplate template) {
        template.setServiceName(serviceName);
        return templateService.createTemplate(template);
    }

    @GetMapping("/{id}")
    public AlarmTemplate getTemplate(
            @PathVariable Long id,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName) {
        return templateService.getTemplate(serviceName, id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void updateTemplate(
            @PathVariable Long id,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            @RequestParam(value = "applicationName", required = false, defaultValue = "") String applicationName,
            @RequestBody @Valid AlarmTemplate template) {
        template.setServiceName(serviceName);
        template.setId(id);
        templateService.updateTemplate(template);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void deleteTemplate(
            @PathVariable Long id,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            @RequestParam(value = "applicationName", required = false, defaultValue = "") String applicationName) {
        templateService.deleteTemplate(serviceName, id);
    }

    @PostMapping("/{id}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public List<AlarmRuleV2> applyTemplate(
            @PathVariable Long id,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            @RequestParam("applicationName") @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH,
                    message = "applicationName is too long")
            String applicationName,
            @RequestParam("applicationType") @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_TYPE_LENGTH,
                    message = "applicationType is too long")
            String applicationType) {
        return templateService.applyTemplate(serviceName, applicationName, id, applicationType);
    }

    @DeleteMapping("/{id}/applications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void unapplyTemplate(
            @PathVariable Long id,
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            @RequestParam("applicationName") @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH,
                    message = "applicationName is too long")
            String applicationName,
            @RequestParam("applicationType") @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_TYPE_LENGTH,
                    message = "applicationType is too long")
            String applicationType) {
         templateService.unapplyTemplate(serviceName, applicationName, id, applicationType);
    }
}
