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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author minwoo.jung
 */
public class JoinApplicationStatBoTest {

    public static final String APPLICATION_ID = "test_app";
    public static final long TIMESTAMP = 14984_6254_5000L;
    public static final long TIMESTAMP1 = 14984_6255_0000L;
    public static final long TIMESTAMP2 = 14984_6255_5000L;
    public static final long TIMESTAMP3 = 14984_6256_5000L;
    public static final long TIMESTAMP4 = 14984_6256_0000L;

    @Test
    public void joinApplicationStatBoByTimeSliceTest() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(
                createCpu("id1", currentTime, 1),
                createCpu("id2", currentTime + 1000, -4),
                createCpu("id3", currentTime + 2000, -3),
                createCpu("id4", currentTime + 3000, 4),
                createCpu("id5", currentTime + 4000, -5)
        );

        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinCpuLoadBo> joinCpuLoadBoList = resultJoinApplicationStatBo.getJoinCpuLoadBoList();
        joinCpuLoadBoList.sort(Comparator.comparingLong(JoinCpuLoadBo::getTimestamp));
        assertJoinCpuLoadBoList(joinCpuLoadBoList);
    }


    private void assertJoinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList) {
        assertThat(joinCpuLoadBoList).hasSize(5);
        JoinCpuLoadBo joinCpuLoadBo1 = joinCpuLoadBoList.get(0);
        assertCpu(joinCpuLoadBo1, "id1", 1487149800000L,
                new JoinDoubleFieldBo(48.6, 22.0, "id5_2", 91.0, "id4_1"),
                new JoinDoubleFieldBo(78.6, 41.0, "id5_4", 91.0, "id4_3"));

        JoinCpuLoadBo joinCpuLoadBo2 = joinCpuLoadBoList.get(1);
        assertCpu(joinCpuLoadBo2, "id1", 1487149805000L,
                new JoinDoubleFieldBo(38.6, 35.0, "id5_2", 81.0, "id4_1"),
                new JoinDoubleFieldBo(68.6, 35.0, "id5_4", 81.0, "id4_3"));

        JoinCpuLoadBo joinCpuLoadBo3 = joinCpuLoadBoList.get(2);
        assertCpu(joinCpuLoadBo3, "id1", 1487149810000L,
                new JoinDoubleFieldBo(28.6, 22.0, "id5_2", 71.0, "id4_1"),
                new JoinDoubleFieldBo(58.6, 22.0, "id5_4", 71.0, "id4_3"));

        JoinCpuLoadBo joinCpuLoadBo4 = joinCpuLoadBoList.get(3);
        assertCpu(joinCpuLoadBo4, "id1", 1487149815000L,
                new JoinDoubleFieldBo(18.6, 12.0, "id5_2", 61.0, "id4_1"),
                new JoinDoubleFieldBo(38.6, 13.0, "id5_4", 93.0, "id4_3"));

        JoinCpuLoadBo joinCpuLoadBo5 = joinCpuLoadBoList.get(4);
        assertCpu(joinCpuLoadBo5, "id1", 1487149820000L,
                new JoinDoubleFieldBo(8.6, 2.0, "id5_2", 93.0, "id4_1"),
                new JoinDoubleFieldBo(28.6, 3.0, "id5_4", 63.0, "id4_3"));
    }

    private void assertCpu(JoinCpuLoadBo joinCpuLoadBo1, String id, long timestamp, JoinDoubleFieldBo jvm, JoinDoubleFieldBo system) {
        assertEquals(joinCpuLoadBo1.getId(), id);
        assertEquals(joinCpuLoadBo1.getTimestamp(), timestamp);
        assertEquals(joinCpuLoadBo1.getJvmCpuLoadJoinValue(), jvm);
        assertEquals(joinCpuLoadBo1.getSystemCpuLoadJoinValue(), system);
    }

    private JoinApplicationStatBo createCpu(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinCpuLoadBoList(id, timestamp, plus).forEach(joinApplicationStatBo::addCpuLoad);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinCpuLoadBo> createJoinCpuLoadBoList(final String id, final long currentTime, int plus) {
        return List.of(
                new JoinCpuLoadBo(id, 50 + plus, 87 + plus, id + "_1", 27 + plus, id + "_2", 80 + plus, 87 + plus, id + "_3", 46 + plus, id + "_4", currentTime),
                new JoinCpuLoadBo(id, 40 + plus, 77 + plus, id + "_1", 40 + plus, id + "_2", 70 + plus, 77 + plus, id + "_3", 40 + plus, id + "_4", currentTime + 5000),
                new JoinCpuLoadBo(id, 30 + plus, 67 + plus, id + "_1", 27 + plus, id + "_2", 60 + plus, 67 + plus, id + "_3", 27 + plus, id + "_4", currentTime + 10000),
                new JoinCpuLoadBo(id, 20 + plus, 57 + plus, id + "_1", 17 + plus, id + "_2", 40 + plus, 89 + plus, id + "_3", 18 + plus, id + "_4", currentTime + 15000),
                new JoinCpuLoadBo(id, 10 + plus, 89 + plus, id + "_1", 7 + plus, id + "_2", 30 + plus, 59 + plus, id + "_3", 8 + plus, id + "_4", currentTime + 20000)
        );
    }

    @Test
    public void joinApplicationStatBoByTimeSlice2Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(
                createMemory("id1", currentTime, 10),
                createMemory("id2", currentTime + 1000, -40),
                createMemory("id3", currentTime + 2000, -30),
                createMemory("id4", currentTime + 3000, 40),
                createMemory("id5", currentTime + 4000, -50)
        );

        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinMemoryBo> joinMemoryBoList = resultJoinApplicationStatBo.getJoinMemoryBoList();
        joinMemoryBoList.sort(Comparator.comparingLong(JoinMemoryBo::getTimestamp));
        assertJoinMemoryBoList(joinMemoryBoList);
    }

    private void assertJoinMemoryBoList(List<JoinMemoryBo> joinMemoryBoList) {
        assertThat(joinMemoryBoList).hasSize(5);

        JoinMemoryBo joinMemoryBo1 = joinMemoryBoList.get(0);
        assertMemory(joinMemoryBo1, "id1", 1487149800000L,
                new JoinLongFieldBo(2986L, 1950L, "id5_1", 5040L, "id4_2"),
                new JoinLongFieldBo(486L, 0L, "id5_3", 640L, "id4_4"));

        JoinMemoryBo joinMemoryBo2 = joinMemoryBoList.get(1);
        assertMemory(joinMemoryBo2, "id1", 1487149805000L,
                new JoinLongFieldBo(3986L, 950L, "id5_1", 7040L, "id4_2"),
                new JoinLongFieldBo(386L, 100L, "id5_3", 640L, "id4_4"));

        JoinMemoryBo joinMemoryBo3 = joinMemoryBoList.get(2);
        assertMemory(joinMemoryBo3, "id1", 1487149810000L,
                new JoinLongFieldBo(4986L, 2950L, "id5_1", 8040L, "id4_2"),
                new JoinLongFieldBo(186L, 50L, "id5_3", 240L, "id4_4"));

        JoinMemoryBo joinMemoryBo4 = joinMemoryBoList.get(3);
        assertMemory(joinMemoryBo4, "id1", 1487149815000L,
                new JoinLongFieldBo(986L, 50L, "id5_1", 3040L, "id4_2"),
                new JoinLongFieldBo(86L, 850L, "id5_3", 1040L, "id4_4"));

        JoinMemoryBo joinMemoryBo5 = joinMemoryBoList.get(4);
        assertMemory(joinMemoryBo5, "id1", 1487149820000L,
                new JoinLongFieldBo(1986L, 950L, "id5_1", 6040L, "id4_2"),
                new JoinLongFieldBo(286L, 50L, "id5_3", 2940L, "id4_4"));
    }

    private void assertMemory(JoinMemoryBo memory, String id, long timestamp, JoinLongFieldBo heap, JoinLongFieldBo nonHeap) {
        assertEquals(id, memory.getId());
        assertEquals(timestamp, memory.getTimestamp());
        assertEquals(heap, memory.getHeapUsedJoinValue(), "heap");
        assertEquals(nonHeap, memory.getNonHeapUsedJoinValue(), "nonHeap");
    }


    private JoinApplicationStatBo createMemory(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinMemoryBoList(id, timestamp, plus)
                .forEach(joinApplicationStatBo::addMemory);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinMemoryBo> createJoinMemoryBoList(final String id, final long currentTime, int plus) {
        return List.of(
                new JoinMemoryBo(id, currentTime, 3000 + plus, 2000 + plus, 5000 + plus, id + "_1", id + "_2", 500 + plus, 50 + plus, 600 + plus, id + "_3", id + "_4"),
                new JoinMemoryBo(id, currentTime + 5000, 4000 + plus, 1000 + plus, 7000 + plus, id + "_1", id + "_2", 400 + plus, 150 + plus, 600 + plus, id + "_3", id + "_4"),
                new JoinMemoryBo(id, currentTime + 10000, 5000 + plus, 3000 + plus, 8000 + plus, id + "_1", id + "_2", 200 + plus, 100 + plus, 200 + plus, id + "_3", id + "_4"),
                new JoinMemoryBo(id, currentTime + 15000, 1000 + plus, 100 + plus, 3000 + plus, id + "_1", id + "_2", 100 + plus, 900 + plus, 1000 + plus, id + "_3", id + "_4"),
                new JoinMemoryBo(id, currentTime + +20000, 2000 + plus, 1000 + plus, 6000 + plus, id + "_1", id + "_2", 300 + plus, 100 + plus, 2900 + plus, id + "_3", id + "_4")
        );
    }

    @Test
    public void joinApplicationStatBoByTimeSlice3Test() {
        List<JoinCpuLoadBo> cpu1 = List.of(
                new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", TIMESTAMP),
                new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", TIMESTAMP1),
                new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", TIMESTAMP2)
        );

        JoinApplicationStatBo join1 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addCpuLoad, cpu1);

        List<JoinCpuLoadBo> cpu2 = List.of(
                new JoinCpuLoadBo("agent1", 33, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", TIMESTAMP),
                new JoinCpuLoadBo("agent1", 22, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", TIMESTAMP1),
                new JoinCpuLoadBo("agent1", 11, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", TIMESTAMP2),
                new JoinCpuLoadBo("agent1", 77, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", TIMESTAMP4)
        );

        JoinApplicationStatBo join2 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addCpuLoad, cpu2);

        List<JoinCpuLoadBo> cpu3 = List.of(
                new JoinCpuLoadBo("agent1", 22, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", TIMESTAMP),
                new JoinCpuLoadBo("agent1", 11, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", TIMESTAMP1),
                new JoinCpuLoadBo("agent1", 88, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", TIMESTAMP3)
        );

        JoinApplicationStatBo join3 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addCpuLoad, cpu3);


        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(join1, join2, join3);
        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), TIMESTAMP);
        List<JoinCpuLoadBo> joinCpuLoadBoList = joinApplicationStatBo.getJoinCpuLoadBoList();
        joinCpuLoadBoList.sort(Comparator.comparingLong(JoinCpuLoadBo::getTimestamp));

        assertThat(joinCpuLoadBoList)
                .hasSize(5)
                .map(JoinCpuLoadBo::getJvmCpuLoadJoinValue)
                .map(JoinDoubleFieldBo::getAvg)
                .containsExactly(33.0, 22.0, 33.0, 77.0, 88.0);
    }

    private <T> JoinApplicationStatBo buildApplicationStat(String applicationId, long timestamp,
                                                           BiConsumer<JoinApplicationStatBo.Builder, T> consumer,
                                                           List<T> loadBos) {
        JoinApplicationStatBo.Builder builder = JoinApplicationStatBo.newBuilder(applicationId, timestamp);
        for (T loadBo : loadBos) {
            consumer.accept(builder, loadBo);
        }
        return builder.build();
    }

    @Test
    public void joinApplicationStatBoByTimeSlice4Test() {
        List<JoinMemoryBo> memory1 = List.of(
                new JoinMemoryBo("agent1", TIMESTAMP, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent2", TIMESTAMP1, 4000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2"),
                new JoinMemoryBo("agent3", TIMESTAMP2, 5000, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3")
        );
        JoinApplicationStatBo join1 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addMemory, memory1);

        List<JoinMemoryBo> memory2 = List.of(
                new JoinMemoryBo("agent1", TIMESTAMP, 4000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent2", TIMESTAMP1, 1000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2"),
                new JoinMemoryBo("agent3", TIMESTAMP2, 3000, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3"),
                new JoinMemoryBo("agent3", TIMESTAMP4, 8800, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3")
        );
        JoinApplicationStatBo join2 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addMemory, memory2);

        List<JoinMemoryBo> memory3 = List.of(
                new JoinMemoryBo("agent1", TIMESTAMP, 5000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent2", TIMESTAMP1, 1000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2"),
                new JoinMemoryBo("agent3", TIMESTAMP3, 7800, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3")
        );
        JoinApplicationStatBo join3 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addMemory, memory3);

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(join1, join2, join3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), TIMESTAMP);
        List<JoinMemoryBo> joinMemoryBoList = joinApplicationStatBo.getJoinMemoryBoList();
        joinMemoryBoList.sort(Comparator.comparingLong(JoinMemoryBo::getTimestamp));

        assertThat(joinMemoryBoList)
                .hasSize(5)
                .map(JoinMemoryBo::getHeapUsedJoinValue)
                .map(JoinLongFieldBo::getAvg)
                .containsExactly(4000L, 2000L, 4000L, 8800L, 7800L);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice5Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> sum = List.of(
                createTranscation("id1", currentTime, 10),
                createTranscation("id2", currentTime + 1000, -40),
                createTranscation("id3", currentTime + 2000, -30),
                createTranscation("id4", currentTime + 3000, 40),
                createTranscation("id5", currentTime + 4000, -50)
        );

        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(sum);
        List<JoinTransactionBo> joinTransactionBoList = resultJoinApplicationStatBo.getJoinTransactionBoList();
        joinTransactionBoList.sort(Comparator.comparingLong(JoinTransactionBo::getTimestamp));
        assertJoinTransactionBoList(joinTransactionBoList);
    }


    private JoinApplicationStatBo createTranscation(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinTransactionBoList(id, timestamp, plus)
                .forEach(joinApplicationStatBo::addTransaction);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinTransactionBo> createJoinTransactionBoList(final String id, final long currentTime, int plus) {
        return List.of(
                new JoinTransactionBo(id, 5000, 100 + plus, 60 + plus, id + "_1", 200 + plus, id + "_2", currentTime),
                new JoinTransactionBo(id, 5000, 300 + plus, 150 + plus, id + "_1", 400 + plus, id + "_2", currentTime + 5000),
                new JoinTransactionBo(id, 5000, 200 + plus, 130 + plus, id + "_1", 300 + plus, id + "_2", currentTime + 10000),
                new JoinTransactionBo(id, 5000, 400 + plus, 200 + plus, id + "_1", 450 + plus, id + "_2", currentTime + 15000),
                new JoinTransactionBo(id, 5000, 350 + plus, 170 + plus, id + "_1", 600 + plus, id + "_2", currentTime + 20000)
        );
    }


    private void assertJoinTransactionBoList(List<JoinTransactionBo> joinTransactionBoList) {
        assertThat(joinTransactionBoList).hasSize(5);

        JoinTransactionBo joinTransactionBo1 = joinTransactionBoList.get(0);
        assertTransaction(joinTransactionBo1, "id1", 1487149800000L,
                new JoinLongFieldBo(86L, 10L, "id5_1", 240L, "id4_2"));

        JoinTransactionBo joinTransactionBo2 = joinTransactionBoList.get(1);
        assertTransaction(joinTransactionBo2, "id1", 1487149805000L,
                new JoinLongFieldBo(286L, 100L, "id5_1", 440L, "id4_2"));

        JoinTransactionBo joinTransactionBo3 = joinTransactionBoList.get(2);
        assertTransaction(joinTransactionBo3, "id1", 1487149810000L,
                new JoinLongFieldBo(186L, 80L, "id5_1", 340L, "id4_2"));

        JoinTransactionBo joinTransactionBo4 = joinTransactionBoList.get(3);
        assertTransaction(joinTransactionBo4, "id1", 1487149815000L,
                new JoinLongFieldBo(386L, 150L, "id5_1", 490L, "id4_2"));

        JoinTransactionBo joinTransactionBo5 = joinTransactionBoList.get(4);
        assertTransaction(joinTransactionBo5, "id1", 1487149820000L,
                new JoinLongFieldBo(336L, 120L, "id5_1", 640L, "id4_2"));
    }

    private static void assertTransaction(JoinTransactionBo transaction,
                                          String id, long timestamp, JoinLongFieldBo field) {
        assertEquals(transaction.getId(), id);
        assertEquals(transaction.getTimestamp(), timestamp);
        assertEquals(transaction.getTotalCountJoinValue(), field);
    }


    @Test
    public void joinApplicationStatBoByTimeSlice6Test() {
        List<JoinTransactionBo> transaction1 = List.of(
                new JoinTransactionBo("agent1", 5000, 100, 60, "agent1", 200, "agent1", TIMESTAMP),
                new JoinTransactionBo("agent2", 5000, 100, 60, "agent2", 200, "agent2", TIMESTAMP1),
                new JoinTransactionBo("agent3", 5000, 100, 60, "agent3", 200, "agent3", TIMESTAMP2)
        );
        JoinApplicationStatBo join1 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addTransaction, transaction1);

        List<JoinTransactionBo> transaction2 = List.of(
                new JoinTransactionBo("agent1", 5000, 50, 20, "agent1", 230, "agent1", TIMESTAMP),
                new JoinTransactionBo("agent2", 5000, 200, 60, "agent2", 400, "agent2", TIMESTAMP1),
                new JoinTransactionBo("agent3", 5000, 500, 10, "agent3", 100, "agent3", TIMESTAMP2),
                new JoinTransactionBo("agent3", 5000, 400, 60, "agent3", 500, "agent3", TIMESTAMP4)
        );
        JoinApplicationStatBo join2 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addTransaction, transaction2);


        List<JoinTransactionBo> transaction3 = List.of(
                new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", TIMESTAMP),
                new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", TIMESTAMP1),
                new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", TIMESTAMP3)
        );
        JoinApplicationStatBo join3 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addTransaction, transaction3);

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(join1, join2, join3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), TIMESTAMP);
        List<JoinTransactionBo> joinTransactionBoList = joinApplicationStatBo.getJoinTransactionBoList();
        joinTransactionBoList.sort(Comparator.comparingLong(JoinTransactionBo::getTimestamp));

        assertThat(joinTransactionBoList)
                .hasSize(5)
                .map(JoinTransactionBo::getTotalCountJoinValue)
                .map(JoinLongFieldBo::getAvg)
                .containsExactly(100L, 200L, 300L, 400L, 30L);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice7Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(
                createActiveTrace("id1", currentTime, 10),
                createActiveTrace("id2", currentTime + 1000, -40),
                createActiveTrace("id3", currentTime + 2000, -30),
                createActiveTrace("id4", currentTime + 3000, 40),
                createActiveTrace("id5", currentTime + 4000, -50)
        );
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinActiveTraceBo> joinActiveTraceBoList = resultJoinApplicationStatBo.getJoinActiveTraceBoList();
        joinActiveTraceBoList.sort(Comparator.comparingLong(JoinActiveTraceBo::getTimestamp));
        assertJoinActiveTraceBoList(joinActiveTraceBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice8Test() {
        List<JoinActiveTraceBo> active1 = List.of(
                new JoinActiveTraceBo("agent1", 1, (short) 2, 100, 60, "agent1", 200, "agent1", TIMESTAMP),
                new JoinActiveTraceBo("agent2", 1, (short) 2, 100, 60, "agent1", 200, "agent1", TIMESTAMP1),
                new JoinActiveTraceBo("agent3", 1, (short) 2, 100, 60, "agent1", 200, "agent1", TIMESTAMP2)
        );

        JoinApplicationStatBo join1 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addActiveTrace, active1);

        List<JoinActiveTraceBo> active2 = List.of(
                new JoinActiveTraceBo("agent1", 1, (short) 2, 50, 20, "agent1", 230, "agent1", TIMESTAMP),
                new JoinActiveTraceBo("agent2", 1, (short) 2, 200, 60, "agent2", 400, "agent2", TIMESTAMP1),
                new JoinActiveTraceBo("agent3", 1, (short) 2, 500, 10, "agent3", 100, "agent3", TIMESTAMP2),
                new JoinActiveTraceBo("agent3", 1, (short) 2, 400, 60, "agent3", 500, "agent3", TIMESTAMP4)
        );
        JoinApplicationStatBo join2 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addActiveTrace, active2);

        List<JoinActiveTraceBo> active3 = List.of(
                new JoinActiveTraceBo("agent1", 1, (short) 2, 150, 20, "agent1", 230, "agent1", TIMESTAMP),
                new JoinActiveTraceBo("agent2", 1, (short) 2, 300, 10, "agent2", 400, "agent2", TIMESTAMP1),
                new JoinActiveTraceBo("agent3", 1, (short) 2, 30, 5, "agent3", 100, "agent3", TIMESTAMP3)
        );
        JoinApplicationStatBo join3 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addActiveTrace, active3);

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(join1, join2, join3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), TIMESTAMP);
        List<JoinActiveTraceBo> joinActiveTraceBoList = joinApplicationStatBo.getJoinActiveTraceBoList();
        joinActiveTraceBoList.sort(Comparator.comparingLong(JoinActiveTraceBo::getTimestamp));

        assertThat(joinActiveTraceBoList)
                .hasSize(5)
                .map(JoinActiveTraceBo::getTotalCountJoinValue)
                .map(JoinIntFieldBo::getAvg)
                .containsExactly(100, 200, 300, 400, 30);
    }


    private JoinApplicationStatBo createActiveTrace(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinActiveTraceBoList(id, timestamp, plus)
                .forEach(joinApplicationStatBo::addActiveTrace);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinActiveTraceBo> createJoinActiveTraceBoList(final String id, final long currentTime, int plus) {
        return List.of(
                new JoinActiveTraceBo(id, 1, (short) 2, 100 + plus, 60 + plus, id + "_1", 200 + plus, id + "_2", currentTime),
                new JoinActiveTraceBo(id, 1, (short) 2, 300 + plus, 150 + plus, id + "_1", 400 + plus, id + "_2", currentTime + 5000),
                new JoinActiveTraceBo(id, 1, (short) 2, 200 + plus, 130 + plus, id + "_1", 300 + plus, id + "_2", currentTime + 10000),
                new JoinActiveTraceBo(id, 1, (short) 2, 400 + plus, 200 + plus, id + "_1", 450 + plus, id + "_2", currentTime + 15000),
                new JoinActiveTraceBo(id, 1, (short) 2, 350 + plus, 170 + plus, id + "_1", 600 + plus, id + "_2", currentTime + 20000)
        );
    }

    private void assertJoinActiveTraceBoList(List<JoinActiveTraceBo> joinActiveTraceBoList) {
        assertThat(joinActiveTraceBoList).hasSize(5);

        JoinActiveTraceBo joinActiveTraceBo1 = joinActiveTraceBoList.get(0);
        assertActiveTrace(joinActiveTraceBo1, "id1", 1487149800000L, 1, 2,
                new JoinIntFieldBo(86, 10, "id5_1", 240, "id4_2"));

        JoinActiveTraceBo joinActiveTraceBo2 = joinActiveTraceBoList.get(1);
        assertActiveTrace(joinActiveTraceBo2, "id1", 1487149805000L, 1, 2,
                new JoinIntFieldBo(286, 100, "id5_1", 440, "id4_2"));

        JoinActiveTraceBo joinActiveTraceBo3 = joinActiveTraceBoList.get(2);
        assertActiveTrace(joinActiveTraceBo3, "id1", 1487149810000L, 1, 2,
                new JoinIntFieldBo(186, 80, "id5_1", 340, "id4_2"));

        JoinActiveTraceBo joinActiveTraceBo4 = joinActiveTraceBoList.get(3);
        assertActiveTrace(joinActiveTraceBo4, "id1", 1487149815000L, 1, 2,
                new JoinIntFieldBo(386, 150, "id5_1", 490, "id4_2"));

        JoinActiveTraceBo joinActiveTraceBo5 = joinActiveTraceBoList.get(4);
        assertActiveTrace(joinActiveTraceBo5, "id1", 1487149820000L, 1, 2,
                new JoinIntFieldBo(336, 120, "id5_1", 640, "id4_2"));
    }

    private void assertActiveTrace(JoinActiveTraceBo joinActiveTraceBo,
                                   String id, long timestamp, int histogramSchemaType, int version, JoinIntFieldBo intField) {
        assertEquals(joinActiveTraceBo.getId(), id);
        assertEquals(joinActiveTraceBo.getTimestamp(), timestamp);
        assertEquals(joinActiveTraceBo.getHistogramSchemaType(), histogramSchemaType);
        assertEquals(joinActiveTraceBo.getVersion(), version);
        assertEquals(joinActiveTraceBo.getTotalCountJoinValue(), intField);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice9Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(
                createResponseTime("id1", currentTime, 10),
                createResponseTime("id2", currentTime + 1000, -40),
                createResponseTime("id3", currentTime + 2000, -30),
                createResponseTime("id4", currentTime + 3000, 40),
                createResponseTime("id5", currentTime + 4000, -50)
        );

        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinResponseTimeBo> joinResponseTimeBoList = resultJoinApplicationStatBo.getJoinResponseTimeBoList();
        joinResponseTimeBoList.sort(Comparator.comparingLong(JoinResponseTimeBo::getTimestamp));

        assertJoinResponseTimeBoList(joinResponseTimeBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice10Test() {

        List<JoinResponseTimeBo> response1 = List.of(
                new JoinResponseTimeBo("agent1", TIMESTAMP, 100, 60, "agent1", 200, "agent1"),
                new JoinResponseTimeBo("agent1", TIMESTAMP1, 100, 60, "agent1", 200, "agent1"),
                new JoinResponseTimeBo("agent1", TIMESTAMP2, 100, 60, "agent1", 200, "agent1")
        );

        JoinApplicationStatBo join1 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addResponseTime, response1);

        List<JoinResponseTimeBo> response2 = List.of(
                new JoinResponseTimeBo("agent1", TIMESTAMP, 50, 20, "agent1", 230, "agent1"),
                new JoinResponseTimeBo("agent2", TIMESTAMP1, 200, 60, "agent2", 400, "agent2"),
                new JoinResponseTimeBo("agent3", TIMESTAMP2, 500, 10, "agent3", 100, "agent3"),
                new JoinResponseTimeBo("agent3", TIMESTAMP4, 400, 60, "agent3", 500, "agent3")
        );

        JoinApplicationStatBo join2 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addResponseTime, response2);

        List<JoinResponseTimeBo> response3 = List.of(
                new JoinResponseTimeBo("agent1", TIMESTAMP, 150, 20, "agent1", 230, "agent1"),
                new JoinResponseTimeBo("agent2", TIMESTAMP1, 300, 10, "agent2", 400, "agent2"),
                new JoinResponseTimeBo("agent3", TIMESTAMP3, 30, 5, "agent3", 100, "agent3")
        );
        JoinApplicationStatBo join3 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addResponseTime, response3);

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(join1, join2, join3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), TIMESTAMP);
        List<JoinResponseTimeBo> joinResponseTimeBoList = joinApplicationStatBo.getJoinResponseTimeBoList();
        joinResponseTimeBoList.sort(Comparator.comparingLong(JoinResponseTimeBo::getTimestamp));

        assertThat(joinResponseTimeBoList)
                .hasSize(5)
                .map(response -> response.getResponseTimeJoinValue().getAvg())
                .containsExactly(100L, 200L, 300L, 400L, 30L);
    }

    private void assertJoinResponseTimeBoList(List<JoinResponseTimeBo> joinResponseTimeBoList) {
        assertThat(joinResponseTimeBoList).hasSize(5);

        JoinResponseTimeBo joinResponseTimeBo1 = joinResponseTimeBoList.get(0);
        assertEquals(joinResponseTimeBo1.getId(), "id1");
        assertEquals(joinResponseTimeBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinResponseTimeBo1.getResponseTimeJoinValue(), new JoinLongFieldBo(286L, 150L, "id5_1", 6040L, "id4_2"));

        JoinResponseTimeBo joinResponseTimeBo2 = joinResponseTimeBoList.get(1);
        assertEquals(joinResponseTimeBo2.getId(), "id1");
        assertEquals(joinResponseTimeBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinResponseTimeBo2.getResponseTimeJoinValue(), new JoinLongFieldBo(186L, 0L, "id5_1", 7040L, "id4_2"));

        JoinResponseTimeBo joinResponseTimeBo3 = joinResponseTimeBoList.get(2);
        assertEquals(joinResponseTimeBo3.getId(), "id1");
        assertEquals(joinResponseTimeBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinResponseTimeBo3.getResponseTimeJoinValue(), new JoinLongFieldBo(386L, 250L, "id5_1", 8040L, "id4_2"));

        JoinResponseTimeBo joinResponseTimeBo4 = joinResponseTimeBoList.get(3);
        assertEquals(joinResponseTimeBo4.getId(), "id1");
        assertEquals(joinResponseTimeBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinResponseTimeBo4.getResponseTimeJoinValue(), new JoinLongFieldBo(486L, 350L, "id5_1", 2040L, "id4_2"));

        JoinResponseTimeBo joinResponseTimeBo5 = joinResponseTimeBoList.get(4);
        assertEquals(joinResponseTimeBo5.getId(), "id1");
        assertEquals(joinResponseTimeBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinResponseTimeBo5.getResponseTimeJoinValue(), new JoinLongFieldBo(86L, 50L, "id5_1", 9040L, "id4_2"));
    }


    private JoinApplicationStatBo createResponseTime(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinResponseTimeList(id, timestamp, plus).forEach(joinApplicationStatBo::addResponseTime);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinResponseTimeBo> createJoinResponseTimeList(String id, long currentTime, int plus) {
        return List.of(
                new JoinResponseTimeBo(id, currentTime, 300 + plus, 200 + plus, id + "_1", 6000 + plus, id + "_2"),
                new JoinResponseTimeBo(id, currentTime + 5000, 200 + plus, 50 + plus, id + "_1", 7000 + plus, id + "_2"),
                new JoinResponseTimeBo(id, currentTime + 10000, 400 + plus, 300 + plus, id + "_1", 8000 + plus, id + "_2"),
                new JoinResponseTimeBo(id, currentTime + 15000, 500 + plus, 400 + plus, id + "_1", 2000 + plus, id + "_2"),
                new JoinResponseTimeBo(id, currentTime + 20000, 100 + plus, 100 + plus, id + "_1", 9000 + plus, id + "_2")
        );
    }

    @Test
    public void joinApplicationStatBoByTimeSlice11Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(
                createDataSource("id1", currentTime, 10),
                createDataSource("id2", currentTime + 1000, -40),
                createDataSource("id3", currentTime + 2000, -30),
                createDataSource("id4", currentTime + 3000, 40),
                createDataSource("id5", currentTime + 4000, -50)
        );

        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinDataSourceListBo> joinDataSourceListBoList = resultJoinApplicationStatBo.getJoinDataSourceListBoList();
        joinDataSourceListBoList.sort(Comparator.comparingLong(JoinDataSourceListBo::getTimestamp));

        assertJoinDataSourceListBoList(joinDataSourceListBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice12Test() {

        List<JoinDataSourceBo> joinDataSourceBoList1 = List.of(new JoinDataSourceBo((short) 1000, "jdbc:mysql", 100, 60, "agent1", 200, "agent1"));

        List<JoinDataSourceListBo> datasource1 = List.of(
                new JoinDataSourceListBo("agent1", joinDataSourceBoList1, TIMESTAMP),
                new JoinDataSourceListBo("agent1", joinDataSourceBoList1, TIMESTAMP1),
                new JoinDataSourceListBo("agent1", joinDataSourceBoList1, TIMESTAMP2)
        );
        JoinApplicationStatBo join1 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addDataSourceList, datasource1);


        List<JoinDataSourceBo> joinDataSourceBoList2_1 = List.of(new JoinDataSourceBo((short) 1000, "jdbc:mysql", 50, 20, "agent1", 230, "agent1"));
        JoinDataSourceListBo joinResponseTimeBo2_1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2_1, TIMESTAMP);

        List<JoinDataSourceBo> joinDataSourceBoList2_2 = List.of(new JoinDataSourceBo((short) 1000, "jdbc:mysql", 200, 60, "agent2", 400, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo2_2 = new JoinDataSourceListBo("agent2", joinDataSourceBoList2_2, TIMESTAMP1);

        List<JoinDataSourceBo> joinDataSourceBoList2_3 = List.of(new JoinDataSourceBo((short) 1000, "jdbc:mysql", 500, 10, "agent3", 100, "agent3"));
        JoinDataSourceListBo joinResponseTimeBo2_3 = new JoinDataSourceListBo("agent3", joinDataSourceBoList2_3, TIMESTAMP2);

        List<JoinDataSourceBo> joinDataSourceBoList2_4 = List.of(new JoinDataSourceBo((short) 1000, "jdbc:mysql", 400, 60, "agent3", 500, "agent3"));
        JoinDataSourceListBo joinResponseTimeBo2_4 = new JoinDataSourceListBo("agent3", joinDataSourceBoList2_4, TIMESTAMP4);

        JoinApplicationStatBo join2 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addDataSourceList,
                List.of(joinResponseTimeBo2_1, joinResponseTimeBo2_2, joinResponseTimeBo2_3, joinResponseTimeBo2_4));


        List<JoinDataSourceBo> joinDataSourceBoList3_1 = List.of(new JoinDataSourceBo((short) 1000, "jdbc:mysql", 150, 20, "agent1", 230, "agent1"));
        JoinDataSourceListBo joinResponseTimeBo3_1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3_1, TIMESTAMP);
        List<JoinDataSourceBo> joinDataSourceBoList3_2 = List.of(new JoinDataSourceBo((short) 1000, "jdbc:mysql", 300, 10, "agent2", 400, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo3_2 = new JoinDataSourceListBo("agent2", joinDataSourceBoList3_2, TIMESTAMP1);
        List<JoinDataSourceBo> joinDataSourceBoList3_3 = List.of(new JoinDataSourceBo((short) 1000, "jdbc:mysql", 30, 5, "agent2", 100, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo3_3 = new JoinDataSourceListBo("agent3", joinDataSourceBoList3_3, TIMESTAMP3);

        JoinApplicationStatBo join3 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addDataSourceList,
                List.of(joinResponseTimeBo3_1, joinResponseTimeBo3_2, joinResponseTimeBo3_3));

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(join1, join2, join3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), TIMESTAMP);
        List<JoinDataSourceListBo> joinDataSourceListBoList = joinApplicationStatBo.getJoinDataSourceListBoList();
        joinDataSourceListBoList.sort(Comparator.comparingLong(JoinDataSourceListBo::getTimestamp));

        assertThat(joinDataSourceListBoList)
                .hasSize(5)
                .flatMap(JoinDataSourceListBo::getJoinDataSourceBoList)
                .map(JoinDataSourceBo::getActiveConnectionSizeJoinValue)
                .map(JoinIntFieldBo::getAvg)
                .containsExactly(100, 200, 300, 400, 30);
    }

    private void assertJoinDataSourceListBoList(List<JoinDataSourceListBo> joinDataSourceListBoList) {
        assertThat(joinDataSourceListBoList).hasSize(5);

        JoinDataSourceListBo joinDataSourceListBo1 = joinDataSourceListBoList.get(0);
        assertEquals(joinDataSourceListBo1.getId(), "id1");
        assertEquals(joinDataSourceListBo1.getTimestamp(), 1487149800000L);
        List<JoinDataSourceBo> joinDataSourceBoList1 = joinDataSourceListBo1.getJoinDataSourceBoList();
        assertThat(joinDataSourceBoList1).hasSize(2);
        JoinDataSourceBo joinDataSourceBo1_1 = joinDataSourceBoList1.get(0);
        assertEquals(joinDataSourceBo1_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo1_1.getUrl(), "jdbc:mysql");
        assertEquals(new JoinIntFieldBo(286, 200, "agent_id_1_-50", 640, "agent_id_6_40"), joinDataSourceBo1_1.getActiveConnectionSizeJoinValue());

        JoinDataSourceBo joinDataSourceBo1_2 = joinDataSourceBoList1.get(1);
        assertEquals(joinDataSourceBo1_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo1_2.getUrl(), "jdbc:mssql");
        assertEquals(new JoinIntFieldBo(386, 300, "agent_id_1_-50", 740, "agent_id_6_40"), joinDataSourceBo1_2.getActiveConnectionSizeJoinValue());

        JoinDataSourceListBo joinDataSourceListBo2 = joinDataSourceListBoList.get(1);
        assertEquals(joinDataSourceListBo2.getId(), "id1");
        assertEquals(joinDataSourceListBo2.getTimestamp(), 1487149805000L);
        List<JoinDataSourceBo> joinDataSourceBoList2 = joinDataSourceListBo2.getJoinDataSourceBoList();
        assertThat(joinDataSourceBoList2).hasSize(2);
        JoinDataSourceBo joinDataSourceBo2_1 = joinDataSourceBoList2.get(0);
        assertEquals(joinDataSourceBo2_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo2_1.getUrl(), "jdbc:mysql");
        assertEquals(new JoinIntFieldBo(186, 0, "agent_id_2_-50", 740, "agent_id_7_40"), joinDataSourceBo2_1.getActiveConnectionSizeJoinValue());

        JoinDataSourceBo joinDataSourceBo2_2 = joinDataSourceBoList2.get(1);
        assertEquals(joinDataSourceBo2_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo2_2.getUrl(), "jdbc:mssql");
        assertEquals(new JoinIntFieldBo(286, 100, "agent_id_2_-50", 840, "agent_id_7_40"), joinDataSourceBo2_2.getActiveConnectionSizeJoinValue());

        JoinDataSourceListBo joinDataSourceListBo3 = joinDataSourceListBoList.get(2);
        assertEquals(joinDataSourceListBo3.getId(), "id1");
        assertEquals(joinDataSourceListBo3.getTimestamp(), 1487149810000L);
        List<JoinDataSourceBo> joinDataSourceBoList3 = joinDataSourceListBo3.getJoinDataSourceBoList();
        assertThat(joinDataSourceBoList3).hasSize(2);
        JoinDataSourceBo joinDataSourceBo3_1 = joinDataSourceBoList3.get(0);
        assertEquals(joinDataSourceBo3_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo3_1.getUrl(), "jdbc:mysql");
        assertEquals(new JoinIntFieldBo(486, 100, "agent_id_3_-50", 940, "agent_id_8_40"), joinDataSourceBo3_1.getActiveConnectionSizeJoinValue());

        JoinDataSourceBo joinDataSourceBo3_2 = joinDataSourceBoList3.get(1);
        assertEquals(joinDataSourceBo3_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo3_2.getUrl(), "jdbc:mssql");
        assertEquals(new JoinIntFieldBo(586, 200, "agent_id_3_-50", 1040, "agent_id_8_40"), joinDataSourceBo3_2.getActiveConnectionSizeJoinValue());

        JoinDataSourceListBo joinDataSourceListBo4 = joinDataSourceListBoList.get(3);
        assertEquals(joinDataSourceListBo4.getId(), "id1");
        assertEquals(joinDataSourceListBo4.getTimestamp(), 1487149815000L);
        List<JoinDataSourceBo> joinDataSourceBoList4 = joinDataSourceListBo4.getJoinDataSourceBoList();
        assertThat(joinDataSourceBoList4).hasSize(2);
        JoinDataSourceBo joinDataSourceBo4_1 = joinDataSourceBoList4.get(0);
        assertEquals(joinDataSourceBo4_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo4_1.getUrl(), "jdbc:mysql");
        assertEquals(new JoinIntFieldBo(386, 500, "agent_id_4_-50", 640, "agent_id_9_40"), joinDataSourceBo4_1.getActiveConnectionSizeJoinValue());

        JoinDataSourceBo joinDataSourceBo4_2 = joinDataSourceBoList4.get(1);
        assertEquals(joinDataSourceBo4_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo4_2.getUrl(), "jdbc:mssql");
        assertEquals(new JoinIntFieldBo(486, 600, "agent_id_4_-50", 740, "agent_id_9_40"), joinDataSourceBo4_2.getActiveConnectionSizeJoinValue());

        JoinDataSourceListBo joinDataSourceListBo5 = joinDataSourceListBoList.get(4);
        assertEquals(joinDataSourceListBo5.getId(), "id1");
        assertEquals(joinDataSourceListBo5.getTimestamp(), 1487149820000L);
        List<JoinDataSourceBo> joinDataSourceBoList5 = joinDataSourceListBo5.getJoinDataSourceBoList();
        assertThat(joinDataSourceBoList5).hasSize(2);
        JoinDataSourceBo joinDataSourceBo5_1 = joinDataSourceBoList5.get(0);
        assertEquals(joinDataSourceBo5_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo5_1.getUrl(), "jdbc:mysql");
        assertEquals(new JoinIntFieldBo(86, 700, "agent_id_5_-50", 840, "agent_id_10_40"), joinDataSourceBo5_1.getActiveConnectionSizeJoinValue());

        JoinDataSourceBo joinDataSourceBo5_2 = joinDataSourceBoList5.get(1);
        assertEquals(joinDataSourceBo5_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo5_2.getUrl(), "jdbc:mssql");
        assertEquals(new JoinIntFieldBo(186, 800, "agent_id_5_-50", 940, "agent_id_10_40"), joinDataSourceBo5_2.getActiveConnectionSizeJoinValue());
    }


    private JoinApplicationStatBo createDataSource(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinDataSourceListBoList(id, timestamp, plus)
                .forEach(joinApplicationStatBo::addDataSourceList);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinDataSourceListBo> createJoinDataSourceListBoList(String id, long currentTime, int plus) {

        List<JoinDataSourceBo> joinDataSourceBoList1 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 300 + plus, 250 + plus, "agent_id_1_" + plus, 600 + plus, "agent_id_6_" + plus),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 400 + plus, 350 + plus, "agent_id_1_" + plus, 700 + plus, "agent_id_6_" + plus)
        );
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo(id, joinDataSourceBoList1, currentTime);

        List<JoinDataSourceBo> joinDataSourceBoList2 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 200 + plus, 50 + plus, "agent_id_2_" + plus, 700 + plus, "agent_id_7_" + plus),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 300 + plus, 150 + plus, "agent_id_2_" + plus, 800 + plus, "agent_id_7_" + plus)
        );
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo(id, joinDataSourceBoList2, currentTime + 5000);

        List<JoinDataSourceBo> joinDataSourceBoList3 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 500 + plus, 150 + plus, "agent_id_3_" + plus, 900 + plus, "agent_id_8_" + plus),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 600 + plus, 250 + plus, "agent_id_3_" + plus, 1000 + plus, "agent_id_8_" + plus)
        );
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo(id, joinDataSourceBoList3, currentTime + 10000);

        List<JoinDataSourceBo> joinDataSourceBoList4 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 400 + plus, 550 + plus, "agent_id_4_" + plus, 600 + plus, "agent_id_9_" + plus),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 500 + plus, 650 + plus, "agent_id_4_" + plus, 700 + plus, "agent_id_9_" + plus)
        );
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo(id, joinDataSourceBoList4, currentTime + 15000);

        List<JoinDataSourceBo> joinDataSourceBoList5 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 100 + plus, 750 + plus, "agent_id_5_" + plus, 800 + plus, "agent_id_10_" + plus),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 200 + plus, 850 + plus, "agent_id_5_" + plus, 900 + plus, "agent_id_10_" + plus)
        );
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo(id, joinDataSourceBoList5, currentTime + 20000);

        return List.of(joinDataSourceListBo1, joinDataSourceListBo2, joinDataSourceListBo3, joinDataSourceListBo4, joinDataSourceListBo5);
    }


    @Test
    public void joinApplicationStatBoByTimeSlice13Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(
                createJoinApplicationStatBo7("id1", currentTime, 10),
                createJoinApplicationStatBo7("id2", currentTime + 1000, -40),
                createJoinApplicationStatBo7("id3", currentTime + 2000, -30),
                createJoinApplicationStatBo7("id4", currentTime + 3000, 40),
                createJoinApplicationStatBo7("id5", currentTime + 4000, -50)
        );

        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinFileDescriptorBo> joinFileDescriptorBoList = resultJoinApplicationStatBo.getJoinFileDescriptorBoList();
        joinFileDescriptorBoList.sort(Comparator.comparingLong(JoinFileDescriptorBo::getTimestamp));
        assertJoinFileDescriptorBoList(joinFileDescriptorBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice14Test() {
        List<JoinFileDescriptorBo> file1 = List.of(
                new JoinFileDescriptorBo("agent1", 440, 700, "agent1", 300, "agent1", TIMESTAMP),
                new JoinFileDescriptorBo("agent1", 330, 400, "agent1", 100, "agent1", TIMESTAMP1),
                new JoinFileDescriptorBo("agent1", 550, 600, "agent1", 70, "agent1", TIMESTAMP2)
        );
        JoinApplicationStatBo join1 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addFileDescriptor, file1);


        List<JoinFileDescriptorBo> file2 = List.of(
                new JoinFileDescriptorBo("agent1", 330, 700, "agent1", 300, "agent1", TIMESTAMP),
                new JoinFileDescriptorBo("agent1", 220, 400, "agent1", 100, "agent1", TIMESTAMP1),
                new JoinFileDescriptorBo("agent1", 110, 600, "agent1", 70, "agent1", TIMESTAMP2),
                new JoinFileDescriptorBo("agent1", 770, 600, "agent1", 70, "agent1", TIMESTAMP4)
        );
        JoinApplicationStatBo join2 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addFileDescriptor, file2);


        List<JoinFileDescriptorBo> file3 = List.of(
                new JoinFileDescriptorBo("agent1", 220, 700, "agent1", 300, "agent1", TIMESTAMP),
                new JoinFileDescriptorBo("agent1", 110, 400, "agent1", 100, "agent1", TIMESTAMP1),
                new JoinFileDescriptorBo("agent1", 880, 600, "agent1", 70, "agent1", TIMESTAMP3)
        );
        JoinApplicationStatBo join3 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addFileDescriptor, file3);

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(join1, join2, join3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), TIMESTAMP);
        List<JoinFileDescriptorBo> joinFileDescriptorBoList = joinApplicationStatBo.getJoinFileDescriptorBoList();
        joinFileDescriptorBoList.sort(Comparator.comparingLong(JoinFileDescriptorBo::getTimestamp));

        assertThat(joinFileDescriptorBoList)
                .hasSize(5)
                .map(JoinFileDescriptorBo::getOpenFdCountJoinValue)
                .map(JoinLongFieldBo::getAvg)
                .containsExactly(330L, 220L, 330L, 770L, 880L);
    }


    private void assertJoinFileDescriptorBoList(List<JoinFileDescriptorBo> joinFileDescriptorBoList) {
        assertThat(joinFileDescriptorBoList).hasSize(5);

        JoinFileDescriptorBo joinFileDescriptorBo1 = joinFileDescriptorBoList.get(0);
        assertFile(joinFileDescriptorBo1, "id1", 1487149800000L,
                new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"));

        JoinFileDescriptorBo joinFileDescriptorBo2 = joinFileDescriptorBoList.get(1);
        assertFile(joinFileDescriptorBo2, "id1", 1487149805000L,
                new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"));

        JoinFileDescriptorBo joinFileDescriptorBo3 = joinFileDescriptorBoList.get(2);
        assertFile(joinFileDescriptorBo3, "id1", 1487149810000L,
                new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"));

        JoinFileDescriptorBo joinFileDescriptorBo4 = joinFileDescriptorBoList.get(3);
        assertFile(joinFileDescriptorBo4, "id1", 1487149815000L,
                new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"));

        JoinFileDescriptorBo joinFileDescriptorBo5 = joinFileDescriptorBoList.get(4);
        assertFile(joinFileDescriptorBo5, "id1", 1487149820000L,
                new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"));
    }

    private static void assertFile(JoinFileDescriptorBo file, String id, long timestamp, JoinLongFieldBo longField) {
        assertEquals(file.getId(), id);
        assertEquals(file.getTimestamp(), timestamp);
        assertEquals(file.getOpenFdCountJoinValue(), longField);
    }

    private JoinApplicationStatBo createJoinApplicationStatBo7(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinFileDescriptorBoList(id, timestamp, plus)
                .forEach(joinApplicationStatBo::addFileDescriptor);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinFileDescriptorBo> createJoinFileDescriptorBoList(final String id, final long currentTime, int plus) {
        return List.of(
                new JoinFileDescriptorBo(id, 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", currentTime),
                new JoinFileDescriptorBo(id, 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", currentTime + 5000),
                new JoinFileDescriptorBo(id, 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", currentTime + 10000),
                new JoinFileDescriptorBo(id, 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", currentTime + 15000),
                new JoinFileDescriptorBo(id, 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", currentTime + 20000)
        );
    }

    @Test
    public void joinApplicationStatBoByTimeSlice15Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(
                createDirectBuffer("id1", currentTime, 10),
                createDirectBuffer("id2", currentTime + 1000, -40),
                createDirectBuffer("id3", currentTime + 2000, -30),
                createDirectBuffer("id4", currentTime + 3000, 40),
                createDirectBuffer("id5", currentTime + 4000, -50)
        );
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinDirectBufferBo> joinDirectBufferBoList = resultJoinApplicationStatBo.getJoinDirectBufferBoList();
        joinDirectBufferBoList.sort(Comparator.comparingLong(JoinDirectBufferBo::getTimestamp));
        assertJoinDirectBufferBoList(joinDirectBufferBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice16Test() {
        List<JoinDirectBufferBo> buffer1 = List.of(
                new JoinDirectBufferBo("agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", TIMESTAMP),
                new JoinDirectBufferBo("agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", TIMESTAMP1),
                new JoinDirectBufferBo("agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", TIMESTAMP2)
        );
        JoinApplicationStatBo join1 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addDirectBuffer, buffer1);

        List<JoinDirectBufferBo> buffer2 = List.of(
                new JoinDirectBufferBo("agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", TIMESTAMP),
                new JoinDirectBufferBo("agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", TIMESTAMP1),
                new JoinDirectBufferBo("agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", TIMESTAMP2),
                new JoinDirectBufferBo("agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", TIMESTAMP4)
        );
        JoinApplicationStatBo join2 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addDirectBuffer, buffer2);

        List<JoinDirectBufferBo> buffer3 = List.of(
                new JoinDirectBufferBo("agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", TIMESTAMP),
                new JoinDirectBufferBo("agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", TIMESTAMP1),
                new JoinDirectBufferBo("agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", TIMESTAMP3)
        );
        JoinApplicationStatBo join3 = buildApplicationStat(APPLICATION_ID, TIMESTAMP,
                JoinApplicationStatBo.Builder::addDirectBuffer, buffer3);

        List<JoinApplicationStatBo> joinApplicationStatBoList = List.of(join1, join2, join3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), TIMESTAMP);
        List<JoinDirectBufferBo> joinDirectBufferBoList = joinApplicationStatBo.getJoinDirectBufferBoList();
        joinDirectBufferBoList.sort(Comparator.comparingLong(JoinDirectBufferBo::getTimestamp));

        assertThat(joinDirectBufferBoList).hasSize(5);

        assertThat(joinDirectBufferBoList)
                .map(JoinDirectBufferBo::getDirectCountJoinValue)
                .map(AbstractJoinFieldBo::getAvg)
                .containsExactly(330L, 220L, 330L, 770L, 880L);

        assertThat(joinDirectBufferBoList)
                .map(JoinDirectBufferBo::getDirectMemoryUsedJoinValue)
                .map(AbstractJoinFieldBo::getAvg)
                .containsExactly(330L, 220L, 330L, 770L, 880L);

        assertThat(joinDirectBufferBoList)
                .map(JoinDirectBufferBo::getMappedCountJoinValue)
                .map(AbstractJoinFieldBo::getAvg)
                .containsExactly(330L, 220L, 330L, 770L, 880L);

        assertThat(joinDirectBufferBoList)
                .map(JoinDirectBufferBo::getMappedMemoryUsedJoinValue)
                .map(AbstractJoinFieldBo::getAvg)
                .containsExactly(330L, 220L, 330L, 770L, 880L);

    }


    private void assertJoinDirectBufferBoList(List<JoinDirectBufferBo> joinDirectBufferBoList) {
        assertThat(joinDirectBufferBoList).hasSize(5);
        //1
        JoinDirectBufferBo joinDirectBufferBo1 = joinDirectBufferBoList.get(0);
        assertMemory(joinDirectBufferBo1, 1487149800000L,
                new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"),
                new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"),
                new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"),
                new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"));

        //2
        JoinDirectBufferBo joinDirectBufferBo2 = joinDirectBufferBoList.get(1);
        assertMemory(joinDirectBufferBo2, 1487149805000L,
                new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"),
                new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"),
                new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"),
                new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"));

        //3
        JoinDirectBufferBo joinDirectBufferBo3 = joinDirectBufferBoList.get(2);
        assertMemory(joinDirectBufferBo3, 1487149810000L,
                new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"),
                new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"),
                new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"),
                new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"));

        //4
        JoinDirectBufferBo joinDirectBufferBo4 = joinDirectBufferBoList.get(3);
        assertMemory(joinDirectBufferBo4, 1487149815000L,
                new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"),
                new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"),
                new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"),
                new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"));

        //5
        JoinDirectBufferBo joinDirectBufferBo5 = joinDirectBufferBoList.get(4);
        assertMemory(joinDirectBufferBo5, 1487149820000L,
                new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"),
                new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"),
                new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"),
                new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"));
    }

    private void assertMemory(JoinDirectBufferBo memory, long timestamp, JoinLongFieldBo directCount, JoinLongFieldBo directUsed, JoinLongFieldBo mappedCount, JoinLongFieldBo mappedUsed) {
        assertEquals(memory.getId(), "id1");
        assertEquals(memory.getTimestamp(), timestamp);
        assertEquals(memory.getDirectCountJoinValue(), directCount);
        assertEquals(memory.getDirectMemoryUsedJoinValue(), directUsed);
        assertEquals(memory.getMappedCountJoinValue(), mappedCount);
        assertEquals(memory.getMappedMemoryUsedJoinValue(), mappedUsed);
    }

    private JoinApplicationStatBo createDirectBuffer(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        createJoinDirectBufferBoList(id, timestamp, plus)
                .forEach(joinApplicationStatBo::addDirectBuffer);
        return joinApplicationStatBo.build();
    }

    private List<JoinDirectBufferBo> createJoinDirectBufferBoList(final String id, final long currentTime, int plus) {
        return List.of(
                new JoinDirectBufferBo(id, 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", currentTime),
                new JoinDirectBufferBo(id, 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", currentTime + 5000),
                new JoinDirectBufferBo(id, 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", currentTime + 10000),
                new JoinDirectBufferBo(id, 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", currentTime + 15000),
                new JoinDirectBufferBo(id, 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", currentTime + 20000)
        );
    }

    @Test
    public void createJoinApplicationStatBoTest() {

        JoinAgentStatBo.Builder builder = JoinAgentStatBo.newBuilder("Agent", Long.MIN_VALUE, TIMESTAMP3);
        List.of(
                new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", TIMESTAMP3),
                new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462570000L),
                new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462575000L),
                new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", 1498462580000L),
                new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", 1498462585000L)
        ).forEach(builder::addCpuLoadBo);

        List.of(
                new JoinMemoryBo("agent1", TIMESTAMP3, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent1", 1498462570000L, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent1", 1498462575000L, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1"),
                new JoinMemoryBo("agent1", 1498462580000L, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1"),
                new JoinMemoryBo("agent1", 1498462585000L, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1")
        ).forEach(builder::addMemory);

        List.of(
                new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", TIMESTAMP3),
                new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", 1498462570000L),
                new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462575000L),
                new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", 1498462580000L),
                new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", 1498462585000L)
        ).forEach(builder::addTransaction);

        List.of(
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP3),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", 1498462570000L),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", 1498462575000L),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", 1498462580000L),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", 1498462585000L)
        ).forEach(builder::addActiveTrace);

        List.of(
                new JoinResponseTimeBo("agent1", TIMESTAMP3, 3000, 2, "app_1_1", 6000, "app_1_2"),
                new JoinResponseTimeBo("agent1", 1498462570000L, 4000, 200, "app_2_1", 9000, "app_2_2"),
                new JoinResponseTimeBo("agent1", 1498462575000L, 2000, 20, "app_3_1", 7000, "app_3_2"),
                new JoinResponseTimeBo("agent1", 1498462580000L, 5000, 20, "app_4_1", 8000, "app_4_2"),
                new JoinResponseTimeBo("agent1", 1498462585000L, 1000, 10, "app_5_1", 6600, "app_5_2")
        ).forEach(builder::addResponseTime);

        List<JoinDataSourceBo> joinDataSourceBoList1 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6")
        );
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, TIMESTAMP3);

        List<JoinDataSourceBo> joinDataSourceBoList2 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7")
        );
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, 1498462570000L);

        List<JoinDataSourceBo> joinDataSourceBoList3 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8")
        );
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, 1498462575000L);

        List<JoinDataSourceBo> joinDataSourceBoList4 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9")
        );
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, 1498462580000L);

        List<JoinDataSourceBo> joinDataSourceBoList5 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10")
        );
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, 1498462585000L);

        List.of(joinDataSourceListBo1, joinDataSourceListBo2, joinDataSourceListBo3, joinDataSourceListBo4, joinDataSourceListBo5)
                .forEach(builder::addDataSourceListBo);

        List.of(
                new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", TIMESTAMP3),
                new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", 1498462570000L),
                new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", 1498462575000L),
                new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", 1498462580000L),
                new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", 1498462585000L)
        ).forEach(builder::addFileDescriptor);

        List.of(
                new JoinDirectBufferBo("agent1", 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , TIMESTAMP3),
                new JoinDirectBufferBo("agent2", 33, 40, "agent2", 10, "agent2"
                        , 33, 40, "agent2", 10, "agent2"
                        , 33, 40, "agent2", 10, "agent2"
                        , 33, 40, "agent2", 10, "agent2"
                        , 1498462570000L),
                new JoinDirectBufferBo("agent3", 55, 60, "agent3", 7, "agent3"
                        , 55, 60, "agent3", 7, "agent3"
                        , 55, 60, "agent3", 7, "agent3"
                        , 55, 60, "agent3", 7, "agent3"
                        , 1498462575000L),
                new JoinDirectBufferBo("agent4", 11, 80, "agent4", 8, "agent4"
                        , 11, 80, "agent4", 8, "agent4"
                        , 11, 80, "agent4", 8, "agent4"
                        , 11, 80, "agent4", 8, "agent4"
                        , 1498462580000L),
                new JoinDirectBufferBo("agent5", 22, 70, "agent5", 12, "agent5"
                        , 22, 70, "agent5", 12, "agent5"
                        , 22, 70, "agent5", 12, "agent5"
                        , 22, 70, "agent5", 12, "agent5"
                        , 1498462585000L)
        ).forEach(builder::addDirectBuffer);

        JoinAgentStatBo agentStatBo = builder.build();
        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo(APPLICATION_ID, agentStatBo, 60000);
        assertThat(joinApplicationStatBoList).hasSize(1);
        JoinApplicationStatBo joinApplicationStatBo = joinApplicationStatBoList.get(0);
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);

        assertHasSize(joinApplicationStatBo, 5);
    }

    @Test
    public void createJoinApplicationStatBo2Test() {
        JoinAgentStatBo.Builder builder = JoinAgentStatBo.newBuilder("Agent", Long.MIN_VALUE, TIMESTAMP);

        List.of(
                new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", TIMESTAMP),
                new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", TIMESTAMP1),
                new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", TIMESTAMP2),
                new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", TIMESTAMP4),
                new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", TIMESTAMP3)
        ).forEach(builder::addCpuLoadBo);

        List.of(
                new JoinMemoryBo("agent1", TIMESTAMP, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent1", TIMESTAMP1, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent1", TIMESTAMP2, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1"),
                new JoinMemoryBo("agent1", TIMESTAMP4, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1"),
                new JoinMemoryBo("agent1", TIMESTAMP3, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1")
        ).forEach(builder::addMemory);

        List.of(
                new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", TIMESTAMP),
                new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent1", TIMESTAMP1),
                new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", TIMESTAMP2),
                new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", TIMESTAMP4),
                new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", TIMESTAMP3)
        ).forEach(builder::addTransaction);

        List.of(
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP1),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP2),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP4),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP3)
        ).forEach(builder::addActiveTrace);

        List.of(
                new JoinResponseTimeBo("agent1", TIMESTAMP, 3000, 2, "app_1_1", 6000, "app_1_2"),
                new JoinResponseTimeBo("agent1", TIMESTAMP1, 4000, 200, "app_2_1", 9000, "app_2_2"),
                new JoinResponseTimeBo("agent1", TIMESTAMP2, 2000, 20, "app_3_1", 7000, "app_3_2"),
                new JoinResponseTimeBo("agent1", TIMESTAMP4, 5000, 20, "app_4_1", 8000, "app_4_2"),
                new JoinResponseTimeBo("agent1", TIMESTAMP3, 1000, 10, "app_5_1", 6600, "app_5_2")
        ).forEach(builder::addResponseTime);


        List<JoinDataSourceBo> joinDataSourceBoList1 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6")
        );
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, TIMESTAMP);

        List<JoinDataSourceBo> joinDataSourceBoList2 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7")
        );
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, TIMESTAMP1);

        List<JoinDataSourceBo> joinDataSourceBoList3 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8")
        );
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, TIMESTAMP2);

        List<JoinDataSourceBo> joinDataSourceBoList4 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9")
        );
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, TIMESTAMP4);

        List<JoinDataSourceBo> joinDataSourceBoList5 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10")
        );
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, TIMESTAMP3);

        List.of(joinDataSourceListBo1, joinDataSourceListBo2, joinDataSourceListBo3, joinDataSourceListBo4, joinDataSourceListBo5)
                .forEach(builder::addDataSourceListBo);

        List.of(
                new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", TIMESTAMP),
                new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", TIMESTAMP1),
                new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", TIMESTAMP2),
                new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", TIMESTAMP4),
                new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", TIMESTAMP3)
        ).forEach(builder::addFileDescriptor);

        List.of(
                new JoinDirectBufferBo("agent1", 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , TIMESTAMP),
                new JoinDirectBufferBo("agent1", 33, 40, "agent1", 10, "agent1"
                        , 33, 40, "agent1", 10, "agent1"
                        , 33, 40, "agent1", 10, "agent1"
                        , 33, 40, "agent1", 10, "agent1"
                        , TIMESTAMP1),
                new JoinDirectBufferBo("agent1", 55, 60, "agent1", 7, "agent1"
                        , 55, 60, "agent1", 7, "agent1"
                        , 55, 60, "agent1", 7, "agent1"
                        , 55, 60, "agent1", 7, "agent1"
                        , TIMESTAMP2),
                new JoinDirectBufferBo("agent1", 11, 80, "agent1", 8, "agent1"
                        , 11, 80, "agent1", 8, "agent1"
                        , 11, 80, "agent1", 8, "agent1"
                        , 11, 80, "agent1", 8, "agent1"
                        , TIMESTAMP4),
                new JoinDirectBufferBo("agent1", 22, 70, "agent1", 12, "agent1"
                        , 22, 70, "agent1", 12, "agent1"
                        , 22, 70, "agent1", 12, "agent1"
                        , 22, 70, "agent1", 12, "agent1"
                        , TIMESTAMP3)
        ).forEach(builder::addDirectBuffer);

        JoinAgentStatBo statBo = builder.build();
        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo(APPLICATION_ID, statBo, 60000);
        assertThat(joinApplicationStatBoList).hasSize(2);
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
            if (joinApplicationStatBo.getTimestamp() == TIMESTAMP4) {
                assertHasSize(joinApplicationStatBo, 2);
            } else if (joinApplicationStatBo.getTimestamp() == 1498462500000L) {
                assertHasSize(joinApplicationStatBo, 3);
            } else {
                fail();
            }
        }
    }

    private void assertHasSize(JoinApplicationStatBo joinApplicationStatBo, int expected) {
        assertThat(joinApplicationStatBo.getJoinCpuLoadBoList()).hasSize(expected);
        assertThat(joinApplicationStatBo.getJoinMemoryBoList()).hasSize(expected);
        assertThat(joinApplicationStatBo.getJoinTransactionBoList()).hasSize(expected);
        assertThat(joinApplicationStatBo.getJoinActiveTraceBoList()).hasSize(expected);
        assertThat(joinApplicationStatBo.getJoinResponseTimeBoList()).hasSize(expected);
        assertThat(joinApplicationStatBo.getJoinDataSourceListBoList()).hasSize(expected);
        assertThat(joinApplicationStatBo.getJoinFileDescriptorBoList()).hasSize(expected);
        assertThat(joinApplicationStatBo.getJoinDirectBufferBoList()).hasSize(expected);
    }

    @Test
    public void createJoinApplicationStatBo3Test() {
        JoinAgentStatBo.Builder builder = JoinAgentStatBo.newBuilder("Agent", Long.MIN_VALUE, TIMESTAMP);

        List.of(
                new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", TIMESTAMP),
                new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", TIMESTAMP1),
                new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", TIMESTAMP2),
                new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", TIMESTAMP4),
                new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", TIMESTAMP3)
        ).forEach(builder::addCpuLoadBo);

        List.of(
                new JoinMemoryBo("agent1", TIMESTAMP, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent1", TIMESTAMP1, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1"),
                new JoinMemoryBo("agent1", TIMESTAMP2, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1"),
                new JoinMemoryBo("agent1", TIMESTAMP4, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1"),
                new JoinMemoryBo("agent1", TIMESTAMP3, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1")
        ).forEach(builder::addMemory);

        List.of(
                new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", TIMESTAMP),
                new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", TIMESTAMP1),
                new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", TIMESTAMP2),
                new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", TIMESTAMP4),
                new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", TIMESTAMP3)
        ).forEach(builder::addTransaction);

        List.of(
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP1),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP2),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP4),
                new JoinActiveTraceBo("agent1", 1, (short) 2, 30, 15, "app_1_1", 40, "app_1_2", TIMESTAMP3)
        ).forEach(builder::addActiveTrace);

        List.of(
                new JoinResponseTimeBo("agent1", TIMESTAMP, 3000, 2, "app_1_1", 6000, "app_1_2"),
                new JoinResponseTimeBo("agent1", TIMESTAMP1, 4000, 200, "app_2_1", 9000, "app_2_2"),
                new JoinResponseTimeBo("agent1", TIMESTAMP2, 2000, 20, "app_3_1", 7000, "app_3_2"),
                new JoinResponseTimeBo("agent1", TIMESTAMP4, 5000, 20, "app_4_1", 8000, "app_4_2"),
                new JoinResponseTimeBo("agent1", TIMESTAMP3, 1000, 10, "app_5_1", 6600, "app_5_2")
        ).forEach(builder::addResponseTime);

        List<JoinDataSourceBo> joinDataSourceBoList1 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6")
        );
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, TIMESTAMP);

        List<JoinDataSourceBo> joinDataSourceBoList2 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7")
        );
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, TIMESTAMP1);

        List<JoinDataSourceBo> joinDataSourceBoList3 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8")
        );
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, TIMESTAMP2);

        List<JoinDataSourceBo> joinDataSourceBoList4 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9")
        );
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, TIMESTAMP4);

        List<JoinDataSourceBo> joinDataSourceBoList5 = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"),
                new JoinDataSourceBo((short) 2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10")
        );
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, TIMESTAMP3);

        List.of(joinDataSourceListBo1, joinDataSourceListBo2, joinDataSourceListBo3, joinDataSourceListBo4, joinDataSourceListBo5)
                .forEach(builder::addDataSourceListBo);

        List.of(
                new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", TIMESTAMP),
                new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", TIMESTAMP1),
                new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", TIMESTAMP2),
                new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", TIMESTAMP4),
                new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", TIMESTAMP3)
        ).forEach(builder::addFileDescriptor);

        List.of(
                new JoinDirectBufferBo("agent1", 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , 44, 70, "agent1", 30, "agent1"
                        , TIMESTAMP),
                new JoinDirectBufferBo("agent1", 33, 40, "agent1", 10, "agent1"
                        , 33, 40, "agent1", 10, "agent1"
                        , 33, 40, "agent1", 10, "agent1"
                        , 33, 40, "agent1", 10, "agent1"
                        , TIMESTAMP1),
                new JoinDirectBufferBo("agent1", 55, 60, "agent1", 7, "agent1"
                        , 55, 60, "agent1", 7, "agent1"
                        , 55, 60, "agent1", 7, "agent1"
                        , 55, 60, "agent1", 7, "agent1"
                        , TIMESTAMP2),
                new JoinDirectBufferBo("agent1", 11, 80, "agent1", 8, "agent1"
                        , 11, 80, "agent1", 8, "agent1"
                        , 11, 80, "agent1", 8, "agent1"
                        , 11, 80, "agent1", 8, "agent1"
                        , TIMESTAMP4),
                new JoinDirectBufferBo("agent1", 22, 70, "agent1", 12, "agent1"
                        , 22, 70, "agent1", 12, "agent1"
                        , 22, 70, "agent1", 12, "agent1"
                        , 22, 70, "agent1", 12, "agent1"
                        , TIMESTAMP3)
        ).forEach(builder::addDirectBuffer);

        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo(APPLICATION_ID, builder.build(), 10000);
        assertThat(joinApplicationStatBoList).hasSize(3);
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
            if (joinApplicationStatBo.getTimestamp() == TIMESTAMP4) {
                assertHasSize(joinApplicationStatBo, 2);
            } else if (joinApplicationStatBo.getTimestamp() == 1498462540000L) {
                assertHasSize(joinApplicationStatBo, 1);
            } else if (joinApplicationStatBo.getTimestamp() == TIMESTAMP1) {
                assertHasSize(joinApplicationStatBo, 2);
            } else {
                fail();
            }
        }
    }

}


