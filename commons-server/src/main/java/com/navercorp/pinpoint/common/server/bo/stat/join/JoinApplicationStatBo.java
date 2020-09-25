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

import com.navercorp.pinpoint.common.server.util.FilterUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author minwoo.jung
 */
public class JoinApplicationStatBo implements JoinStatBo {
    public static final JoinApplicationStatBo EMPTY_JOIN_APPLICATION_STAT_BO = newEmptyApplicationStatBo();

    private static JoinApplicationStatBo newEmptyApplicationStatBo() {
        Builder builder = JoinApplicationStatBo.newBuilder(UNKNOWN_ID, Long.MIN_VALUE);
        return builder.build();
    }

    private static final long SHIFT_RANGE = 1000 * 5;

    private final String applicationId;
    private final List<JoinCpuLoadBo> joinCpuLoadBoList;
    private final List<JoinMemoryBo> joinMemoryBoList;
    private final List<JoinTransactionBo> joinTransactionBoList;
    private final List<JoinActiveTraceBo> joinActiveTraceBoList;
    private final List<JoinResponseTimeBo> joinResponseTimeBoList;
    private final List<JoinDataSourceListBo> joinDataSourceListBoList;
    private final List<JoinFileDescriptorBo> joinFileDescriptorBoList;
    private final List<JoinDirectBufferBo> joinDirectBufferBoList;
    private final List<JoinTotalThreadCountBo> joinTotalThreadCountBoList;
    private final List<JoinLoadedClassBo> joinLoadedClassBoList;

    private final long timestamp;
    private final StatType statType;

    protected JoinApplicationStatBo(JoinApplicationStatBo joinApplicationStatBo) {
        Objects.requireNonNull(joinApplicationStatBo, "joinApplicationStatBo");

        this.applicationId = joinApplicationStatBo.getId();
        this.joinCpuLoadBoList = joinApplicationStatBo.getJoinCpuLoadBoList();
        this.joinMemoryBoList = joinApplicationStatBo.getJoinMemoryBoList();
        this.joinTransactionBoList = joinApplicationStatBo.getJoinTransactionBoList();
        this.joinActiveTraceBoList = joinApplicationStatBo.getJoinActiveTraceBoList();
        this.joinResponseTimeBoList = joinApplicationStatBo.getJoinResponseTimeBoList();
        this.joinDataSourceListBoList = joinApplicationStatBo.getJoinDataSourceListBoList();
        this.joinFileDescriptorBoList = joinApplicationStatBo.getJoinFileDescriptorBoList();
        this.joinDirectBufferBoList = joinApplicationStatBo.getJoinDirectBufferBoList();
        this.joinTotalThreadCountBoList = joinApplicationStatBo.getJoinTotalThreadCountBoList();
        this.joinLoadedClassBoList = joinApplicationStatBo.getJoinLoadedClassBoList();

        this.timestamp = joinApplicationStatBo.getTimestamp();
        this.statType = joinApplicationStatBo.getStatType();
    }


    JoinApplicationStatBo(Builder builder) {
        Objects.requireNonNull(builder, "builder");

        this.applicationId = builder.applicationId;
        this.joinCpuLoadBoList = FilterUtils.filter(builder.statList, JoinCpuLoadBo.class);
        this.joinMemoryBoList = FilterUtils.filter(builder.statList, JoinMemoryBo.class);
        this.joinTransactionBoList = FilterUtils.filter(builder.statList, JoinTransactionBo.class);
        this.joinActiveTraceBoList = FilterUtils.filter(builder.statList, JoinActiveTraceBo.class);
        this.joinResponseTimeBoList = FilterUtils.filter(builder.statList, JoinResponseTimeBo.class);
        this.joinDataSourceListBoList = FilterUtils.filter(builder.statList, JoinDataSourceListBo.class);
        this.joinFileDescriptorBoList = FilterUtils.filter(builder.statList, JoinFileDescriptorBo.class);
        this.joinDirectBufferBoList = FilterUtils.filter(builder.statList, JoinDirectBufferBo.class);
        this.joinTotalThreadCountBoList = FilterUtils.filter(builder.statList, JoinTotalThreadCountBo.class);
        this.joinLoadedClassBoList = FilterUtils.filter(builder.statList, JoinLoadedClassBo.class);

        this.timestamp = builder.timestamp;
        this.statType = builder.statType;

    }

