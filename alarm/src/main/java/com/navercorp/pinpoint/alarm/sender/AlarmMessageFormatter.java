package com.navercorp.pinpoint.alarm.sender;

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.evaluation.ConditionUtils;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.QueriedRange;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Formats alarm messages by replacing template variables.
 */
public class AlarmMessageFormatter {

    /**
     * The zone is part of the output: the link next to it resolves in the recipient's own
     * timezone setting, so a bare wall clock would invite comparing two different zones.
     */
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    // The title is all a recipient sees in an inbox list or a phone notification, so it
    // names the application. A standalone rule has no bundle, and an empty bracket pair
    // would just be noise, so that segment is dropped rather than left blank.
    private static final String DEFAULT_TITLE_TEMPLATE =
            "[${severity}][${application_name}] ${name}";
    private static final String DEFAULT_BUNDLED_TITLE_TEMPLATE =
            "[${severity}][${application_name}][${template_name}] ${name}";
    private static final String BRAND_TEXT = "PINPOINT";

    private static final String HTML_BODY_TEMPLATE = "alarm/alarm-email";

    /** Detail lines are truncated per line; plain text bodies are read on small screens. */
    private static final int PLAIN_DETAIL_MAX_CHARS = 100;
    private static final int HTML_DETAIL_MAX_CHARS = 300;
    /**
     * An SMS gateway that promotes a long message rather than truncating it bills the
     * longer form, so the detail block gets a byte budget that leaves room for the
     * fixed body instead of relying on the gateway to cut it.
     */
    private static final int SMS_DETAIL_MAX_BYTES = 1200;
    private static final int UNLIMITED_DETAIL_BYTES = Integer.MAX_VALUE;
    private static final Pattern CONTROL_CHARS = Pattern.compile("\\p{Cntrl}");

    /**
     * Wraps base64-encoded PNG text as a data URI usable in the email template.
     */
    public static String toLogoPngBase64(String base64Text) {
        return "data:image/png;base64," + base64Text;
    }

    private final TemplateEngine templateEngine;
    private final AlarmDataSourceRegistry dataSourceRegistry;
    private final String logoDataUri;
    private final String pinpointBaseUrl;
    /** Path of the rule page the notification links back to; routing is the deployment's. */
    private final String alarmPagePath;

    public AlarmMessageFormatter(TemplateEngine templateEngine, String logoDataUri, String pinpointBaseUrl,
                                 String alarmPagePath, AlarmDataSourceRegistry dataSourceRegistry) {
        this.templateEngine = Objects.requireNonNull(templateEngine, "templateEngine");
        this.dataSourceRegistry = Objects.requireNonNull(dataSourceRegistry, "dataSourceRegistry");
        this.logoDataUri = Objects.requireNonNullElse(logoDataUri, "");
        this.pinpointBaseUrl = StringUtils.hasText(pinpointBaseUrl)
                ? pinpointBaseUrl.stripTrailing().replaceAll("/+$", "") : null;
        this.alarmPagePath = Objects.requireNonNull(alarmPagePath, "alarmPagePath");
    }

    public String formatTitle(AlarmRuleV2 rule, String customTitle, MetricQueryResult metricResults) {
        ZonedDateTime now = ZonedDateTime.now();
        String template = StringUtils.hasText(customTitle) ? customTitle : defaultTitleTemplate(rule);
        return replaceVariables(template, rule, metricResults, now, UNLIMITED_DETAIL_BYTES);
    }

    private static String defaultTitleTemplate(AlarmRuleV2 rule) {
        return StringUtils.hasText(rule.getTemplateName())
                ? DEFAULT_BUNDLED_TITLE_TEMPLATE : DEFAULT_TITLE_TEMPLATE;
    }

    /** Plain body without a size cap. Every channel that ships uses a capped or richer variant. */
    String formatBody(AlarmRuleV2 rule, String customTemplate, MetricQueryResult metricResults) {
        return formatBody(rule, customTemplate, metricResults, UNLIMITED_DETAIL_BYTES);
    }

