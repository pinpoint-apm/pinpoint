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
package com.navercorp.pinpoint.alarm.sender;

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.evaluation.ConditionUtils;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.Detail;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.Details;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.QueriedRange;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import org.apache.commons.text.StringSubstitutor;
import org.jspecify.annotations.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
        // The service segment goes after it, and a trailing '/' would make that "//{service}".
        this.alarmPagePath = Objects.requireNonNull(alarmPagePath, "alarmPagePath").replaceAll("/+$", "");
    }

    public String formatTitle(AlarmRuleV2 rule, String customTitle, MetricQueryResult metricResults) {
        ZonedDateTime now = ZonedDateTime.now();
        String template = StringUtils.hasText(customTitle) ? customTitle : defaultTitleTemplate(rule);
        return replaceVariables(template, rule, metricResults, now, UNLIMITED_DETAIL_BYTES, DetailStyle.TEXT_ONLY);
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
            return replaceVariables(customTemplate, rule, metricResults, now, detailBudgetBytes, DetailStyle.TEXT_ONLY);
        }
        return buildDefaultBody(rule, metricResults, now, detailBudgetBytes);
    }

    /**
     * How much of a metric's details a channel shows. The SMS and the title keep to the text: the id
     * and the link are long, and an SMS is billed by length, so a metric with a summary shows the
     * summary alone. The email and webhook messages show the summary above the lines.
     */
    enum DetailStyle {
        TEXT_ONLY,
        FULL
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
            return replaceVariables(customTemplate, rule, metricResults, now, UNLIMITED_DETAIL_BYTES, DetailStyle.FULL);
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
                    .append(" : ").append(formatNumber(e.getValue()));
            if (hasDisplayCondition(leaf)) {
                sb.append(" (").append(nullSafe(leaf.getOp())).append(" ").append(formatNumber(leaf.getThreshold()));
                if (leaf.getWindowSec() != null) {
                    sb.append(", window: ").append(leaf.getWindowSec()).append("s");
                }
                sb.append(")");
            }
            sb.append("\n");

            Rendered details = buildDetails(metricResults.details(e.getKey()), DetailStyle.FULL,
                    HTML_DETAIL_MAX_CHARS, UNLIMITED_DETAIL_BYTES);
            for (Line line : details.lines()) {
                sb.append("        • ").append(linkedLine(line, mrkdwn)).append("\n");
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
        String body = replaceVariables(customTemplate, rule, metricResults, now, UNLIMITED_DETAIL_BYTES, DetailStyle.FULL);
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
                .map(e -> {
                    Details details = metricResults.details(e.getKey());
                    String summary = details.summary() != null
                            ? summaryText(details.summary(), HTML_DETAIL_MAX_CHARS) : null;
                    Rendered lines = buildDetails(new Details(null, details.lines(), details.omitted()),
                            DetailStyle.FULL, HTML_DETAIL_MAX_CHARS, UNLIMITED_DETAIL_BYTES);
                    return ConditionRow.from(e.getKey(), e.getValue(), leafByMetric.get(e.getKey()), summary, lines);
                })
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
    record ConditionRow(String metric, String value, boolean displayCondition,
                        String op, String threshold, Integer windowSec, String summary, Rendered details) {
        static ConditionRow from(MetricQueryKey key, Double value, AlarmCondition leaf, String summary,
                                 Rendered details) {
            if (!hasDisplayCondition(leaf)) {
                return new ConditionRow(metricName(key), formatNumber(value), false, null, null, null,
                        summary, details);
            }
            return new ConditionRow(metricName(key), formatNumber(value), true,
                    nullSafe(leaf.getOp()), formatNumber(leaf.getThreshold()), leaf.getWindowSec(), summary, details);
        }
    }

    /** A detail line as rendered: the id is null where the channel hides it, and the url is absolute or null. */
    record Line(String id, String body, String url) {

        /** The id goes in front, where a cut of a long body cannot reach it. */
        public String text() {
            return id != null ? "[" + id + "] " + body : body;
        }

        int bytes() {
            return utf8Length(text()) + (url != null ? utf8Length(url) : 0);
        }
    }

    /** Detail lines that fit the channel budget, plus how many were left out. */
    record Rendered(List<Line> lines, int more) {

        public boolean isEmpty() {
            return lines.isEmpty() && more == 0;
        }

        public String moreText() {
            return AlarmMessageFormatter.moreText(more);
        }
    }

    private static String moreText(int more) {
        return String.format(Locale.ROOT, "... and %,d more", more);
    }

    private static String summaryText(String summary, int maxChars) {
        return truncate(stripControlChars(summary), maxChars);
    }

    private Rendered buildDetails(Details details, DetailStyle style, int maxChars, int budgetBytes) {
        return render(details, style, maxChars, budgetBytes, pinpointBaseUrl);
    }

    /**
     * What the alarm history keeps of a metric's details: the lines the email shows, in the same
     * order, without the links and without a cap.
     */
    public static List<String> historyLines(Details details) {
        Rendered rendered = render(details, DetailStyle.FULL, Integer.MAX_VALUE, UNLIMITED_DETAIL_BYTES, null);
        List<String> lines = new ArrayList<>(rendered.lines().size() + 1);
        for (Line line : rendered.lines()) {
            lines.add(line.text());
        }
        if (rendered.more() > 0) {
            lines.add(rendered.moreText());
        }
        return lines;
    }

    /**
     * The one place that decides how the lines, their ids and links combine, after the summary when
     * there is one. The default email body places the summary itself. Keeps each
     * line within {@code maxChars} and all of them within {@code budgetBytes}. What the query
     * already left out is counted too, so the count stays right when it returned only a sample.
     */
    private static Rendered render(Details details, DetailStyle style, int maxChars, int budgetBytes,
                                   String baseUrl) {
        boolean full = style == DetailStyle.FULL;
        List<Line> lines = new ArrayList<>(details.lines().size() + 1);
        int consumed = 0;
        if (details.summary() != null) {
            Line summary = new Line(null, summaryText(details.summary(), maxChars), null);
            if (summary.bytes() <= budgetBytes) {
                lines.add(summary);
                consumed = summary.bytes();
            }
            if (!full) {
                return new Rendered(lines, 0);
            }
        }
        int shown = 0;
        for (Detail detail : details.lines()) {
            String text = truncate(stripControlChars(detail.text()), maxChars);
            String id = full && detail.id() != null ? stripControlChars(detail.id()) : null;
            String url = full && detail.link() != null && baseUrl != null ? baseUrl + detail.link() : null;
            Line line = new Line(id, text, url);
            if (consumed + line.bytes() > budgetBytes) {
                break;
            }
            lines.add(line);
            consumed += line.bytes();
            shown++;
        }
        return new Rendered(lines, details.lines().size() - shown + details.omitted());
    }

    private static int utf8Length(String text) {
        return text.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Slack takes the link as the line itself; anywhere else the URL goes on its own line,
     * since a URL glued to text from the monitored application is not what a client turns
     * into a link.
     */
    private static String linkedLine(Line line, boolean mrkdwn) {
        if (line.url() == null) {
            return escapeMrkdwn(line.text(), mrkdwn);
        }
        if (mrkdwn) {
            return "<" + line.url() + "|" + escapeMrkdwn(line.text(), mrkdwn) + ">";
        }
        return line.text() + "\n          " + line.url();
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
            sb.append("  ").append(metricName(e.getKey())).append(": ").append(formatNumber(e.getValue()));
            if (hasDisplayCondition(leaf)) {
                sb.append(" (condition: ").append(nullSafe(leaf.getOp())).append(" ")
                        .append(formatNumber(leaf.getThreshold()));
                if (leaf.getWindowSec() != null) {
                    sb.append(", window: ").append(leaf.getWindowSec()).append("s");
                }
                sb.append(")");
            }
            sb.append("\n");

            Rendered details = buildDetails(metricResults.details(e.getKey()), DetailStyle.TEXT_ONLY,
                    PLAIN_DETAIL_MAX_CHARS, detailBudget);
            for (Line line : details.lines()) {
                sb.append("    - ").append(line.text()).append("\n");
                detailBudget -= line.bytes();
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
        String path = dataSource.detailPath(rule, fromMs, toMs);
        return path != null ? pinpointBaseUrl + path : null;
    }

    /**
     * The service goes in the path, where the web reads the service of a page, so the link opens
     * the rule's service whichever one the recipient has selected. Two applications can share a
     * name and differ in type, so the link carries both.
     */
    private String buildHistoryLink(AlarmRuleV2 rule) {
        if (pinpointBaseUrl == null || rule.getId() == null) {
            return null;
        }
        return String.format("%s%s/%s?ruleId=%d&applicationName=%s&applicationType=%s",
                pinpointBaseUrl,
                alarmPagePath,
                AlarmDataSource.serviceSegment(rule.getServiceName()),
                rule.getId(),
                URLEncoder.encode(nullSafe(rule.getApplicationName()), StandardCharsets.UTF_8),
                URLEncoder.encode(nullSafe(rule.getApplicationType()), StandardCharsets.UTF_8));
    }

    private String replaceVariables(String template, AlarmRuleV2 rule, MetricQueryResult metricResults,
                                    ZonedDateTime now, int detailBudgetBytes, DetailStyle style) {
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
                    .map(e -> metricName(e.getKey()) + "=" + formatNumber(e.getValue()))
                    .collect(Collectors.joining(", "));
            variables.put("metrics", summary);

            Map.Entry<MetricQueryKey, Double> first = metricResults.values().entrySet().iterator().next();
            variables.put("metric", metricName(first.getKey()));
            variables.put("value", formatNumber(first.getValue()));
            variables.put("summary", buildSummaries(metricResults));
            variables.put("details", buildDetailSummary(metricResults, detailBudgetBytes, style));
        }

        if (rule.getConditions() != null) {
            List<AlarmCondition> leaves = ConditionUtils.extractLeaves(rule.getConditions());
            if (!leaves.isEmpty()) {
                AlarmCondition firstLeaf = leaves.get(0);
                variables.put("op", nullSafe(firstLeaf.getOp()));
                variables.put("threshold", formatNumber(firstLeaf.getThreshold()));
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

    /** Every metric's summary, one per line, for {@code ${summary}} in custom templates. */
    private static String buildSummaries(MetricQueryResult metricResults) {
        return metricResults.values().keySet().stream()
                .map(key -> metricResults.details(key).summary())
                .filter(Objects::nonNull)
                .map(summary -> summaryText(summary, PLAIN_DETAIL_MAX_CHARS))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Renders every metric's detail lines as one block for {@code ${details}} in custom templates.
     * The summary is {@code ${summary}}, so a template places each where it wants.
     */
    private String buildDetailSummary(MetricQueryResult metricResults, int detailBudgetBytes, DetailStyle style) {
        List<String> lines = new ArrayList<>();
        int budget = detailBudgetBytes;
        for (Map.Entry<MetricQueryKey, Double> e : metricResults.values().entrySet()) {
            Details lineDetails = metricResults.details(e.getKey());
            Details withoutSummary = new Details(null, lineDetails.lines(), lineDetails.omitted());
            Rendered details = buildDetails(withoutSummary, style, PLAIN_DETAIL_MAX_CHARS, budget);
            for (Line line : details.lines()) {
                lines.add("- " + line.text());
                budget -= line.bytes();
                if (line.url() != null) {
                    lines.add("  " + line.url());
                }
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

    /** Two decimals at most and no trailing zeros: 7.0 reads 7, 40.98925 reads 40.99. */
    static String formatNumber(Double value) {
        if (value == null) {
            return "";
        }
        if (!Double.isFinite(value)) {
            return String.valueOf(value);
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private static String metricName(MetricQueryKey metricKey) {
        return Objects.toString(metricKey.metric(), "");
    }

    private static boolean hasDisplayCondition(AlarmCondition condition) {
        return condition != null && condition.getTrigger() != AlarmCondition.Trigger.NEW_GROUP;
    }
}