    public static JoinApplicationStatBo joinApplicationStatBoByTimeSlice(final List<JoinApplicationStatBo> joinApplicationStatBoList) {
        if (joinApplicationStatBoList.isEmpty()) {
            return EMPTY_JOIN_APPLICATION_STAT_BO;
        }
        String applicationId = joinApplicationStatBoList.get(0).getId();
        long timestamp = extractMinTimestamp(joinApplicationStatBoList);
        JoinApplicationStatBo.Builder builder = JoinApplicationStatBo.newBuilder(applicationId, timestamp);

        Joiner joiner = new Joiner(joinApplicationStatBoList, builder);
        joiner.join(JoinApplicationStatBo::getJoinCpuLoadBoList, JoinCpuLoadBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinMemoryBoList, JoinMemoryBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinTransactionBoList, JoinTransactionBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinActiveTraceBoList, JoinActiveTraceBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinResponseTimeBoList, JoinResponseTimeBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinDataSourceListBoList, JoinDataSourceListBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinFileDescriptorBoList, JoinFileDescriptorBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinDirectBufferBoList, JoinDirectBufferBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinTotalThreadCountBoList, JoinTotalThreadCountBo::apply);
        joiner.join(JoinApplicationStatBo::getJoinLoadedClassBoList, JoinLoadedClassBo::apply);

        return builder.build();
    }



    public static class Joiner {
        private final List<JoinApplicationStatBo> joinApplicationStatBoList;
        private final JoinApplicationStatBo.Builder builder;

        public Joiner(List<JoinApplicationStatBo> joinApplicationStatBoList, JoinApplicationStatBo.Builder builder) {
            this.joinApplicationStatBoList = Objects.requireNonNull(joinApplicationStatBoList, "joinApplicationStatBoList");
            this.builder = Objects.requireNonNull(builder, "builder");
        }

        public interface MappingFunction<T> {
            void apply(JoinApplicationStatBo.Builder builder, List<T> t, Long timeStamp);
        }

        private <T extends JoinStatBo> void join(Function<JoinApplicationStatBo, List<T>> fieldSupplier,
                                                    MappingFunction<T> joinStatBoTrasform) {
            Map<Long, List<T>> joinMap = join(fieldSupplier);
            map(joinMap, joinStatBoTrasform);
        }

        private <T extends JoinStatBo> void map(Map<Long, List<T>> joinMap, MappingFunction<T> joinStatBoTrasform) {
            for (Map.Entry<Long, List<T>> entry : joinMap.entrySet()) {
                final Long key = entry.getKey();
                final List<T> statData = entry.getValue();

                joinStatBoTrasform.apply(builder, statData, key);
            }
        }