    /**
     * Same as {@link #formatBody} but caps the detail block so the message stays
     * within the LMS byte limit.
     */
    public String formatSmsBody(AlarmRuleV2 rule, String customTemplate, MetricQueryResult metricResults) {
        return formatBody(rule, customTemplate, metricResults, SMS_DETAIL_MAX_BYTES);
    }

    private String formatBody(AlarmRuleV2 rule, String customTemplate, MetricQueryResult metricResults,
                              int detailBudgetBytes) {
        ZonedDateTime now = ZonedDateTime.now();
        if (StringUtils.hasText(customTemplate)) {
            return replaceVariables(customTemplate, rule, metricResults, now, detailBudgetBytes);
        }
        return buildDefaultBody(rule, metricResults, now, detailBudgetBytes);
    }

    /**
     * Body for webhook deliveries: the same layout with or without Slack mrkdwn, since
     * {@code *bold*} and backticks render only in Slack and are literal text anywhere else.
     * A custom template is rendered as-is, like the other channels.
     */
    public String formatWebhookBody(AlarmRuleV2 rule, String customTemplate, MetricQueryResult metricResults,
                                    boolean mrkdwn) {
        ZonedDateTime now = ZonedDateTime.now();
        if (StringUtils.hasText(customTemplate)) {
            return replaceVariables(customTemplate, rule, metricResults, now, UNLIMITED_DETAIL_BYTES);
        }
        return buildWebhookBody(rule, metricResults, now, mrkdwn);
    }

    private String buildWebhookBody(AlarmRuleV2 rule, MetricQueryResult metricResults, ZonedDateTime now,
                                    boolean mrkdwn) {
        Map<MetricQueryKey, AlarmCondition> leafByMetric = buildLeafByMetricKey(rule);
        AlarmSeverity severity = rule.getSeverity();

        StringBuilder sb = new StringBuilder();
        if (mrkdwn) {
            // Slack shows one text block, so the body leads with the headline. A DEFAULT
            // payload has its own title field saying the same thing, so it starts at the
            // fields instead of repeating it.
            sb.append(severityEmoji(severity)).append(' ')
                    .append(bold("[" + severityName(severity) + "]", mrkdwn)).append(' ')
                    .append(bold("[" + nullSafe(rule.getApplicationName()) + "]", mrkdwn)).append(' ')
                    .append(nullSafe(rule.getName())).append("\n\n");
        }

        if (StringUtils.hasText(rule.getDescription())) {
            appendField(sb, "📝", "Description", rule.getDescription(), mrkdwn);
        }
        appendField(sb, "🌐", "Service", nullSafe(rule.getServiceName()), mrkdwn);
        appendField(sb, "📦", "Application", nullSafe(rule.getApplicationName()), mrkdwn);
        appendField(sb, "📊", "Data Source", nullSafe(rule.getDataSource()), mrkdwn);

        sb.append("\n• 🔍 ").append(bold("Conditions:", mrkdwn)).append("\n");
        for (Map.Entry<MetricQueryKey, Double> e : metricResults.values().entrySet()) {
            AlarmCondition leaf = leafByMetric.get(e.getKey());
            sb.append("    └ ").append(code(metricName(e.getKey()), mrkdwn))
                    .append(" : ").append(e.getValue());
            if (hasDisplayCondition(leaf)) {
                sb.append(" (").append(nullSafe(leaf.getOp())).append(" ").append(leaf.getThreshold());
                if (leaf.getWindowSec() != null) {
                    sb.append(", window: ").append(leaf.getWindowSec()).append("s");
                }
                sb.append(")");
            }
            sb.append("\n");

            Details details = buildDetails(metricResults.details(e.getKey()), e.getValue(),
                    HTML_DETAIL_MAX_CHARS, UNLIMITED_DETAIL_BYTES);
            for (String line : details.lines()) {
                sb.append("        • ").append(escapeMrkdwn(line, mrkdwn)).append("\n");
            }
            if (details.more() > 0) {
                sb.append("        ").append(details.moreText()).append("\n");
            }
        }

        List<AlarmFilter> filters = rule.getFilters();
        if (!CollectionUtils.isEmpty(filters)) {
            sb.append("\n• 🚫 ").append(bold("Filters:", mrkdwn)).append("\n");
            for (AlarmFilter f : filters) {
                sb.append("    └ ").append(code(nullSafe(f.getKey()), mrkdwn))
                        .append(" ").append(nullSafe(f.getOp()))
                        .append(" [").append(nullSafe(f.getValue())).append("]\n");
            }
        }

        sb.append("\n");
        appendField(sb, "⏱️", "Check Interval", formatInterval(rule.getCheckIntervalSec()), mrkdwn);
        appendField(sb, "🕒", "Fired At", now.format(DT_FORMAT), mrkdwn);
        return sb.toString().stripTrailing();
    }

