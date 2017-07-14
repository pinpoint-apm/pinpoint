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

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.mapper.AgentInfoMapper;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author minwoo.jung
 */
public class ApplicationCache {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String SPEL_KEY = "#application.getAgentId() + '.' + #application.getAgentStartTime()";
    public final static String NOT_FOOUND_APP_ID = "notFoundId";
    private final static AgentInfoMapper agentInfoMapper = new AgentInfoMapper();

    private static HbaseTemplate2 hbaseTemplate2;

    public void setHbaseTemplate2(HbaseTemplate2 hbaseTemplate2) {
        ApplicationCache.hbaseTemplate2 = hbaseTemplate2;
    }

    @Cacheable(value="applicationId", key=SPEL_KEY)
    public String findApplicationId(ApplicationKey application) {
        final String agentId = application.getAgentId();
        final long agentStartTimestamp = application.getAgentStartTime();
        final byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(Bytes.toBytes(agentId), HBaseTables.AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(agentStartTimestamp));

        Get get = new Get(rowKey);
        get.addColumn(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER);
        AgentInfo agentInfo = null;
        try {
            agentInfo = hbaseTemplate2.get(HBaseTables.AGENTINFO, get, agentInfoMapper);
        } catch (Exception e) {
            logger.error("can't found application id({})", agentId, e);
        }
        String applicationId = NOT_FOOUND_APP_ID;

        if (agentInfo != null) {
            applicationId = agentInfo.getApplicationName();
        } else {
            logger.warn("can't found application id : {}", agentId);
        }

        return applicationId;
    }

    public static class ApplicationKey {
        public String getAgentId() {
            return agentId;
        }

        private String agentId;

        public long getAgentStartTime() {
            return agentStartTime;
        }

        private long agentStartTime;

        public ApplicationKey(String agentId, long agentStartTime) {
            this.agentId = agentId;
            this.agentStartTime = agentStartTime;
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
