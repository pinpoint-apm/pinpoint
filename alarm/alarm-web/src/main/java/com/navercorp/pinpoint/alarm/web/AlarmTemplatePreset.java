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
