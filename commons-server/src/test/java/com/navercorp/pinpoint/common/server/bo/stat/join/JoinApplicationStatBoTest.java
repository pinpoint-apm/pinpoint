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

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinApplicationStatBoTest {

    @Test
    public void joinApplicationStatBoByTimeSliceTest() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id1", currentTime, 1));
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id2", currentTime + 1000, -4));
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id3", currentTime + 2000, -3));
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id4", currentTime + 3000, 4));
        joinApplicationStatBoList.add(createJoinApplicationStatBo("id5", currentTime + 4000, -5));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinCpuLoadBo> joinCpuLoadBoList = resultJoinApplicationStatBo.getJoinCpuLoadBoList();
        Collections.sort(joinCpuLoadBoList, new ComparatorImpl());
        assertJoinCpuLoadBoList(joinCpuLoadBoList);
    }

    private class ComparatorImpl implements Comparator<JoinCpuLoadBo> {
        @Override
        public int compare(JoinCpuLoadBo bo1, JoinCpuLoadBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private void assertJoinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList) {
        assertEquals(joinCpuLoadBoList.size(), 5);
        JoinCpuLoadBo joinCpuLoadBo1 = joinCpuLoadBoList.get(0);
        assertEquals(joinCpuLoadBo1.getId(), "id1");
        assertEquals(joinCpuLoadBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinCpuLoadBo1.getJvmCpuLoad(), 48.6, 0);
        assertEquals(joinCpuLoadBo1.getMinJvmCpuLoad(), 22.0, 0);
        assertEquals(joinCpuLoadBo1.getMinJvmCpuAgentId(), "id5_2");
        assertEquals(joinCpuLoadBo1.getMaxJvmCpuLoad(), 91.0, 0);
        assertEquals(joinCpuLoadBo1.getMaxJvmCpuAgentId(), "id4_1");
        assertEquals(joinCpuLoadBo1.getSystemCpuLoad(), 78.6, 0);
        assertEquals(joinCpuLoadBo1.getMinSystemCpuLoad(), 41.0, 0);
        assertEquals(joinCpuLoadBo1.getMinSysCpuAgentId(), "id5_4");
        assertEquals(joinCpuLoadBo1.getMaxSystemCpuLoad(), 91.0, 0);
        assertEquals(joinCpuLoadBo1.getMaxSysCpuAgentId(), "id4_3");

        JoinCpuLoadBo joinCpuLoadBo2 = joinCpuLoadBoList.get(1);
        assertEquals(joinCpuLoadBo2.getId(), "id1");
        assertEquals(joinCpuLoadBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinCpuLoadBo2.getJvmCpuLoad(), 38.6, 0);
        assertEquals(joinCpuLoadBo2.getMinJvmCpuLoad(), 35.0, 0);
        assertEquals(joinCpuLoadBo2.getMinJvmCpuAgentId(), "id5_2");
        assertEquals(joinCpuLoadBo2.getMaxJvmCpuLoad(), 81.0, 0);
        assertEquals(joinCpuLoadBo2.getMaxJvmCpuAgentId(), "id4_1");
        assertEquals(joinCpuLoadBo2.getSystemCpuLoad(), 68.6, 0);
        assertEquals(joinCpuLoadBo2.getMinSystemCpuLoad(), 35.0, 0);
        assertEquals(joinCpuLoadBo2.getMinSysCpuAgentId(), "id5_4");
        assertEquals(joinCpuLoadBo2.getMaxSystemCpuLoad(), 81.0, 0);
        assertEquals(joinCpuLoadBo2.getMaxSysCpuAgentId(), "id4_3");

        JoinCpuLoadBo joinCpuLoadBo3 = joinCpuLoadBoList.get(2);
        assertEquals(joinCpuLoadBo3.getId(), "id1");
        assertEquals(joinCpuLoadBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinCpuLoadBo3.getJvmCpuLoad(), 28.6, 0);
        assertEquals(joinCpuLoadBo3.getMinJvmCpuLoad(), 22.0, 0);
        assertEquals(joinCpuLoadBo3.getMinJvmCpuAgentId(), "id5_2");
        assertEquals(joinCpuLoadBo3.getMaxJvmCpuLoad(), 71.0, 0);
        assertEquals(joinCpuLoadBo3.getMaxJvmCpuAgentId(), "id4_1");
        assertEquals(joinCpuLoadBo3.getSystemCpuLoad(), 58.6, 0);
        assertEquals(joinCpuLoadBo3.getMinSystemCpuLoad(), 22.0, 0);
        assertEquals(joinCpuLoadBo3.getMinSysCpuAgentId(), "id5_4");
        assertEquals(joinCpuLoadBo3.getMaxSystemCpuLoad(), 71.0, 0);
        assertEquals(joinCpuLoadBo3.getMaxSysCpuAgentId(), "id4_3");

        JoinCpuLoadBo joinCpuLoadBo4 = joinCpuLoadBoList.get(3);
        assertEquals(joinCpuLoadBo4.getId(), "id1");
        assertEquals(joinCpuLoadBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinCpuLoadBo4.getJvmCpuLoad(), 18.6, 0);
        assertEquals(joinCpuLoadBo4.getMinJvmCpuLoad(), 12.0, 0);
        assertEquals(joinCpuLoadBo4.getMinJvmCpuAgentId(), "id5_2");
        assertEquals(joinCpuLoadBo4.getMaxJvmCpuLoad(), 61.0, 0);
        assertEquals(joinCpuLoadBo4.getMaxJvmCpuAgentId(), "id4_1");
        assertEquals(joinCpuLoadBo4.getSystemCpuLoad(), 38.6, 0);
        assertEquals(joinCpuLoadBo4.getMinSystemCpuLoad(), 13.0, 0);
        assertEquals(joinCpuLoadBo4.getMinSysCpuAgentId(), "id5_4");
        assertEquals(joinCpuLoadBo4.getMaxSystemCpuLoad(), 93.0, 0);
        assertEquals(joinCpuLoadBo4.getMaxSysCpuAgentId(), "id4_3");

        JoinCpuLoadBo joinCpuLoadBo5 = joinCpuLoadBoList.get(4);
        assertEquals(joinCpuLoadBo5.getId(), "id1");
        assertEquals(joinCpuLoadBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinCpuLoadBo5.getJvmCpuLoad(), 8.6, 0);
        assertEquals(joinCpuLoadBo5.getMinJvmCpuLoad(), 2.0, 0);
        assertEquals(joinCpuLoadBo5.getMinJvmCpuAgentId(), "id5_2");
        assertEquals(joinCpuLoadBo5.getMaxJvmCpuLoad(), 93.0, 0);
        assertEquals(joinCpuLoadBo5.getMaxJvmCpuAgentId(), "id4_1");
        assertEquals(joinCpuLoadBo5.getSystemCpuLoad(), 28.6, 0);
        assertEquals(joinCpuLoadBo5.getMinSystemCpuLoad(), 3.0, 0);
        assertEquals(joinCpuLoadBo5.getMinSysCpuAgentId(), "id5_4");
        assertEquals(joinCpuLoadBo5.getMaxSystemCpuLoad(), 63.0, 0);
        assertEquals(joinCpuLoadBo5.getMaxSysCpuAgentId(), "id4_3");
    }

    private JoinApplicationStatBo createJoinApplicationStatBo(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinCpuLoadBoList(createJoinCpuLoadBoList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinCpuLoadBo> createJoinCpuLoadBoList(final String id, final long currentTime, int plus) {
        final List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
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
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo2("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinMemoryBo> joinMemoryBoList = resultJoinApplicationStatBo.getJoinMemoryBoList();
        Collections.sort(joinMemoryBoList, new ComparatorImpl2());
        assertJoinMemoryBoList(joinMemoryBoList);
    }

    private void assertJoinMemoryBoList(List<JoinMemoryBo> joinMemoryBoList) {
        assertEquals(5, joinMemoryBoList.size());

        JoinMemoryBo joinMemoryBo1 = joinMemoryBoList.get(0);
        assertEquals("id1", joinMemoryBo1.getId());
        assertEquals(1487149800000L, joinMemoryBo1.getTimestamp());
        assertEquals(2986, joinMemoryBo1.getHeapUsed());
        assertEquals(1950, joinMemoryBo1.getMinHeapUsed());
        assertEquals(5040, joinMemoryBo1.getMaxHeapUsed());
        assertEquals("id5_1", joinMemoryBo1.getMinHeapAgentId());
        assertEquals("id4_2", joinMemoryBo1.getMaxHeapAgentId());
        assertEquals(486, joinMemoryBo1.getNonHeapUsed());
        assertEquals(0, joinMemoryBo1.getMinNonHeapUsed());
        assertEquals(640, joinMemoryBo1.getMaxNonHeapUsed());
        assertEquals("id5_3", joinMemoryBo1.getMinNonHeapAgentId());
        assertEquals("id4_4", joinMemoryBo1.getMaxNonHeapAgentId());

        JoinMemoryBo joinMemoryBo2 = joinMemoryBoList.get(1);
        assertEquals("id1", joinMemoryBo2.getId());
        assertEquals(1487149805000L, joinMemoryBo2.getTimestamp());
        assertEquals(3986, joinMemoryBo2.getHeapUsed());
        assertEquals(950, joinMemoryBo2.getMinHeapUsed());
        assertEquals(7040, joinMemoryBo2.getMaxHeapUsed());
        assertEquals("id5_1", joinMemoryBo2.getMinHeapAgentId());
        assertEquals("id4_2", joinMemoryBo2.getMaxHeapAgentId());
        assertEquals(386, joinMemoryBo2.getNonHeapUsed());
        assertEquals(100, joinMemoryBo2.getMinNonHeapUsed());
        assertEquals(640, joinMemoryBo2.getMaxNonHeapUsed());
        assertEquals("id5_3", joinMemoryBo2.getMinNonHeapAgentId());
        assertEquals("id4_4", joinMemoryBo2.getMaxNonHeapAgentId());

        JoinMemoryBo joinMemoryBo3 = joinMemoryBoList.get(2);
        assertEquals("id1", joinMemoryBo3.getId());
        assertEquals(1487149810000L, joinMemoryBo3.getTimestamp());
        assertEquals(4986, joinMemoryBo3.getHeapUsed());
        assertEquals(2950, joinMemoryBo3.getMinHeapUsed());
        assertEquals(8040, joinMemoryBo3.getMaxHeapUsed());
        assertEquals("id5_1", joinMemoryBo3.getMinHeapAgentId());
        assertEquals("id4_2", joinMemoryBo3.getMaxHeapAgentId());
        assertEquals(186, joinMemoryBo3.getNonHeapUsed());
        assertEquals(50, joinMemoryBo3.getMinNonHeapUsed());
        assertEquals(240, joinMemoryBo3.getMaxNonHeapUsed());
        assertEquals("id5_3", joinMemoryBo3.getMinNonHeapAgentId());
        assertEquals("id4_4", joinMemoryBo3.getMaxNonHeapAgentId());

        JoinMemoryBo joinMemoryBo4 = joinMemoryBoList.get(3);
        assertEquals("id1", joinMemoryBo4.getId());
        assertEquals(1487149815000L, joinMemoryBo4.getTimestamp());
        assertEquals(986, joinMemoryBo4.getHeapUsed());
        assertEquals(50, joinMemoryBo4.getMinHeapUsed());
        assertEquals(3040, joinMemoryBo4.getMaxHeapUsed());
        assertEquals("id5_1", joinMemoryBo4.getMinHeapAgentId());
        assertEquals("id4_2", joinMemoryBo4.getMaxHeapAgentId());
        assertEquals(86, joinMemoryBo4.getNonHeapUsed());
        assertEquals(850, joinMemoryBo4.getMinNonHeapUsed());
        assertEquals(1040, joinMemoryBo4.getMaxNonHeapUsed());
        assertEquals("id5_3", joinMemoryBo4.getMinNonHeapAgentId());
        assertEquals("id4_4", joinMemoryBo4.getMaxNonHeapAgentId());

        JoinMemoryBo joinMemoryBo5 = joinMemoryBoList.get(4);
        assertEquals("id1", joinMemoryBo5.getId());
        assertEquals(1487149820000L, joinMemoryBo5.getTimestamp());
        assertEquals(1986, joinMemoryBo5.getHeapUsed());
        assertEquals(950, joinMemoryBo5.getMinHeapUsed());
        assertEquals(6040, joinMemoryBo5.getMaxHeapUsed());
        assertEquals("id5_1", joinMemoryBo5.getMinHeapAgentId());
        assertEquals("id4_2", joinMemoryBo5.getMaxHeapAgentId());
        assertEquals(286, joinMemoryBo5.getNonHeapUsed());
        assertEquals(50, joinMemoryBo5.getMinNonHeapUsed());
        assertEquals(2940, joinMemoryBo5.getMaxNonHeapUsed());
        assertEquals("id5_3", joinMemoryBo5.getMinNonHeapAgentId());
        assertEquals("id4_4", joinMemoryBo5.getMaxNonHeapAgentId());
    }

    private class ComparatorImpl2 implements Comparator<JoinMemoryBo> {
        @Override
        public int compare(JoinMemoryBo bo1, JoinMemoryBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private JoinApplicationStatBo createJoinApplicationStatBo2(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinMemoryBoList(createJoinMemoryBoList2(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinMemoryBo> createJoinMemoryBoList2(final String id, final long currentTime, int plus) {
        final List<JoinMemoryBo> joinMemoryBoList = new ArrayList<JoinMemoryBo>();
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
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();

        List<JoinCpuLoadBo> joinCpuLoadBoList1 = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo1_1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo1_2 = new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo1_3 = new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462555000L);
        joinCpuLoadBoList1.add(joinCpuLoadBo1_1);
        joinCpuLoadBoList1.add(joinCpuLoadBo1_2);
        joinCpuLoadBoList1.add(joinCpuLoadBo1_3);
        JoinApplicationStatBo joinApplicationStatBo1 = new JoinApplicationStatBo();
        joinApplicationStatBo1.setId("test_app");
        joinApplicationStatBo1.setJoinCpuLoadBoList(joinCpuLoadBoList1);
        joinApplicationStatBo1.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo1);

        List<JoinCpuLoadBo> joinCpuLoadBoList2 = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo2_1 = new JoinCpuLoadBo("agent1", 33, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo2_2 = new JoinCpuLoadBo("agent1", 22, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo2_3 = new JoinCpuLoadBo("agent1", 11, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462555000L);
        JoinCpuLoadBo joinCpuLoadBo2_4 = new JoinCpuLoadBo("agent1", 77, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462560000L);
        joinCpuLoadBoList2.add(joinCpuLoadBo2_1);
        joinCpuLoadBoList2.add(joinCpuLoadBo2_2);
        joinCpuLoadBoList2.add(joinCpuLoadBo2_3);
        joinCpuLoadBoList2.add(joinCpuLoadBo2_4);
        JoinApplicationStatBo joinApplicationStatBo2 = new JoinApplicationStatBo();
        joinApplicationStatBo2.setId("test_app");
        joinApplicationStatBo2.setJoinCpuLoadBoList(joinCpuLoadBoList2);
        joinApplicationStatBo2.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo2);

        List<JoinCpuLoadBo> joinCpuLoadBoList3 = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo3_1 = new JoinCpuLoadBo("agent1", 22, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo3_2 = new JoinCpuLoadBo("agent1", 11, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo3_3 = new JoinCpuLoadBo("agent1", 88, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462565000L);
        joinCpuLoadBoList3.add(joinCpuLoadBo3_1);
        joinCpuLoadBoList3.add(joinCpuLoadBo3_2);
        joinCpuLoadBoList3.add(joinCpuLoadBo3_3);
        JoinApplicationStatBo joinApplicationStatBo3 = new JoinApplicationStatBo();
        joinApplicationStatBo3.setId("test_app");
        joinApplicationStatBo3.setJoinCpuLoadBoList(joinCpuLoadBoList3);
        joinApplicationStatBo3.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo3);


        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinCpuLoadBo> joinCpuLoadBoList = joinApplicationStatBo.getJoinCpuLoadBoList();
        Collections.sort(joinCpuLoadBoList, new ComparatorImpl());

        assertEquals(joinCpuLoadBoList.size(), 5);
        assertEquals(joinCpuLoadBoList.get(0).getJvmCpuLoad(), 33,0);
        assertEquals(joinCpuLoadBoList.get(1).getJvmCpuLoad(), 22,0);
        assertEquals(joinCpuLoadBoList.get(2).getJvmCpuLoad(), 33,0);
        assertEquals(joinCpuLoadBoList.get(3).getJvmCpuLoad(), 77,0);
        assertEquals(joinCpuLoadBoList.get(4).getJvmCpuLoad(), 88,0);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice4Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();

        List<JoinMemoryBo> joinMemoryBoList1 = new ArrayList<JoinMemoryBo>();
        JoinMemoryBo joinMemoryBo1_1 = new JoinMemoryBo("agent1", 1498462545000L, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo1_2 = new JoinMemoryBo("agent2", 1498462550000L, 4000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2");
        JoinMemoryBo joinMemoryBo1_3 = new JoinMemoryBo("agent3", 1498462555000L, 5000, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3");
        joinMemoryBoList1.add(joinMemoryBo1_1);
        joinMemoryBoList1.add(joinMemoryBo1_2);
        joinMemoryBoList1.add(joinMemoryBo1_3);
        JoinApplicationStatBo joinApplicationStatBo1 = new JoinApplicationStatBo();
        joinApplicationStatBo1.setId("test_app");
        joinApplicationStatBo1.setJoinMemoryBoList(joinMemoryBoList1);
        joinApplicationStatBo1.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo1);

        List<JoinMemoryBo> joinMemoryBoList2 = new ArrayList<JoinMemoryBo>();
        JoinMemoryBo joinMemoryBo2_1 = new JoinMemoryBo("agent1", 1498462545000L, 4000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo2_2 = new JoinMemoryBo("agent2", 1498462550000L, 1000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2");
        JoinMemoryBo joinMemoryBo2_3 = new JoinMemoryBo("agent3", 1498462555000L, 3000, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3");
        JoinMemoryBo joinMemoryBo2_4 = new JoinMemoryBo("agent3", 1498462560000L, 8800, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3");
        joinMemoryBoList2.add(joinMemoryBo2_1);
        joinMemoryBoList2.add(joinMemoryBo2_2);
        joinMemoryBoList2.add(joinMemoryBo2_3);
        joinMemoryBoList2.add(joinMemoryBo2_4);
        JoinApplicationStatBo joinApplicationStatBo2 = new JoinApplicationStatBo();
        joinApplicationStatBo2.setId("test_app");
        joinApplicationStatBo2.setJoinMemoryBoList(joinMemoryBoList2);
        joinApplicationStatBo2.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo2);

        List<JoinMemoryBo> joinMemoryBoList3 = new ArrayList<JoinMemoryBo>();
        JoinMemoryBo joinMemoryBo3_1 = new JoinMemoryBo("agent1", 1498462545000L, 5000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo3_2 = new JoinMemoryBo("agent2", 1498462550000L, 1000, 1000, 7000, "agent2", "agent2", 400, 150, 600, "agent2", "agent2");
        JoinMemoryBo joinMemoryBo3_3 = new JoinMemoryBo("agent3", 1498462565000L, 7800, 3000, 8000, "agent3", "agent3", 200, 100, 200, "agent3", "agent3");
        joinMemoryBoList3.add(joinMemoryBo3_1);
        joinMemoryBoList3.add(joinMemoryBo3_2);
        joinMemoryBoList3.add(joinMemoryBo3_3);
        JoinApplicationStatBo joinApplicationStatBo3 = new JoinApplicationStatBo();
        joinApplicationStatBo3.setId("test_app");
        joinApplicationStatBo3.setJoinMemoryBoList(joinMemoryBoList3);
        joinApplicationStatBo3.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinMemoryBo> joinMemoryBoList = joinApplicationStatBo.getJoinMemoryBoList();
        Collections.sort(joinMemoryBoList, new ComparatorImpl2());
        assertEquals(joinMemoryBoList.size(), 5);
        assertEquals(joinMemoryBoList.get(0).getHeapUsed(), 4000);
        assertEquals(joinMemoryBoList.get(1).getHeapUsed(), 2000);
        assertEquals(joinMemoryBoList.get(2).getHeapUsed(), 4000);
        assertEquals(joinMemoryBoList.get(3).getHeapUsed(), 8800);
        assertEquals(joinMemoryBoList.get(4).getHeapUsed(), 7800);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice5Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo3("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinTransactionBo> joinTransactionBoList = resultJoinApplicationStatBo.getJoinTransactionBoList();
        Collections.sort(joinTransactionBoList, new ComparatorImpl3());
        assertJoinTransactionBoList(joinTransactionBoList);
    }

    private class ComparatorImpl3 implements Comparator<JoinTransactionBo> {
        @Override
        public int compare(JoinTransactionBo bo1, JoinTransactionBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private JoinApplicationStatBo createJoinApplicationStatBo3(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinTransactionBoList(createJoinTransactionBoList3(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinTransactionBo> createJoinTransactionBoList3(final String id, final long currentTime, int plus) {
        final List<JoinTransactionBo> joinTransactionBoList = new ArrayList<JoinTransactionBo>();

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


        for (JoinTransactionBo joinTransactionBo : joinTransactionBoList) {
            System.out.println(joinTransactionBo);
        }

        return joinTransactionBoList;
    }


    private void assertJoinTransactionBoList(List<JoinTransactionBo> joinTransactionBoList) {
        assertEquals(joinTransactionBoList.size(), 5);

        JoinTransactionBo joinTransactionBo1 = joinTransactionBoList.get(0);
        assertEquals(joinTransactionBo1.getId(), "id1");
        assertEquals(joinTransactionBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinTransactionBo1.getTotalCount(), 86);
        assertEquals(joinTransactionBo1.getMinTotalCount(), 10);
        assertEquals(joinTransactionBo1.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinTransactionBo1.getMaxTotalCount(), 240);
        assertEquals(joinTransactionBo1.getMaxTotalCountAgentId(), "id4_2");

        JoinTransactionBo joinTransactionBo2 = joinTransactionBoList.get(1);
        assertEquals(joinTransactionBo2.getId(), "id1");
        assertEquals(joinTransactionBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinTransactionBo2.getTotalCount(), 286);
        assertEquals(joinTransactionBo2.getMinTotalCount(), 100);
        assertEquals(joinTransactionBo2.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinTransactionBo2.getMaxTotalCount(), 440);
        assertEquals(joinTransactionBo2.getMaxTotalCountAgentId(), "id4_2");

        JoinTransactionBo joinTransactionBo3 = joinTransactionBoList.get(2);
        assertEquals(joinTransactionBo3.getId(), "id1");
        assertEquals(joinTransactionBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinTransactionBo3.getTotalCount(), 186);
        assertEquals(joinTransactionBo3.getMinTotalCount(), 80);
        assertEquals(joinTransactionBo3.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinTransactionBo3.getMaxTotalCount(), 340);
        assertEquals(joinTransactionBo3.getMaxTotalCountAgentId(), "id4_2");

        JoinTransactionBo joinTransactionBo4 = joinTransactionBoList.get(3);
        assertEquals(joinTransactionBo4.getId(), "id1");
        assertEquals(joinTransactionBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinTransactionBo4.getTotalCount(), 386);
        assertEquals(joinTransactionBo4.getMinTotalCount(), 150);
        assertEquals(joinTransactionBo4.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinTransactionBo4.getMaxTotalCount(), 490);
        assertEquals(joinTransactionBo4.getMaxTotalCountAgentId(), "id4_2");

        JoinTransactionBo joinTransactionBo5 = joinTransactionBoList.get(4);
        assertEquals(joinTransactionBo5.getId(), "id1");
        assertEquals(joinTransactionBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinTransactionBo5.getTotalCount(), 336);
        assertEquals(joinTransactionBo5.getMinTotalCount(), 120);
        assertEquals(joinTransactionBo5.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinTransactionBo5.getMaxTotalCount(), 640);
        assertEquals(joinTransactionBo5.getMaxTotalCountAgentId(), "id4_2");
    }

    @Test
    public void joinApplicationStatBoByTimeSlice6Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();

        List<JoinTransactionBo> joinTransactionBoList1 = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo1_1 = new JoinTransactionBo("agent1", 5000, 100, 60, "agent1", 200, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo1_2 = new JoinTransactionBo("agent2", 5000, 100, 60, "agent2", 200, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo1_3 = new JoinTransactionBo("agent3", 5000, 100, 60, "agent3", 200, "agent3", 1498462555000L);
        joinTransactionBoList1.add(joinTransactionBo1_1);
        joinTransactionBoList1.add(joinTransactionBo1_2);
        joinTransactionBoList1.add(joinTransactionBo1_3);
        JoinApplicationStatBo joinApplicationStatBo1 = new JoinApplicationStatBo();
        joinApplicationStatBo1.setId("test_app");
        joinApplicationStatBo1.setJoinTransactionBoList(joinTransactionBoList1);
        joinApplicationStatBo1.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo1);

        List<JoinTransactionBo> joinTransactionBoList2 = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo2_1 = new JoinTransactionBo("agent1", 5000, 50, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo2_2 = new JoinTransactionBo("agent2", 5000, 200, 60, "agent2", 400, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo2_3 = new JoinTransactionBo("agent3", 5000, 500, 10, "agent3", 100, "agent3", 1498462555000L);
        JoinTransactionBo joinTransactionBo2_4 = new JoinTransactionBo("agent3", 5000, 400, 60, "agent3", 500, "agent3", 1498462560000L);
        joinTransactionBoList2.add(joinTransactionBo2_1);
        joinTransactionBoList2.add(joinTransactionBo2_2);
        joinTransactionBoList2.add(joinTransactionBo2_3);
        joinTransactionBoList2.add(joinTransactionBo2_4);
        JoinApplicationStatBo joinApplicationStatBo2 = new JoinApplicationStatBo();
        joinApplicationStatBo2.setId("test_app");
        joinApplicationStatBo2.setJoinTransactionBoList(joinTransactionBoList2);
        joinApplicationStatBo2.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo2);

        List<JoinTransactionBo> joinTransactionBoList3 = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo3_1 = new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo3_2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo3_3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462565000L);
        joinTransactionBoList3.add(joinTransactionBo3_1);
        joinTransactionBoList3.add(joinTransactionBo3_2);
        joinTransactionBoList3.add(joinTransactionBo3_3);
        JoinApplicationStatBo joinApplicationStatBo3 = new JoinApplicationStatBo();
        joinApplicationStatBo3.setId("test_app");
        joinApplicationStatBo3.setJoinTransactionBoList(joinTransactionBoList3);
        joinApplicationStatBo3.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinTransactionBo> joinTransactionBoList = joinApplicationStatBo.getJoinTransactionBoList();
        Collections.sort(joinTransactionBoList, new ComparatorImpl3());
        assertEquals(joinTransactionBoList.size(), 5);
        assertEquals(joinTransactionBoList.get(0).getTotalCount(), 100);
        assertEquals(joinTransactionBoList.get(1).getTotalCount(), 200);
        assertEquals(joinTransactionBoList.get(2).getTotalCount(), 300);
        assertEquals(joinTransactionBoList.get(3).getTotalCount(), 400);
        assertEquals(joinTransactionBoList.get(4).getTotalCount(), 30);
    }

    @Test
    public void createJoinApplicationStatBoTest() {
        JoinAgentStatBo joinAgentStatBo = new JoinAgentStatBo();
        joinAgentStatBo.setTimestamp(1498462565000L);

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462565000L);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462570000L);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462575000L);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", 1498462580000L);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", 1498462585000L);
        joinCpuLoadBoList.add(joinCpuLoadBo1);
        joinCpuLoadBoList.add(joinCpuLoadBo2);
        joinCpuLoadBoList.add(joinCpuLoadBo3);
        joinCpuLoadBoList.add(joinCpuLoadBo4);
        joinCpuLoadBoList.add(joinCpuLoadBo5);
        joinAgentStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);

        List<JoinMemoryBo> joinMemoryBoList = new ArrayList<JoinMemoryBo>();
        JoinMemoryBo joinMemoryBo1 = new JoinMemoryBo("agent1", 1498462565000L, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo2 = new JoinMemoryBo("agent1", 1498462570000L, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo3 = new JoinMemoryBo("agent1", 1498462575000L, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo4 = new JoinMemoryBo("agent1", 1498462580000L, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo5 = new JoinMemoryBo("agent1", 1498462585000L, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1");
        joinMemoryBoList.add(joinMemoryBo1);
        joinMemoryBoList.add(joinMemoryBo2);
        joinMemoryBoList.add(joinMemoryBo3);
        joinMemoryBoList.add(joinMemoryBo4);
        joinMemoryBoList.add(joinMemoryBo5);
        joinAgentStatBo.setJoinMemoryBoList(joinMemoryBoList);

        List<JoinTransactionBo> joinTransactionBoList = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", 1498462565000L);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", 1498462570000L);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462575000L);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", 1498462580000L);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", 1498462585000L);
        joinTransactionBoList.add(joinTransactionBo1);
        joinTransactionBoList.add(joinTransactionBo2);
        joinTransactionBoList.add(joinTransactionBo3);
        joinTransactionBoList.add(joinTransactionBo4);
        joinTransactionBoList.add(joinTransactionBo5);
        joinAgentStatBo.setJoinTransactionBoList(joinTransactionBoList);

        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", joinAgentStatBo, 60000);
        assertEquals(joinApplicationStatBoList.size(), 1);
        JoinApplicationStatBo joinApplicationStatBo = joinApplicationStatBoList.get(0);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 5);
        assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 5);
        assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 5);
    }

    @Test
    public void createJoinApplicationStatBo2Test() {
        JoinAgentStatBo joinAgentStatBo = new JoinAgentStatBo();
        joinAgentStatBo.setTimestamp(1498462545000L);

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462555000L);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", 1498462560000L);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", 1498462565000L);
        joinCpuLoadBoList.add(joinCpuLoadBo1);
        joinCpuLoadBoList.add(joinCpuLoadBo2);
        joinCpuLoadBoList.add(joinCpuLoadBo3);
        joinCpuLoadBoList.add(joinCpuLoadBo4);
        joinCpuLoadBoList.add(joinCpuLoadBo5);
        joinAgentStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);

        List<JoinMemoryBo> joinMemoryBoList = new ArrayList<JoinMemoryBo>();
        JoinMemoryBo joinMemoryBo1 = new JoinMemoryBo("agent1", 1498462545000L, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo2 = new JoinMemoryBo("agent1", 1498462550000L, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo3 = new JoinMemoryBo("agent1", 1498462555000L, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo4 = new JoinMemoryBo("agent1", 1498462560000L, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo5 = new JoinMemoryBo("agent1", 1498462565000L, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1");
        joinMemoryBoList.add(joinMemoryBo1);
        joinMemoryBoList.add(joinMemoryBo2);
        joinMemoryBoList.add(joinMemoryBo3);
        joinMemoryBoList.add(joinMemoryBo4);
        joinMemoryBoList.add(joinMemoryBo5);
        joinAgentStatBo.setJoinMemoryBoList(joinMemoryBoList);

        List<JoinTransactionBo> joinTransactionBoList = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462555000L);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", 1498462560000L);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", 1498462565000L);
        joinTransactionBoList.add(joinTransactionBo1);
        joinTransactionBoList.add(joinTransactionBo2);
        joinTransactionBoList.add(joinTransactionBo3);
        joinTransactionBoList.add(joinTransactionBo4);
        joinTransactionBoList.add(joinTransactionBo5);
        joinAgentStatBo.setJoinTransactionBoList(joinTransactionBoList);

        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", joinAgentStatBo, 60000);
        assertEquals(joinApplicationStatBoList.size(), 2);
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            assertEquals(joinApplicationStatBo.getId(), "test_app");
            if (joinApplicationStatBo.getTimestamp() == 1498462560000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 2);
            } else if (joinApplicationStatBo.getTimestamp() == 1498462500000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 3);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 3);
            } else {
                fail();
            }
        }
    }

    @Test
    public void createJoinApplicationStatBo3Test() {
        JoinAgentStatBo joinAgentStatBo = new JoinAgentStatBo();
        joinAgentStatBo.setTimestamp(1498462545000L);

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1498462545000L);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo("agent1", 33, 40, "agent1", 10, "agent1", 20, 78, "agent1", 12, "agent1", 1498462550000L);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo("agent1", 55, 60, "agent1", 7, "agent1", 30, 39, "agent1", 30, "agent1", 1498462555000L);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo("agent1", 11, 80, "agent1", 8, "agent1", 10, 50, "agent1", 14, "agent1", 1498462560000L);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo("agent1", 22, 70, "agent1", 12, "agent1", 40, 99, "agent1", 50, "agent1", 1498462565000L);
        joinCpuLoadBoList.add(joinCpuLoadBo1);
        joinCpuLoadBoList.add(joinCpuLoadBo2);
        joinCpuLoadBoList.add(joinCpuLoadBo3);
        joinCpuLoadBoList.add(joinCpuLoadBo4);
        joinCpuLoadBoList.add(joinCpuLoadBo5);
        joinAgentStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);

        List<JoinMemoryBo> joinMemoryBoList = new ArrayList<JoinMemoryBo>();
        JoinMemoryBo joinMemoryBo1 = new JoinMemoryBo("agent1", 1498462545000L, 3000, 2000, 5000, "agent1", "agent1", 500, 50, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo2 = new JoinMemoryBo("agent1", 1498462550000L, 4000, 1000, 7000, "agent1", "agent1", 400, 150, 600, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo3 = new JoinMemoryBo("agent1", 1498462555000L, 5000, 3000, 8000, "agent1", "agent1", 200, 100, 200, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo4 = new JoinMemoryBo("agent1", 1498462560000L, 1000, 100, 3000, "agent1", "agent1", 100, 900, 1000, "agent1", "agent1");
        JoinMemoryBo joinMemoryBo5 = new JoinMemoryBo("agent1", 1498462565000L, 2000, 1000, 6000, "agent1", "agent1", 300, 100, 2900, "agent1", "agent1");
        joinMemoryBoList.add(joinMemoryBo1);
        joinMemoryBoList.add(joinMemoryBo2);
        joinMemoryBoList.add(joinMemoryBo3);
        joinMemoryBoList.add(joinMemoryBo4);
        joinMemoryBoList.add(joinMemoryBo5);
        joinAgentStatBo.setJoinMemoryBoList(joinMemoryBoList);

        List<JoinTransactionBo> joinTransactionBoList = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo("agent1", 5000, 150, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent2", 1498462550000L);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462555000L);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", 1498462560000L);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", 1498462565000L);
        joinTransactionBoList.add(joinTransactionBo1);
        joinTransactionBoList.add(joinTransactionBo2);
        joinTransactionBoList.add(joinTransactionBo3);
        joinTransactionBoList.add(joinTransactionBo4);
        joinTransactionBoList.add(joinTransactionBo5);
        joinAgentStatBo.setJoinTransactionBoList(joinTransactionBoList);

        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", joinAgentStatBo, 10000);
        assertEquals(joinApplicationStatBoList.size(), 3);
        for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
            assertEquals(joinApplicationStatBo.getId(), "test_app");
            if (joinApplicationStatBo.getTimestamp() == 1498462560000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 2);
            } else if (joinApplicationStatBo.getTimestamp() == 1498462540000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 1);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 1);
            } else if (joinApplicationStatBo.getTimestamp() == 1498462550000L) {
                assertEquals(joinApplicationStatBo.getJoinCpuLoadBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinMemoryBoList().size(), 2);
                assertEquals(joinApplicationStatBo.getJoinTransactionBoList().size(), 2);
            } else {
                fail();
            }
        }
    }

}