    private static void appendField(StringBuilder sb, String emoji, String label, String value, boolean mrkdwn) {
        sb.append("• ").append(emoji).append(' ').append(bold(label + ":", mrkdwn))
                .append(' ').append(value).append("\n");
    }

    private static String bold(String text, boolean mrkdwn) {
        return mrkdwn ? "*" + text + "*" : text;
    }

    private static String code(String text, boolean mrkdwn) {
        return mrkdwn ? "`" + text + "`" : text;
    }

    /**
     * Slack renders the text block as mrkdwn, so text coming from the monitored application
     * has to escape the characters that build links ({@code <url|label>}) and mentions.
     */
    private static String escapeMrkdwn(String text, boolean mrkdwn) {
        if (!mrkdwn) {
            return text;
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String severityName(AlarmSeverity severity) {
        return severity != null ? severity.name() : "UNKNOWN";
    }

    private static String severityEmoji(AlarmSeverity severity) {
        if (severity == null) {
            return "🔔";
        }
        return switch (severity) {
            case CRITICAL -> "🚨";
            case WARNING -> "⚠️";
        };
    }

    public String formatHtmlBody(AlarmRuleV2 rule, String customTemplate, MetricQueryResult metricResults) {
        ZonedDateTime now = ZonedDateTime.now();
        if (StringUtils.hasText(customTemplate)) {
            return buildCustomTemplate(rule, customTemplate, metricResults, now);
        }
        return buildDefaultHtmlBody(rule, metricResults, now);
    }

    @NonNull
    private String buildCustomTemplate(AlarmRuleV2 rule, String customTemplate, MetricQueryResult metricResults, ZonedDateTime now) {
        String body = replaceVariables(customTemplate, rule, metricResults, now, UNLIMITED_DETAIL_BYTES);
        String escaped = HtmlUtils.htmlEscape(body);
        return escaped.replace("\n", "<br>\n");
    }

    private static Map<MetricQueryKey, AlarmCondition> buildLeafByMetricKey(AlarmRuleV2 rule) {
        List<AlarmCondition> leaves = rule.getConditions() != null
                ? ConditionUtils.extractLeaves(rule.getConditions()) : List.of();
        return leaves.stream()
                .filter(l -> l.getMetric() != null)
                .collect(Collectors.toMap(MetricQueryKey::from, l -> l, (a, b) -> a));
    }

    private String buildDefaultHtmlBody(AlarmRuleV2 rule, MetricQueryResult metricResults, ZonedDateTime now) {
        Map<MetricQueryKey, AlarmCondition> leafByMetric = buildLeafByMetricKey(rule);
        List<ConditionRow> conditions = metricResults.values().entrySet().stream()
                .map(e -> ConditionRow.from(e.getKey(), e.getValue(), leafByMetric.get(e.getKey()),
                        buildDetails(metricResults.details(e.getKey()), e.getValue(),
                                HTML_DETAIL_MAX_CHARS, UNLIMITED_DETAIL_BYTES)))
                .toList();

        Context context = new Context();
        context.setVariable("logoDataUri", logoDataUri);
        context.setVariable("brandText", BRAND_TEXT);
        context.setVariable("severityColor", severityColor(rule.getSeverity()));
        context.setVariable("severityName", rule.getSeverity() != null ? rule.getSeverity().name() : "UNKNOWN");
        context.setVariable("name", nullSafe(rule.getName()));
        context.setVariable("description", rule.getDescription());
        context.setVariable("serviceName", nullSafe(rule.getServiceName()));
        context.setVariable("applicationName", nullSafe(rule.getApplicationName()));
        context.setVariable("dataSource", nullSafe(rule.getDataSource()));
        context.setVariable("checkInterval", formatInterval(rule.getCheckIntervalSec()));
        context.setVariable("firedAt", now.format(DT_FORMAT));
        context.setVariable("conditions", conditions);
        context.setVariable("filters", rule.getFilters() != null ? rule.getFilters() : List.of());
        context.setVariable("detailLink",
                pinpointBaseUrl != null ? buildDetailLink(rule, metricResults.range()) : null);
        context.setVariable("historyLink", buildHistoryLink(rule));
        return templateEngine.process(HTML_BODY_TEMPLATE, context);
    }

    /**
     * View row for the conditions table in the default HTML body. {@code op} is the
     * comparison symbol; condition columns are {@code null} when the leaf is a pulse
     * trigger with no displayable threshold.
     */
    record ConditionRow(String metric, Double value, boolean displayCondition,
                        String op, Double threshold, Integer windowSec, Details details) {
        static ConditionRow from(MetricQueryKey key, Double value, AlarmCondition leaf, Details details) {
            if (!hasDisplayCondition(leaf)) {
                return new ConditionRow(metricName(key), value, false, null, null, null, details);
            }
            return new ConditionRow(metricName(key), value, true,
                    nullSafe(leaf.getOp()), leaf.getThreshold(), leaf.getWindowSec(), details);
        }
    }

    /**
     * Detail lines that fit the channel budget, plus how many were left out.
     */
    record Details(List<String> lines, int more) {

        static final Details NONE = new Details(List.of(), 0);

        public boolean isEmpty() {
            return lines.isEmpty() && more == 0;
        }

        public String moreText() {
            return "... and " + more + " more";
        }
    }

    /**
     * Keeps detail lines within {@code maxChars} each and {@code budgetBytes} in total.
     * {@code value} is the metric value, so the omitted count stays right even when the
     * query itself already returned only a sample of the groups.
     */
    private static Details buildDetails(List<String> details, Double value, int maxChars, int budgetBytes) {
        if (details.isEmpty()) {
            return Details.NONE;
        }
        List<String> lines = new ArrayList<>(details.size());
        int consumed = 0;
        for (String detail : details) {
            String line = truncate(stripControlChars(detail), maxChars);
            int size = line.getBytes(StandardCharsets.UTF_8).length;
            if (consumed + size > budgetBytes) {
                break;
            }
            lines.add(line);
            consumed += size;
        }
        int total = value != null ? (int) Math.round(value) : details.size();
        return new Details(lines, Math.max(0, total - lines.size()));
    }

    private static String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, truncateEndIndex(text, maxChars)) + "...";
    }

