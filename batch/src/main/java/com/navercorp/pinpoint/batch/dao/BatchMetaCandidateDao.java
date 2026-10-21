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

package com.navercorp.pinpoint.batch.dao;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Reads the Spring Batch job executions of the meta data source in primary key order, for
 * {@code batchMetaCleanupJob} to pick the instances to delete.
 * <p>
 * {@code JobExplorer.getJobInstances(name, start, count)} is not usable for this: it loads every instance
 * of the job ordered by id descending and skips {@code start} rows on the client. The standard schema has
 * no index on {@code CREATE_TIME} either, so the candidates are read as ranges of {@code JOB_EXECUTION_ID},
 * which grows with time: the oldest executions come first and the scan stops at the first page that is
 * younger than the retention.
 */
public class BatchMetaCandidateDao {

    public static final String DEFAULT_TABLE_PREFIX = "BATCH_";

    /**
     * One row of {@code BATCH_JOB_EXECUTION} joined with its instance.
     *
     * @param executionCount executions of the instance in total; 1 for every pinpoint job (never restarted), so the
     *                       row alone decides the instance and the repository need not be asked
     */
    public record JobExecutionRow(long executionId, long instanceId, String jobName, LocalDateTime createTime, String status,
                                  int executionCount) {
    }

    private final JdbcTemplate jdbcTemplate;
    private final String selectPage;
    private final String lockInstance;

    public BatchMetaCandidateDao(DataSource metaDataSource) {
        this(metaDataSource, DEFAULT_TABLE_PREFIX);
    }

    public BatchMetaCandidateDao(DataSource metaDataSource, String tablePrefix) {
        this.jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(metaDataSource, "metaDataSource"));
        Objects.requireNonNull(tablePrefix, "tablePrefix");
        // the correlated count uses the FK index on JOB_INSTANCE_ID: one extra index probe per row, no extra round trip
        this.selectPage = "SELECT e.JOB_EXECUTION_ID, e.JOB_INSTANCE_ID, i.JOB_NAME, e.CREATE_TIME, e.STATUS,"
                + " (SELECT COUNT(*) FROM " + tablePrefix + "JOB_EXECUTION x WHERE x.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID) AS EXECUTION_COUNT"
                + " FROM " + tablePrefix + "JOB_EXECUTION e"
                + " JOIN " + tablePrefix + "JOB_INSTANCE i ON i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID"
                + " WHERE e.JOB_EXECUTION_ID > ?"
                + " ORDER BY e.JOB_EXECUTION_ID"
                + " LIMIT ?";
        this.lockInstance = "SELECT JOB_INSTANCE_ID FROM " + tablePrefix + "JOB_INSTANCE WHERE JOB_INSTANCE_ID = ? FOR UPDATE";
    }

    /**
     * Locks the instance row until the current transaction ends, so that no execution can be inserted for
     * it meanwhile (an insert takes a shared lock on the parent row for the foreign key).
     *
     * @return {@code false} when the instance is gone
     */
    public boolean lockInstance(long instanceId) {
        return !jdbcTemplate.queryForList(lockInstance, Long.class, instanceId).isEmpty();
    }

    /**
     * @return up to {@code limit} executions with an id greater than {@code afterExecutionId}, oldest first
     */
    public List<JobExecutionRow> nextPage(long afterExecutionId, int limit) {
        return jdbcTemplate.query(selectPage, (rs, rowNum) -> {
            final Timestamp createTime = rs.getTimestamp("CREATE_TIME");
            return new JobExecutionRow(
                    rs.getLong("JOB_EXECUTION_ID"),
                    rs.getLong("JOB_INSTANCE_ID"),
                    rs.getString("JOB_NAME"),
                    createTime == null ? null : createTime.toLocalDateTime(),
                    rs.getString("STATUS"),
                    rs.getInt("EXECUTION_COUNT"));
        }, afterExecutionId, limit);
    }
}
