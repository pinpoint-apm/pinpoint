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
    public static final JoinApplicationStatBo EMPTY_JOIN_APPLICATION_STAT_BO = new JoinApplicationStatBo();

    private static final long SHIFT_RANGE = 1000 * 5;

    private String applicationId = UNKNOWN_ID;
    private List<JoinCpuLoadBo> joinCpuLoadBoList = Collections.emptyList();
    private List<JoinMemoryBo> joinMemoryBoList = Collections.emptyList();
    private List<JoinTransactionBo> joinTransactionBoList = Collections.emptyList();
    private List<JoinActiveTraceBo> joinActiveTraceBoList = Collections.emptyList();
    private List<JoinResponseTimeBo> joinResponseTimeBoList = Collections.emptyList();
    private List<JoinDataSourceListBo> joinDataSourceListBoList = Collections.emptyList();
    private List<JoinFileDescriptorBo> joinFileDescriptorBoList = Collections.emptyList();
    private List<JoinDirectBufferBo> joinDirectBufferBoList = Collections.emptyList();

    private long timestamp = Long.MIN_VALUE;
    private StatType statType = StatType.APP_STST;

    protected JoinApplicationStatBo(JoinApplicationStatBo joinApplicationStatBo) {
        if (joinApplicationStatBo == null) {
            throw new IllegalArgumentException("joinApplicationStatBo cannot be null");
        }

        this.applicationId = joinApplicationStatBo.getId();
        this.joinCpuLoadBoList = joinApplicationStatBo.getJoinCpuLoadBoList();
        this.joinMemoryBoList = joinApplicationStatBo.getJoinMemoryBoList();
        this.joinTransactionBoList = joinApplicationStatBo.getJoinTransactionBoList();
        this.joinActiveTraceBoList = joinApplicationStatBo.getJoinActiveTraceBoList();
        this.joinResponseTimeBoList = joinApplicationStatBo.getJoinResponseTimeBoList();
        this.joinDataSourceListBoList = joinApplicationStatBo.getJoinDataSourceListBoList();
        this.joinFileDescriptorBoList = joinApplicationStatBo.getJoinFileDescriptorBoList();
        this.joinDirectBufferBoList = joinApplicationStatBo.getJoinDirectBufferBoList();

        this.timestamp= joinApplicationStatBo.getTimestamp();
        this.statType = joinApplicationStatBo.getStatType();
    }

    public JoinApplicationStatBo() {
    }

    public static JoinApplicationStatBo joinApplicationStatBoByTimeSlice(final List<JoinApplicationStatBo> joinApplicationStatBoList) {
        if (joinApplicationStatBoList.isEmpty()) {
            return EMPTY_JOIN_APPLICATION_STAT_BO;
        }

        JoinApplicationStatBo newJoinApplicationStatBo = new JoinApplicationStatBo();
        newJoinApplicationStatBo.setId(joinApplicationStatBoList.get(0).getId());
        newJoinApplicationStatBo.setJoinCpuLoadBoList(joinCpuLoadBoByTimeSlice(joinApplicationStatBoList));
        newJoinApplicationStatBo.setJoinMemoryBoList(joinMemoryBoByTimeSlice(joinApplicationStatBoList));
        newJoinApplicationStatBo.setJoinTransactionBoList(joinTransactionBoByTimeSlice(joinApplicationStatBoList));
        newJoinApplicationStatBo.setJoinActiveTraceBoList(joinActiveTraceBoByTimeSlice(joinApplicationStatBoList));
        newJoinApplicationStatBo.setJoinResponseTimeBoList(joinResponseTimeBoByTimeSlice(joinApplicationStatBoList));
        newJoinApplicationStatBo.setJoinDataSourceListBoList(JoinDataSourceListBoBytTimeSlice(joinApplicationStatBoList));
        newJoinApplicationStatBo.setJoinFileDescriptorBoList(joinFileDescriptorBoByTimeSlice(joinApplicationStatBoList));
        newJoinApplicationStatBo.setJoinDirectBufferBoList(joinDirectBufferBoByTimeSlice(joinApplicationStatBoList));
        newJoinApplicationStatBo.setTimestamp(extractMinTimestamp(newJoinApplicationStatBo));
        return newJoinApplicationStatBo;
    }

    private static long extractMinTimestamp(JoinApplicationStatBo joinApplicationStatBo) {
        long minTimestamp = Long.MAX_VALUE;

        for (JoinCpuLoadBo joinCpuLoadBo : joinApplicationStatBo.getJoinCpuLoadBoList()) {
            if (joinCpuLoadBo.getTimestamp() < minTimestamp) {
                minTimestamp = joinCpuLoadBo.getTimestamp();
            }
        }

        for (JoinMemoryBo joinMemoryBo : joinApplicationStatBo.getJoinMemoryBoList()) {
            if (joinMemoryBo.getTimestamp() < minTimestamp) {
                minTimestamp = joinMemoryBo.getTimestamp();
            }
        }

        for (JoinTransactionBo joinTransactionBo : joinApplicationStatBo.getJoinTransactionBoList()) {
            if (joinTransactionBo.getTimestamp() < minTimestamp) {
                minTimestamp = joinTransactionBo.getTimestamp();
            }
        }

        for (JoinActiveTraceBo joinActiveTraceBo : joinApplicationStatBo.getJoinActiveTraceBoList()) {
            if (joinActiveTraceBo.getTimestamp() < minTimestamp) {
                minTimestamp = joinActiveTraceBo.getTimestamp();
            }
        }

        for (JoinResponseTimeBo joinResponseTimeBo : joinApplicationStatBo.getJoinResponseTimeBoList()) {
            if (joinResponseTimeBo.getTimestamp() < minTimestamp) {
                minTimestamp = joinResponseTimeBo.getTimestamp();
            }
        }

        for (JoinDataSourceListBo joinDataSourceListBo : joinApplicationStatBo.getJoinDataSourceListBoList()) {
            if (joinDataSourceListBo.getTimestamp() < minTimestamp) {
                minTimestamp = joinDataSourceListBo.getTimestamp();
            }
        }

        for (JoinFileDescriptorBo joinFileDescriptorBo : joinApplicationStatBo.getJoinFileDescriptorBoList()) {
            if (joinFileDescriptorBo.getTimestamp() < minTimestamp) {
                minTimestamp = joinFileDescriptorBo.getTimestamp();
            }
        }

        for (JoinDirectBufferBo joinDirectBufferBo : joinApplicationStatBo.getJoinDirectBufferBoList()) {
            if (joinDirectBufferBo.getTimestamp() < minTimestamp) {
                minTimestamp = joinDirectBufferBo.getTimestamp();
            }
        }

        return minTimestamp;
    }

    private static List<JoinResponseTimeBo> joinResponseTimeBoByTimeSlice(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        Map<Long, List<JoinResponseTimeBo>> joinResponseTimeBoMap = new HashMap<Long, List<JoinResponseTimeBo>>();

        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            for (JoinResponseTimeBo joinResponseTimeBo : joinApplicationStatBo.getJoinResponseTimeBoList()) {
                long shiftTimestamp = shiftTimestamp(joinResponseTimeBo.getTimestamp());
                List<JoinResponseTimeBo> joinResponseTimeBoList = joinResponseTimeBoMap.get(shiftTimestamp);

                if (joinResponseTimeBoList == null) {
                    joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
                    joinResponseTimeBoMap.put(shiftTimestamp, joinResponseTimeBoList);
                }

                joinResponseTimeBoList.add(joinResponseTimeBo);
            }
        }

        List<JoinResponseTimeBo> newJoinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();

        for (Map.Entry<Long, List<JoinResponseTimeBo>> entry : joinResponseTimeBoMap.entrySet()) {
            List<JoinResponseTimeBo> joinResponseTimeBoList = entry.getValue();
            JoinResponseTimeBo joinResponseTimeBo = JoinResponseTimeBo.joinResponseTimeBoList(joinResponseTimeBoList, entry.getKey());
            newJoinResponseTimeBoList.add(joinResponseTimeBo);
        }

        return newJoinResponseTimeBoList;
    }

    private static List<JoinDataSourceListBo> JoinDataSourceListBoBytTimeSlice(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        Map<Long, List<JoinDataSourceListBo>> joinDataSourceListBoMap = new HashMap<Long, List<JoinDataSourceListBo>>();

        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            for (JoinDataSourceListBo joinDataSourceListBo : joinApplicationStatBo.getJoinDataSourceListBoList()) {
                long shiftTimestamp = shiftTimestamp(joinDataSourceListBo.getTimestamp());
                List<JoinDataSourceListBo> joinDataSourceListBoList = joinDataSourceListBoMap.get(shiftTimestamp);

                if (joinDataSourceListBoList == null) {
                    joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();
                    joinDataSourceListBoMap.put(shiftTimestamp, joinDataSourceListBoList);
                }

                joinDataSourceListBoList.add(joinDataSourceListBo);
            }
        }

        List<JoinDataSourceListBo> newJoinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();

        for (Map.Entry<Long, List<JoinDataSourceListBo>> entry : joinDataSourceListBoMap.entrySet()) {
            List<JoinDataSourceListBo> joinDataSourceListBoList = entry.getValue();
            JoinDataSourceListBo joinDataSourceListBo = JoinDataSourceListBo.joinDataSourceListBoList(joinDataSourceListBoList, entry.getKey());
            newJoinDataSourceListBoList.add(joinDataSourceListBo);
        }

        return newJoinDataSourceListBoList;
    }

    private static List<JoinActiveTraceBo> joinActiveTraceBoByTimeSlice(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        Map<Long, List<JoinActiveTraceBo>> joinActiveTraceBoMap = new HashMap<Long, List<JoinActiveTraceBo>>();

        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            for (JoinActiveTraceBo joinActiveTraceBo : joinApplicationStatBo.getJoinActiveTraceBoList()) {
                long shiftTimestamp = shiftTimestamp(joinActiveTraceBo.getTimestamp());
                List<JoinActiveTraceBo> joinActiveTraceBoList = joinActiveTraceBoMap.get(shiftTimestamp);

                if (joinActiveTraceBoList == null) {
                    joinActiveTraceBoList = new ArrayList<JoinActiveTraceBo>();
                    joinActiveTraceBoMap.put(shiftTimestamp, joinActiveTraceBoList);
                }

                joinActiveTraceBoList.add(joinActiveTraceBo);
            }
        }

        List<JoinActiveTraceBo> newJoinActiveTraceBoList = new ArrayList<JoinActiveTraceBo>();

        for (Map.Entry<Long, List<JoinActiveTraceBo>> entry : joinActiveTraceBoMap.entrySet()) {
            List<JoinActiveTraceBo> joinActiveTraceBoList = entry.getValue();
            JoinActiveTraceBo joinActiveTraceBo = JoinActiveTraceBo.joinActiveTraceBoList(joinActiveTraceBoList, entry.getKey());
            newJoinActiveTraceBoList.add(joinActiveTraceBo);
        }

        return newJoinActiveTraceBoList;
    }


    private static List<JoinTransactionBo> joinTransactionBoByTimeSlice(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        Map<Long, List<JoinTransactionBo>> joinTransactionBoMap = new HashMap<Long, List<JoinTransactionBo>>();

        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            for (JoinTransactionBo joinTransactionBo : joinApplicationStatBo.getJoinTransactionBoList()) {
                long shiftTimestamp = shiftTimestamp(joinTransactionBo.getTimestamp());
                List<JoinTransactionBo> joinTransactionBoList = joinTransactionBoMap.get(shiftTimestamp);

                if (joinTransactionBoList == null) {
                    joinTransactionBoList = new ArrayList<JoinTransactionBo>();
                    joinTransactionBoMap.put(shiftTimestamp, joinTransactionBoList);
                }

                joinTransactionBoList.add(joinTransactionBo);
            }
        }

        List<JoinTransactionBo> newJoinTransactionBoList = new ArrayList<JoinTransactionBo>();

        for (Map.Entry<Long, List<JoinTransactionBo>> entry  : joinTransactionBoMap.entrySet()) {
            List<JoinTransactionBo> joinTransactionBoList = entry.getValue();
            JoinTransactionBo joinTransactionBo = JoinTransactionBo.joinTransactionBoLIst(joinTransactionBoList, entry.getKey());
            newJoinTransactionBoList.add(joinTransactionBo);
        }

        return newJoinTransactionBoList;
    }

    private static List<JoinMemoryBo> joinMemoryBoByTimeSlice(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        Map<Long, List<JoinMemoryBo>> joinMemoryBoMap = new HashMap<Long, List<JoinMemoryBo>>();

        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            for (JoinMemoryBo joinMemoryBo : joinApplicationStatBo.getJoinMemoryBoList()) {
                long shiftTimestamp = shiftTimestamp(joinMemoryBo.getTimestamp());
                List<JoinMemoryBo> joinMemoryBoList = joinMemoryBoMap.get(shiftTimestamp);

                if (joinMemoryBoList == null) {
                    joinMemoryBoList = new ArrayList<JoinMemoryBo>();
                    joinMemoryBoMap.put(shiftTimestamp, joinMemoryBoList);
                }

                joinMemoryBoList.add(joinMemoryBo);
            }
        }

        List<JoinMemoryBo> newJoinMemoryBoList = new ArrayList<JoinMemoryBo>();

        for (Map.Entry<Long, List<JoinMemoryBo>> entry : joinMemoryBoMap.entrySet()) {
            List<JoinMemoryBo> joinMemoryBoList = entry.getValue();
            JoinMemoryBo joinMemoryBo = JoinMemoryBo.joinMemoryBoList(joinMemoryBoList, entry.getKey());
            newJoinMemoryBoList.add(joinMemoryBo);
        }

        return newJoinMemoryBoList;
    }


    private static List<JoinCpuLoadBo> joinCpuLoadBoByTimeSlice(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        Map<Long, List<JoinCpuLoadBo>> joinCpuLoadBoMap = new HashMap<Long, List<JoinCpuLoadBo>>();

        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            for (JoinCpuLoadBo joinCpuLoadBo : joinApplicationStatBo.getJoinCpuLoadBoList()) {
                long shiftTimestamp = shiftTimestamp(joinCpuLoadBo.getTimestamp());
                List<JoinCpuLoadBo> joinCpuLoadBoList = joinCpuLoadBoMap.get(shiftTimestamp);

                if (joinCpuLoadBoList == null) {
                    joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
                    joinCpuLoadBoMap.put(shiftTimestamp, joinCpuLoadBoList);
                }

                joinCpuLoadBoList.add(joinCpuLoadBo);
            }
        }

        List<JoinCpuLoadBo> newJoinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();

        for (Map.Entry<Long, List<JoinCpuLoadBo>> entry : joinCpuLoadBoMap.entrySet()) {
            List<JoinCpuLoadBo> joinCpuLoadBoList = entry.getValue();
            JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, entry.getKey());
            newJoinCpuLoadBoList.add(joinCpuLoadBo);
        }
        return newJoinCpuLoadBoList;
    }

    private static List<JoinFileDescriptorBo> joinFileDescriptorBoByTimeSlice(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        Map<Long, List<JoinFileDescriptorBo>> joinFileDescriptorBoMap = new HashMap<Long, List<JoinFileDescriptorBo>>();

        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            for (JoinFileDescriptorBo joinFileDescriptorBo : joinApplicationStatBo.getJoinFileDescriptorBoList()) {
                long shiftTimestamp = shiftTimestamp(joinFileDescriptorBo.getTimestamp());
                List<JoinFileDescriptorBo> joinFileDescriptorBoList = joinFileDescriptorBoMap.get(shiftTimestamp);

                if (joinFileDescriptorBoList == null) {
                    joinFileDescriptorBoList = new ArrayList<JoinFileDescriptorBo>();
                    joinFileDescriptorBoMap.put(shiftTimestamp, joinFileDescriptorBoList);
                }

                joinFileDescriptorBoList.add(joinFileDescriptorBo);
            }
        }

        List<JoinFileDescriptorBo> newJoinFileDescriptorBoList = new ArrayList<JoinFileDescriptorBo>();

        for (Map.Entry<Long, List<JoinFileDescriptorBo>> entry : joinFileDescriptorBoMap.entrySet()) {
            List<JoinFileDescriptorBo> joinFileDescriptorBoList = entry.getValue();
            JoinFileDescriptorBo joinFileDescriptorBo = JoinFileDescriptorBo.joinFileDescriptorBoList(joinFileDescriptorBoList, entry.getKey());
            newJoinFileDescriptorBoList.add(joinFileDescriptorBo);
        }
        return newJoinFileDescriptorBoList;
    }

    private static List<JoinDirectBufferBo> joinDirectBufferBoByTimeSlice(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        Map<Long, List<JoinDirectBufferBo>> joinDirectBufferBoMap = new HashMap<Long, List<JoinDirectBufferBo>>();

        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            for (JoinDirectBufferBo joinDirectBufferBo : joinApplicationStatBo.getJoinDirectBufferBoList()) {
                long shiftTimestamp = shiftTimestamp(joinDirectBufferBo.getTimestamp());
                List<JoinDirectBufferBo> joinDirectBufferBoList = joinDirectBufferBoMap.get(shiftTimestamp);

                if (joinDirectBufferBoList == null) {
                    joinDirectBufferBoList = new ArrayList<JoinDirectBufferBo>();
                    joinDirectBufferBoMap.put(shiftTimestamp, joinDirectBufferBoList);
                }

                joinDirectBufferBoList.add(joinDirectBufferBo);
            }
        }

        List<JoinDirectBufferBo> newJoinDirectBufferBoList = new ArrayList<JoinDirectBufferBo>();

        for (Map.Entry<Long, List<JoinDirectBufferBo>> entry : joinDirectBufferBoMap.entrySet()) {
            List<JoinDirectBufferBo> joinDirectBufferBoList = entry.getValue();
            JoinDirectBufferBo joinDirectBufferBo = JoinDirectBufferBo.joinDirectBufferBoList(joinDirectBufferBoList, entry.getKey());
            newJoinDirectBufferBoList.add(joinDirectBufferBo);
        }
        return newJoinDirectBufferBoList;
    }
    public static JoinApplicationStatBo joinApplicationStatBo(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        JoinApplicationStatBo newJoinApplicationStatBo = new JoinApplicationStatBo();

        if (joinApplicationStatBoList.isEmpty()) {
            return newJoinApplicationStatBo;
        }

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            joinCpuLoadBoList.addAll(joinApplicationStatBo.getJoinCpuLoadBoList());
        }
        Long timestamp = joinCpuLoadBoList.get(0).getTimestamp();
        JoinCpuLoadBo newJoinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, timestamp);
        List<JoinCpuLoadBo> newJoinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        newJoinCpuLoadBoList.add(newJoinCpuLoadBo);

        newJoinApplicationStatBo.setId(joinApplicationStatBoList.get(0).getId());
        newJoinApplicationStatBo.setTimestamp(timestamp);
        newJoinApplicationStatBo.setJoinCpuLoadBoList(newJoinCpuLoadBoList);
        newJoinApplicationStatBo.setStatType(StatType.APP_STST);

        return newJoinApplicationStatBo;
    }

    private static long shiftTimestamp(long timestamp) {
        return timestamp - (timestamp % SHIFT_RANGE);
    }

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

    public void setJoinMemoryBoList(List<JoinMemoryBo> joinMemoryBoList) {
        this.joinMemoryBoList = joinMemoryBoList;
    }

    public List<JoinMemoryBo> getJoinMemoryBoList() {
        return joinMemoryBoList;
    }

    public List<JoinTransactionBo> getJoinTransactionBoList() {
        return joinTransactionBoList;
    }

    public void setJoinTransactionBoList(List<JoinTransactionBo> joinTransactionBoList) {
        this.joinTransactionBoList = joinTransactionBoList;
    }

    public void setJoinActiveTraceBoList(List<JoinActiveTraceBo> joinActiveTraceBoList) {
        this.joinActiveTraceBoList = joinActiveTraceBoList;
    }

    public List<JoinActiveTraceBo> getJoinActiveTraceBoList() {
        return joinActiveTraceBoList;
    }

    public List<JoinResponseTimeBo> getJoinResponseTimeBoList() {
        return joinResponseTimeBoList;
    }

    public void setJoinResponseTimeBoList(List<JoinResponseTimeBo> joinResponseTimeBoList) {
        this.joinResponseTimeBoList = joinResponseTimeBoList;
    }

    public void setJoinDataSourceListBoList(List<JoinDataSourceListBo> joinDataSourceListBoList) {
        this.joinDataSourceListBoList = joinDataSourceListBoList;
    }

    public List<JoinDataSourceListBo> getJoinDataSourceListBoList() {
        return joinDataSourceListBoList;
    }

    public List<JoinFileDescriptorBo> getJoinFileDescriptorBoList() {
        return joinFileDescriptorBoList;
    }

    public void setJoinFileDescriptorBoList(List<JoinFileDescriptorBo> joinFileDescriptorBoList) {
        this.joinFileDescriptorBoList = joinFileDescriptorBoList;
    }

    public List<JoinDirectBufferBo> getJoinDirectBufferBoList() {
        return joinDirectBufferBoList;
    }

    public void setJoinDirectBufferBoList(List<JoinDirectBufferBo> joinDirectBufferBoList) {
        this.joinDirectBufferBoList = joinDirectBufferBoList;
    }

    public static List<JoinApplicationStatBo> createJoinApplicationStatBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime) {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        List<JoinAgentStatBo> joinAgentStatBoList = splitJoinAgentStatBo(applicationId, joinAgentStatBo, rangeTime);

        for (JoinAgentStatBo sliceJoinAgentStatBo : joinAgentStatBoList) {
            JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
            joinApplicationStatBo.setId(applicationId);
            joinApplicationStatBo.setTimestamp(sliceJoinAgentStatBo.getTimestamp());
            joinApplicationStatBo.setJoinCpuLoadBoList(sliceJoinAgentStatBo.getJoinCpuLoadBoList());
            joinApplicationStatBo.setJoinMemoryBoList(sliceJoinAgentStatBo.getJoinMemoryBoList());
            joinApplicationStatBo.setJoinTransactionBoList(sliceJoinAgentStatBo.getJoinTransactionBoList());
            joinApplicationStatBo.setJoinActiveTraceBoList(sliceJoinAgentStatBo.getJoinActiveTraceBoList());
            joinApplicationStatBo.setJoinResponseTimeBoList(sliceJoinAgentStatBo.getJoinResponseTimeBoList());
            joinApplicationStatBo.setJoinDataSourceListBoList(sliceJoinAgentStatBo.getJoinDataSourceListBoList());
            joinApplicationStatBo.setJoinFileDescriptorBoList(sliceJoinAgentStatBo.getJoinFileDescriptorBoList());
            joinApplicationStatBo.setJoinDirectBufferBoList(sliceJoinAgentStatBo.getJoinDirectBufferBoList());
            joinApplicationStatBoList.add(joinApplicationStatBo);
        }

        return joinApplicationStatBoList;
    }

    private static List<JoinAgentStatBo> splitJoinAgentStatBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime) {
        Map<Long, JoinAgentStatBo> joinAgentStatBoMap = new HashMap<Long, JoinAgentStatBo>();
        sliceJoinCpuLoadBo(applicationId, joinAgentStatBo, rangeTime, joinAgentStatBoMap);
        sliceJoinMemoryBo(applicationId, joinAgentStatBo, rangeTime, joinAgentStatBoMap);
        sliceJoinTransactionBo(applicationId, joinAgentStatBo, rangeTime, joinAgentStatBoMap);
        sliceJoinActiveTraceBo(applicationId, joinAgentStatBo, rangeTime, joinAgentStatBoMap);
        sliceJoinResponseTimeBo(applicationId, joinAgentStatBo, rangeTime, joinAgentStatBoMap);
        sliceJoinDataSourceListBo(applicationId, joinAgentStatBo, rangeTime, joinAgentStatBoMap);
        sliceJoinFileDescriptorBo(applicationId, joinAgentStatBo, rangeTime, joinAgentStatBoMap);
        sliceJoinDirectBufferBo(applicationId, joinAgentStatBo, rangeTime, joinAgentStatBoMap);
        return new ArrayList<JoinAgentStatBo>(joinAgentStatBoMap.values());
    }

    private static void sliceJoinDataSourceListBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime, Map<Long, JoinAgentStatBo> joinAgentStatBoMap) {
        Map<Long, List<JoinDataSourceListBo>> joinDataSourceListBoMap = new HashMap<Long, List<JoinDataSourceListBo>>();

        for (JoinDataSourceListBo joinDataSourceListBo : joinAgentStatBo.getJoinDataSourceListBoList()) {
            long timestamp = joinDataSourceListBo.getTimestamp();
            long time = timestamp - (timestamp % rangeTime);

            List<JoinDataSourceListBo> joinDataSourceListBoList = joinDataSourceListBoMap.get(time);

            if (joinDataSourceListBoList == null) {
                joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();
                joinDataSourceListBoMap.put(time, joinDataSourceListBoList);
            }

            joinDataSourceListBoList.add(joinDataSourceListBo);
        }
        for (Map.Entry<Long, List<JoinDataSourceListBo>> entry : joinDataSourceListBoMap.entrySet()) {
            long time = entry.getKey();
            JoinAgentStatBo sliceJoinAgentStatBo = getORCreateJoinAgentStatBo(applicationId, joinAgentStatBoMap, time);
            sliceJoinAgentStatBo.setJoinDataSourceListBoList(entry.getValue());
        }
    }

    private static void sliceJoinResponseTimeBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime, Map<Long, JoinAgentStatBo> joinAgentStatBoMap) {
        Map<Long, List<JoinResponseTimeBo>> joinResponseTimeBoMap = new HashMap<Long, List<JoinResponseTimeBo>>();

        for (JoinResponseTimeBo joinResponseTimeBo : joinAgentStatBo.getJoinResponseTimeBoList()) {
            long timestamp = joinResponseTimeBo.getTimestamp();
            long time = timestamp - (timestamp % rangeTime);
            List<JoinResponseTimeBo> joinResponseTimeBoList = joinResponseTimeBoMap.get(time);

            if (joinResponseTimeBoList == null) {
                joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
                joinResponseTimeBoMap.put(time, joinResponseTimeBoList);
            }

            joinResponseTimeBoList.add(joinResponseTimeBo);
        }
        for (Map.Entry<Long, List<JoinResponseTimeBo>> entry : joinResponseTimeBoMap.entrySet()) {
            long time = entry.getKey();
            JoinAgentStatBo sliceJoinAgentStatBo = getORCreateJoinAgentStatBo(applicationId, joinAgentStatBoMap, time);
            sliceJoinAgentStatBo.setJoinResponseTimeBoList(entry.getValue());
        }
    }

    private static void sliceJoinActiveTraceBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime, Map<Long, JoinAgentStatBo> joinAgentStatBoMap) {
        Map<Long, List<JoinActiveTraceBo>> joinActiveTraceBoMap = new HashMap<Long, List<JoinActiveTraceBo>>();

        for (JoinActiveTraceBo joinActiveTraceBo : joinAgentStatBo.getJoinActiveTraceBoList()) {
            long timestamp = joinActiveTraceBo.getTimestamp();
            long time = timestamp - (timestamp % rangeTime);
            List<JoinActiveTraceBo> joinActiveTraceBoList = joinActiveTraceBoMap.get(time);

            if (joinActiveTraceBoList == null) {
                joinActiveTraceBoList = new ArrayList<JoinActiveTraceBo>();
                joinActiveTraceBoMap.put(time, joinActiveTraceBoList);
            }

            joinActiveTraceBoList.add(joinActiveTraceBo);
        }
        for (Map.Entry<Long, List<JoinActiveTraceBo>> entry : joinActiveTraceBoMap.entrySet()) {
            long time = entry.getKey();
            JoinAgentStatBo sliceJoinAgentStatBo = getORCreateJoinAgentStatBo(applicationId, joinAgentStatBoMap, time);
            sliceJoinAgentStatBo.setJoinActiveTraceBoList(entry.getValue());
        }
    }

    private static void sliceJoinTransactionBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime, Map<Long, JoinAgentStatBo> joinAgentStatBoMap) {
        Map<Long, List<JoinTransactionBo>> joinTransactionBoMap = new HashMap<Long, List<JoinTransactionBo>>();

        for (JoinTransactionBo joinTransactionBo : joinAgentStatBo.getJoinTransactionBoList()) {
            long timestamp = joinTransactionBo.getTimestamp();
            long time = timestamp - (timestamp % rangeTime);
            List<JoinTransactionBo> joinTransactionBoList = joinTransactionBoMap.get(time);

            if (joinTransactionBoList == null) {
                joinTransactionBoList = new ArrayList<JoinTransactionBo>();
                joinTransactionBoMap.put(time, joinTransactionBoList);
            }

            joinTransactionBoList.add(joinTransactionBo);
        }
        for (Map.Entry<Long, List<JoinTransactionBo>> entry : joinTransactionBoMap.entrySet()) {
            long time = entry.getKey();
            JoinAgentStatBo sliceJoinAgentStatBo = getORCreateJoinAgentStatBo(applicationId, joinAgentStatBoMap, time);
            sliceJoinAgentStatBo.setJoinTransactionBoList(entry.getValue());
        }
    }

    private static void sliceJoinMemoryBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime, Map<Long, JoinAgentStatBo> joinAgentStatBoMap) {
        Map<Long, List<JoinMemoryBo>> joinMemoryBoMap = new HashMap<Long, List<JoinMemoryBo>>();

        for (JoinMemoryBo joinMemoryBo : joinAgentStatBo.getJoinMemoryBoList()) {
            long timestamp = joinMemoryBo.getTimestamp();
            long time = timestamp - (timestamp % rangeTime);
            List<JoinMemoryBo> joinMemoryBoList = joinMemoryBoMap.get(time);

            if (joinMemoryBoList == null) {
                joinMemoryBoList = new ArrayList<JoinMemoryBo>();
                joinMemoryBoMap.put(time, joinMemoryBoList);
            }

            joinMemoryBoList.add(joinMemoryBo);
        }
        for (Map.Entry<Long, List<JoinMemoryBo>> entry : joinMemoryBoMap.entrySet()) {
            long time = entry.getKey();
            JoinAgentStatBo sliceJoinAgentStatBo = getORCreateJoinAgentStatBo(applicationId, joinAgentStatBoMap, time);
            sliceJoinAgentStatBo.setJoinMemoryBoList(entry.getValue());
        }
    }

    private static void sliceJoinCpuLoadBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime, Map<Long, JoinAgentStatBo> joinAgentStatBoMap) {
        Map<Long, List<JoinCpuLoadBo>> joinCpuLoadBoMap = new HashMap<Long, List<JoinCpuLoadBo>>();

        for (JoinCpuLoadBo joinCpuLoadBo : joinAgentStatBo.getJoinCpuLoadBoList()) {
            long timestamp = joinCpuLoadBo.getTimestamp();
            long time = timestamp - (timestamp % rangeTime);
            List<JoinCpuLoadBo> joinCpuLoadBoList = joinCpuLoadBoMap.get(time);

            if (joinCpuLoadBoList == null) {
                joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
                joinCpuLoadBoMap.put(time, joinCpuLoadBoList);
            }

            joinCpuLoadBoList.add(joinCpuLoadBo);
        }
        for (Map.Entry<Long, List<JoinCpuLoadBo>> entry : joinCpuLoadBoMap.entrySet()) {
            long time = entry.getKey();
            JoinAgentStatBo sliceJoinAgentStatBo = getORCreateJoinAgentStatBo(applicationId, joinAgentStatBoMap, time);
            sliceJoinAgentStatBo.setJoinCpuLoadBoList(entry.getValue());
        }
    }

    private static void sliceJoinFileDescriptorBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime, Map<Long, JoinAgentStatBo> joinAgentStatBoMap) {
        Map<Long, List<JoinFileDescriptorBo>> joinFileDescriptorBoMap = new HashMap<Long, List<JoinFileDescriptorBo>>();

        for (JoinFileDescriptorBo joinFileDescriptorBo : joinAgentStatBo.getJoinFileDescriptorBoList()) {
            long timestamp = joinFileDescriptorBo.getTimestamp();
            long time = timestamp - (timestamp % rangeTime);
            List<JoinFileDescriptorBo> joinFileDescriptorBoList = joinFileDescriptorBoMap.get(time);

            if (joinFileDescriptorBoList == null) {
                joinFileDescriptorBoList = new ArrayList<JoinFileDescriptorBo>();
                joinFileDescriptorBoMap.put(time, joinFileDescriptorBoList);
            }

            joinFileDescriptorBoList.add(joinFileDescriptorBo);
        }
        for (Map.Entry<Long, List<JoinFileDescriptorBo>> entry : joinFileDescriptorBoMap.entrySet()) {
            long time = entry.getKey();
            JoinAgentStatBo sliceJoinAgentStatBo = getORCreateJoinAgentStatBo(applicationId, joinAgentStatBoMap, time);
            sliceJoinAgentStatBo.setJoinFileDescriptorBoList(entry.getValue());
        }
    }

    private static void sliceJoinDirectBufferBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime, Map<Long, JoinAgentStatBo> joinAgentStatBoMap) {
        Map<Long, List<JoinDirectBufferBo>> joinDirectBufferBoMap = new HashMap<Long, List<JoinDirectBufferBo>>();

        for (JoinDirectBufferBo joinDirectBufferBo : joinAgentStatBo.getJoinDirectBufferBoList()) {
            long timestamp = joinDirectBufferBo.getTimestamp();
            long time = timestamp - (timestamp % rangeTime);
            List<JoinDirectBufferBo> joinDirectBufferBoList = joinDirectBufferBoMap.get(time);

            if (joinDirectBufferBoList == null) {
                joinDirectBufferBoList = new ArrayList<JoinDirectBufferBo>();
                joinDirectBufferBoMap.put(time, joinDirectBufferBoList);
            }

            joinDirectBufferBoList.add(joinDirectBufferBo);
        }
        for (Map.Entry<Long, List<JoinDirectBufferBo>> entry : joinDirectBufferBoMap.entrySet()) {
            long time = entry.getKey();
            JoinAgentStatBo sliceJoinAgentStatBo = getORCreateJoinAgentStatBo(applicationId, joinAgentStatBoMap, time);
            sliceJoinAgentStatBo.setJoinDirectBufferBoList(entry.getValue());
        }
    }
    private static JoinAgentStatBo getORCreateJoinAgentStatBo(String applicationId, Map<Long, JoinAgentStatBo> joinAgentStatBoMap, long time) {
        JoinAgentStatBo joinAgentStatBo = joinAgentStatBoMap.get(time);

        if (joinAgentStatBo == null) {
            joinAgentStatBo = new JoinAgentStatBo();
            joinAgentStatBo.setId(applicationId);
            joinAgentStatBo.setTimestamp(time);
            joinAgentStatBoMap.put(time, joinAgentStatBo);
        }

        return joinAgentStatBo;
    }

    @Override
    public String toString() {
        return "JoinApplicationStatBo{" +
            ", timestamp=" + new Date(timestamp) +
            ", applicationId='" + applicationId + '\'' +
            ", joinCpuLoadBoList=" + joinCpuLoadBoList +
            ", joinMemoryBoList=" + joinMemoryBoList +
            ", joinTransactionBoList=" + joinTransactionBoList +
            ", joinActiveTraceBoList=" + joinActiveTraceBoList +
            ", joinResponseTimeBoList=" + joinResponseTimeBoList +
            ", joinDataSourceListBoList=" + joinDataSourceListBoList +
            ", joinFileDescriptorBoList=" + joinFileDescriptorBoList +
            ", joinDirectBufferBoList=" + joinDirectBufferBoList +
            ", statType=" + statType +
            '}';
    }
}