    /**
     * A lone high surrogate left by cutting a surrogate pair in half is not encodable to
     * UTF-8, so it turns into '?' once the message is stored or sent. Drop it instead.
     */
    private static int truncateEndIndex(String text, int maxChars) {
        return maxChars > 0 && Character.isHighSurrogate(text.charAt(maxChars - 1))
                ? maxChars - 1 : maxChars;
    }

    /**
     * Detail text comes from the monitored application, so control characters have to go:
     * the plain and webhook bodies are line oriented and a newline forges a field.
     */
    private static String stripControlChars(String text) {
        return CONTROL_CHARS.matcher(text).replaceAll(" ");
    }

    private static String severityColor(AlarmSeverity severity) {
        if (severity == null) {
            return "#546e7a";
        }
        return switch (severity) {
            case CRITICAL -> "#b20011";
            case WARNING -> "#ff9800";
        };
    }

    private String buildDefaultBody(AlarmRuleV2 rule, MetricQueryResult metricResults, ZonedDateTime now,
                                    int detailBudgetBytes) {
        Map<MetricQueryKey, AlarmCondition> leafByMetric = buildLeafByMetricKey(rule);

        StringBuilder sb = new StringBuilder();
        sb.append("Service: ").append(nullSafe(rule.getServiceName())).append("\n");
        sb.append("Application: ").append(nullSafe(rule.getApplicationName())).append("\n");
        sb.append("Data Source: ").append(nullSafe(rule.getDataSource())).append("\n");

        if (StringUtils.hasText(rule.getDescription())) {
            sb.append("Description: ").append(rule.getDescription()).append("\n");
        }

        sb.append("Conditions:\n");
        int detailBudget = detailBudgetBytes;
        for (Map.Entry<MetricQueryKey, Double> e : metricResults.values().entrySet()) {
            AlarmCondition leaf = leafByMetric.get(e.getKey());
            sb.append("  ").append(metricName(e.getKey())).append(": ").append(e.getValue());
            if (hasDisplayCondition(leaf)) {
                sb.append(" (condition: ").append(nullSafe(leaf.getOp())).append(" ").append(leaf.getThreshold());
                if (leaf.getWindowSec() != null) {
                    sb.append(", window: ").append(leaf.getWindowSec()).append("s");
                }
                sb.append(")");
            }
            sb.append("\n");

            Details details = buildDetails(metricResults.details(e.getKey()), e.getValue(),
                    PLAIN_DETAIL_MAX_CHARS, detailBudget);
            for (String line : details.lines()) {
                sb.append("    - ").append(line).append("\n");
                detailBudget -= line.getBytes(StandardCharsets.UTF_8).length;
            }
            if (details.more() > 0) {
                sb.append("    ").append(details.moreText()).append("\n");
            }
        }

        List<AlarmFilter> filters = rule.getFilters();
        if (!CollectionUtils.isEmpty(filters)) {
            sb.append("Filters:\n");
            for (AlarmFilter f : filters) {
                sb.append("  ").append(f.getKey()).append(" ").append(f.getOp())
                  .append(" ").append(f.getValue()).append("\n");
            }
        }

        sb.append("Check interval: ").append(formatInterval(rule.getCheckIntervalSec())).append("\n");
        sb.append("Fired at: ").append(now.format(DT_FORMAT));
        return sb.toString();
    }

