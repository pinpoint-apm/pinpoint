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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author minwoo.jung
 */
public class JoinApplicationStatBoTest {

    @Test
    public void joinApplicationStatBoByTimeSliceTest() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id1", currentTime, 1));
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id2", currentTime + 1000, -4));
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id3", currentTime + 2000, -3));
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id4", currentTime + 3000, 4));
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id5", currentTime + 4000, -5));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinCpuLoadBo> joinCpuLoadBoList = resultJoinApplicationStatBo.getJoinCpuLoadBoList();
        joinCpuLoadBoList.sort(Comparator.comparingLong(JoinCpuLoadBo::getTimestamp));
        assertJoinCpuLoadBoList(joinCpuLoadBoList);
    }


    private void assertJoinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList) {
        assertEquals(joinCpuLoadBoList.size(), 5);
        JoinCpuLoadBo joinCpuLoadBo1 = joinCpuLoadBoList.get(0);
        assertEquals(joinCpuLoadBo1.getId(), "id1");
        assertEquals(joinCpuLoadBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinCpuLoadBo1.getJvmCpuLoadJoinValue(), new JoinDoubleFieldBo(48.6, 22.0, "id5_2", 91.0, "id4_1"));
        assertEquals(joinCpuLoadBo1.getSystemCpuLoadJoinValue(), new JoinDoubleFieldBo(78.6, 41.0, "id5_4", 91.0, "id4_3"));

        JoinCpuLoadBo joinCpuLoadBo2 = joinCpuLoadBoList.get(1);
        assertEquals(joinCpuLoadBo2.getId(), "id1");
        assertEquals(joinCpuLoadBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinCpuLoadBo2.getJvmCpuLoadJoinValue(), new JoinDoubleFieldBo(38.6, 35.0, "id5_2", 81.0, "id4_1"));
        assertEquals(joinCpuLoadBo2.getSystemCpuLoadJoinValue(), new JoinDoubleFieldBo(68.6, 35.0, "id5_4", 81.0, "id4_3"));

        JoinCpuLoadBo joinCpuLoadBo3 = joinCpuLoadBoList.get(2);
        assertEquals(joinCpuLoadBo3.getId(), "id1");
        assertEquals(joinCpuLoadBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinCpuLoadBo3.getJvmCpuLoadJoinValue(), new JoinDoubleFieldBo(28.6, 22.0, "id5_2", 71.0, "id4_1"));
        assertEquals(joinCpuLoadBo3.getSystemCpuLoadJoinValue(), new JoinDoubleFieldBo(58.6, 22.0, "id5_4", 71.0, "id4_3"));

        JoinCpuLoadBo joinCpuLoadBo4 = joinCpuLoadBoList.get(3);
        assertEquals(joinCpuLoadBo4.getId(), "id1");
        assertEquals(joinCpuLoadBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinCpuLoadBo4.getJvmCpuLoadJoinValue(), new JoinDoubleFieldBo(18.6, 12.0, "id5_2", 61.0, "id4_1"));
        assertEquals(joinCpuLoadBo4.getSystemCpuLoadJoinValue(), new JoinDoubleFieldBo(38.6, 13.0, "id5_4", 93.0, "id4_3"));

        JoinCpuLoadBo joinCpuLoadBo5 = joinCpuLoadBoList.get(4);
        assertEquals(joinCpuLoadBo5.getId(), "id1");
        assertEquals(joinCpuLoadBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinCpuLoadBo5.getJvmCpuLoadJoinValue(), new JoinDoubleFieldBo(8.6, 2.0, "id5_2", 93.0, "id4_1"));
        assertEquals(joinCpuLoadBo5.getSystemCpuLoadJoinValue(), new JoinDoubleFieldBo(28.6, 3.0, "id5_4", 63.0, "id4_3"));
    }

    private JoinApplicationStatBo createJoinApplicationStatBo(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinCpuLoadBoList(id, timestamp, plus).forEach(joinApplicationStatBo::addCpuLoad);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinCpuLoadBo> createJoinCpuLoadBoList(final String id, final long currentTime, int plus) {
        final List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<>();
        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo(id, 50 + plus, 87 + plus, id + "_1", 27 + plus, id + "_2", 80 + plus, 87 + plus, id + "_3", 46 + plus, id + "_4", currentTime);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo(id, 40 + plus, 77 + plus, id + "_1", 40 + plus, id + "_2", 70 + plus, 77 + plus, id + "_3", 40 + plus, id + "_4", currentTime + 5000);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo(id, 30 + plus, 67 + plus, id + "_1", 27 + plus, id + "_2", 60 + plus, 67 + plus, id + "_3", 27 + plus, id + "_4", currentTime + 10000);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo(id, 20 + plus, 57 + plus, id + "_1", 17 + plus, id + "_2", 40 + plus, 89 + plus, id + "_3", 18 + plus, id + "_4", currentTime + 15000);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo(id, 10 + plus, 89 + plus, id + "_1", 7 + plus, id + "_2", 30 + plus, 59 + plus, id + "_3", 8 + plus, id + "_4", currentTime + 20000);

        joinCpuLoadBoList.add(joinCpuLoadBo1);
        joinCpuLoadBoList.add(joinCpuLoadBo2);
        joinCpuLoadBoList.add(joinCpuLoadBo3);
        joinCpuLoadBoList.add(joinCpuLoadBo4);
        joinCpuLoadBoList.add(joinCpuLoadBo5);

        return joinCpuLoadBoList;
    }

    @Test
    public void joinApplicationStatBoByTimeSlice2Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinMemoryBo> joinMemoryBoList = resultJoinApplicationStatBo.getJoinMemoryBoList();
        joinMemoryBoList.sort(Comparator.comparingLong(JoinMemoryBo::getTimestamp));
        assertJoinMemoryBoList(joinMemoryBoList);
    }

    private void assertJoinMemoryBoList(List<JoinMemoryBo> joinMemoryBoList) {
        assertEquals(5, joinMemoryBoList.size());

        JoinMemoryBo joinMemoryBo1 = joinMemoryBoList.get(0);
        assertEquals("id1", joinMemoryBo1.getId());
        assertEquals(1487149800000L, joinMemoryBo1.getTimestamp());
        assertEquals(new JoinLongFieldBo(2986L, 1950L, "id5_1", 5040L, "id4_2"), joinMemoryBo1.getHeapUsedJoinValue());
        assertEquals(new JoinLongFieldBo(486L, 0L, "id5_3", 640L, "id4_4"), joinMemoryBo1.getNonHeapUsedJoinValue());

        JoinMemoryBo joinMemoryBo2 = joinMemoryBoList.get(1);
        assertEquals("id1", joinMemoryBo2.getId());
        assertEquals(1487149805000L, joinMemoryBo2.getTimestamp());
        assertEquals(new JoinLongFieldBo(3986L, 950L, "id5_1", 7040L, "id4_2"), joinMemoryBo2.getHeapUsedJoinValue());
        assertEquals(new JoinLongFieldBo(386L, 100L, "id5_3", 640L, "id4_4"), joinMemoryBo2.getNonHeapUsedJoinValue());

        JoinMemoryBo joinMemoryBo3 = joinMemoryBoList.get(2);
        assertEquals("id1", joinMemoryBo3.getId());
        assertEquals(1487149810000L, joinMemoryBo3.getTimestamp());
        assertEquals(new JoinLongFieldBo(4986L, 2950L, "id5_1", 8040L, "id4_2"), joinMemoryBo3.getHeapUsedJoinValue());
        assertEquals(new JoinLongFieldBo(186L, 50L, "id5_3", 240L, "id4_4"), joinMemoryBo3.getNonHeapUsedJoinValue());

        JoinMemoryBo joinMemoryBo4 = joinMemoryBoList.get(3);
        assertEquals("id1", joinMemoryBo4.getId());
        assertEquals(1487149815000L, joinMemoryBo4.getTimestamp());
        assertEquals(new JoinLongFieldBo(986L, 50L, "id5_1", 3040L, "id4_2"), joinMemoryBo4.getHeapUsedJoinValue());
        assertEquals(new JoinLongFieldBo(86L, 850L, "id5_3", 1040L, "id4_4"), joinMemoryBo4.getNonHeapUsedJoinValue());

        JoinMemoryBo joinMemoryBo5 = joinMemoryBoList.get(4);
        assertEquals("id1", joinMemoryBo5.getId());
        assertEquals(1487149820000L, joinMemoryBo5.getTimestamp());
        assertEquals(new JoinLongFieldBo(1986L, 950L, "id5_1", 6040L, "id4_2"), joinMemoryBo5.getHeapUsedJoinValue());
        assertEquals(new JoinLongFieldBo(286L, 50L, "id5_3", 2940L, "id4_4"), joinMemoryBo5.getNonHeapUsedJoinValue());
    }


    private JoinApplicationStatBo createJoinApplicationStatBo2(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinMemoryBoList(id, timestamp, plus).forEach(joinApplicationStatBo::addMemory);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinMemoryBo> createJoinMemoryBoList(final String id, final long currentTime, int plus) {
        final List<JoinMemoryBo> joinMemoryBoList = new ArrayList<>();
        JoinMemoryBo joinMemoryBo1 = new JoinMemoryBo(id, currentTime, 3000 + plus, 2000 + plus, 5000 + plus, id + "_1", id + "_2", 500 + plus, 50 + plus, 600 + plus, id + "_3", id + "_4");
        JoinMemoryBo joinMemoryBo2 = new JoinMemoryBo(id, currentTime + 5000, 4000 + plus, 1000 + plus, 7000 + plus, id + "_1", id + "_2", 400 + plus, 150 + plus, 600 + plus, id + "_3", id + "_4");
        JoinMemoryBo joinMemoryBo3 = new JoinMemoryBo(id, currentTime + 10000, 5000 + plus, 3000 + plus, 8000 + plus, id + "_1", id + "_2", 200 + plus, 100 + plus, 200 + plus, id + "_3", id + "_4");
        JoinMemoryBo joinMemoryBo4 = new JoinMemoryBo(id, currentTime + 15000, 1000 + plus, 100 + plus, 3000 + plus, id + "_1", id + "_2", 100 + plus, 900 + plus, 1000 + plus, id + "_3", id + "_4");
        JoinMemoryBo joinMemoryBo5 = new JoinMemoryBo(id, currentTime + + 20000, 2000 + plus, 1000 + plus, 6000 + plus, id + "_1", id + "_2", 300 + plus, 100 + plus, 2900 + plus, id + "_3", id + "_4");

        joinMemoryBoList.add(joinMemoryBo1);
        joinMemoryBoList.add(joinMemoryBo2);
        joinMemoryBoList.add(joinMemoryBo3);
        joinMemoryBoList.add(joinMemoryBo4);
        joinMemoryBoList.add(joinMemoryBo5);

        return joinMemoryBoList;
    }

    @Test
    public void joinApplicationStatBoByTimeSlice3Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();

        JoinCpuLoadBo joinCpuLoadBo1_1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo1_2 = new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo1_3 = new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462555000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo1 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo1.addCpuLoad(joinCpuLoadBo1_1);
        joinApplicationStatBo1.addCpuLoad(joinCpuLoadBo1_2);
        joinApplicationStatBo1.addCpuLoad(joinCpuLoadBo1_3);
        joinApplicationStatBoList.add(joinApplicationStatBo1.build());

        JoinCpuLoadBo joinCpuLoadBo2_1 = new JoinCpuLoadBo("agent1", 33, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo2_2 = new JoinCpuLoadBo("agent1", 22, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo2_3 = new JoinCpuLoadBo("agent1", 11, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462555000L);
        JoinCpuLoadBo joinCpuLoadBo2_4 = new JoinCpuLoadBo("agent1", 77, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462560000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo2 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo2.addCpuLoad(joinCpuLoadBo2_1);
        joinApplicationStatBo2.addCpuLoad(joinCpuLoadBo2_2);
        joinApplicationStatBo2.addCpuLoad(joinCpuLoadBo2_3);
        joinApplicationStatBo2.addCpuLoad(joinCpuLoadBo2_4);
        joinApplicationStatBoList.add(joinApplicationStatBo2.build());

        JoinCpuLoadBo joinCpuLoadBo3_1 = new JoinCpuLoadBo("agent1", 22, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo3_2 = new JoinCpuLoadBo("agent1", 11, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo3_3 = new JoinCpuLoadBo("agent1", 88, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462565000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo3 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo3.addCpuLoad(joinCpuLoadBo3_1);
        joinApplicationStatBo3.addCpuLoad(joinCpuLoadBo3_2);
        joinApplicationStatBo3.addCpuLoad(joinCpuLoadBo3_3);
        joinApplicationStatBoList.add(joinApplicationStatBo3.build());


        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinCpuLoadBo> joinCpuLoadBoList = joinApplicationStatBo.getJoinCpuLoadBoList();
        joinCpuLoadBoList.sort(Comparator.comparingLong(JoinCpuLoadBo::getTimestamp));

        assertEquals(joinCpuLoadBoList.size(), 5);
        assertEquals(joinCpuLoadBoList.get(0).getJvmCpuLoadJoinValue().getAvg(), 33,0);
        assertEquals(joinCpuLoadBoList.get(1).getJvmCpuLoadJoinValue().getAvg(), 22,0);
        assertEquals(joinCpuLoadBoList.get(2).getJvmCpuLoadJoinValue().getAvg(), 33,0);
        assertEquals(joinCpuLoadBoList.get(3).getJvmCpuLoadJoinValue().getAvg(), 77,0);
        assertEquals(joinCpuLoadBoList.get(4).getJvmCpuLoadJoinValue().getAvg(), 88,0);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice4Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();

        JoinMemoryBo joinMemoryBo1_1 = new JoinMemoryBo("agent1", 1498462545000L, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo1_2 = new JoinMemoryBo("agent2", 1498462550000L, 4000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2");
        JoinMemoryBo joinMemoryBo1_3 = new JoinMemoryBo("agent3", 1498462555000L, 5000, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3");
        JoinApplicationStatBo.Builder joinApplicationStatBo1 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo1.addMemory(joinMemoryBo1_1);
        joinApplicationStatBo1.addMemory(joinMemoryBo1_2);
        joinApplicationStatBo1.addMemory(joinMemoryBo1_3);
        joinApplicationStatBoList.add(joinApplicationStatBo1.build());

        JoinMemoryBo joinMemoryBo2_1 = new JoinMemoryBo("agent1", 1498462545000L, 4000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo2_2 = new JoinMemoryBo("agent2", 1498462550000L, 1000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2");
        JoinMemoryBo joinMemoryBo2_3 = new JoinMemoryBo("agent3", 1498462555000L, 3000, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3");
        JoinMemoryBo joinMemoryBo2_4 = new JoinMemoryBo("agent3", 1498462560000L, 8800, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3");
        JoinApplicationStatBo.Builder joinApplicationStatBo2 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo2.addMemory(joinMemoryBo2_1);
        joinApplicationStatBo2.addMemory(joinMemoryBo2_2);
        joinApplicationStatBo2.addMemory(joinMemoryBo2_3);
        joinApplicationStatBo2.addMemory(joinMemoryBo2_4);
        joinApplicationStatBoList.add(joinApplicationStatBo2.build());

        JoinMemoryBo joinMemoryBo3_1 = new JoinMemoryBo("agent1", 1498462545000L, 5000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo3_2 = new JoinMemoryBo("agent2", 1498462550000L, 1000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2");
        JoinMemoryBo joinMemoryBo3_3 = new JoinMemoryBo("agent3", 1498462565000L, 7800, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3");
        JoinApplicationStatBo.Builder joinApplicationStatBo3 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo3.addMemory(joinMemoryBo3_1);
        joinApplicationStatBo3.addMemory(joinMemoryBo3_2);
        joinApplicationStatBo3.addMemory(joinMemoryBo3_3);
        joinApplicationStatBoList.add(joinApplicationStatBo3.build());

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinMemoryBo> joinMemoryBoList = joinApplicationStatBo.getJoinMemoryBoList();
        joinMemoryBoList.sort(Comparator.comparingLong(JoinMemoryBo::getTimestamp));
        assertEquals(joinMemoryBoList.size(), 5);
        assertEquals((long) joinMemoryBoList.get(0).getHeapUsedJoinValue().getAvg(), 4000);
        assertEquals((long) joinMemoryBoList.get(1).getHeapUsedJoinValue().getAvg(), 2000);
        assertEquals((long) joinMemoryBoList.get(2).getHeapUsedJoinValue().getAvg(), 4000);
        assertEquals((long) joinMemoryBoList.get(3).getHeapUsedJoinValue().getAvg(), 8800);
        assertEquals((long) joinMemoryBoList.get(4).getHeapUsedJoinValue().getAvg(), 7800);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice5Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinTransactionBo> joinTransactionBoList = resultJoinApplicationStatBo.getJoinTransactionBoList();
        joinTransactionBoList.sort(Comparator.comparingLong(JoinTransactionBo::getTimestamp));
        assertJoinTransactionBoList(joinTransactionBoList);
    }


    private JoinApplicationStatBo createJoinApplicationStatBo3(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinTransactionBoList(id, timestamp, plus).forEach(joinApplicationStatBo::addTransaction);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinTransactionBo> createJoinTransactionBoList(final String id, final long currentTime, int plus) {
        final List<JoinTransactionBo> joinTransactionBoList = new ArrayList<>();

        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo(id, 5000, 100 + plus, 60 + plus, id + "_1", 200 + plus, id + "_2", currentTime);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo(id, 5000, 300 + plus, 150 + plus, id + "_1", 400 + plus, id + "_2", currentTime + 5000);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo(id, 5000, 200 + plus, 130 + plus, id + "_1", 300 + plus, id + "_2", currentTime + 10000);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo(id, 5000, 400 + plus, 200 + plus, id + "_1", 450 + plus, id + "_2", currentTime + 15000);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo(id, 5000, 350 + plus, 170 + plus, id + "_1", 600 + plus, id + "_2", currentTime + 20000);

        joinTransactionBoList.add(joinTransactionBo1);
        joinTransactionBoList.add(joinTransactionBo2);
        joinTransactionBoList.add(joinTransactionBo3);
        joinTransactionBoList.add(joinTransactionBo4);
        joinTransactionBoList.add(joinTransactionBo5);

        return joinTransactionBoList;
    }


    private void assertJoinTransactionBoList(List<JoinTransactionBo> joinTransactionBoList) {
        assertEquals(joinTransactionBoList.size(), 5);

        JoinTransactionBo joinTransactionBo1 = joinTransactionBoList.get(0);
        assertEquals(joinTransactionBo1.getId(), "id1");
        assertEquals(joinTransactionBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinTransactionBo1.getTotalCountJoinValue(), new JoinLongFieldBo(86L, 10L, "id5_1", 240L, "id4_2"));

        JoinTransactionBo joinTransactionBo2 = joinTransactionBoList.get(1);
        assertEquals(joinTransactionBo2.getId(), "id1");
        assertEquals(joinTransactionBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinTransactionBo2.getTotalCountJoinValue(), new JoinLongFieldBo(286L, 100L, "id5_1", 440L, "id4_2"));

        JoinTransactionBo joinTransactionBo3 = joinTransactionBoList.get(2);
        assertEquals(joinTransactionBo3.getId(), "id1");
        assertEquals(joinTransactionBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinTransactionBo3.getTotalCountJoinValue(), new JoinLongFieldBo(186L, 80L, "id5_1", 340L, "id4_2"));

        JoinTransactionBo joinTransactionBo4 = joinTransactionBoList.get(3);
        assertEquals(joinTransactionBo4.getId(), "id1");
        assertEquals(joinTransactionBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinTransactionBo4.getTotalCountJoinValue(), new JoinLongFieldBo(386L, 150L, "id5_1", 490L, "id4_2"));

        JoinTransactionBo joinTransactionBo5 = joinTransactionBoList.get(4);
        assertEquals(joinTransactionBo5.getId(), "id1");
        assertEquals(joinTransactionBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinTransactionBo5.getTotalCountJoinValue(), new JoinLongFieldBo(336L, 120L, "id5_1", 640L, "id4_2"));
    }

    @Test
    public void joinApplicationStatBoByTimeSlice6Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();

        JoinTransactionBo joinTransactionBo1_1 = new JoinTransactionBo("agent1", 5000, 100, 60, "agent1", 200, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo1_2 = new JoinTransactionBo("agent2", 5000, 100, 60, "agent2", 200, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo1_3 = new JoinTransactionBo("agent3", 5000, 100, 60, "agent3", 200, "agent3", 1498462555000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo1 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo1.addTransaction(joinTransactionBo1_1);
        joinApplicationStatBo1.addTransaction(joinTransactionBo1_2);
        joinApplicationStatBo1.addTransaction(joinTransactionBo1_3);

        joinApplicationStatBoList.add(joinApplicationStatBo1.build());

        JoinTransactionBo joinTransactionBo2_1 = new JoinTransactionBo("agent1", 5000, 50, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo2_2 = new JoinTransactionBo("agent2", 5000, 200, 60, "agent2", 400, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo2_3 = new JoinTransactionBo("agent3", 5000, 500, 10, "agent3", 100, "agent3", 1498462555000L);
        JoinTransactionBo joinTransactionBo2_4 = new JoinTransactionBo("agent3", 5000, 400, 60, "agent3", 500, "agent3", 1498462560000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo2 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo2.addTransaction(joinTransactionBo2_1);
        joinApplicationStatBo2.addTransaction(joinTransactionBo2_2);
        joinApplicationStatBo2.addTransaction(joinTransactionBo2_3);
        joinApplicationStatBo2.addTransaction(joinTransactionBo2_4);
        joinApplicationStatBoList.add(joinApplicationStatBo2.build());

        JoinTransactionBo joinTransactionBo3_1 = new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo3_2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo3_3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462565000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo3 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo3.addTransaction(joinTransactionBo3_1);
        joinApplicationStatBo3.addTransaction(joinTransactionBo3_2);
        joinApplicationStatBo3.addTransaction(joinTransactionBo3_3);
        joinApplicationStatBoList.add(joinApplicationStatBo3.build());

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinTransactionBo> joinTransactionBoList = joinApplicationStatBo.getJoinTransactionBoList();
        joinTransactionBoList.sort(Comparator.comparingLong(JoinTransactionBo::getTimestamp));
        assertEquals(joinTransactionBoList.size(), 5);
        assertEquals((long) joinTransactionBoList.get(0).getTotalCountJoinValue().getAvg(), 100);
        assertEquals((long) joinTransactionBoList.get(1).getTotalCountJoinValue().getAvg(), 200);
        assertEquals((long) joinTransactionBoList.get(2).getTotalCountJoinValue().getAvg(), 300);
        assertEquals((long) joinTransactionBoList.get(3).getTotalCountJoinValue().getAvg(), 400);
        assertEquals((long) joinTransactionBoList.get(4).getTotalCountJoinValue().getAvg(), 30);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice7Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinActiveTraceBo> joinActiveTraceBoList = resultJoinApplicationStatBo.getJoinActiveTraceBoList();
        joinActiveTraceBoList.sort(Comparator.comparingLong(JoinActiveTraceBo::getTimestamp));
        assertJoinActiveTraceBoList(joinActiveTraceBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice8Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();

        JoinActiveTraceBo joinActiveTraceBo1_1 = new JoinActiveTraceBo("agent1", 1, (short)2, 100, 60, "agent1", 200, "agent1", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo1_2 = new JoinActiveTraceBo("agent2", 1, (short)2, 100, 60, "agent1", 200, "agent1", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo1_3 = new JoinActiveTraceBo("agent3", 1, (short)2, 100, 60, "agent1", 200, "agent1", 1498462555000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo1 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo1.addActiveTrace(joinActiveTraceBo1_1);
        joinApplicationStatBo1.addActiveTrace(joinActiveTraceBo1_2);
        joinApplicationStatBo1.addActiveTrace(joinActiveTraceBo1_3);
        joinApplicationStatBoList.add(joinApplicationStatBo1.build());

        JoinActiveTraceBo joinActiveTraceBo2_1 = new JoinActiveTraceBo("agent1", 1, (short)2, 50, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo2_2 = new JoinActiveTraceBo("agent2", 1, (short)2, 200, 60, "agent2", 400, "agent2", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo2_3 = new JoinActiveTraceBo("agent3", 1, (short)2, 500, 10, "agent3", 100, "agent3", 1498462555000L);
        JoinActiveTraceBo joinActiveTraceBo2_4 = new JoinActiveTraceBo("agent3", 1, (short)2, 400, 60, "agent3", 500, "agent3", 1498462560000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo2 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo2.addActiveTrace(joinActiveTraceBo2_1);
        joinApplicationStatBo2.addActiveTrace(joinActiveTraceBo2_2);
        joinApplicationStatBo2.addActiveTrace(joinActiveTraceBo2_3);
        joinApplicationStatBo2.addActiveTrace(joinActiveTraceBo2_4);
        joinApplicationStatBoList.add(joinApplicationStatBo2.build());

        JoinActiveTraceBo joinActiveTraceBo3_1 = new JoinActiveTraceBo("agent1", 1, (short)2, 150, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo3_2 = new JoinActiveTraceBo("agent2", 1, (short)2, 300, 10, "agent2", 400, "agent2", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo3_3 = new JoinActiveTraceBo("agent3", 1, (short)2, 30, 5, "agent3", 100, "agent3", 1498462565000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo3 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo3.addActiveTrace(joinActiveTraceBo3_1);
        joinApplicationStatBo3.addActiveTrace(joinActiveTraceBo3_2);
        joinApplicationStatBo3.addActiveTrace(joinActiveTraceBo3_3);
        joinApplicationStatBoList.add(joinApplicationStatBo3.build());

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinActiveTraceBo> joinActiveTraceBoList = joinApplicationStatBo.getJoinActiveTraceBoList();
        joinActiveTraceBoList.sort(Comparator.comparingLong(JoinActiveTraceBo::getTimestamp));
        assertEquals(joinActiveTraceBoList.size(), 5);
        assertEquals((int) joinActiveTraceBoList.get(0).getTotalCountJoinValue().getAvg(), 100);
        assertEquals((int) joinActiveTraceBoList.get(1).getTotalCountJoinValue().getAvg(), 200);
        assertEquals((int) joinActiveTraceBoList.get(2).getTotalCountJoinValue().getAvg(), 300);
        assertEquals((int) joinActiveTraceBoList.get(3).getTotalCountJoinValue().getAvg(), 400);
        assertEquals((int) joinActiveTraceBoList.get(4).getTotalCountJoinValue().getAvg(), 30);
    }


    private JoinApplicationStatBo createJoinApplicationStatBo4(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinActiveTraceBoList(id, timestamp, plus).forEach(joinApplicationStatBo::addActiveTrace);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinActiveTraceBo> createJoinActiveTraceBoList(final String id, final long currentTime, int plus) {
        final List<JoinActiveTraceBo> joinActiveTraceBoList = new ArrayList<>();
        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo(id, 1, (short)2, 100 + plus, 60 + plus, id + "_1", 200 + plus, id + "_2", currentTime);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo(id, 1, (short)2, 300 + plus, 150 + plus, id + "_1", 400 + plus, id + "_2", currentTime + 5000);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo(id, 1, (short)2, 200 + plus, 130 + plus, id + "_1", 300 + plus, id + "_2", currentTime + 10000);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo(id, 1, (short)2, 400 + plus, 200 + plus, id + "_1", 450 + plus, id + "_2", currentTime + 15000);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo(id, 1, (short)2, 350 + plus, 170 + plus, id + "_1", 600 + plus, id + "_2", currentTime + 20000);

        joinActiveTraceBoList.add(joinActiveTraceBo1);
        joinActiveTraceBoList.add(joinActiveTraceBo2);
        joinActiveTraceBoList.add(joinActiveTraceBo3);
        joinActiveTraceBoList.add(joinActiveTraceBo4);
        joinActiveTraceBoList.add(joinActiveTraceBo5);

        return joinActiveTraceBoList;
    }

    private void assertJoinActiveTraceBoList(List<JoinActiveTraceBo> joinActiveTraceBoList) {
        assertEquals(joinActiveTraceBoList.size(), 5);

        JoinActiveTraceBo joinActiveTraceBo1 = joinActiveTraceBoList.get(0);
        assertEquals(joinActiveTraceBo1.getId(), "id1");
        assertEquals(joinActiveTraceBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinActiveTraceBo1.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo1.getVersion(), 2);
        assertEquals(new JoinIntFieldBo(86, 10, "id5_1", 240, "id4_2"), joinActiveTraceBo1.getTotalCountJoinValue());

        JoinActiveTraceBo joinActiveTraceBo2 = joinActiveTraceBoList.get(1);
        assertEquals(joinActiveTraceBo2.getId(), "id1");
        assertEquals(joinActiveTraceBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinActiveTraceBo2.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo2.getVersion(), 2);
        assertEquals(new JoinIntFieldBo(286, 100, "id5_1", 440, "id4_2"), joinActiveTraceBo2.getTotalCountJoinValue());

        JoinActiveTraceBo joinActiveTraceBo3 = joinActiveTraceBoList.get(2);
        assertEquals(joinActiveTraceBo3.getId(), "id1");
        assertEquals(joinActiveTraceBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinActiveTraceBo3.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo3.getVersion(), 2);
        assertEquals(new JoinIntFieldBo(186, 80, "id5_1", 340, "id4_2"), joinActiveTraceBo3.getTotalCountJoinValue());

        JoinActiveTraceBo joinActiveTraceBo4 = joinActiveTraceBoList.get(3);
        assertEquals(joinActiveTraceBo4.getId(), "id1");
        assertEquals(joinActiveTraceBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinActiveTraceBo4.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo4.getVersion(), 2);
        assertEquals(new JoinIntFieldBo(386, 150, "id5_1", 490, "id4_2"), joinActiveTraceBo4.getTotalCountJoinValue());

        JoinActiveTraceBo joinActiveTraceBo5 = joinActiveTraceBoList.get(4);
        assertEquals(joinActiveTraceBo5.getId(), "id1");
        assertEquals(joinActiveTraceBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinActiveTraceBo5.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo5.getVersion(), 2);
        assertEquals(new JoinIntFieldBo(336, 120, "id5_1", 640, "id4_2"), joinActiveTraceBo5.getTotalCountJoinValue());
    }

    @Test
    public void joinApplicationStatBoByTimeSlice9Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinResponseTimeBo> joinResponseTimeBoList = resultJoinApplicationStatBo.getJoinResponseTimeBoList();
        joinResponseTimeBoList.sort(Comparator.comparingLong(JoinResponseTimeBo::getTimestamp));

        assertJoinResponseTimeBoList(joinResponseTimeBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice10Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();

        JoinResponseTimeBo joinResponseTimeBo1_1 = new JoinResponseTimeBo("agent1", 1498462545000L, 100, 60, "agent1", 200, "agent1");
        JoinResponseTimeBo joinResponseTimeBo1_2 = new JoinResponseTimeBo("agent1", 1498462550000L, 100, 60, "agent1", 200, "agent1");
        JoinResponseTimeBo joinResponseTimeBo1_3 = new JoinResponseTimeBo("agent1", 1498462555000L, 100, 60, "agent1", 200, "agent1");
        JoinApplicationStatBo.Builder joinApplicationStatBo1 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo1.addResponseTime(joinResponseTimeBo1_1);
        joinApplicationStatBo1.addResponseTime(joinResponseTimeBo1_2);
        joinApplicationStatBo1.addResponseTime(joinResponseTimeBo1_3);

        joinApplicationStatBoList.add(joinApplicationStatBo1.build());

        List<JoinResponseTimeBo> joinResponseTimeBoList2 = new ArrayList<>();
        JoinResponseTimeBo joinResponseTimeBo2_1 = new JoinResponseTimeBo("agent1", 1498462545000L, 50, 20, "agent1", 230, "agent1");
        JoinResponseTimeBo joinResponseTimeBo2_2 = new JoinResponseTimeBo("agent2", 1498462550000L, 200, 60, "agent2", 400, "agent2");
        JoinResponseTimeBo joinResponseTimeBo2_3 = new JoinResponseTimeBo("agent3", 1498462555000L, 500, 10, "agent3", 100, "agent3");
        JoinResponseTimeBo joinResponseTimeBo2_4 = new JoinResponseTimeBo("agent3", 1498462560000L, 400, 60, "agent3", 500, "agent3");
        JoinApplicationStatBo.Builder joinApplicationStatBo2 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo2.addResponseTime(joinResponseTimeBo2_1);
        joinApplicationStatBo2.addResponseTime(joinResponseTimeBo2_2);
        joinApplicationStatBo2.addResponseTime(joinResponseTimeBo2_3);
        joinApplicationStatBo2.addResponseTime(joinResponseTimeBo2_4);

        joinApplicationStatBoList.add(joinApplicationStatBo2.build());

        JoinResponseTimeBo joinResponseTimeBo3_1 = new JoinResponseTimeBo("agent1", 1498462545000L, 150, 20, "agent1", 230, "agent1");
        JoinResponseTimeBo joinResponseTimeBo3_2 = new JoinResponseTimeBo("agent2", 1498462550000L, 300, 10, "agent2", 400, "agent2");
        JoinResponseTimeBo joinResponseTimeBo3_3 = new JoinResponseTimeBo("agent3", 1498462565000L, 30, 5, "agent3", 100, "agent3");
        JoinApplicationStatBo.Builder joinApplicationStatBo3 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo3.addResponseTime(joinResponseTimeBo3_1);
        joinApplicationStatBo3.addResponseTime(joinResponseTimeBo3_2);
        joinApplicationStatBo3.addResponseTime(joinResponseTimeBo3_3);

        joinApplicationStatBoList.add(joinApplicationStatBo3.build());

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinResponseTimeBo> joinResponseTimeBoList = joinApplicationStatBo.getJoinResponseTimeBoList();
        joinResponseTimeBoList.sort(Comparator.comparingLong(JoinResponseTimeBo::getTimestamp));
        assertEquals(joinResponseTimeBoList.size(), 5);


        assertEquals((long) joinResponseTimeBoList.get(0).getResponseTimeJoinValue().getAvg(), 100);
        assertEquals((long) joinResponseTimeBoList.get(1).getResponseTimeJoinValue().getAvg(), 200);
        assertEquals((long) joinResponseTimeBoList.get(2).getResponseTimeJoinValue().getAvg(), 300);
        assertEquals((long) joinResponseTimeBoList.get(3).getResponseTimeJoinValue().getAvg(), 400);
        assertEquals((long) joinResponseTimeBoList.get(4).getResponseTimeJoinValue().getAvg(), 30);
    }

    private void assertJoinResponseTimeBoList(List<JoinResponseTimeBo> joinResponseTimeBoList) {
        assertEquals(joinResponseTimeBoList.size(), 5);

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



    private JoinApplicationStatBo createJoinApplicationStatBo5(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinResponseTimeList(id, timestamp, plus).forEach(joinApplicationStatBo::addResponseTime);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinResponseTimeBo> createJoinResponseTimeList(String id, long currentTime, int plus) {
        final List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<>();
        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo(id, currentTime, 300 + plus, 200 + plus, id + "_1", 6000 + plus, id + "_2");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo(id, currentTime + 5000, 200 + plus, 50 + plus, id + "_1", 7000 + plus, id + "_2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo(id, currentTime + 10000, 400 + plus, 300 + plus, id + "_1", 8000 + plus, id + "_2");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo(id, currentTime + 15000, 500 + plus, 400 + plus, id + "_1", 2000 + plus, id + "_2");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo(id, currentTime + 20000, 100 + plus, 100 + plus, id + "_1", 9000 + plus, id + "_2");
        joinResponseTimeBoList.add(joinResponseTimeBo1);
        joinResponseTimeBoList.add(joinResponseTimeBo2);
        joinResponseTimeBoList.add(joinResponseTimeBo3);
        joinResponseTimeBoList.add(joinResponseTimeBo4);
        joinResponseTimeBoList.add(joinResponseTimeBo5);

        return joinResponseTimeBoList;
    }

    @Test
    public void joinApplicationStatBoByTimeSlice11Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinDataSourceListBo> joinDataSourceListBoList = resultJoinApplicationStatBo.getJoinDataSourceListBoList();
        joinDataSourceListBoList.sort(Comparator.comparingLong(JoinDataSourceListBo::getTimestamp));

        assertJoinDataSourceListBoList(joinDataSourceListBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice12Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();


        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100, 60, "agent1", 200, "agent1"));
        JoinDataSourceListBo joinDataSourceListBo1_1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462545000L);
        JoinDataSourceListBo joinDataSourceListBo1_2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462550000L);
        JoinDataSourceListBo joinDataSourceListBo1_3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462555000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo1 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo1.addDataSourceList(joinDataSourceListBo1_1);
        joinApplicationStatBo1.addDataSourceList(joinDataSourceListBo1_2);
        joinApplicationStatBo1.addDataSourceList(joinDataSourceListBo1_3);

        joinApplicationStatBoList.add(joinApplicationStatBo1.build());

        List<JoinDataSourceBo> joinDataSourceBoList2_1 = new ArrayList<>();
        joinDataSourceBoList2_1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 50, 20, "agent1", 230, "agent1"));
        JoinDataSourceListBo joinResponseTimeBo2_1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2_1, 1498462545000L);
        List<JoinDataSourceBo> joinDataSourceBoList2_2 = new ArrayList<>();
        joinDataSourceBoList2_2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200, 60, "agent2", 400, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo2_2 = new JoinDataSourceListBo("agent2", joinDataSourceBoList2_2, 1498462550000L);
        List<JoinDataSourceBo> joinDataSourceBoList2_3 = new ArrayList<>();
        joinDataSourceBoList2_3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500, 10, "agent3", 100, "agent3"));
        JoinDataSourceListBo joinResponseTimeBo2_3 = new JoinDataSourceListBo("agent3", joinDataSourceBoList2_3, 1498462555000L);
        List<JoinDataSourceBo> joinDataSourceBoList2_4 = new ArrayList<>();
        joinDataSourceBoList2_4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400, 60, "agent3", 500, "agent3"));
        JoinDataSourceListBo joinResponseTimeBo2_4 = new JoinDataSourceListBo("agent3", joinDataSourceBoList2_4, 1498462560000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo2 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo2.addDataSourceList(joinResponseTimeBo2_1);
        joinApplicationStatBo2.addDataSourceList(joinResponseTimeBo2_2);
        joinApplicationStatBo2.addDataSourceList(joinResponseTimeBo2_3);
        joinApplicationStatBo2.addDataSourceList(joinResponseTimeBo2_4);
        joinApplicationStatBoList.add(joinApplicationStatBo2.build());

        List<JoinDataSourceBo> joinDataSourceBoList3_1 = new ArrayList<>();
        joinDataSourceBoList3_1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 150, 20, "agent1", 230, "agent1"));
        JoinDataSourceListBo joinResponseTimeBo3_1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3_1, 1498462545000L);
        List<JoinDataSourceBo> joinDataSourceBoList3_2 = new ArrayList<>();
        joinDataSourceBoList3_2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300, 10, "agent2", 400, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo3_2 = new JoinDataSourceListBo("agent2", joinDataSourceBoList3_2, 1498462550000L);
        List<JoinDataSourceBo> joinDataSourceBoList3_3 = new ArrayList<>();
        joinDataSourceBoList3_3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 30, 5, "agent2", 100, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo3_3 = new JoinDataSourceListBo("agent3", joinDataSourceBoList3_3, 1498462565000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo3 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo3.addDataSourceList(joinResponseTimeBo3_1);
        joinApplicationStatBo3.addDataSourceList(joinResponseTimeBo3_2);
        joinApplicationStatBo3.addDataSourceList(joinResponseTimeBo3_3);
        joinApplicationStatBoList.add(joinApplicationStatBo3.build());

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinDataSourceListBo> joinDataSourceListBoList = joinApplicationStatBo.getJoinDataSourceListBoList();
        joinDataSourceListBoList.sort(Comparator.comparingLong(JoinDataSourceListBo::getTimestamp));
        assertEquals(joinDataSourceListBoList.size(), 5);
        assertEquals((int) joinDataSourceListBoList.get(0).getJoinDataSourceBoList().get(0).getActiveConnectionSizeJoinValue().getAvg(), 100);
        assertEquals(joinDataSourceListBoList.get(0).getJoinDataSourceBoList().size(), 1);
        assertEquals((int) joinDataSourceListBoList.get(1).getJoinDataSourceBoList().get(0).getActiveConnectionSizeJoinValue().getAvg(), 200);
        assertEquals(joinDataSourceListBoList.get(1).getJoinDataSourceBoList().size(), 1);
        assertEquals((int) joinDataSourceListBoList.get(2).getJoinDataSourceBoList().get(0).getActiveConnectionSizeJoinValue().getAvg(), 300);
        assertEquals(joinDataSourceListBoList.get(2).getJoinDataSourceBoList().size(), 1);
        assertEquals((int) joinDataSourceListBoList.get(3).getJoinDataSourceBoList().get(0).getActiveConnectionSizeJoinValue().getAvg(), 400);
        assertEquals(joinDataSourceListBoList.get(3).getJoinDataSourceBoList().size(), 1);
        assertEquals((int) joinDataSourceListBoList.get(4).getJoinDataSourceBoList().get(0).getActiveConnectionSizeJoinValue().getAvg(), 30);
        assertEquals(joinDataSourceListBoList.get(4).getJoinDataSourceBoList().size(), 1);
    }

    private void assertJoinDataSourceListBoList(List<JoinDataSourceListBo> joinDataSourceListBoList) {
        assertEquals(joinDataSourceListBoList.size(), 5);

        JoinDataSourceListBo joinDataSourceListBo1 = joinDataSourceListBoList.get(0);
        assertEquals(joinDataSourceListBo1.getId(), "id1");
        assertEquals(joinDataSourceListBo1.getTimestamp(), 1487149800000L);
        List<JoinDataSourceBo> joinDataSourceBoList1 = joinDataSourceListBo1.getJoinDataSourceBoList();
        assertEquals(joinDataSourceBoList1.size(), 2);
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
        assertEquals(joinDataSourceBoList2.size(), 2);
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
        assertEquals(joinDataSourceBoList3.size(), 2);
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
        assertEquals(joinDataSourceBoList4.size(), 2);
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
        assertEquals(joinDataSourceBoList5.size(), 2);
        JoinDataSourceBo joinDataSourceBo5_1 = joinDataSourceBoList5.get(0);
        assertEquals(joinDataSourceBo5_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo5_1.getUrl(), "jdbc:mysql");
        assertEquals(new JoinIntFieldBo(86, 700, "agent_id_5_-50", 840, "agent_id_10_40"), joinDataSourceBo5_1.getActiveConnectionSizeJoinValue());

        JoinDataSourceBo joinDataSourceBo5_2 = joinDataSourceBoList5.get(1);
        assertEquals(joinDataSourceBo5_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo5_2.getUrl(), "jdbc:mssql");
        assertEquals(new JoinIntFieldBo(186, 800, "agent_id_5_-50", 940, "agent_id_10_40"), joinDataSourceBo5_2.getActiveConnectionSizeJoinValue());
    }


    private JoinApplicationStatBo createJoinApplicationStatBo6(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinDataSourceListBoList(id, timestamp, plus).forEach(joinApplicationStatBo::addDataSourceList);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinDataSourceListBo> createJoinDataSourceListBoList(String id, long currentTime, int plus) {
        final List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<>();

        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300 + plus, 250 + plus, "agent_id_1_" + plus, 600 + plus, "agent_id_6_" + plus));
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 400 + plus, 350 + plus, "agent_id_1_" + plus, 700 + plus, "agent_id_6_" + plus));
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo(id, joinDataSourceBoList1, currentTime);

        List<JoinDataSourceBo> joinDataSourceBoList2 = new ArrayList<>();
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200 + plus, 50 + plus, "agent_id_2_" + plus, 700 + plus, "agent_id_7_" + plus));
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 300 + plus, 150 + plus, "agent_id_2_" + plus, 800 + plus, "agent_id_7_" + plus));
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo(id, joinDataSourceBoList2, currentTime + 5000);

        List<JoinDataSourceBo> joinDataSourceBoList3 = new ArrayList<>();
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500 + plus, 150 + plus, "agent_id_3_" + plus, 900 + plus, "agent_id_8_" + plus));
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 600 + plus, 250 + plus, "agent_id_3_" + plus, 1000 + plus, "agent_id_8_" + plus));
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo(id, joinDataSourceBoList3, currentTime + 10000);

        List<JoinDataSourceBo> joinDataSourceBoList4 = new ArrayList<>();
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400 + plus, 550 + plus, "agent_id_4_" + plus, 600 + plus, "agent_id_9_" + plus));
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 500 + plus, 650 + plus, "agent_id_4_" + plus, 700 + plus, "agent_id_9_" + plus));
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo(id, joinDataSourceBoList4, currentTime + 15000);

        List<JoinDataSourceBo> joinDataSourceBoList5 = new ArrayList<>();
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100 + plus, 750 + plus, "agent_id_5_" + plus, 800 + plus, "agent_id_10_" + plus));
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 200 + plus, 850 + plus, "agent_id_5_" + plus, 900 + plus, "agent_id_10_" + plus));
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo(id, joinDataSourceBoList5, currentTime + 20000);

        joinDataSourceListBoList.add(joinDataSourceListBo1);
        joinDataSourceListBoList.add(joinDataSourceListBo2);
        joinDataSourceListBoList.add(joinDataSourceListBo3);
        joinDataSourceListBoList.add(joinDataSourceListBo4);
        joinDataSourceListBoList.add(joinDataSourceListBo5);

        return joinDataSourceListBoList;
    }


    @Test
    public void joinApplicationStatBoByTimeSlice13Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinFileDescriptorBo> joinFileDescriptorBoList = resultJoinApplicationStatBo.getJoinFileDescriptorBoList();
        joinFileDescriptorBoList.sort(Comparator.comparingLong(JoinFileDescriptorBo::getTimestamp));
        assertJoinFileDescriptorBoList(joinFileDescriptorBoList);
    }
    @Test
    public void joinApplicationStatBoByTimeSlice14Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();

        JoinFileDescriptorBo joinFileDescriptorBo1_1 = new JoinFileDescriptorBo("agent1", 440, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo1_2 = new JoinFileDescriptorBo("agent1", 330, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo1_3 = new JoinFileDescriptorBo("agent1", 550, 600, "agent1", 70, "agent1", 1498462555000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo1 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo1.addFileDescriptor(joinFileDescriptorBo1_1);
        joinApplicationStatBo1.addFileDescriptor(joinFileDescriptorBo1_2);
        joinApplicationStatBo1.addFileDescriptor(joinFileDescriptorBo1_3);
        joinApplicationStatBoList.add(joinApplicationStatBo1.build());

        JoinFileDescriptorBo joinFileDescriptorBo2_1 = new JoinFileDescriptorBo("agent1", 330, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo2_2 = new JoinFileDescriptorBo("agent1", 220, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo2_3 = new JoinFileDescriptorBo("agent1", 110, 600, "agent1", 70, "agent1", 1498462555000L);
        JoinFileDescriptorBo joinFileDescriptorBo2_4 = new JoinFileDescriptorBo("agent1", 770, 600, "agent1", 70, "agent1", 1498462560000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo2 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo2.addFileDescriptor(joinFileDescriptorBo2_1);
        joinApplicationStatBo2.addFileDescriptor(joinFileDescriptorBo2_2);
        joinApplicationStatBo2.addFileDescriptor(joinFileDescriptorBo2_3);
        joinApplicationStatBo2.addFileDescriptor(joinFileDescriptorBo2_4);
        joinApplicationStatBoList.add(joinApplicationStatBo2.build());

        JoinFileDescriptorBo joinFileDescriptorBo3_1 = new JoinFileDescriptorBo("agent1", 220, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo3_2 = new JoinFileDescriptorBo("agent1", 110, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo3_3 = new JoinFileDescriptorBo("agent1", 880, 600, "agent1", 70, "agent1", 1498462565000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo3 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo3.addFileDescriptor(joinFileDescriptorBo3_1);
        joinApplicationStatBo3.addFileDescriptor(joinFileDescriptorBo3_2);
        joinApplicationStatBo3.addFileDescriptor(joinFileDescriptorBo3_3);
        joinApplicationStatBoList.add(joinApplicationStatBo3.build());


        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinFileDescriptorBo> joinFileDescriptorBoList = joinApplicationStatBo.getJoinFileDescriptorBoList();
        joinFileDescriptorBoList.sort(Comparator.comparingLong(JoinFileDescriptorBo::getTimestamp));

        assertEquals(joinFileDescriptorBoList.size(), 5);
        assertEquals(joinFileDescriptorBoList.get(0).getOpenFdCountJoinValue().getAvg(), 330,0);
        assertEquals(joinFileDescriptorBoList.get(1).getOpenFdCountJoinValue().getAvg(), 220,0);
        assertEquals(joinFileDescriptorBoList.get(2).getOpenFdCountJoinValue().getAvg(), 330,0);
        assertEquals(joinFileDescriptorBoList.get(3).getOpenFdCountJoinValue().getAvg(), 770,0);
        assertEquals(joinFileDescriptorBoList.get(4).getOpenFdCountJoinValue().getAvg(), 880,0);
    }


    private void assertJoinFileDescriptorBoList(List<JoinFileDescriptorBo> joinFileDescriptorBoList) {
        assertEquals(joinFileDescriptorBoList.size(), 5);
        JoinFileDescriptorBo joinFileDescriptorBo1 = joinFileDescriptorBoList.get(0);
        assertEquals(joinFileDescriptorBo1.getId(), "id1");
        assertEquals(joinFileDescriptorBo1.getTimestamp(), 1487149800000L);
        assertEquals(new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"), joinFileDescriptorBo1.getOpenFdCountJoinValue());

        JoinFileDescriptorBo joinFileDescriptorBo2 = joinFileDescriptorBoList.get(1);
        assertEquals(joinFileDescriptorBo2.getId(), "id1");
        assertEquals(joinFileDescriptorBo2.getTimestamp(), 1487149805000L);
        assertEquals(new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"), joinFileDescriptorBo2.getOpenFdCountJoinValue());

        JoinFileDescriptorBo joinFileDescriptorBo3 = joinFileDescriptorBoList.get(2);
        assertEquals(joinFileDescriptorBo3.getId(), "id1");
        assertEquals(joinFileDescriptorBo3.getTimestamp(), 1487149810000L);
        assertEquals(new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"), joinFileDescriptorBo3.getOpenFdCountJoinValue());

        JoinFileDescriptorBo joinFileDescriptorBo4 = joinFileDescriptorBoList.get(3);
        assertEquals(joinFileDescriptorBo4.getId(), "id1");
        assertEquals(joinFileDescriptorBo4.getTimestamp(), 1487149815000L);
        assertEquals(new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"), joinFileDescriptorBo4.getOpenFdCountJoinValue());

        JoinFileDescriptorBo joinFileDescriptorBo5 = joinFileDescriptorBoList.get(4);
        assertEquals(joinFileDescriptorBo5.getId(), "id1");
        assertEquals(joinFileDescriptorBo5.getTimestamp(), 1487149820000L);
        assertEquals(new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"), joinFileDescriptorBo5.getOpenFdCountJoinValue());
    }

    private JoinApplicationStatBo createJoinApplicationStatBo7(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        createJoinFileDescriptorBoList(id, timestamp, plus).forEach(joinApplicationStatBo::addFileDescriptor);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo.build();
    }

    private List<JoinFileDescriptorBo> createJoinFileDescriptorBoList(final String id, final long currentTime, int plus) {
        final List<JoinFileDescriptorBo> joinFileDescriptorBoList = new ArrayList<>();
        JoinFileDescriptorBo joinFileDescriptorBo1 = new JoinFileDescriptorBo(id, 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", currentTime);
        JoinFileDescriptorBo joinFileDescriptorBo2 = new JoinFileDescriptorBo(id, 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", currentTime + 5000);
        JoinFileDescriptorBo joinFileDescriptorBo3 = new JoinFileDescriptorBo(id, 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", currentTime + 10000);
        JoinFileDescriptorBo joinFileDescriptorBo4 = new JoinFileDescriptorBo(id, 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", currentTime + 15000);
        JoinFileDescriptorBo joinFileDescriptorBo5 = new JoinFileDescriptorBo(id, 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", currentTime + 20000);

        joinFileDescriptorBoList.add(joinFileDescriptorBo1);
        joinFileDescriptorBoList.add(joinFileDescriptorBo2);
        joinFileDescriptorBoList.add(joinFileDescriptorBo3);
        joinFileDescriptorBoList.add(joinFileDescriptorBo4);
        joinFileDescriptorBoList.add(joinFileDescriptorBo5);

        return joinFileDescriptorBoList;
    }

    @Test
    public void joinApplicationStatBoByTimeSlice15Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinDirectBufferBo> joinDirectBufferBoList = resultJoinApplicationStatBo.getJoinDirectBufferBoList();
        joinDirectBufferBoList.sort(Comparator.comparingLong(JoinDirectBufferBo::getTimestamp));
        assertJoinDirectBufferBoList(joinDirectBufferBoList);
    }
    @Test
    public void joinApplicationStatBoByTimeSlice16Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<>();

        JoinDirectBufferBo joinDirectBufferBo1_1 = new JoinDirectBufferBo("agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinDirectBufferBo joinDirectBufferBo1_2 = new JoinDirectBufferBo("agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinDirectBufferBo joinDirectBufferBo1_3 = new JoinDirectBufferBo("agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", 1498462555000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo1 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo1.addDirectBuffer(joinDirectBufferBo1_1);
        joinApplicationStatBo1.addDirectBuffer(joinDirectBufferBo1_2);
        joinApplicationStatBo1.addDirectBuffer(joinDirectBufferBo1_3);

        joinApplicationStatBoList.add(joinApplicationStatBo1.build());

        JoinDirectBufferBo joinDirectBufferBo2_1 = new JoinDirectBufferBo("agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinDirectBufferBo joinDirectBufferBo2_2 = new JoinDirectBufferBo("agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinDirectBufferBo joinDirectBufferBo2_3 = new JoinDirectBufferBo("agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", 1498462555000L);
        JoinDirectBufferBo joinDirectBufferBo2_4 = new JoinDirectBufferBo("agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", 1498462560000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo2 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo2.addDirectBuffer(joinDirectBufferBo2_1);
        joinApplicationStatBo2.addDirectBuffer(joinDirectBufferBo2_2);
        joinApplicationStatBo2.addDirectBuffer(joinDirectBufferBo2_3);
        joinApplicationStatBo2.addDirectBuffer(joinDirectBufferBo2_4);
        joinApplicationStatBoList.add(joinApplicationStatBo2.build());

        JoinDirectBufferBo joinDirectBufferBo3_1 = new JoinDirectBufferBo("agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinDirectBufferBo joinDirectBufferBo3_2 = new JoinDirectBufferBo("agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinDirectBufferBo joinDirectBufferBo3_3 = new JoinDirectBufferBo("agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", 1498462565000L);
        JoinApplicationStatBo.Builder joinApplicationStatBo3 = JoinApplicationStatBo.newBuilder("test_app", 1498462545000L);
        joinApplicationStatBo3.addDirectBuffer(joinDirectBufferBo3_1);
        joinApplicationStatBo3.addDirectBuffer(joinDirectBufferBo3_2);
        joinApplicationStatBo3.addDirectBuffer(joinDirectBufferBo3_3);
        joinApplicationStatBoList.add(joinApplicationStatBo3.build());


        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinDirectBufferBo> joinDirectBufferBoList = joinApplicationStatBo.getJoinDirectBufferBoList();
        joinDirectBufferBoList.sort(Comparator.comparingLong(JoinDirectBufferBo::getTimestamp));

        assertEquals(joinDirectBufferBoList.size(), 5);
        assertEquals(joinDirectBufferBoList.get(0).getDirectCountJoinValue().getAvg(), 330,0);
        assertEquals(joinDirectBufferBoList.get(0).getDirectMemoryUsedJoinValue().getAvg(), 330,0);
        assertEquals(joinDirectBufferBoList.get(0).getMappedCountJoinValue().getAvg(), 330,0);
        assertEquals(joinDirectBufferBoList.get(0).getMappedMemoryUsedJoinValue().getAvg(), 330,0);

        assertEquals(joinDirectBufferBoList.get(1).getDirectCountJoinValue().getAvg(), 220,0);
        assertEquals(joinDirectBufferBoList.get(1).getDirectMemoryUsedJoinValue().getAvg(), 220,0);
        assertEquals(joinDirectBufferBoList.get(1).getMappedCountJoinValue().getAvg(), 220,0);
        assertEquals(joinDirectBufferBoList.get(1).getMappedMemoryUsedJoinValue().getAvg(), 220,0);

        assertEquals(joinDirectBufferBoList.get(2).getDirectCountJoinValue().getAvg(), 330,0);
        assertEquals(joinDirectBufferBoList.get(2).getDirectMemoryUsedJoinValue().getAvg(), 330,0);
        assertEquals(joinDirectBufferBoList.get(2).getMappedCountJoinValue().getAvg(), 330,0);
        assertEquals(joinDirectBufferBoList.get(2).getMappedMemoryUsedJoinValue().getAvg(), 330,0);

        assertEquals(joinDirectBufferBoList.get(3).getDirectCountJoinValue().getAvg(), 770,0);
        assertEquals(joinDirectBufferBoList.get(3).getDirectMemoryUsedJoinValue().getAvg(), 770,0);
        assertEquals(joinDirectBufferBoList.get(3).getMappedCountJoinValue().getAvg(), 770,0);
        assertEquals(joinDirectBufferBoList.get(3).getMappedMemoryUsedJoinValue().getAvg(), 770,0);

        assertEquals(joinDirectBufferBoList.get(4).getDirectCountJoinValue().getAvg(), 880,0);
        assertEquals(joinDirectBufferBoList.get(4).getDirectMemoryUsedJoinValue().getAvg(), 880,0);
        assertEquals(joinDirectBufferBoList.get(4).getMappedCountJoinValue().getAvg(), 880,0);
        assertEquals(joinDirectBufferBoList.get(4).getMappedMemoryUsedJoinValue().getAvg(), 880,0);
    }


    private void assertJoinDirectBufferBoList(List<JoinDirectBufferBo> joinDirectBufferBoList) {
        assertEquals(joinDirectBufferBoList.size(), 5);
        //1
        JoinDirectBufferBo joinDirectBufferBo1 = joinDirectBufferBoList.get(0);
        assertEquals(joinDirectBufferBo1.getId(), "id1");
        assertEquals(joinDirectBufferBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinDirectBufferBo1.getDirectCountJoinValue(), new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"));
        assertEquals(joinDirectBufferBo1.getDirectMemoryUsedJoinValue(), new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"));
        assertEquals(joinDirectBufferBo1.getMappedCountJoinValue(), new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"));
        assertEquals(joinDirectBufferBo1.getMappedMemoryUsedJoinValue(), new JoinLongFieldBo(486L, 220L, "id5_2", 910L, "id4_1"));

        //2
        JoinDirectBufferBo joinDirectBufferBo2 = joinDirectBufferBoList.get(1);
        assertEquals(joinDirectBufferBo2.getId(), "id1");
        assertEquals(joinDirectBufferBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinDirectBufferBo2.getDirectCountJoinValue(), new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"));
        assertEquals(joinDirectBufferBo2.getDirectMemoryUsedJoinValue(), new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"));
        assertEquals(joinDirectBufferBo2.getMappedCountJoinValue(), new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"));
        assertEquals(joinDirectBufferBo2.getMappedMemoryUsedJoinValue(), new JoinLongFieldBo(386L, 350L, "id5_2", 810L, "id4_1"));

        //3
        JoinDirectBufferBo joinDirectBufferBo3 = joinDirectBufferBoList.get(2);
        assertEquals(joinDirectBufferBo3.getId(), "id1");
        assertEquals(joinDirectBufferBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinDirectBufferBo3.getDirectCountJoinValue(), new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"));
        assertEquals(joinDirectBufferBo3.getDirectMemoryUsedJoinValue(), new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"));
        assertEquals(joinDirectBufferBo3.getMappedCountJoinValue(), new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"));
        assertEquals(joinDirectBufferBo3.getMappedMemoryUsedJoinValue(), new JoinLongFieldBo(286L, 220L, "id5_2", 710L, "id4_1"));

        //4
        JoinDirectBufferBo joinDirectBufferBo4 = joinDirectBufferBoList.get(3);
        assertEquals(joinDirectBufferBo4.getId(), "id1");
        assertEquals(joinDirectBufferBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinDirectBufferBo4.getDirectCountJoinValue(), new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"));
        assertEquals(joinDirectBufferBo4.getDirectMemoryUsedJoinValue(), new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"));
        assertEquals(joinDirectBufferBo4.getMappedCountJoinValue(), new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"));
        assertEquals(joinDirectBufferBo4.getMappedMemoryUsedJoinValue(), new JoinLongFieldBo(186L, 120L, "id5_2", 610L, "id4_1"));

        //5
        JoinDirectBufferBo joinDirectBufferBo5 = joinDirectBufferBoList.get(4);
        assertEquals(joinDirectBufferBo5.getId(), "id1");
        assertEquals(joinDirectBufferBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinDirectBufferBo5.getDirectCountJoinValue(), new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"));
        assertEquals(joinDirectBufferBo5.getDirectMemoryUsedJoinValue(), new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"));
        assertEquals(joinDirectBufferBo5.getMappedCountJoinValue(), new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"));
        assertEquals(joinDirectBufferBo5.getMappedMemoryUsedJoinValue(), new JoinLongFieldBo(86L, 20L, "id5_2", 930L, "id4_1"));
    }

    private JoinApplicationStatBo createJoinApplicationStatBo8(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo.Builder joinApplicationStatBo = JoinApplicationStatBo.newBuilder(id, timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        createJoinDirectBufferBoList(id, timestamp, plus).forEach(joinApplicationStatBo::addDirectBuffer);
        return joinApplicationStatBo.build();
    }

    private List<JoinDirectBufferBo> createJoinDirectBufferBoList(final String id, final long currentTime, int plus) {
        final List<JoinDirectBufferBo> joinDirectBufferBoList = new ArrayList<>();
        JoinDirectBufferBo joinDirectBufferBo1 = new JoinDirectBufferBo(id, 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", 500 + plus, 870 + plus, id + "_1", 270 + plus, id + "_2", currentTime);
        JoinDirectBufferBo joinDirectBufferBo2 = new JoinDirectBufferBo(id, 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", 400 + plus, 770 + plus, id + "_1", 400 + plus, id + "_2", currentTime + 5000);
        JoinDirectBufferBo joinDirectBufferBo3 = new JoinDirectBufferBo(id, 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", 300 + plus, 670 + plus, id + "_1", 270 + plus, id + "_2", currentTime + 10000);
        JoinDirectBufferBo joinDirectBufferBo4 = new JoinDirectBufferBo(id, 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", 200 + plus, 570 + plus, id + "_1", 170 + plus, id + "_2", currentTime + 15000);
        JoinDirectBufferBo joinDirectBufferBo5 = new JoinDirectBufferBo(id, 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", 100 + plus, 890 + plus, id + "_1", 70 + plus, id + "_2", currentTime + 20000);

        joinDirectBufferBoList.add(joinDirectBufferBo1);
        joinDirectBufferBoList.add(joinDirectBufferBo2);
        joinDirectBufferBoList.add(joinDirectBufferBo3);
        joinDirectBufferBoList.add(joinDirectBufferBo4);
        joinDirectBufferBoList.add(joinDirectBufferBo5);

        return joinDirectBufferBoList;
    }

    @Test
    public void createJoinApplicationStatBoTest() {

        JoinAgentStatBo.Builder builder = JoinAgentStatBo.newBuilder("Agent", Long.MIN_VALUE,  1498462565000L);;

        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462565000L);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462570000L);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462575000L);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", 1498462580000L);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", 1498462585000L);
        builder.addCpuLoadBo(joinCpuLoadBo1);
        builder.addCpuLoadBo(joinCpuLoadBo2);
        builder.addCpuLoadBo(joinCpuLoadBo3);
        builder.addCpuLoadBo(joinCpuLoadBo4);
        builder.addCpuLoadBo(joinCpuLoadBo5);

        JoinMemoryBo joinMemoryBo1 = new JoinMemoryBo("agent1", 1498462565000L, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo2 = new JoinMemoryBo("agent1", 1498462570000L, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo3 = new JoinMemoryBo("agent1", 1498462575000L, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo4 = new JoinMemoryBo("agent1", 1498462580000L, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo5 = new JoinMemoryBo("agent1", 1498462585000L, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1");
        builder.addMemory(joinMemoryBo1);
        builder.addMemory(joinMemoryBo2);
        builder.addMemory(joinMemoryBo3);
        builder.addMemory(joinMemoryBo4);
        builder.addMemory(joinMemoryBo5);

        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", 1498462565000L);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", 1498462570000L);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462575000L);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", 1498462580000L);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", 1498462585000L);
        builder.addTransaction(joinTransactionBo1);
        builder.addTransaction(joinTransactionBo2);
        builder.addTransaction(joinTransactionBo3);
        builder.addTransaction(joinTransactionBo4);
        builder.addTransaction(joinTransactionBo5);

        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462565000L);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462570000L);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462575000L);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462580000L);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462585000L);
        builder.addActiveTrace(joinActiveTraceBo1);
        builder.addActiveTrace(joinActiveTraceBo2);
        builder.addActiveTrace(joinActiveTraceBo3);
        builder.addActiveTrace(joinActiveTraceBo4);
        builder.addActiveTrace(joinActiveTraceBo5);

        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo("agent1", 1498462565000L, 3000, 2, "app_1_1", 6000, "app_1_2");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo("agent1", 1498462570000L, 4000, 200, "app_2_1", 9000, "app_2_2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo("agent1", 1498462575000L, 2000, 20, "app_3_1", 7000, "app_3_2");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo("agent1", 1498462580000L, 5000, 20, "app_4_1", 8000, "app_4_2");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo("agent1", 1498462585000L, 1000, 10, "app_5_1", 6600, "app_5_2");
        builder.addResponseTime(joinResponseTimeBo1);
        builder.addResponseTime(joinResponseTimeBo2);
        builder.addResponseTime(joinResponseTimeBo3);
        builder.addResponseTime(joinResponseTimeBo4);
        builder.addResponseTime(joinResponseTimeBo5);

        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"));
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6"));
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462565000L);

        List<JoinDataSourceBo> joinDataSourceBoList2 = new ArrayList<>();
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"));
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7"));
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, 1498462570000L);

        List<JoinDataSourceBo> joinDataSourceBoList3 = new ArrayList<>();
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"));
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8"));
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, 1498462575000L);

        List<JoinDataSourceBo> joinDataSourceBoList4 = new ArrayList<>();
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"));
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9"));
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, 1498462580000L);

        List<JoinDataSourceBo> joinDataSourceBoList5 = new ArrayList<>();
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"));
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10"));
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, 1498462585000L);

        builder.addDataSourceListBo(joinDataSourceListBo1);
        builder.addDataSourceListBo(joinDataSourceListBo2);
        builder.addDataSourceListBo(joinDataSourceListBo3);
        builder.addDataSourceListBo(joinDataSourceListBo4);
        builder.addDataSourceListBo(joinDataSourceListBo5);

        JoinFileDescriptorBo joinFileDescriptorBo1 = new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", 1498462565000L);
        JoinFileDescriptorBo joinFileDescriptorBo2 = new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", 1498462570000L);
        JoinFileDescriptorBo joinFileDescriptorBo3 = new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", 1498462575000L);
        JoinFileDescriptorBo joinFileDescriptorBo4 = new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", 1498462580000L);
        JoinFileDescriptorBo joinFileDescriptorBo5 = new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", 1498462585000L);
        builder.addFileDescriptor(joinFileDescriptorBo1);
        builder.addFileDescriptor(joinFileDescriptorBo2);
        builder.addFileDescriptor(joinFileDescriptorBo3);
        builder.addFileDescriptor(joinFileDescriptorBo4);
        builder.addFileDescriptor(joinFileDescriptorBo5);

        JoinDirectBufferBo joinDirectBufferBo1 = new JoinDirectBufferBo("agent1", 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 1498462565000L);
        JoinDirectBufferBo joinDirectBufferBo2 = new JoinDirectBufferBo("agent2", 33, 40, "agent2", 10, "agent2"
                , 33, 40, "agent2", 10, "agent2"
                , 33, 40, "agent2", 10, "agent2"
                , 33, 40, "agent2", 10, "agent2"
                , 1498462570000L);
        JoinDirectBufferBo joinDirectBufferBo3 = new JoinDirectBufferBo("agent3", 55, 60, "agent3", 7, "agent3"
                , 55, 60, "agent3", 7, "agent3"
                , 55, 60, "agent3", 7, "agent3"
                , 55, 60, "agent3", 7, "agent3"
                , 1498462575000L);
        JoinDirectBufferBo joinDirectBufferBo4 = new JoinDirectBufferBo("agent4", 11, 80, "agent4", 8, "agent4"
                , 11, 80, "agent4", 8, "agent4"
                , 11, 80, "agent4", 8, "agent4"
                , 11, 80, "agent4", 8, "agent4"
                , 1498462580000L);
        JoinDirectBufferBo joinDirectBufferBo5 = new JoinDirectBufferBo("agent5", 22, 70, "agent5", 12, "agent5"
                , 22, 70, "agent5", 12, "agent5"
                , 22, 70, "agent5", 12, "agent5"
                , 22, 70, "agent5", 12, "agent5"
                , 1498462585000L);
        builder.addDirectBuffer(joinDirectBufferBo1);
        builder.addDirectBuffer(joinDirectBufferBo2);
        builder.addDirectBuffer(joinDirectBufferBo3);
        builder.addDirectBuffer(joinDirectBufferBo4);
        builder.addDirectBuffer(joinDirectBufferBo5);

        JoinAgentStatBo agentStatBo = builder.build();
        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", agentStatBo, 60000);
        assertEquals(joinApplicationStatBoList.size(), 1);
        JoinApplicationStatBo joinApplicationStatBo = joinApplicationStatBoList.get(0);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 5);
        assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 5);
        assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 5);
        assertEquals(joinApplicationStatBo.getJoinActiveTraceBoList().size(), 5);
        assertEquals(joinApplicationStatBo.getJoinResponseTimeBoList().size(), 5);
        assertEquals(joinApplicationStatBo.getJoinDataSourceListBoList().size(), 5);
        assertEquals(joinApplicationStatBo.getJoinFileDescriptorBoList().size(), 5);
    }

    @Test
    public void createJoinApplicationStatBo2Test() {
        JoinAgentStatBo.Builder builder = JoinAgentStatBo.newBuilder("Agent", Long.MIN_VALUE,  1498462545000L);

        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462555000L);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", 1498462560000L);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", 1498462565000L);
        builder.addCpuLoadBo(joinCpuLoadBo1);
        builder.addCpuLoadBo(joinCpuLoadBo2);
        builder.addCpuLoadBo(joinCpuLoadBo3);
        builder.addCpuLoadBo(joinCpuLoadBo4);
        builder.addCpuLoadBo(joinCpuLoadBo5);

        JoinMemoryBo joinMemoryBo1 = new JoinMemoryBo("agent1", 1498462545000L, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo2 = new JoinMemoryBo("agent1", 1498462550000L, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo3 = new JoinMemoryBo("agent1", 1498462555000L, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo4 = new JoinMemoryBo("agent1", 1498462560000L, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo5 = new JoinMemoryBo("agent1", 1498462565000L, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1");
        builder.addMemory(joinMemoryBo1);
        builder.addMemory(joinMemoryBo2);
        builder.addMemory(joinMemoryBo3);
        builder.addMemory(joinMemoryBo4);
        builder.addMemory(joinMemoryBo5);

        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent1", 1498462550000L);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462555000L);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", 1498462560000L);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", 1498462565000L);
        builder.addTransaction(joinTransactionBo1);
        builder.addTransaction(joinTransactionBo2);
        builder.addTransaction(joinTransactionBo3);
        builder.addTransaction(joinTransactionBo4);
        builder.addTransaction(joinTransactionBo5);

        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462555000L);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462560000L);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462565000L);
        builder.addActiveTrace(joinActiveTraceBo1);
        builder.addActiveTrace(joinActiveTraceBo2);
        builder.addActiveTrace(joinActiveTraceBo3);
        builder.addActiveTrace(joinActiveTraceBo4);
        builder.addActiveTrace(joinActiveTraceBo5);


        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo("agent1", 1498462545000L, 3000, 2, "app_1_1", 6000, "app_1_2");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo("agent1", 1498462550000L, 4000, 200, "app_2_1", 9000, "app_2_2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo("agent1", 1498462555000L, 2000, 20, "app_3_1", 7000, "app_3_2");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo("agent1", 1498462560000L, 5000, 20, "app_4_1", 8000, "app_4_2");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo("agent1", 1498462565000L, 1000, 10, "app_5_1", 6600, "app_5_2");
        builder.addResponseTime(joinResponseTimeBo1);
        builder.addResponseTime(joinResponseTimeBo2);
        builder.addResponseTime(joinResponseTimeBo3);
        builder.addResponseTime(joinResponseTimeBo4);
        builder.addResponseTime(joinResponseTimeBo5);

        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"));
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6"));
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462545000L);

        List<JoinDataSourceBo> joinDataSourceBoList2 = new ArrayList<>();
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"));
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7"));
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, 1498462550000L);

        List<JoinDataSourceBo> joinDataSourceBoList3 = new ArrayList<>();
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"));
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8"));
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, 1498462555000L);

        List<JoinDataSourceBo> joinDataSourceBoList4 = new ArrayList<>();
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"));
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9"));
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, 1498462560000L);

        List<JoinDataSourceBo> joinDataSourceBoList5 = new ArrayList<>();
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"));
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10"));
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, 1498462565000L);

        builder.addDataSourceListBo(joinDataSourceListBo1);
        builder.addDataSourceListBo(joinDataSourceListBo2);
        builder.addDataSourceListBo(joinDataSourceListBo3);
        builder.addDataSourceListBo(joinDataSourceListBo4);
        builder.addDataSourceListBo(joinDataSourceListBo5);

        JoinFileDescriptorBo joinFileDescriptorBo1 = new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo2 = new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo3 = new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", 1498462555000L);
        JoinFileDescriptorBo joinFileDescriptorBo4 = new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", 1498462560000L);
        JoinFileDescriptorBo joinFileDescriptorBo5 = new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", 1498462565000L);
        builder.addFileDescriptor(joinFileDescriptorBo1);
        builder.addFileDescriptor(joinFileDescriptorBo2);
        builder.addFileDescriptor(joinFileDescriptorBo3);
        builder.addFileDescriptor(joinFileDescriptorBo4);
        builder.addFileDescriptor(joinFileDescriptorBo5);

        JoinDirectBufferBo joinDirectBufferBo1 = new JoinDirectBufferBo("agent1", 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 1498462545000L);
        JoinDirectBufferBo joinDirectBufferBo2 = new JoinDirectBufferBo("agent1", 33, 40, "agent1", 10, "agent1"
                , 33, 40, "agent1", 10, "agent1"
                , 33, 40, "agent1", 10, "agent1"
                , 33, 40, "agent1", 10, "agent1"
                , 1498462550000L);
        JoinDirectBufferBo joinDirectBufferBo3 = new JoinDirectBufferBo("agent1", 55, 60, "agent1", 7, "agent1"
                , 55, 60, "agent1", 7, "agent1"
                , 55, 60, "agent1", 7, "agent1"
                , 55, 60, "agent1", 7, "agent1"
                , 1498462555000L);
        JoinDirectBufferBo joinDirectBufferBo4 = new JoinDirectBufferBo("agent1", 11, 80, "agent1", 8, "agent1"
                , 11, 80, "agent1", 8, "agent1"
                , 11, 80, "agent1", 8, "agent1"
                , 11, 80, "agent1", 8, "agent1"
                , 1498462560000L);
        JoinDirectBufferBo joinDirectBufferBo5 = new JoinDirectBufferBo("agent1", 22, 70, "agent1", 12, "agent1"
                , 22, 70, "agent1", 12, "agent1"
                , 22, 70, "agent1", 12, "agent1"
                , 22, 70, "agent1", 12, "agent1"
                , 1498462565000L);
        builder.addDirectBuffer(joinDirectBufferBo1);
        builder.addDirectBuffer(joinDirectBufferBo2);
        builder.addDirectBuffer(joinDirectBufferBo3);
        builder.addDirectBuffer(joinDirectBufferBo4);
        builder.addDirectBuffer(joinDirectBufferBo5);

        JoinAgentStatBo statBo = builder.build();
        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", statBo, 60000);
        assertEquals(joinApplicationStatBoList.size(), 2);
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            assertEquals(joinApplicationStatBo.getId(), "test_app");
            if (joinApplicationStatBo.getTimestamp() == 1498462560000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinActiveTraceBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinResponseTimeBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinDataSourceListBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinFileDescriptorBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinDirectBufferBoList().size(), 2);
            } else if (joinApplicationStatBo.getTimestamp() == 1498462500000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinActiveTraceBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinResponseTimeBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinDataSourceListBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinFileDescriptorBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinDirectBufferBoList().size(), 3);
            } else {
                fail();
            }
        }
    }

    @Test
    public void createJoinApplicationStatBo3Test() {
        JoinAgentStatBo.Builder builder = JoinAgentStatBo.newBuilder("Agent", Long.MIN_VALUE,  1498462545000L);

        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462555000L);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", 1498462560000L);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", 1498462565000L);
        builder.addCpuLoadBo(joinCpuLoadBo1);
        builder.addCpuLoadBo(joinCpuLoadBo2);
        builder.addCpuLoadBo(joinCpuLoadBo3);
        builder.addCpuLoadBo(joinCpuLoadBo4);
        builder.addCpuLoadBo(joinCpuLoadBo5);

        JoinMemoryBo joinMemoryBo1 = new JoinMemoryBo("agent1", 1498462545000L, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo2 = new JoinMemoryBo("agent1", 1498462550000L, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo3 = new JoinMemoryBo("agent1", 1498462555000L, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo4 = new JoinMemoryBo("agent1", 1498462560000L, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo5 = new JoinMemoryBo("agent1", 1498462565000L, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1");
        builder.addMemory(joinMemoryBo1);
        builder.addMemory(joinMemoryBo2);
        builder.addMemory(joinMemoryBo3);
        builder.addMemory(joinMemoryBo4);
        builder.addMemory(joinMemoryBo5);

        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462555000L);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", 1498462560000L);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", 1498462565000L);
        builder.addTransaction(joinTransactionBo1);
        builder.addTransaction(joinTransactionBo2);
        builder.addTransaction(joinTransactionBo3);
        builder.addTransaction(joinTransactionBo4);
        builder.addTransaction(joinTransactionBo5);

        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462555000L);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462560000L);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462565000L);
        builder.addActiveTrace(joinActiveTraceBo1);
        builder.addActiveTrace(joinActiveTraceBo2);
        builder.addActiveTrace(joinActiveTraceBo3);
        builder.addActiveTrace(joinActiveTraceBo4);
        builder.addActiveTrace(joinActiveTraceBo5);

        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo("agent1", 1498462545000L, 3000, 2, "app_1_1", 6000, "app_1_2");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo("agent1", 1498462550000L, 4000, 200, "app_2_1", 9000, "app_2_2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo("agent1", 1498462555000L, 2000, 20, "app_3_1", 7000, "app_3_2");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo("agent1", 1498462560000L, 5000, 20, "app_4_1", 8000, "app_4_2");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo("agent1", 1498462565000L, 1000, 10, "app_5_1", 6600, "app_5_2");
        builder.addResponseTime(joinResponseTimeBo1);
        builder.addResponseTime(joinResponseTimeBo2);
        builder.addResponseTime(joinResponseTimeBo3);
        builder.addResponseTime(joinResponseTimeBo4);
        builder.addResponseTime(joinResponseTimeBo5);

        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"));
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6"));
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462545000L);

        List<JoinDataSourceBo> joinDataSourceBoList2 = new ArrayList<>();
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"));
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7"));
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, 1498462550000L);

        List<JoinDataSourceBo> joinDataSourceBoList3 = new ArrayList<>();
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"));
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8"));
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, 1498462555000L);

        List<JoinDataSourceBo> joinDataSourceBoList4 = new ArrayList<>();
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"));
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9"));
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, 1498462560000L);

        List<JoinDataSourceBo> joinDataSourceBoList5 = new ArrayList<>();
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"));
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10"));
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, 1498462565000L);

        builder.addDataSourceListBo(joinDataSourceListBo1);
        builder.addDataSourceListBo(joinDataSourceListBo2);
        builder.addDataSourceListBo(joinDataSourceListBo3);
        builder.addDataSourceListBo(joinDataSourceListBo4);
        builder.addDataSourceListBo(joinDataSourceListBo5);


        JoinFileDescriptorBo joinFileDescriptorBo1 = new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo2 = new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo3 = new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", 1498462555000L);
        JoinFileDescriptorBo joinFileDescriptorBo4 = new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", 1498462560000L);
        JoinFileDescriptorBo joinFileDescriptorBo5 = new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", 1498462565000L);
        builder.addFileDescriptor(joinFileDescriptorBo1);
        builder.addFileDescriptor(joinFileDescriptorBo2);
        builder.addFileDescriptor(joinFileDescriptorBo3);
        builder.addFileDescriptor(joinFileDescriptorBo4);
        builder.addFileDescriptor(joinFileDescriptorBo5);

        JoinDirectBufferBo joinDirectBufferBo1 = new JoinDirectBufferBo("agent1", 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 44, 70, "agent1", 30, "agent1"
                , 1498462545000L);
        JoinDirectBufferBo joinDirectBufferBo2 = new JoinDirectBufferBo("agent1", 33, 40, "agent1", 10, "agent1"
                , 33, 40, "agent1", 10, "agent1"
                , 33, 40, "agent1", 10, "agent1"
                , 33, 40, "agent1", 10, "agent1"
                , 1498462550000L);
        JoinDirectBufferBo joinDirectBufferBo3 = new JoinDirectBufferBo("agent1", 55, 60, "agent1", 7, "agent1"
                , 55, 60, "agent1", 7, "agent1"
                , 55, 60, "agent1", 7, "agent1"
                , 55, 60, "agent1", 7, "agent1"
                , 1498462555000L);
        JoinDirectBufferBo joinDirectBufferBo4 = new JoinDirectBufferBo("agent1", 11, 80, "agent1", 8, "agent1"
                , 11, 80, "agent1", 8, "agent1"
                , 11, 80, "agent1", 8, "agent1"
                , 11, 80, "agent1", 8, "agent1"
                , 1498462560000L);
        JoinDirectBufferBo joinDirectBufferBo5 = new JoinDirectBufferBo("agent1", 22, 70, "agent1", 12, "agent1"
                , 22, 70, "agent1", 12, "agent1"
                , 22, 70, "agent1", 12, "agent1"
                , 22, 70, "agent1", 12, "agent1"
                , 1498462565000L);
        builder.addDirectBuffer(joinDirectBufferBo1);
        builder.addDirectBuffer(joinDirectBufferBo2);
        builder.addDirectBuffer(joinDirectBufferBo3);
        builder.addDirectBuffer(joinDirectBufferBo4);
        builder.addDirectBuffer(joinDirectBufferBo5);

        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", builder.build(), 10000);
        assertEquals(joinApplicationStatBoList.size(), 3);
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            assertEquals(joinApplicationStatBo.getId(), "test_app");
            if (joinApplicationStatBo.getTimestamp() == 1498462560000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinActiveTraceBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinResponseTimeBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinDataSourceListBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinFileDescriptorBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinDirectBufferBoList().size(), 2);
            } else if (joinApplicationStatBo.getTimestamp() == 1498462540000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinActiveTraceBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinResponseTimeBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinDataSourceListBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinFileDescriptorBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinDirectBufferBoList().size(), 1);
            } else if (joinApplicationStatBo.getTimestamp() == 1498462550000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinActiveTraceBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinResponseTimeBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinDataSourceListBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinFileDescriptorBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinDirectBufferBoList().size(), 2);
            } else {
                fail();
            }
        }
    }

}


