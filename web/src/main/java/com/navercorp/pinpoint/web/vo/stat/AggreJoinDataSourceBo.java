/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;


/**
 * @author minwoo.jung
 */
public class AggreJoinDataSourceBo extends JoinDataSourceBo implements AggregationStatData {

    private long timestamp;


    public AggreJoinDataSourceBo(short serviceTypeCode, String url, int avgActiveConnectionSize, int minActiveConnectionSize, String minActiveConnectionAgentId, int maxActiveConnectionSize, String maxActiveConnectionAgentId ,long timestamp) {
        super(serviceTypeCode, url, avgActiveConnectionSize, minActiveConnectionSize, minActiveConnectionAgentId, maxActiveConnectionSize, maxActiveConnectionAgentId);
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggreJoinDataSourceBo)) return false;
        if (!super.equals(o)) return false;

        AggreJoinDataSourceBo that = (AggreJoinDataSourceBo) o;

        return timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