    /** 24h cap: after a batch outage lastCheckedAt can be days old, and the screen would choke. */
    private static final long MAX_LINK_WINDOW_MS = Duration.ofDays(1).toMillis();

    /**
     * The screen a recipient should land on depends on what the rule watches, not on
     * the rule itself, so the route comes from the data source. One this deployment
     * does not have installed, or one with no such screen, gets no link rather than a
     * wrong one.
     * <p>
     * Links to the range the queries actually covered, so the recipient sees the data that
     * fired the alarm. The range is absent only when no query ran (a check failure), and the
     * check interval is then the best guess available.
     * <p>
     * from/to are epoch millis: a wall clock string would have to be written in some zone,
     * and the screen reads it back in the viewer's own timezone setting -- which the batch
     * cannot know. Epoch carries the instant with no zone to agree on.
     */
    private String buildDetailLink(AlarmRuleV2 rule, QueriedRange range) {
        AlarmDataSource dataSource = dataSourceRegistry.find(rule.getDataSource()).orElse(null);
        if (dataSource == null) {
            return null;
        }
        long toMs = range != null ? range.toMs() : System.currentTimeMillis();
        long fromMs = range != null
                ? Math.max(range.fromMs(), toMs - MAX_LINK_WINDOW_MS)
                : toMs - 1000L * (rule.getCheckIntervalSec() != null ? rule.getCheckIntervalSec() : 0);
        return dataSource.detailLink(
                pinpointBaseUrl,
                rule.getApplicationName() + "@" + nullSafe(rule.getApplicationType()),
                fromMs,
                toMs);
    }

    private String buildHistoryLink(AlarmRuleV2 rule) {
        if (pinpointBaseUrl == null || rule.getId() == null) {
            return null;
        }
        return String.format("%s%s?ruleId=%d&serviceName=%s&applicationName=%s",
                pinpointBaseUrl,
                alarmPagePath,
                rule.getId(),
                URLEncoder.encode(nullSafe(rule.getServiceName()), StandardCharsets.UTF_8),
                URLEncoder.encode(nullSafe(rule.getApplicationName()), StandardCharsets.UTF_8));
    }

