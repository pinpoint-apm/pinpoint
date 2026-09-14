package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;

import java.util.List;

/**
 * Built-in alarm template preset served to the template start view.
 * Deserialized from the classpath catalog by {@link AlarmTemplatePresetLoader}
 * and returned as-is by the catalog API; never stored in the database.
 * Display texts carry both locales — the frontend shows the active locale and
 * stamps that language's plain strings into the template it creates.
 */
public record AlarmTemplatePreset(LocalizedText name, LocalizedText description, List<Rule> rules) {

    public record LocalizedText(String ko, String en) {
    }

    /** {@code description} is optional; when present it seeds the bundle item's own. */
    public record Rule(LocalizedText name,
                       LocalizedText description,
                       AlarmSeverity severity,
                       String dataSource,
                       Integer checkIntervalSec,
                       Integer actionIntervalSec,
                       AlarmCondition conditions,
                       List<AlarmFilter> filters) {
    }
}
