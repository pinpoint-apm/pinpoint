package com.navercorp.pinpoint.alarm.sender;

import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.QueriedRange;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmMetricDefinition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AlarmMessageFormatterTest {

    private static final String ALARM_PAGE_PATH = "/config/alarm";

    private final AlarmMessageFormatter formatter =
            new AlarmMessageFormatter(templateEngine(), AlarmMessageFormatter.toLogoPngBase64("dGVzdC1sb2dv"), null, ALARM_PAGE_PATH,
                    new AlarmDataSourceRegistry(List.of(new TestAlarmDataSource.Provider())));
    private final AlarmMessageFormatter linkFormatter =
            new AlarmMessageFormatter(templateEngine(), "", "https://pinpoint.example.com/", ALARM_PAGE_PATH,
                    new AlarmDataSourceRegistry(List.of(new TestAlarmDataSource.Provider())));

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(true);
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Test
    void defaultTitleNamesTheApplication() {
        AlarmRuleV2 rule = createRule();
        String title = formatter.formatTitle(rule, null, metricResults(rule, 127.0));

        assertEquals("[CRITICAL][test-app] Test Alarm", title);
    }

    @Test
    void defaultTitleNamesTheBundleWhenTheRuleCameFromOne() {
        AlarmRuleV2 rule = createRule();
        rule.setTemplateName("default bundle");
        String title = formatter.formatTitle(rule, null, metricResults(rule, 127.0));

        assertEquals("[CRITICAL][test-app][default bundle] Test Alarm", title);
    }

    @Test
    void defaultTitleDropsTheBundleSegmentForAStandaloneRule() {
        AlarmRuleV2 rule = createRule();
        rule.setTemplateName("   ");
        String title = formatter.formatTitle(rule, null, metricResults(rule, 127.0));

        // No bundle: the segment goes away rather than rendering an empty bracket pair.
        assertEquals("[CRITICAL][test-app] Test Alarm", title);
    }

    @Test
    void customTitle() {
        AlarmRuleV2 rule = createRule();
        String title = formatter.formatTitle(rule, "ALERT: ${name}", metricResults(rule, 127.0));

        assertEquals("ALERT: Test Alarm", title);
    }

    @Test
    void defaultBody_containsAllFields() {
        AlarmRuleV2 rule = createRule();
        String body = formatter.formatBody(rule, null, metricResults(rule, 127.0));

        assertThat(body).contains("test-service", "test-app", "PRIMARY", "error_count", "127.0");
    }

    @Test
    void defaultBody_usesMetricNameOnly() {
        AlarmRuleV2 rule = createNewGroupRule();
        String body = formatter.formatBody(rule, null, metricResults(rule, 3.0));

        assertThat(body)
                .contains("new_group_count: 3.0")
                .doesNotContain("condition:", "trigger=", "window=", "aggregation=", "null");
    }

    @Test
    void customMetricsTemplate_usesMetricNameOnly() {
        AlarmRuleV2 rule = createNewGroupRule();
        String body = formatter.formatBody(rule, "${metrics}", metricResults(rule, 3.0));

        assertEquals("new_group_count=3.0", body);
    }

    @Test
    void customTemplate() {
        AlarmRuleV2 rule = createRule();
        String body = formatter.formatBody(rule,
                "${metric} is ${value} on ${application_name}",
                metricResults(rule, 127.0));

        assertEquals("error_count is 127.0 on test-app", body);
    }

    @Test
    void thresholdFromLeafCondition() {
        AlarmRuleV2 rule = createRule();
        String body = formatter.formatBody(rule, "threshold: ${op} ${threshold}",
                metricResults(rule, 127.0));

        assertEquals("threshold: >= 50.0", body);
    }

    @Test
    void defaultHtmlBody_rendersTemplate() {
        AlarmRuleV2 rule = createRule();

        String html = formatter.formatHtmlBody(rule, null, metricResults(rule, 127.0));

        assertThat(html)
                .contains("Test Alarm", "CRITICAL", "test-service", "error_count", "127.0",
                        "&gt;= 50.0", "(window: 300s)")
                // logo img rendered from the data URI, with brand alt text
                .contains("<img", "PINPOINT")
                // template fully processed — no leftover thymeleaf attributes
                .doesNotContain("th:text", "th:if", "th:each");
    }

    @Test
    void defaultHtmlBody_blankLogo_fallsBackToBrandText() {
        AlarmMessageFormatter noLogoFormatter = new AlarmMessageFormatter(templateEngine(), "", null, ALARM_PAGE_PATH, new AlarmDataSourceRegistry(List.of(new TestAlarmDataSource.Provider())));
        AlarmRuleV2 rule = createRule();

        String html = noLogoFormatter.formatHtmlBody(rule, null, metricResults(rule, 127.0));

        assertThat(html)
                .contains(">PINPOINT<")
                .doesNotContain("<img");
    }

    @Test
    void defaultHtmlBody_newGroupRule_hidesConditionColumn() {
        AlarmRuleV2 rule = createNewGroupRule();

        String html = formatter.formatHtmlBody(rule, null, metricResults(rule, 3.0));

        assertThat(html)
                .contains("new_group_count", "3.0")
                .doesNotContain("(window:", "null");
    }

    private AlarmRuleV2 createRule() {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setName("Test Alarm");
        rule.setSeverity(AlarmSeverity.CRITICAL);
        rule.setDataSource(TestAlarmDataSource.PRIMARY.name());
        rule.setServiceName("test-service");
        rule.setApplicationName("test-app");

        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("error_count");
        leaf.setOp(AlarmCondition.ComparisonOp.GTE);
        leaf.setThreshold(50.0);
        leaf.setWindowSec(300);
        leaf.setAggregation(AlarmCondition.Aggregation.COUNT);
        rule.setConditions(leaf);

        return rule;
    }

    private AlarmRuleV2 createNewGroupRule() {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setName("Test Alarm");
        rule.setSeverity(AlarmSeverity.CRITICAL);
        rule.setDataSource(TestAlarmDataSource.PRIMARY.name());
        rule.setServiceName("test-service");
        rule.setApplicationName("test-app");

        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("new_group_count");
        leaf.setTrigger(AlarmCondition.Trigger.NEW_GROUP);
        leaf.setOp(AlarmCondition.ComparisonOp.GTE);
        leaf.setThreshold(1.0);
        leaf.setWindowSec(3600);
        rule.setConditions(leaf);

        return rule;
    }

    @Test
    void defaultBody_newGroupRule_listsNewErrorGroups() {
        AlarmRuleV2 rule = createNewGroupRule();
        String body = formatter.formatBody(rule, null, metricResults(rule, 2.0,
                List.of("TypeError: undefined is not a function", "ReferenceError: foo is not defined")));

        assertThat(body)
                .contains("    - TypeError: undefined is not a function")
                .contains("    - ReferenceError: foo is not defined")
                .doesNotContain("and 0 more");
    }

    @Test
    void defaultBody_moreNewGroupsThanDescribed_reportsTheRemainder() {
        AlarmRuleV2 rule = createNewGroupRule();
        String body = formatter.formatBody(rule, null, metricResults(rule, 12.0,
                List.of("TypeError: undefined is not a function")));

        assertThat(body).contains("    ... and 11 more");
    }

    @Test
    void defaultBody_longMessage_isTruncated() {
        AlarmRuleV2 rule = createNewGroupRule();
        String body = formatter.formatBody(rule, null, metricResults(rule, 1.0,
                List.of("TypeError: " + "x".repeat(500))));

        assertThat(body).contains("...").doesNotContain("x".repeat(200));
    }

    @Test
    void smsBody_multiByteMessages_staysWithinTheLmsByteLimit() {
        AlarmRuleV2 rule = createNewGroupRule();
        List<String> details = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            details.add("에러" + i + ": " + "메시지".repeat(100));
        }

        String smsBody = formatter.formatSmsBody(rule, null, metricResults(rule, 10.0, details));
        String body = formatter.formatBody(rule, null, metricResults(rule, 10.0, details));

        assertThat(smsBody.getBytes(StandardCharsets.UTF_8).length).isLessThan(2000);
        assertThat(smsBody).contains("more");
        // the same content without the SMS budget keeps every group
        assertThat(body).doesNotContain("more");
    }

    @Test
    void defaultHtmlBody_newGroupRule_listsNewErrorGroups() {
        AlarmRuleV2 rule = createNewGroupRule();
        String html = formatter.formatHtmlBody(rule, null, metricResults(rule, 3.0,
                List.of("TypeError: undefined is not a function")));

        assertThat(html)
                .contains("TypeError: undefined is not a function")
                .contains("... and 2 more");
    }

    @Test
    void customDetailsTemplate_rendersDetailLines() {
        AlarmRuleV2 rule = createNewGroupRule();
        String body = formatter.formatBody(rule, "${details}", metricResults(rule, 1.0,
                List.of("TypeError: undefined is not a function")));

        assertEquals("- TypeError: undefined is not a function", body);
    }

    @Test
    void detailLines_neverCarryControlCharacters() {
        AlarmRuleV2 rule = createNewGroupRule();
        MetricQueryResult results = metricResults(rule, 1.0,
                List.of("Error: boom\n\u2022 \ud83c\udf10 Service: forged"));

        assertThat(formatter.formatBody(rule, null, results))
                .contains("    - Error: boom \u2022 \ud83c\udf10 Service: forged");
    }

    @Test
    void longMessage_isNotTruncatedInsideASurrogatePair() {
        AlarmRuleV2 rule = createNewGroupRule();
        // PLAIN_DETAIL_MAX_CHARS is 100, so the pair straddles the cut at 99/100
        String detail = "x".repeat(99) + "\ud83d\ude80" + "y".repeat(50);

        String body = formatter.formatBody(rule, null, metricResults(rule, 1.0, List.of(detail)));

        assertThat(body).doesNotContain("\ud83d");
    }

    @Test
    void webhookBody_slack_usesMrkdwnAndSeverityHeadline() {
        AlarmRuleV2 rule = createNewGroupRule();
        rule.setSeverity(AlarmSeverity.WARNING);
        String body = formatter.formatWebhookBody(rule, null, metricResults(rule, 1.0,
                List.of("TypeError: x is not a function")), true);

        assertThat(body)
                .startsWith("⚠️ *[WARNING]* *[test-app]* Test Alarm")
                .contains("• 🌐 *Service:* test-service")
                .contains("• 🔍 *Conditions:*")
                .contains("    └ `new_group_count` : 1.0")
                .contains("        • TypeError: x is not a function");
    }

    @Test
    void webhookBody_default_keepsLayoutWithoutMarkup() {
        AlarmRuleV2 rule = createNewGroupRule();
        rule.setSeverity(AlarmSeverity.WARNING);
        String body = formatter.formatWebhookBody(rule, null, metricResults(rule, 1.0), false);

        assertThat(body)
                // the DEFAULT payload carries the headline in its own title field
                .startsWith("• 🌐 Service: test-service")
                .doesNotContain("[WARNING]", "Test Alarm")
                .contains("    └ new_group_count : 1.0")
                .doesNotContain("*", "`");
    }

    @Test
    void webhookBody_critical_usesCriticalHeadline() {
        AlarmRuleV2 rule = createRule();
        String body = formatter.formatWebhookBody(rule, null, metricResults(rule, 127.0), true);

        assertThat(body)
                .startsWith("🚨 *[CRITICAL]* *[test-app]* Test Alarm")
                .contains("    └ `error_count` : 127.0 (>= 50.0, window: 300s)");
    }

    @Test
    void webhookBody_customTemplate_isRenderedAsIs() {
        AlarmRuleV2 rule = createRule();
        String body = formatter.formatWebhookBody(rule, "${metrics}", metricResults(rule, 127.0), true);

        assertEquals("error_count=127.0", body);
    }

    @Test
    void webhookBody_slack_escapesMrkdwnInDetailLines() {
        AlarmRuleV2 rule = createNewGroupRule();
        String body = formatter.formatWebhookBody(rule, null, metricResults(rule, 1.0,
                List.of("Error: <https://evil.example|click here> & <!channel>")), true);

        assertThat(body)
                .contains("        \u2022 Error: &lt;https://evil.example|click here&gt; "
                        + "&amp; &lt;!channel&gt;")
                .doesNotContain("<https://evil.example", "<!channel>");
    }

    @Test
    void detailLink_usesTheRangeTheQueryCovered() {
        AlarmRuleV2 rule = createNewGroupRule();
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setCheckIntervalSec(300);
        // The batch fell behind: the query looked at 2h, not the check interval and not the
        // 3600s windowSec the leaf happens to carry.
        long toMs = 1789041600000L;
        long fromMs = toMs - Duration.ofHours(2).toMillis();
        MetricQueryResult results = metricResultsWithRange(rule, 3.0, new QueriedRange(fromMs, toMs));

        String link = linkFormatter.formatBody(rule, "${detail_link}", results);

        assertEquals("https://pinpoint.example.com/detail/test-app@javascript"
                + "?from=" + fromMs + "&to=" + toMs, link);
    }

    @Test
    void detailLink_capsTheRangeAtOneDay() {
        AlarmRuleV2 rule = createNewGroupRule();
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        // lastCheckedAt can be days old after an outage, and the screen would choke on that
        long toMs = 1789041600000L;
        MetricQueryResult results = metricResultsWithRange(rule, 3.0,
                new QueriedRange(toMs - Duration.ofDays(5).toMillis(), toMs));

        String link = linkFormatter.formatBody(rule, "${detail_link}", results);

        assertEquals("https://pinpoint.example.com/detail/test-app@javascript"
                + "?from=" + (toMs - Duration.ofDays(1).toMillis()) + "&to=" + toMs, link);
    }

    @Test
    void detailLink_fallsBackToTheCheckIntervalWhenNoQueryRan() {
        AlarmRuleV2 rule = createRule();
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setCheckIntervalSec(60);

        // a check failure carries no metric results, so there is no queried range to use
        String link = linkFormatter.formatBody(rule, "${detail_link}", MetricQueryResult.empty());

        // 60s, the check interval -- not the leaf's 300s windowSec
        assertEquals(Duration.ofSeconds(60), linkRange(link));
    }

    // A deployable can run without the module that owns a rule's data source. The rule is
    // still evaluated and still notifies; it just gets no link rather than a wrong one.
    @Test
    void detailLink_isEmptyWhenNoInstalledModuleOwnsTheDataSource() {
        AlarmMessageFormatter noDataSources = new AlarmMessageFormatter(templateEngine(), "",
                "https://pinpoint.example.com/", ALARM_PAGE_PATH, new AlarmDataSourceRegistry(List.of()));
        AlarmRuleV2 rule = createNewGroupRule();
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        long toMs = 1789041600000L;
        MetricQueryResult results = metricResultsWithRange(rule, 3.0,
                new QueriedRange(toMs - Duration.ofHours(2).toMillis(), toMs));

        assertEquals("", noDataSources.formatBody(rule, "${detail_link}", results));
        assertThat(noDataSources.formatHtmlBody(rule, null, results)).doesNotContain("/detail/");
    }

    // The interface's default: a data source whose screens are not part of this
    // distribution contributes no route, and the link is dropped the same way.
    @Test
    void detailLink_isEmptyForADataSourceWithNoScreen() {
        AlarmDataSource screenless = new AlarmDataSource() {
            @Override
            public String name() {
                return TestAlarmDataSource.PRIMARY.name();
            }

            @Override
            public String label() {
                return "screenless";
            }

            @Override
            public List<String> filterKeys() {
                return List.of();
            }

            @Override
            public List<AlarmMetricDefinition> metrics() {
                return List.of();
            }
        };
        AlarmMessageFormatter noScreen = new AlarmMessageFormatter(templateEngine(), "",
                "https://pinpoint.example.com/", ALARM_PAGE_PATH,
                new AlarmDataSourceRegistry(List.of(() -> List.of(screenless))));
        AlarmRuleV2 rule = createNewGroupRule();
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        long toMs = 1789041600000L;

        String link = noScreen.formatBody(rule, "${detail_link}", metricResultsWithRange(rule, 3.0,
                new QueriedRange(toMs - Duration.ofHours(2).toMillis(), toMs)));

        assertEquals("", link);
    }

    @Test
    void defaultHtmlBody_linksToTheQueriedRange() {
        AlarmRuleV2 rule = createNewGroupRule();
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        long toMs = 1789041600000L;
        long fromMs = toMs - Duration.ofHours(2).toMillis();

        // the default HTML body is the common mail path: no custom template involved
        String html = linkFormatter.formatHtmlBody(rule, null,
                metricResultsWithRange(rule, 3.0, new QueriedRange(fromMs, toMs)));

        // the template escapes the query separator, so the two bounds are asserted apart
        assertThat(html)
                .contains("/detail/test-app@javascript?from=" + fromMs)
                .contains("to=" + toMs);
    }

    /** Span of a link's range, for the fallback whose absolute instants come from the clock. */
    private static Duration linkRange(String link) {
        MultiValueMap<String, String> params =
                UriComponentsBuilder.fromUriString(link).build().getQueryParams();
        return Duration.ofMillis(
                Long.parseLong(params.getFirst("to")) - Long.parseLong(params.getFirst("from")));
    }

    private MetricQueryResult metricResults(AlarmRuleV2 rule, double value) {
        return MetricQueryResult.of(Map.of(MetricQueryKey.from(rule.getConditions()), value));
    }

    private MetricQueryResult metricResults(AlarmRuleV2 rule, double value, List<String> details) {
        MetricQueryKey key = MetricQueryKey.from(rule.getConditions());
        return new MetricQueryResult(Map.of(key, value), Map.of(key, details), null);
    }

    private MetricQueryResult metricResultsWithRange(AlarmRuleV2 rule, double value, QueriedRange range) {
        return MetricQueryResult.of(Map.of(MetricQueryKey.from(rule.getConditions()), value), range);
    }
}