        private <T extends JoinStatBo> Map<Long, List<T>> join(Function<JoinApplicationStatBo, List<T>> fieldSupplier) {
            final Map<Long, List<T>> result = new HashMap<>();
            for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
                final List<T> statList = fieldSupplier.apply(joinApplicationStatBo);
                for (T statBo : statList) {
                    long shiftTimestamp = shiftTimestamp(statBo.getTimestamp());
                    List<T> joinResponseTimeBoList = result.computeIfAbsent(shiftTimestamp, k -> new ArrayList<>());
                    joinResponseTimeBoList.add(statBo);
                }
            }
            return result;
        }

    }

    private static long extractMinTimestamp(List<JoinApplicationStatBo> joinApplicationStatBoList) {
        long minTimestamp = Long.MAX_VALUE;
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinCpuLoadBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinMemoryBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinTransactionBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinActiveTraceBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinResponseTimeBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinDataSourceListBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinFileDescriptorBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinDirectBufferBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinTotalThreadCountBoList(), minTimestamp);
            minTimestamp = minTimestamp(joinApplicationStatBo.getJoinLoadedClassBoList(), minTimestamp);
        }
        return minTimestamp;
    }


    private static <T extends JoinStatBo> long minTimestamp(List<T> list, long minTimestamp) {
        for (T statBo : list) {
            minTimestamp = Math.min(statBo.getTimestamp(), minTimestamp);
        }
        return minTimestamp;
    }

    private static long shiftTimestamp(long timestamp) {
        return timestamp - (timestamp % SHIFT_RANGE);
    }

    public long getTimestamp() {
        return timestamp;
    }


    public String getId() {
        return applicationId;
    }

    public List<JoinCpuLoadBo> getJoinCpuLoadBoList() {
        return joinCpuLoadBoList;
    }

    public StatType getStatType() {
        return statType;
    }


    public List<JoinMemoryBo> getJoinMemoryBoList() {
        return joinMemoryBoList;
    }

    public List<JoinTransactionBo> getJoinTransactionBoList() {
        return joinTransactionBoList;
    }

    public List<JoinActiveTraceBo> getJoinActiveTraceBoList() {
        return joinActiveTraceBoList;
    }

    public List<JoinResponseTimeBo> getJoinResponseTimeBoList() {
        return joinResponseTimeBoList;
    }

    public List<JoinDataSourceListBo> getJoinDataSourceListBoList() {
        return joinDataSourceListBoList;
    }

    public List<JoinFileDescriptorBo> getJoinFileDescriptorBoList() {
        return joinFileDescriptorBoList;
    }

    public List<JoinDirectBufferBo> getJoinDirectBufferBoList() {
        return joinDirectBufferBoList;
    }

    public List<JoinTotalThreadCountBo> getJoinTotalThreadCountBoList() {
        return joinTotalThreadCountBoList;
    }

    public List<JoinLoadedClassBo> getJoinLoadedClassBoList() {
        return joinLoadedClassBoList;
    }

    public static List<JoinApplicationStatBo> createJoinApplicationStatBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime) {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        List<JoinAgentStatBo> joinAgentStatBoList = splitJoinAgentStatBo(applicationId, joinAgentStatBo, rangeTime);

        for (JoinAgentStatBo sliceJoinAgentStatBo : joinAgentStatBoList) {
            JoinApplicationStatBo.Builder builder = JoinApplicationStatBo.newBuilder(applicationId, sliceJoinAgentStatBo.getTimestamp());

            sliceJoinAgentStatBo.getJoinCpuLoadBoList().forEach(builder::addCpuLoad);
            sliceJoinAgentStatBo.getJoinMemoryBoList().forEach(builder::addMemory);
            sliceJoinAgentStatBo.getJoinTransactionBoList().forEach(builder::addTransaction);
            sliceJoinAgentStatBo.getJoinActiveTraceBoList().forEach(builder::addActiveTrace);
            sliceJoinAgentStatBo.getJoinResponseTimeBoList().forEach(builder::addResponseTime);
            sliceJoinAgentStatBo.getJoinDataSourceListBoList().forEach(builder::addDataSourceList);
            sliceJoinAgentStatBo.getJoinFileDescriptorBoList().forEach(builder::addFileDescriptor);
            sliceJoinAgentStatBo.getJoinDirectBufferBoList().forEach(builder::addDirectBuffer);
            sliceJoinAgentStatBo.getJoinTotalThreadCountBoList().forEach(builder::addTotalThreadCount);
            sliceJoinAgentStatBo.getJoinLoadedClassBoList().forEach(builder::addLoadedClass);

            joinApplicationStatBoList.add(builder.build());
        }

        return joinApplicationStatBoList;
    }

    private static List<JoinAgentStatBo> splitJoinAgentStatBo(String applicationId, JoinAgentStatBo joinAgentStatBo, long rangeTime) {
        Slicer slicer = new Slicer(applicationId, rangeTime);

        slicer.slice(joinAgentStatBo.getJoinCpuLoadBoList(), JoinAgentStatBo.Builder::addCpuLoadBo);
        slicer.slice(joinAgentStatBo.getJoinMemoryBoList(), JoinAgentStatBo.Builder::addMemory);
        slicer.slice(joinAgentStatBo.getJoinTransactionBoList(), JoinAgentStatBo.Builder::addTransaction);
        slicer.slice(joinAgentStatBo.getJoinActiveTraceBoList(), JoinAgentStatBo.Builder::addActiveTrace);
        slicer.slice(joinAgentStatBo.getJoinResponseTimeBoList(), JoinAgentStatBo.Builder::addResponseTime);
        slicer.slice(joinAgentStatBo.getJoinDataSourceListBoList(), JoinAgentStatBo.Builder::addDataSourceListBo);
        slicer.slice(joinAgentStatBo.getJoinFileDescriptorBoList(), JoinAgentStatBo.Builder::addFileDescriptor);
        slicer.slice(joinAgentStatBo.getJoinDirectBufferBoList(), JoinAgentStatBo.Builder::addDirectBuffer);
        slicer.slice(joinAgentStatBo.getJoinTotalThreadCountBoList(), JoinAgentStatBo.Builder::addTotalThreadCount);
        slicer.slice(joinAgentStatBo.getJoinLoadedClassBoList(), JoinAgentStatBo.Builder::addLoadedClass);
        return slicer.build();
    }

    public static class Slicer {
        private final Map<Long, JoinAgentStatBo.Builder> joinAgentStatBoMap = new HashMap<>();
        private final String applicationId;
        private final long rangeTime;

        public Slicer(String applicationId, long rangeTime) {
            this.applicationId = Objects.requireNonNull(applicationId, "applicationId");
            this.rangeTime = rangeTime;
        }

        public <T extends JoinStatBo> void slice(List<T> agentStatList, BiConsumer<JoinAgentStatBo.Builder, T> consumer) {
            if (agentStatList == null) {
                return;
            }

            final Map<Long, List<T>> result = map(agentStatList);

            for (Map.Entry<Long, List<T>> entry : result.entrySet()) {
                long time = entry.getKey();
                JoinAgentStatBo.Builder statBo = getORCreateJoinAgentStatBo(applicationId, time);
                List<T> value = entry.getValue();
                for (T t : value) {
                    consumer.accept(statBo, t);
                }
            }
        }

        private <T extends JoinStatBo> Map<Long, List<T>> map(List<T> agentStatList) {
            final Map<Long, List<T>> result = new HashMap<>();
            for (T joinStatListBo : agentStatList) {
                long timestamp = joinStatListBo.getTimestamp();
                long time = timestamp - (timestamp % rangeTime);

                List<T> statList = result.computeIfAbsent(time, k -> new ArrayList<>());
                statList.add(joinStatListBo);
            }
            return result;
        }


        private JoinAgentStatBo.Builder getORCreateJoinAgentStatBo(String applicationId, long time) {
            JoinAgentStatBo.Builder joinAgentStatBo = joinAgentStatBoMap.get(time);
            if (joinAgentStatBo == null) {
                joinAgentStatBo = new JoinAgentStatBo.Builder(applicationId, Long.MIN_VALUE, time);
                joinAgentStatBoMap.put(time, joinAgentStatBo);
            }

            return joinAgentStatBo;
        }

        public List<JoinAgentStatBo> build() {
            Stream<JoinAgentStatBo.Builder> stream = joinAgentStatBoMap.values().stream();
            return stream.map(JoinAgentStatBo.Builder::build).collect(Collectors.toList());
        }
    }

    public static Builder newBuilder(String applicationId, long timestamp) {
        return new Builder(applicationId, timestamp);
    }

    public static class Builder {
        private final String applicationId;
        private final long timestamp;
        private StatType statType = StatType.APP_STST;

        private final List<JoinStatBo> statList = new ArrayList<>();

        Builder(String applicationId, long timestamp) {
            this.applicationId = Objects.requireNonNull(applicationId, "applicationId");
            this.timestamp = timestamp;
        }


        public void addCpuLoad(JoinCpuLoadBo joinCpuLoadBoList) {
            Objects.requireNonNull(joinCpuLoadBoList, "joinCpuLoadBoList");
            this.statList.add(joinCpuLoadBoList);
        }

        public void setStatType(StatType statType) {
            this.statType = Objects.requireNonNull(statType, "statType");
        }

        public void addMemory(JoinMemoryBo joinMemory) {
            Objects.requireNonNull(joinMemory, "joinMemory");
            this.statList.add(joinMemory);

        }

        public void addTransaction(JoinTransactionBo joinTransaction) {
            Objects.requireNonNull(joinTransaction, "joinTransaction");
            this.statList.add(joinTransaction);
        }

        public void addActiveTrace(JoinActiveTraceBo joinActiveTrace) {
            Objects.requireNonNull(joinActiveTrace, "joinActiveTrace");
            this.statList.add(joinActiveTrace);
        }


        public void addResponseTime(JoinResponseTimeBo joinResponseTime) {
            Objects.requireNonNull(joinResponseTime, "joinResponseTime");
            this.statList.add(joinResponseTime);
        }

        public void addDataSourceList(JoinDataSourceListBo joinDataSourceList) {
            Objects.requireNonNull(joinDataSourceList, "joinDataSourceList");
            this.statList.add(joinDataSourceList);
        }


        public void addFileDescriptor(JoinFileDescriptorBo joinFileDescriptor) {
            Objects.requireNonNull(joinFileDescriptor, "joinFileDescriptor");
            this.statList.add(joinFileDescriptor);
        }


        public void addDirectBuffer(JoinDirectBufferBo joinDirectBuffer) {
            Objects.requireNonNull(joinDirectBuffer, "joinDirectBuffer");
            this.statList.add(joinDirectBuffer);
        }

        public void addTotalThreadCount(JoinTotalThreadCountBo joinTotalThreadCount) {
            Objects.requireNonNull(joinTotalThreadCount, "joinTotalThreadCount");
            this.statList.add(joinTotalThreadCount);
        }

        public void addLoadedClass(JoinLoadedClassBo joinLoadedClass) {
            Objects.requireNonNull(joinLoadedClass, "joinLoadedClass");
            this.statList.add(joinLoadedClass);
        }

        public JoinApplicationStatBo build() {

            return new JoinApplicationStatBo(this);
        }

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
                ", joinTotalThreadCountBoList=" + joinTotalThreadCountBoList +
                ", joinLoadedClassBoList=" + joinLoadedClassBoList +
                ", statType=" + statType +
                '}';
    }
}