    private String replaceVariables(String template, AlarmRuleV2 rule, MetricQueryResult metricResults,
                                    ZonedDateTime now, int detailBudgetBytes) {
        // absent keys (e.g. ${metric} with no metric results) are left as-is, like the
        // previous chained-replace implementation
        Map<String, String> variables = new HashMap<>();
        variables.put("severity", rule.getSeverity() != null ? rule.getSeverity().name() : "");
        variables.put("name", nullSafe(rule.getName()));
        variables.put("description", nullSafe(rule.getDescription()));
        variables.put("service_name", nullSafe(rule.getServiceName()));
        variables.put("application_name", nullSafe(rule.getApplicationName()));
        variables.put("template_name", nullSafe(rule.getTemplateName()));
        variables.put("data_source", nullSafe(rule.getDataSource()));
        variables.put("check_interval", rule.getCheckIntervalSec() != null
                ? String.valueOf(rule.getCheckIntervalSec()) : "");
        variables.put("fired_at", now.format(DT_FORMAT));
        String detailLink = pinpointBaseUrl != null
                ? nullSafe(buildDetailLink(rule, metricResults.range())) : "";
        variables.put("detail_link", detailLink);
        // Message bodies already stored by users spell it ${rum_link}; an unknown key is
        // left in the output verbatim, so dropping the old name would print it to recipients.
        variables.put("rum_link", detailLink);
        variables.put("history_link", nullSafe(buildHistoryLink(rule)));

        if (!metricResults.isEmpty()) {
            String summary = metricResults.values().entrySet().stream()
                    .map(e -> metricName(e.getKey()) + "=" + e.getValue())
                    .collect(Collectors.joining(", "));
            variables.put("metrics", summary);

            Map.Entry<MetricQueryKey, Double> first = metricResults.values().entrySet().iterator().next();
            variables.put("metric", metricName(first.getKey()));
            variables.put("value", String.valueOf(first.getValue()));
            variables.put("details", buildDetailSummary(metricResults, detailBudgetBytes));
        }

        if (rule.getConditions() != null) {
            List<AlarmCondition> leaves = ConditionUtils.extractLeaves(rule.getConditions());
            if (!leaves.isEmpty()) {
                AlarmCondition firstLeaf = leaves.get(0);
                variables.put("op", nullSafe(firstLeaf.getOp()));
                variables.put("threshold", firstLeaf.getThreshold() != null
                        ? String.valueOf(firstLeaf.getThreshold()) : "");
            }
        }

        List<AlarmFilter> filters = rule.getFilters();
        String filterSummary;
        if (CollectionUtils.isEmpty(filters)) {
            filterSummary = "";
        }
        else {
            filterSummary = filters.stream()
                    .map(f -> f.getKey() + " " + f.getOp() + " " + f.getValue())
                    .collect(Collectors.joining(", "));
        }
        variables.put("filters", filterSummary);

        StringSubstitutor substitutor = new StringSubstitutor(variables);
        substitutor.setDisableSubstitutionInValues(true);
        return substitutor.replace(template);
    }

    /**
     * Renders every metric's detail lines as one block for {@code ${details}} in custom templates.
     */
    private static String buildDetailSummary(MetricQueryResult metricResults, int detailBudgetBytes) {
        List<String> lines = new ArrayList<>();
        int budget = detailBudgetBytes;
        for (Map.Entry<MetricQueryKey, Double> e : metricResults.values().entrySet()) {
            Details details = buildDetails(metricResults.details(e.getKey()), e.getValue(),
                    PLAIN_DETAIL_MAX_CHARS, budget);
            for (String line : details.lines()) {
                lines.add("- " + line);
                budget -= line.getBytes(StandardCharsets.UTF_8).length;
            }
            if (details.more() > 0) {
                lines.add(details.moreText());
            }
        }
        return String.join("\n", lines);
    }

    private static String formatInterval(Integer intervalSec) {
        return intervalSec != null ? intervalSec + "s" : "";
    }

    // ComparisonOp.toString() is its symbol, so formatted output matches the raw string form
    private static String nullSafe(Object value) {
        return Objects.toString(value, "");
    }

    private static String metricName(MetricQueryKey metricKey) {
        return Objects.toString(metricKey.metric(), "");
    }

    private static boolean hasDisplayCondition(AlarmCondition condition) {
        return condition != null && condition.getTrigger() != AlarmCondition.Trigger.NEW_GROUP;
    }
}
