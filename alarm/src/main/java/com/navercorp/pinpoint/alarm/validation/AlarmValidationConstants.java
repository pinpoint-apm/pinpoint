package com.navercorp.pinpoint.alarm.validation;


import java.util.List;

/**
 * Shared limits for alarm rule input.
 *
 * <p>The rule editor enforces the same limits so an invalid rule is caught before it
 * is submitted; these are the authority, and the editor mirrors them.
 */
public final class AlarmValidationConstants {

    private AlarmValidationConstants() {}

    /**
     * Maximum depth of a condition tree.
     * <p>
     * depth 0: root (leaf or group)
     * depth 1: children of root group
     * depth 2: children of depth-1 group (leaf only)
     * <p>
     * Both the validator and the evaluator read this value.
     */
    public static final int MAX_CONDITION_DEPTH = 2;

    /** Maximum nodes (group + leaf) in one condition tree. */
    public static final int MAX_CONDITION_NODE_COUNT = 10;

    /** Maximum leaf conditions in one group. */
    public static final int MAX_LEAVES_PER_GROUP = 3;

    /** Maximum subgroups in one group. */
    public static final int MAX_GROUPS_PER_GROUP = 2;

    /** Maximum filters on a single alarm rule. */
    public static final int MAX_FILTER_COUNT = 5;

    /**
     * Maximum rule definitions in one bundle. Editing a bundle creates a rule in every
     * application it is applied to for each item added, so without a ceiling a single
     * request amplifies into item count x application count inserts.
     */
    public static final int MAX_TEMPLATE_ITEM_COUNT = 30;

    /** Minimum query window for an ordinary metric condition; NEW_GROUP triggers ignore it. */
    public static final int MIN_WINDOW_SEC = 60;

    /** Selectable check intervals, in seconds. Must stay ascending. */
    public static final List<Integer> CHECK_INTERVAL_SEC_OPTIONS = List.of(60, 300, 600, 1800, 3600);

    /** Selectable notification intervals, in seconds. Must stay ascending. */
    public static final List<Integer> ACTION_INTERVAL_SEC_OPTIONS =
            List.of(300, 600, 1800, 3600, 21600, 43200, 86400);

    /** Filter values are free text the user types, so they get a length limit of their own. */
    public static final int MAX_FILTER_VALUE_LENGTH = 512;

    /** Matches alarm_rule_local_config.name and alarm_template_item.name, VARCHAR(255). */
    public static final int MAX_RULE_NAME_LENGTH = 255;

    /** Matches alarm_rule_local_config.description and alarm_template_item.description, VARCHAR(1000). */
    public static final int MAX_RULE_DESCRIPTION_LENGTH = 1000;

    /** Matches alarm_notification_channel.channel_name, VARCHAR(100). */
    public static final int MAX_CHANNEL_NAME_LENGTH = 100;

    /** Matches alarm_notification_channel.destination, VARCHAR(256). */
    public static final int MAX_DESTINATION_LENGTH = 256;

    /** Matches alarm_history_v2.message, VARCHAR(2000). */
    public static final int MAX_HISTORY_MESSAGE_LENGTH = 2000;

    /** Matches alarm_rule_v2.application_type, VARCHAR(30). */
    public static final int MAX_APPLICATION_TYPE_LENGTH = 30;

    /** Matches the service/application identifier columns, VARCHAR(127). */
    public static final int MAX_APPLICATION_IDENTIFIER_LENGTH = 127;
}
