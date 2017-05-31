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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import java.util.*;

/**
 * @author minwoo.jung
 */
public class JoinApplicationStatBo implements JoinStatBo {
    private static final long SHIFT_RANGE = 1000 * 5;
    private String applicationId;
    private List<JoinCpuLoadBo> joinCpuLoadBoList;
    private long timestamp;
    private StatType statType = StatType.APP_CPU_LOAD;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timeStamp) {
        this.timestamp = timeStamp;
    }

    public String getId() {
        return applicationId;
    }

    public void setId(String applicationId) {
        this.applicationId = applicationId;
    }

    public static JoinApplicationStatBo joinApplicationStatBoByTimeSlice(final List<JoinApplicationStatBo> joinApplicaitonStatBoList) {
        JoinApplicationStatBo newJoinApplicationStatBo = new JoinApplicationStatBo();

        if (joinApplicaitonStatBoList.size() == 0) {
            return newJoinApplicationStatBo;
        }

        Map<Long, List<JoinCpuLoadBo>> joinCpuLoadBoMap = new HashMap<Long, List<JoinCpuLoadBo>>();
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicaitonStatBoList) {
            for (JoinCpuLoadBo joinCpuLoadBo : joinApplicationStatBo.getJoinCpuLoadBoList()) {
                long shiftTimestamp = shiftTimestamp(joinCpuLoadBo.getTimestamp());
                List<JoinCpuLoadBo> joinCpuLoadBoList = joinCpuLoadBoMap.get(shiftTimestamp);
                if (joinCpuLoadBoList != null) {
                    joinCpuLoadBoList.add(joinCpuLoadBo);
                } else {
                    ArrayList<JoinCpuLoadBo> newJoinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
                    newJoinCpuLoadBoList.add(joinCpuLoadBo);
                    joinCpuLoadBoMap.put(shiftTimestamp, newJoinCpuLoadBoList);
                }
            }
        }

        List<JoinCpuLoadBo> newJoinAgentStatBoList = new ArrayList<JoinCpuLoadBo>();
        long minTimestamp = Long.MAX_VALUE;
        for (Map.Entry<Long, List<JoinCpuLoadBo>> entry : joinCpuLoadBoMap.entrySet()) {
            List<JoinCpuLoadBo> joinCpuLoadBoList = entry.getValue();
            JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, entry.getKey());
            joinCpuLoadBo.setTimestamp(entry.getKey());
            newJoinAgentStatBoList.add(joinCpuLoadBo);
            if (entry.getKey() < minTimestamp) {
                minTimestamp = entry.getKey();
            }
        }

        newJoinApplicationStatBo.setId(joinApplicaitonStatBoList.get(0).getId());
        newJoinApplicationStatBo.setTimestamp(minTimestamp);
        newJoinApplicationStatBo.setJoinCpuLoadBoList(newJoinAgentStatBoList);

        return newJoinApplicationStatBo;

    }

    private static long shiftTimestamp(long timestamp) {
        return timestamp - (timestamp % SHIFT_RANGE);
    }

    public List<JoinCpuLoadBo> getJoinCpuLoadBoList() {
        return joinCpuLoadBoList;
    }

    public void setJoinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList) {
        this.joinCpuLoadBoList = joinCpuLoadBoList;
    }

    public StatType getStatType() {
        return statType;
    }

    public void setStatType(StatType statType) {
        this.statType = statType;
    }

    public static JoinApplicationStatBo joinApplicationStatBo(List<JoinApplicationStatBo> joinApplicaitonStatBoList) {
        JoinApplicationStatBo newJoinApplicationStatBo = new JoinApplicationStatBo();

        if (joinApplicaitonStatBoList.size() == 0) {
            return newJoinApplicationStatBo;
        }

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicaitonStatBoList) {
            joinCpuLoadBoList.addAll(joinApplicationStatBo.getJoinCpuLoadBoList());
        }
        Long timestamp = joinCpuLoadBoList.get(0).getTimestamp();
        JoinCpuLoadBo newJoinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, timestamp);
        List<JoinCpuLoadBo> newJoinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        newJoinCpuLoadBoList.add(newJoinCpuLoadBo);

        newJoinApplicationStatBo.setId(joinApplicaitonStatBoList.get(0).getId());
        newJoinApplicationStatBo.setTimestamp(timestamp);
        newJoinApplicationStatBo.setJoinCpuLoadBoList(newJoinCpuLoadBoList);
        newJoinApplicationStatBo.setStatType(StatType.APP_CPU_LOAD_AGGRE);

        return newJoinApplicationStatBo;
    }

    @Override
    public String toString() {
        return "JoinApplicationStatBo{" +
            "applicationId='" + applicationId + '\'' +
            ", joinCpuLoadBoList=" + joinCpuLoadBoList +
            ", timestamp=" + new Date(timestamp) +
            ", statType=" + statType +
            '}';
    }
}
