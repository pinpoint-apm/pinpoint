/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.flink.process;

import com.navercorp.pinpoint.common.hbase.HbaseTableConstatns;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.mapper.AgentInfoMapper;
import com.navercorp.pinpoint.web.vo.AgentInfo;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.util.Objects;

import static com.navercorp.pinpoint.common.hbase.HbaseColumnFamily.AGENTINFO_INFO;

/**
 * @author minwoo.jung
 */
public class ApplicationCache {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String SPEL_KEY = "#application.getAgentId() + '.' + #application.getAgentStartTime()";
    public static final String NOT_FOUND_APP_ID = "notFoundId";

    private final transient AgentInfoMapper agentInfoMapper = new AgentInfoMapper();

    private final transient HbaseTemplate2 hbaseTemplate2;

    private final transient TableNameProvider tableNameProvider;

    public ApplicationCache(HbaseTemplate2 hbaseTemplate2, TableNameProvider tableNameProvider) {
        this.hbaseTemplate2 = Objects.requireNonNull(hbaseTemplate2, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Cacheable(value="applicationId", key=SPEL_KEY)
    public String findApplicationId(ApplicationKey application) {
        final String agentId = application.getAgentId();
        final long agentStartTimestamp = application.getAgentStartTime();
        final byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(Bytes.toBytes(agentId), HbaseTableConstatns.AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(agentStartTimestamp));

        Get get = new Get(rowKey);

        get.addColumn(AGENTINFO_INFO.getName(), AGENTINFO_INFO.QUALIFIER_IDENTIFIER);
        AgentInfo agentInfo = null;
        try {
            TableName tableName = tableNameProvider.getTableName(AGENTINFO_INFO.getTable());
            agentInfo = hbaseTemplate2.get(tableName, get, agentInfoMapper);
        } catch (Exception e) {
            logger.error("can't found application id({})", agentId, e);
        }

        return getApplicationId(agentInfo, agentId);
    }

    private String getApplicationId(AgentInfo agentInfo, String agentId) {
        if (agentInfo == null) {
            logger.warn("can't found application id : {}", agentId);
            return NOT_FOUND_APP_ID;
        }
        return agentInfo.getApplicationName();
    }

    public static class ApplicationKey {
        private final String agentId;
        private final long agentStartTime;

        public ApplicationKey(String agentId, long agentStartTime) {
            this.agentId = agentId;
            this.agentStartTime = agentStartTime;
        }

        public String getAgentId() {
            return agentId;
        }

        public long getAgentStartTime() {
            return agentStartTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ApplicationKey that = (ApplicationKey) o;

            if (agentStartTime != that.agentStartTime) return false;
            return agentId != null ? agentId.equals(that.agentId) : that.agentId == null;

        }

        @Override
        public int hashCode() {
            int result = agentId != null ? agentId.hashCode() : 0;
            result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "ApplicationKey{" +
                "agentId='" + agentId + '\'' +
                ", agentStartTime=" + agentStartTime +
                '}';
        }
    }
}
