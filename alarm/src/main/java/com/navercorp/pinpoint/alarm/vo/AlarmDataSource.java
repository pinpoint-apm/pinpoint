package com.navercorp.pinpoint.alarm.vo;

import java.util.List;

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

    /** Stable code. Persisted, so it must be unique across every contributor. */
    String name();

    /** Human readable name, for the rule editor. */
    String label();

    /**
     * Filter keys a rule may filter on. This is a whitelist: filter expressions
     * reach the query as string substitution, so anything outside this list is
     * rejected before it gets there.
     */
    List<String> filterKeys();

    /** Metrics a condition leaf may target, with the aggregations each allows. */
    List<AlarmMetricDefinition> metrics();

    /**
     * Where a recipient should land to see what fired, or null when this data source
     * has no such screen. The screen depends on what the rule watches rather than on
     * the rule, so the route belongs with the data source that knows it -- and a data
     * source whose screens are not part of this distribution keeps them to itself.
     *
     * @param application {@code name@type}, as the URL carries it
     * @param fromMs      start of the range the link should open, epoch millis
     * @param toMs        end of that range, epoch millis
     */
    default String detailLink(String baseUrl, String application, long fromMs, long toMs) {
        return null;
    }
}
