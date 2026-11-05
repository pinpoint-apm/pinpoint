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
package com.navercorp.pinpoint.alarm.vo;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * What a rule measures: the metric family a condition draws from, plus the filter
 * keys and metrics that are valid for it.
 *
 * <p>An interface rather than one enum because which data sources exist depends on
 * what the deployment has installed. Implementations are normally enums, one per
 * contributing module, collected through {@code AlarmDataSourceProvider}.
 *
 * <p>{@link #name()} is the persisted value -- it is what
 * {@code alarm_rule_v2.data_source} stores and what the JSON API exchanges -- so
 * renaming a constant is a data migration. Enum implementations get it for free.
 */
public interface AlarmDataSource {

    /** The category of the applications the agent reports. */
    String APM_CATEGORY = "APM";

    /** Stable code. Persisted, so it must be unique across every contributor. */
    String name();

    /** Human readable name, for the rule editor. */
    String label();

    /**
     * What kind of target this measures. A rule may only use a data source of its
     * application's category, and a bundle holds items of one category, since a data
     * source read for an application it does not record answers nothing instead of failing.
     */
    String category();

    /**
     * Filter keys a rule may filter on. This is a whitelist: filter expressions
     * reach the query as string substitution, so anything outside this list is
     * rejected before it gets there.
     */
    List<String> filterKeys();

    /** Metrics a condition leaf may target, with the aggregations each allows. */
    List<AlarmMetricDefinition> metrics();

    /**
     * Where a recipient should land to see what fired, as a path under the web's base URL, or null
     * when this data source has no such screen. The screen depends on what the rule watches rather
     * than on the rule, so the route belongs with the data source that knows it -- and a data
     * source whose screens are not part of this distribution keeps them to itself.
     * <p>
     * The pages of the web read their service from the path. A link without the service opens the
     * service the recipient happens to look at, not the service of the rule, so a path builds its
     * segments with {@link #serviceSegment} and {@link #applicationSegment}.
     *
     * @param fromMs start of the range the link should open, epoch millis
     * @param toMs   end of that range, epoch millis
     */
    default String detailPath(AlarmRuleV2 rule, long fromMs, long toMs) {
        return null;
    }

    /**
     * The service of a rule as a path segment, for every link that opens a page of the web.
     * A rule saved without a service belongs to DEFAULT, the service the web takes when a request
     * names none.
     * <p>
     * The web reads a service segment with decodeURIComponent, and takes a segment that keeps a raw
     * '@' for an application. So '@' has to be encoded too, which a path segment encoding does not do.
     */
    static String serviceSegment(String serviceName) {
        String name = StringUtils.hasText(serviceName) ? serviceName : ServiceUid.DEFAULT_SERVICE_UID_NAME;
        return UriUtils.encode(name, StandardCharsets.UTF_8);
    }

    /**
     * {@code name@type}, as the URL carries an application. An application name is whatever the
     * agent reported it to be, so each half is encoded as a path segment of its own; '@' joins them.
     */
    static String applicationSegment(String applicationName, String applicationType) {
        return UriUtils.encodePathSegment(Objects.toString(applicationName, ""), StandardCharsets.UTF_8)
                + "@" + UriUtils.encodePathSegment(Objects.toString(applicationType, ""), StandardCharsets.UTF_8);
    }

    /** How the web's pages take a time in their {@code from} and {@code to} parameters. */
    DateTimeFormatter LINK_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    /** Written in this process's zone, the one the message prints its own times in. */
    static String linkTime(long epochMs) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault())
                .format(LINK_TIME_FORMAT);
    }
}
