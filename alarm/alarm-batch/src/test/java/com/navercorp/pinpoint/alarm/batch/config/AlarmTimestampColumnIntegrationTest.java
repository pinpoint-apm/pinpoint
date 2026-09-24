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
package com.navercorp.pinpoint.alarm.batch.config;

import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmEventType;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Five columns carry a DDL default that fills them from the database server's own clock, which is
 * not the clock the rest of the schema is written on. The default is still in place, so a statement
 * that leaves the column out is not an error: the row is written, just hours away from every other
 * timestamp beside it, and nothing says so.
 */
@SpringJUnitConfig
@ContextConfiguration(classes = AlarmJobIntegrationTestConfig.class)
@TestPropertySource(locations = "classpath:application-alarm-test.yml")
class AlarmTimestampColumnIntegrationTest {

    /** Wide enough for a slow container, far narrower than the nine hours a wrong clock costs. */
    private static final Duration TOLERANCE = Duration.ofMinutes(1);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AlarmTemplateDao alarmTemplateDao;

    @Autowired
    private AlarmTemplateItemDao alarmTemplateItemDao;

    @Autowired
    private AlarmRuleV2Dao alarmRuleV2Dao;

    @Autowired
    private AlarmNotificationChannelDao alarmNotificationChannelDao;

    @Autowired
    private AlarmHistoryV2Dao alarmHistoryV2Dao;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * Without this the rest of the class proves nothing: on a server that runs on UTC the DDL
     * default and UTC_TIMESTAMP() are the same value.
     */
    @Test
    void theDatabaseServerIsNotOnUtc() {
        LocalDateTime server = jdbc.queryForObject("SELECT CURRENT_TIMESTAMP", LocalDateTime.class);
        assertNotNull(server);
        Duration drift = Duration.between(LocalDateTime.now(ZoneOffset.UTC), server).abs();
        assertTrue(drift.toMinutes() >= 60,
                "the container has to run on something other than utc for these assertions to"
                        + " mean anything, server clock reads " + server);
    }

    // ---- alarm_template.updated_at ----

    @Test
    void templateInsertWritesUtc() {
        AlarmTemplate template = insertTemplate();
        assertUtc("alarm_template", "updated_at", template.getId());
    }

    @Test
    void templateUpdateWritesUtc() {
        AlarmTemplate template = insertTemplate();
        template.setName("renamed");
        alarmTemplateDao.update(template);
        assertUtc("alarm_template", "updated_at", template.getId());
    }

    @Test
    void templateMarkDeletedWritesUtc() {
        AlarmTemplate template = insertTemplate();
        alarmTemplateDao.markDeleted(template.getId());
        assertUtc("alarm_template", "updated_at", template.getId());
    }

    // ---- alarm_template_item.updated_at ----

    @Test
    void templateItemInsertWritesUtc() {
        AlarmTemplateItem item = insertItem(insertTemplate().getId());
        assertUtc("alarm_template_item", "updated_at", item.getId());
    }

    @Test
    void templateItemUpdateWritesUtc() {
        AlarmTemplateItem item = insertItem(insertTemplate().getId());
        item.setName("renamed");
        alarmTemplateItemDao.update(item);
        assertUtc("alarm_template_item", "updated_at", item.getId());
    }

    @Test
    void templateItemMarkDeletedWritesUtc() {
        AlarmTemplateItem item = insertItem(insertTemplate().getId());
        alarmTemplateItemDao.markDeleted(item.getId());
        assertUtc("alarm_template_item", "updated_at", item.getId());
    }

    @Test
    void templateItemMarkDeletedByTemplateIdWritesUtc() {
        AlarmTemplate template = insertTemplate();
        AlarmTemplateItem item = insertItem(template.getId());
        alarmTemplateItemDao.markDeletedByTemplateId(template.getId());
        assertUtc("alarm_template_item", "updated_at", item.getId());
    }

    // ---- alarm_rule_v2.updated_at ----

    @Test
    void ruleInsertWritesUtc() {
        AlarmRuleV2 rule = insertRule();
        assertUtc("alarm_rule_v2", "updated_at", rule.getId());
    }

    @Test
    void ruleUpdateWritesUtc() {
        AlarmRuleV2 rule = insertRule();
        rule.setName("renamed");
        alarmRuleV2Dao.updateRule(rule);
        assertUtc("alarm_rule_v2", "updated_at", rule.getId());
    }

    @Test
    void ruleUpdateEnabledWritesUtc() {
        AlarmRuleV2 rule = insertRule();
        alarmRuleV2Dao.updateEnabled(rule.getId(), false);
        assertUtc("alarm_rule_v2", "updated_at", rule.getId());
    }

    // ---- alarm_notification_channel.updated_at ----

    @Test
    void channelInsertWritesUtc() {
        AlarmNotificationChannel channel = insertChannel();
        assertUtc("alarm_notification_channel", "updated_at", channel.getId());
    }

    @Test
    void channelUpdateWritesUtc() {
        AlarmNotificationChannel channel = insertChannel();
        channel.setChannelName("renamed");
        alarmNotificationChannelDao.update(channel);
        assertUtc("alarm_notification_channel", "updated_at", channel.getId());
    }

    // ---- alarm_history_v2.created_at ----

    @Test
    void historyInsertWritesUtc() {
        AlarmHistoryV2 history = insertHistory(insertRule().getId());
        assertUtc("alarm_history_v2", "created_at", history.getId());
    }

