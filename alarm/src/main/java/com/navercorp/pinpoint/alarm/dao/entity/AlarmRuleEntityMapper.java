package com.navercorp.pinpoint.alarm.dao.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleDetails;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;

import java.util.List;
import java.util.Objects;

public class AlarmRuleEntityMapper {

    private final ObjectReader conditionReader;
    private final ObjectWriter conditionWriter;
    private final ObjectReader filtersReader;
    private final ObjectWriter filtersWriter;

    public AlarmRuleEntityMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.conditionReader = objectMapper.readerFor(AlarmCondition.class);
        this.conditionWriter = objectMapper.writerFor(AlarmCondition.class);
        TypeReference<List<AlarmFilter>> filtersType = new TypeReference<>() {
        };
        this.filtersReader = objectMapper.readerFor(filtersType);
        this.filtersWriter = objectMapper.writerFor(filtersType);
    }

    public AlarmRuleV2 toModel(AlarmRuleEntity source) {
        if (source == null) {
            return null;
        }
        AlarmRuleV2 target = new AlarmRuleV2();
        target.setId(source.getId());
        target.setDataSource(source.getDataSource());
        target.setApplicationType(source.getApplicationType());
        target.setTemplateItemId(source.getTemplateItemId());
        target.setServiceName(source.getServiceName());
        target.setApplicationName(source.getApplicationName());
        target.setEnabled(source.isEnabled());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public AlarmRuleEntity toEntity(AlarmRuleV2 source) {
        if (source == null) {
            return null;
        }
        AlarmRuleEntity target = new AlarmRuleEntity();
        target.setId(source.getId());
        target.setDataSource(source.getDataSource());
        target.setApplicationType(source.getApplicationType());
        target.setTemplateItemId(source.getTemplateItemId());
        target.setServiceName(source.getServiceName());
        target.setApplicationName(source.getApplicationName());
        target.setEnabled(source.isEnabled());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public AlarmTemplateItem toModel(AlarmTemplateItemEntity source) {
        if (source == null) {
            return null;
        }
        AlarmTemplateItem target = new AlarmTemplateItem();
        target.setId(source.getId());
        target.setTemplateId(source.getTemplateId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setSeverity(source.getSeverity());
        target.setDataSource(source.getDataSource());
        target.setCheckIntervalSec(source.getCheckIntervalSec());
        target.setActionIntervalSec(source.getActionIntervalSec());
        target.setConditions(readCondition(source.getConditions()));
        target.setFilters(readFilters(source.getFilters()));
        target.setUpdatedAt(source.getUpdatedAt());
        target.setUsedRuleCount(source.getUsedRuleCount());
        target.setEnabledUsedRuleCount(source.getEnabledUsedRuleCount());
        return target;
    }

    public AlarmTemplateItemEntity toEntity(AlarmTemplateItem source) {
        if (source == null) {
            return null;
        }
        AlarmTemplateItemEntity target = new AlarmTemplateItemEntity();
        target.setId(source.getId());
        target.setTemplateId(source.getTemplateId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setSeverity(source.getSeverity());
        target.setDataSource(source.getDataSource());
        target.setCheckIntervalSec(source.getCheckIntervalSec());
        target.setActionIntervalSec(source.getActionIntervalSec());
        target.setConditions(writeCondition(source.getConditions()));
        target.setFilters(writeFilters(source.getFilters()));
        target.setUpdatedAt(source.getUpdatedAt());
        target.setUsedRuleCount(source.getUsedRuleCount());
        target.setEnabledUsedRuleCount(source.getEnabledUsedRuleCount());
        return target;
    }

    public AlarmRuleLocalConfig toModel(AlarmRuleLocalConfigEntity source) {
        if (source == null) {
            return null;
        }
        AlarmRuleLocalConfig target = new AlarmRuleLocalConfig();
        target.setRuleId(source.getRuleId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setSeverity(source.getSeverity());
        target.setCheckIntervalSec(source.getCheckIntervalSec());
        target.setActionIntervalSec(source.getActionIntervalSec());
        target.setConditions(readCondition(source.getConditions()));
        target.setFilters(readFilters(source.getFilters()));
        return target;
    }

    public AlarmRuleLocalConfigEntity toEntity(AlarmRuleLocalConfig source) {
        if (source == null) {
            return null;
        }
        AlarmRuleLocalConfigEntity target = new AlarmRuleLocalConfigEntity();
        target.setRuleId(source.getRuleId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setSeverity(source.getSeverity());
        target.setCheckIntervalSec(source.getCheckIntervalSec());
        target.setActionIntervalSec(source.getActionIntervalSec());
        target.setConditions(writeCondition(source.getConditions()));
        target.setFilters(writeFilters(source.getFilters()));
        return target;
    }

    public AlarmRuleDetails toModel(AlarmRuleDetailsEntity source) {
        if (source == null) {
            return null;
        }
        AlarmRuleDetails target = new AlarmRuleDetails();
        target.setRule(toModel(source.getRule()));
        target.setLocalConfig(toModel(source.getLocalConfig()));
        target.setTemplateItem(toModel(source.getTemplateItem()));
        target.setTemplate(source.getTemplate());
        return target;
    }

    private AlarmCondition readCondition(String json) {
        if (json == null) {
            return null;
        }
        try {
            return conditionReader.readValue(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize alarm condition", e);
        }
    }

    private List<AlarmFilter> readFilters(String json) {
        if (json == null) {
            return null;
        }
        try {
            return filtersReader.readValue(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize alarm filters", e);
        }
    }

    private String writeCondition(AlarmCondition condition) {
        if (condition == null) {
            return null;
        }
        try {
            return conditionWriter.writeValueAsString(condition);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize alarm condition", e);
        }
    }

    private String writeFilters(List<AlarmFilter> filters) {
        if (filters == null) {
            return null;
        }
        try {
            return filtersWriter.writeValueAsString(filters);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize alarm filters", e);
        }
    }
}
