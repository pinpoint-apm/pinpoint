/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.alarm.batch.config;

import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ContextConfiguration(classes = AlarmJobIntegrationTestConfig.class)
@TestPropertySource(locations = "classpath:application-alarm-test.yml")
class AlarmHistoryCleanupIntegrationTest {

    @Autowired
    private AlarmHistoryV2Dao alarmHistoryV2Dao;

    @Autowired
    private AlarmNotificationOutboxDao alarmNotificationOutboxDao;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DELETE FROM pinpoint.alarm_notification_outbox");
        jdbc.execute("DELETE FROM pinpoint.alarm_history_v2");
    }

    @Test
    void deletesExpiredHistoryOnlyAfterItsOutboxIsTerminal() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime threshold = now.minusDays(14);
        long sentHistory = insertHistory(now.minusDays(20));
        long pendingHistory = insertHistory(now.minusDays(20));
        long deadHistory = insertHistory(now.minusDays(20));
        long bareHistory = insertHistory(now.minusDays(20));
        long recentHistory = insertHistory(now.minusDays(1));
        insertOutbox(sentHistory, "SENT");
        insertOutbox(pendingHistory, "PENDING");
        insertOutbox(deadHistory, "DEAD");
        insertOutbox(recentHistory, "DEAD");

        assertThat(alarmNotificationOutboxDao.deleteTerminalOlderThan(threshold, 100)).isEqualTo(2);
        assertThat(alarmHistoryV2Dao.deleteOlderThan(threshold, 100)).isEqualTo(3);

        assertThat(jdbc.queryForList("SELECT id FROM pinpoint.alarm_history_v2 ORDER BY id", Long.class))
                .containsExactly(pendingHistory, recentHistory);
        assertThat(jdbc.queryForList("SELECT status FROM pinpoint.alarm_notification_outbox ORDER BY id", String.class))
                .containsExactly("PENDING", "DEAD");
    }

    @Test
    void deletesNoMoreThanTheLimit() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        insertHistory(now.minusDays(20));
        insertHistory(now.minusDays(20));

        assertThat(alarmHistoryV2Dao.deleteOlderThan(now.minusDays(14), 1)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM pinpoint.alarm_history_v2", Long.class)).isEqualTo(1L);
    }

    private long insertHistory(LocalDateTime createdAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO pinpoint.alarm_history_v2 (rule_id, event_type, created_at) VALUES (1, 'FIRING', ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1, createdAt);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void insertOutbox(long historyId, String status) {
        jdbc.update("INSERT INTO pinpoint.alarm_notification_outbox (history_id, channel_id, method_type, payload, status) "
                + "VALUES (?, 1, 'WEBHOOK', '{}', ?)", historyId, status);
    }
}