    /**
     * The tests above assert on the statements they call, so a new write arrives with nothing
     * asserting on it. Every write the module ships is counted, not only the five mappers that own
     * these tables -- telling which statements write them would mean reading the sql. Of the
     * fourteen against these tables, AlarmHistoryV2Mapper.updateContext is the one with no test
     * above: alarm_history_v2.created_at carries no ON UPDATE, so an update never touches it.
     */
    @Test
    void everyWriteIsAccountedFor() {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        // By name, not over getMappedStatements(): a bare name two mappers share holds an
        // ambiguity marker, and iterating that collection throws before any filter runs.
        List<String> writes = List.copyOf(configuration.getMappedStatementNames()).stream()
                .filter(id -> id.indexOf('.') >= 0)
                .filter(id -> {
                    SqlCommandType command = configuration.getMappedStatement(id).getSqlCommandType();
                    return command == SqlCommandType.INSERT || command == SqlCommandType.UPDATE;
                })
                .distinct()
                .sorted()
                .toList();

        assertTrue(writes.size() == 23,
                "a write appeared or went away -- " + writes.size() + " now. If it writes one of"
                        + " alarm_template, alarm_template_item, alarm_rule_v2,"
                        + " alarm_notification_channel or alarm_history_v2, give it a test above"
                        + " before moving this number: " + writes);
    }

    /**
     * The ON UPDATE count holds the one exclusion above in place: give alarm_history_v2.created_at
     * one and updateContext starts writing the server clock, with nothing else here moving.
     */
    @Test
    void everyServerFilledColumnIsKnown() {
        List<String> tables = List.of("alarm_template", "alarm_template_item", "alarm_rule_v2",
                "alarm_notification_channel", "alarm_history_v2");
        Integer onUpdate = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns"
                        + " WHERE table_schema = 'pinpoint' AND table_name IN ("
                        + "'alarm_template','alarm_template_item','alarm_rule_v2',"
                        + "'alarm_notification_channel','alarm_history_v2')"
                        + " AND extra LIKE '%on update%'",
                Integer.class);
        assertTrue(onUpdate != null && onUpdate == 4,
                "the columns the server refills on update changed: " + onUpdate + " of them now,"
                        + " so AlarmHistoryV2Mapper.updateContext may no longer be safe to skip");

        int declared = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns"
                        + " WHERE table_schema = 'pinpoint' AND table_name IN ("
                        + "'alarm_template','alarm_template_item','alarm_rule_v2',"
                        + "'alarm_notification_channel','alarm_history_v2')"
                        + " AND column_default = 'CURRENT_TIMESTAMP'",
                Integer.class);
        assertTrue(declared == tables.size(),
                "a table gained or lost a server-filled timestamp column: " + declared
                        + " of them now, so the writes that touch it need covering here too");
    }

    // ---- assertions ----

    private void assertUtc(String table, String column, Long id) {
        LocalDateTime written = jdbc.queryForObject(
                "SELECT " + column + " FROM pinpoint." + table + " WHERE id = ?",
                LocalDateTime.class, id);
        assertNotNull(written, table + "." + column + " was not written at all");

        Duration drift = Duration.between(written, LocalDateTime.now(ZoneOffset.UTC)).abs();
        assertTrue(drift.compareTo(TOLERANCE) < 0,
                table + "." + column + " reads " + written + ", " + drift.toMinutes()
                        + " minutes from utc -- the statement left it to the database server's"
                        + " clock instead of assigning it from UTC_TIMESTAMP()");
    }

    // ---- fixtures ----

    private AlarmTemplate insertTemplate() {
        AlarmTemplate template = new AlarmTemplate();
        template.setServiceName("tz-service");
        template.setName("tz-template");
        alarmTemplateDao.insert(template);
        return template;
    }

    private AlarmTemplateItem insertItem(Long templateId) {
        AlarmTemplateItem item = new AlarmTemplateItem();
        item.setTemplateId(templateId);
        item.setName("tz-item");
        item.setSeverity(AlarmSeverity.WARNING);
        item.setDataSource(IntegrationTestAlarmDataSource.PRIMARY.name());
        item.setCheckIntervalSec(0);
        item.setActionIntervalSec(3600);
        item.setConditions(condition());
        item.setFilters(List.of());
        alarmTemplateItemDao.insert(item);
        return item;
    }

    private AlarmRuleV2 insertRule() {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setName("tz-rule");
        rule.setSeverity(AlarmSeverity.CRITICAL);
        rule.setDataSource(IntegrationTestAlarmDataSource.PRIMARY.name());
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setServiceName("tz-service");
        rule.setApplicationName("tz-app");
        rule.setCheckIntervalSec(0);
        rule.setActionIntervalSec(3600);
        rule.setEnabled(true);
        rule.setConditions(condition());
        rule.setFilters(List.of());
        alarmRuleV2Dao.insertRule(rule);
        return rule;
    }

    private AlarmNotificationChannel insertChannel() {
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setServiceName("tz-service");
        channel.setChannelName("tz-channel");
        channel.setMethodType(AlarmMethodType.WEBHOOK);
        channel.setDestination("https://example.com/tz");
        channel.setConfig("{}");
        alarmNotificationChannelDao.insert(channel);
        return channel;
    }

    private AlarmHistoryV2 insertHistory(Long ruleId) {
        AlarmHistoryV2 history = new AlarmHistoryV2();
        history.setRuleId(ruleId);
        history.setEventType(AlarmEventType.FIRED);
        history.setMessage("fired");
        history.setContext("{}");
        alarmHistoryV2Dao.insert(history);
        return history;
    }

    private AlarmCondition condition() {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("error_count");
        leaf.setOp(AlarmCondition.ComparisonOp.GTE);
        leaf.setThreshold(1.0);
        leaf.setWindowSec(300);
        leaf.setAggregation(AlarmCondition.Aggregation.COUNT);
        return leaf;
    }
}
