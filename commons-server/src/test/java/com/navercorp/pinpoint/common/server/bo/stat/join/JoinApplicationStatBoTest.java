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
        joinApplicationStatBo.setJoinMemoryBoList(createJoinMemoryBoList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinMemoryBo> createJoinMemoryBoList(final String id, final long currentTime, int plus) {
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
        joinApplicationStatBo.setJoinTransactionBoList(createJoinTransactionBoList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinTransactionBo> createJoinTransactionBoList(final String id, final long currentTime, int plus) {
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
    public void joinApplicationStatBoByTimeSlice7Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo4("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinActiveTraceBo> joinActiveTraceBoList = resultJoinApplicationStatBo.getJoinActiveTraceBoList();
        Collections.sort(joinActiveTraceBoList, new ComparatorImpl4());
        assertJoinActiveTraceBoList(joinActiveTraceBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice8Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();

        List<JoinActiveTraceBo> joinActiveTraceBoList1 = new ArrayList<JoinActiveTraceBo>();
        JoinActiveTraceBo joinActiveTraceBo1_1 = new JoinActiveTraceBo("agent1", 1, (short)2, 100, 60, "agent1", 200, "agent1", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo1_2 = new JoinActiveTraceBo("agent2", 1, (short)2, 100, 60, "agent1", 200, "agent1", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo1_3 = new JoinActiveTraceBo("agent3", 1, (short)2, 100, 60, "agent1", 200, "agent1", 1498462555000L);
        joinActiveTraceBoList1.add(joinActiveTraceBo1_1);
        joinActiveTraceBoList1.add(joinActiveTraceBo1_2);
        joinActiveTraceBoList1.add(joinActiveTraceBo1_3);
        JoinApplicationStatBo joinApplicationStatBo1 = new JoinApplicationStatBo();
        joinApplicationStatBo1.setId("test_app");
        joinApplicationStatBo1.setJoinActiveTraceBoList(joinActiveTraceBoList1);
        joinApplicationStatBo1.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo1);

        List<JoinActiveTraceBo> joinActiveTraceBoList2 = new ArrayList<JoinActiveTraceBo>();
        JoinActiveTraceBo joinActiveTraceBo2_1 = new JoinActiveTraceBo("agent1", 1, (short)2, 50, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo2_2 = new JoinActiveTraceBo("agent2", 1, (short)2, 200, 60, "agent2", 400, "agent2", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo2_3 = new JoinActiveTraceBo("agent3", 1, (short)2, 500, 10, "agent3", 100, "agent3", 1498462555000L);
        JoinActiveTraceBo joinActiveTraceBo2_4 = new JoinActiveTraceBo("agent3", 1, (short)2, 400, 60, "agent3", 500, "agent3", 1498462560000L);
        joinActiveTraceBoList2.add(joinActiveTraceBo2_1);
        joinActiveTraceBoList2.add(joinActiveTraceBo2_2);
        joinActiveTraceBoList2.add(joinActiveTraceBo2_3);
        joinActiveTraceBoList2.add(joinActiveTraceBo2_4);
        JoinApplicationStatBo joinApplicationStatBo2 = new JoinApplicationStatBo();
        joinApplicationStatBo2.setId("test_app");
        joinApplicationStatBo2.setJoinActiveTraceBoList(joinActiveTraceBoList2);
        joinApplicationStatBo2.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo2);

        List<JoinActiveTraceBo> joinActiveTraceBoList3 = new ArrayList<JoinActiveTraceBo>();
        JoinActiveTraceBo joinActiveTraceBo3_1 = new JoinActiveTraceBo("agent1", 1, (short)2, 150, 20, "agent1", 230, "agent1", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo3_2 = new JoinActiveTraceBo("agent2", 1, (short)2, 300, 10, "agent2", 400, "agent2", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo3_3 = new JoinActiveTraceBo("agent3", 1, (short)2, 30, 5, "agent3", 100, "agent3", 1498462565000L);
        joinActiveTraceBoList3.add(joinActiveTraceBo3_1);
        joinActiveTraceBoList3.add(joinActiveTraceBo3_2);
        joinActiveTraceBoList3.add(joinActiveTraceBo3_3);
        JoinApplicationStatBo joinApplicationStatBo3 = new JoinApplicationStatBo();
        joinApplicationStatBo3.setId("test_app");
        joinApplicationStatBo3.setJoinActiveTraceBoList(joinActiveTraceBoList3);
        joinApplicationStatBo3.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinActiveTraceBo> joinActiveTraceBoList = joinApplicationStatBo.getJoinActiveTraceBoList();
        Collections.sort(joinActiveTraceBoList, new ComparatorImpl4());
        assertEquals(joinActiveTraceBoList.size(), 5);
        assertEquals(joinActiveTraceBoList.get(0).getTotalCount(), 100);
        assertEquals(joinActiveTraceBoList.get(1).getTotalCount(), 200);
        assertEquals(joinActiveTraceBoList.get(2).getTotalCount(), 300);
        assertEquals(joinActiveTraceBoList.get(3).getTotalCount(), 400);
        assertEquals(joinActiveTraceBoList.get(4).getTotalCount(), 30);
    }

    private class ComparatorImpl4 implements Comparator<JoinActiveTraceBo> {
        @Override
        public int compare(JoinActiveTraceBo bo1, JoinActiveTraceBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private JoinApplicationStatBo createJoinApplicationStatBo4(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinActiveTraceBoList(createJoinActiveTraceBoList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinActiveTraceBo> createJoinActiveTraceBoList(final String id, final long currentTime, int plus) {
        final List<JoinActiveTraceBo> joinActiveTraceBoList = new ArrayList<JoinActiveTraceBo>();
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
        assertEquals(joinActiveTraceBo1.getTotalCount(), 86);
        assertEquals(joinActiveTraceBo1.getMinTotalCount(), 10);
        assertEquals(joinActiveTraceBo1.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinActiveTraceBo1.getMaxTotalCount(), 240);
        assertEquals(joinActiveTraceBo1.getMaxTotalCountAgentId(), "id4_2");

        JoinActiveTraceBo joinActiveTraceBo2 = joinActiveTraceBoList.get(1);
        assertEquals(joinActiveTraceBo2.getId(), "id1");
        assertEquals(joinActiveTraceBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinActiveTraceBo2.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo2.getVersion(), 2);
        assertEquals(joinActiveTraceBo2.getTotalCount(), 286);
        assertEquals(joinActiveTraceBo2.getMinTotalCount(), 100);
        assertEquals(joinActiveTraceBo2.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinActiveTraceBo2.getMaxTotalCount(), 440);
        assertEquals(joinActiveTraceBo2.getMaxTotalCountAgentId(), "id4_2");

        JoinActiveTraceBo joinActiveTraceBo3 = joinActiveTraceBoList.get(2);
        assertEquals(joinActiveTraceBo3.getId(), "id1");
        assertEquals(joinActiveTraceBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinActiveTraceBo3.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo3.getVersion(), 2);
        assertEquals(joinActiveTraceBo3.getTotalCount(), 186);
        assertEquals(joinActiveTraceBo3.getMinTotalCount(), 80);
        assertEquals(joinActiveTraceBo3.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinActiveTraceBo3.getMaxTotalCount(), 340);
        assertEquals(joinActiveTraceBo3.getMaxTotalCountAgentId(), "id4_2");

        JoinActiveTraceBo joinActiveTraceBo4 = joinActiveTraceBoList.get(3);
        assertEquals(joinActiveTraceBo4.getId(), "id1");
        assertEquals(joinActiveTraceBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinActiveTraceBo4.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo4.getVersion(), 2);
        assertEquals(joinActiveTraceBo4.getTotalCount(), 386);
        assertEquals(joinActiveTraceBo4.getMinTotalCount(), 150);
        assertEquals(joinActiveTraceBo4.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinActiveTraceBo4.getMaxTotalCount(), 490);
        assertEquals(joinActiveTraceBo4.getMaxTotalCountAgentId(), "id4_2");

        JoinActiveTraceBo joinActiveTraceBo5 = joinActiveTraceBoList.get(4);
        assertEquals(joinActiveTraceBo5.getId(), "id1");
        assertEquals(joinActiveTraceBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinActiveTraceBo5.getHistogramSchemaType(), 1);
        assertEquals(joinActiveTraceBo5.getVersion(), 2);
        assertEquals(joinActiveTraceBo5.getTotalCount(), 336);
        assertEquals(joinActiveTraceBo5.getMinTotalCount(), 120);
        assertEquals(joinActiveTraceBo5.getMinTotalCountAgentId(), "id5_1");
        assertEquals(joinActiveTraceBo5.getMaxTotalCount(), 640);
        assertEquals(joinActiveTraceBo5.getMaxTotalCountAgentId(), "id4_2");
    }

    @Test
    public void joinApplicationStatBoByTimeSlice9Test() {
        final long currentTime = 1487149800000L; // 18:10:00 15 2 2017
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo5("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinResponseTimeBo> joinResponseTimeBoList = resultJoinApplicationStatBo.getJoinResponseTimeBoList();
        Collections.sort(joinResponseTimeBoList, new ComparatorImpl5());

        assertJoinResponseTimeBoList(joinResponseTimeBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice10Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();

        List<JoinResponseTimeBo> joinResponseTimeBoList1 = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo1_1 = new JoinResponseTimeBo("agent1", 1498462545000L, 100, 60, "agent1", 200, "agent1");
        JoinResponseTimeBo joinResponseTimeBo1_2 = new JoinResponseTimeBo("agent1", 1498462550000L, 100, 60, "agent1", 200, "agent1");
        JoinResponseTimeBo joinResponseTimeBo1_3 = new JoinResponseTimeBo("agent1", 1498462555000L, 100, 60, "agent1", 200, "agent1");
        joinResponseTimeBoList1.add(joinResponseTimeBo1_1);
        joinResponseTimeBoList1.add(joinResponseTimeBo1_2);
        joinResponseTimeBoList1.add(joinResponseTimeBo1_3);
        JoinApplicationStatBo joinApplicationStatBo1 = new JoinApplicationStatBo();
        joinApplicationStatBo1.setId("test_app");
        joinApplicationStatBo1.setJoinResponseTimeBoList(joinResponseTimeBoList1);
        joinApplicationStatBo1.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo1);

        List<JoinResponseTimeBo> joinResponseTimeBoList2 = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo2_1 = new JoinResponseTimeBo("agent1", 1498462545000L, 50, 20, "agent1", 230, "agent1");
        JoinResponseTimeBo joinResponseTimeBo2_2 = new JoinResponseTimeBo("agent2", 1498462550000L, 200, 60, "agent2", 400, "agent2");
        JoinResponseTimeBo joinResponseTimeBo2_3 = new JoinResponseTimeBo("agent3", 1498462555000L, 500, 10, "agent3", 100, "agent3");
        JoinResponseTimeBo joinResponseTimeBo2_4 = new JoinResponseTimeBo("agent3", 1498462560000L, 400, 60, "agent3", 500, "agent3");
        joinResponseTimeBoList2.add(joinResponseTimeBo2_1);
        joinResponseTimeBoList2.add(joinResponseTimeBo2_2);
        joinResponseTimeBoList2.add(joinResponseTimeBo2_3);
        joinResponseTimeBoList2.add(joinResponseTimeBo2_4);
        JoinApplicationStatBo joinApplicationStatBo2 = new JoinApplicationStatBo();
        joinApplicationStatBo2.setId("test_app");
        joinApplicationStatBo2.setJoinResponseTimeBoList(joinResponseTimeBoList2);
        joinApplicationStatBo2.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo2);

        List<JoinResponseTimeBo> joinResponseTimeBoList3 = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo3_1 = new JoinResponseTimeBo("agent1", 1498462545000L, 150, 20, "agent1", 230, "agent1");
        JoinResponseTimeBo joinResponseTimeBo3_2 = new JoinResponseTimeBo("agent2", 1498462550000L, 300, 10, "agent2", 400, "agent2");
        JoinResponseTimeBo joinResponseTimeBo3_3 = new JoinResponseTimeBo("agent3", 1498462565000L, 30, 5, "agent3", 100, "agent3");
        joinResponseTimeBoList3.add(joinResponseTimeBo3_1);
        joinResponseTimeBoList3.add(joinResponseTimeBo3_2);
        joinResponseTimeBoList3.add(joinResponseTimeBo3_3);
        JoinApplicationStatBo joinApplicationStatBo3 = new JoinApplicationStatBo();
        joinApplicationStatBo3.setId("test_app");
        joinApplicationStatBo3.setJoinResponseTimeBoList(joinResponseTimeBoList3);
        joinApplicationStatBo3.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinResponseTimeBo> joinResponseTimeBoList = joinApplicationStatBo.getJoinResponseTimeBoList();
        Collections.sort(joinResponseTimeBoList, new ComparatorImpl5());
        assertEquals(joinResponseTimeBoList.size(), 5);
        assertEquals(joinResponseTimeBoList.get(0).getAvg(), 100);
        assertEquals(joinResponseTimeBoList.get(1).getAvg(), 200);
        assertEquals(joinResponseTimeBoList.get(2).getAvg(), 300);
        assertEquals(joinResponseTimeBoList.get(3).getAvg(), 400);
        assertEquals(joinResponseTimeBoList.get(4).getAvg(), 30);
    }

    private void assertJoinResponseTimeBoList(List<JoinResponseTimeBo> joinResponseTimeBoList) {
        assertEquals(joinResponseTimeBoList.size(), 5);

        JoinResponseTimeBo joinResponseTimeBo1 = joinResponseTimeBoList.get(0);
        assertEquals(joinResponseTimeBo1.getId(), "id1");
        assertEquals(joinResponseTimeBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinResponseTimeBo1.getAvg(), 286);
        assertEquals(joinResponseTimeBo1.getMinAvg(), 150);
        assertEquals(joinResponseTimeBo1.getMinAvgAgentId(), "id5_1");
        assertEquals(joinResponseTimeBo1.getMaxAvg(), 6040);
        assertEquals(joinResponseTimeBo1.getMaxAvgAgentId(), "id4_2");

        JoinResponseTimeBo joinResponseTimeBo2 = joinResponseTimeBoList.get(1);
        assertEquals(joinResponseTimeBo2.getId(), "id1");
        assertEquals(joinResponseTimeBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinResponseTimeBo2.getAvg(), 186);
        assertEquals(joinResponseTimeBo2.getMinAvg(), 0);
        assertEquals(joinResponseTimeBo2.getMinAvgAgentId(), "id5_1");
        assertEquals(joinResponseTimeBo2.getMaxAvg(), 7040);
        assertEquals(joinResponseTimeBo2.getMaxAvgAgentId(), "id4_2");

        JoinResponseTimeBo joinResponseTimeBo3 = joinResponseTimeBoList.get(2);
        assertEquals(joinResponseTimeBo3.getId(), "id1");
        assertEquals(joinResponseTimeBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinResponseTimeBo3.getAvg(), 386);
        assertEquals(joinResponseTimeBo3.getMinAvg(), 250);
        assertEquals(joinResponseTimeBo3.getMinAvgAgentId(), "id5_1");
        assertEquals(joinResponseTimeBo3.getMaxAvg(), 8040);
        assertEquals(joinResponseTimeBo3.getMaxAvgAgentId(), "id4_2");

        JoinResponseTimeBo joinResponseTimeBo4 = joinResponseTimeBoList.get(3);
        assertEquals(joinResponseTimeBo4.getId(), "id1");
        assertEquals(joinResponseTimeBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinResponseTimeBo4.getAvg(), 486);
        assertEquals(joinResponseTimeBo4.getMinAvg(), 350);
        assertEquals(joinResponseTimeBo4.getMinAvgAgentId(), "id5_1");
        assertEquals(joinResponseTimeBo4.getMaxAvg(), 2040);
        assertEquals(joinResponseTimeBo4.getMaxAvgAgentId(), "id4_2");

        JoinResponseTimeBo joinResponseTimeBo5 = joinResponseTimeBoList.get(4);
        assertEquals(joinResponseTimeBo5.getId(), "id1");
        assertEquals(joinResponseTimeBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinResponseTimeBo5.getAvg(), 86);
        assertEquals(joinResponseTimeBo5.getMinAvg(), 50);
        assertEquals(joinResponseTimeBo5.getMinAvgAgentId(), "id5_1");
        assertEquals(joinResponseTimeBo5.getMaxAvg(), 9040);
        assertEquals(joinResponseTimeBo5.getMaxAvgAgentId(), "id4_2");
    }

    private class ComparatorImpl5 implements Comparator<JoinResponseTimeBo> {
        @Override
        public int compare(JoinResponseTimeBo bo1, JoinResponseTimeBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private JoinApplicationStatBo createJoinApplicationStatBo5(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinResponseTimeBoList(createJoinResponseTimeList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinResponseTimeBo> createJoinResponseTimeList(String id, long currentTime, int plus) {
        final List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
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
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo6("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinDataSourceListBo> joinDataSourceListBoList = resultJoinApplicationStatBo.getJoinDataSourceListBoList();
        Collections.sort(joinDataSourceListBoList, new ComparatorImpl6());

        assertJoinDataSourceListBoList(joinDataSourceListBoList);
    }

    @Test
    public void joinApplicationStatBoByTimeSlice12Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();

        List<JoinDataSourceListBo> joinDataSourceLIstBoList1 = new ArrayList<JoinDataSourceListBo>();
        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100, 60, "agent1", 200, "agent1"));
        JoinDataSourceListBo joinDataSourceListBo1_1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462545000L);
        JoinDataSourceListBo joinDataSourceListBo1_2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462550000L);
        JoinDataSourceListBo joinDataSourceListBo1_3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462555000L);
        joinDataSourceLIstBoList1.add(joinDataSourceListBo1_1);
        joinDataSourceLIstBoList1.add(joinDataSourceListBo1_2);
        joinDataSourceLIstBoList1.add(joinDataSourceListBo1_3);
        JoinApplicationStatBo joinApplicationStatBo1 = new JoinApplicationStatBo();
        joinApplicationStatBo1.setId("test_app");
        joinApplicationStatBo1.setJoinDataSourceListBoList(joinDataSourceLIstBoList1);
        joinApplicationStatBo1.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo1);

        List<JoinDataSourceListBo> joinDataSourceLIstBoList2 = new ArrayList<JoinDataSourceListBo>();
        List<JoinDataSourceBo> joinDataSourceBoList2_1 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList2_1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 50, 20, "agent1", 230, "agent1"));
        JoinDataSourceListBo joinResponseTimeBo2_1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2_1, 1498462545000L);
        List<JoinDataSourceBo> joinDataSourceBoList2_2 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList2_2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200, 60, "agent2", 400, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo2_2 = new JoinDataSourceListBo("agent2", joinDataSourceBoList2_2, 1498462550000L);
        List<JoinDataSourceBo> joinDataSourceBoList2_3 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList2_3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500, 10, "agent3", 100, "agent3"));
        JoinDataSourceListBo joinResponseTimeBo2_3 = new JoinDataSourceListBo("agent3", joinDataSourceBoList2_3, 1498462555000L);
        List<JoinDataSourceBo> joinDataSourceBoList2_4 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList2_4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400, 60, "agent3", 500, "agent3"));
        JoinDataSourceListBo joinResponseTimeBo2_4 = new JoinDataSourceListBo("agent3", joinDataSourceBoList2_4, 1498462560000L);
        joinDataSourceLIstBoList2.add(joinResponseTimeBo2_1);
        joinDataSourceLIstBoList2.add(joinResponseTimeBo2_2);
        joinDataSourceLIstBoList2.add(joinResponseTimeBo2_3);
        joinDataSourceLIstBoList2.add(joinResponseTimeBo2_4);
        JoinApplicationStatBo joinApplicationStatBo2 = new JoinApplicationStatBo();
        joinApplicationStatBo2.setId("test_app");
        joinApplicationStatBo2.setJoinDataSourceListBoList(joinDataSourceLIstBoList2);
        joinApplicationStatBo2.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo2);

        List<JoinDataSourceListBo> joinResponseTimeBoList3 = new ArrayList<JoinDataSourceListBo>();
        List<JoinDataSourceBo> joinDataSourceBoList3_1 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList3_1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 150, 20, "agent1", 230, "agent1"));
        JoinDataSourceListBo joinResponseTimeBo3_1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3_1, 1498462545000L);
        List<JoinDataSourceBo> joinDataSourceBoList3_2 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList3_2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300, 10, "agent2", 400, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo3_2 = new JoinDataSourceListBo("agent2", joinDataSourceBoList3_2, 1498462550000L);
        List<JoinDataSourceBo> joinDataSourceBoList3_3 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList3_3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 30, 5, "agent2", 100, "agent2"));
        JoinDataSourceListBo joinResponseTimeBo3_3 = new JoinDataSourceListBo("agent3", joinDataSourceBoList3_3, 1498462565000L);
        joinResponseTimeBoList3.add(joinResponseTimeBo3_1);
        joinResponseTimeBoList3.add(joinResponseTimeBo3_2);
        joinResponseTimeBoList3.add(joinResponseTimeBo3_3);
        JoinApplicationStatBo joinApplicationStatBo3 = new JoinApplicationStatBo();
        joinApplicationStatBo3.setId("test_app");
        joinApplicationStatBo3.setJoinDataSourceListBoList(joinResponseTimeBoList3);
        joinApplicationStatBo3.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo3);

        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinDataSourceListBo> joinDataSourceListBoList = joinApplicationStatBo.getJoinDataSourceListBoList();
        Collections.sort(joinDataSourceListBoList, new ComparatorImpl6());
        assertEquals(joinDataSourceListBoList.size(), 5);
        assertEquals(joinDataSourceListBoList.get(0).getJoinDataSourceBoList().get(0).getAvgActiveConnectionSize(), 100);
        assertEquals(joinDataSourceListBoList.get(0).getJoinDataSourceBoList().size(), 1);
        assertEquals(joinDataSourceListBoList.get(1).getJoinDataSourceBoList().get(0).getAvgActiveConnectionSize(), 200);
        assertEquals(joinDataSourceListBoList.get(1).getJoinDataSourceBoList().size(), 1);
        assertEquals(joinDataSourceListBoList.get(2).getJoinDataSourceBoList().get(0).getAvgActiveConnectionSize(), 300);
        assertEquals(joinDataSourceListBoList.get(2).getJoinDataSourceBoList().size(), 1);
        assertEquals(joinDataSourceListBoList.get(3).getJoinDataSourceBoList().get(0).getAvgActiveConnectionSize(), 400);
        assertEquals(joinDataSourceListBoList.get(3).getJoinDataSourceBoList().size(), 1);
        assertEquals(joinDataSourceListBoList.get(4).getJoinDataSourceBoList().get(0).getAvgActiveConnectionSize(), 30);
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
        assertEquals(joinDataSourceBo1_1.getAvgActiveConnectionSize(), 286);
        assertEquals(joinDataSourceBo1_1.getMinActiveConnectionSize(), 200);
        assertEquals(joinDataSourceBo1_1.getMinActiveConnectionAgentId(), "agent_id_1_-50");
        assertEquals(joinDataSourceBo1_1.getMaxActiveConnectionSize(), 640);
        assertEquals(joinDataSourceBo1_1.getMaxActiveConnectionAgentId(), "agent_id_6_40");
        JoinDataSourceBo joinDataSourceBo1_2 = joinDataSourceBoList1.get(1);
        assertEquals(joinDataSourceBo1_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo1_2.getUrl(), "jdbc:mssql");
        assertEquals(joinDataSourceBo1_2.getAvgActiveConnectionSize(), 386);
        assertEquals(joinDataSourceBo1_2.getMinActiveConnectionSize(), 300);
        assertEquals(joinDataSourceBo1_2.getMinActiveConnectionAgentId(), "agent_id_1_-50");
        assertEquals(joinDataSourceBo1_2.getMaxActiveConnectionSize(), 740);
        assertEquals(joinDataSourceBo1_2.getMaxActiveConnectionAgentId(), "agent_id_6_40");

        JoinDataSourceListBo joinDataSourceListBo2 = joinDataSourceListBoList.get(1);
        assertEquals(joinDataSourceListBo2.getId(), "id1");
        assertEquals(joinDataSourceListBo2.getTimestamp(), 1487149805000L);
        List<JoinDataSourceBo> joinDataSourceBoList2 = joinDataSourceListBo2.getJoinDataSourceBoList();
        assertEquals(joinDataSourceBoList2.size(), 2);
        JoinDataSourceBo joinDataSourceBo2_1 = joinDataSourceBoList2.get(0);
        assertEquals(joinDataSourceBo2_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo2_1.getUrl(), "jdbc:mysql");
        assertEquals(joinDataSourceBo2_1.getAvgActiveConnectionSize(), 186);
        assertEquals(joinDataSourceBo2_1.getMinActiveConnectionSize(), 0);
        assertEquals(joinDataSourceBo2_1.getMinActiveConnectionAgentId(), "agent_id_2_-50");
        assertEquals(joinDataSourceBo2_1.getMaxActiveConnectionSize(), 740);
        assertEquals(joinDataSourceBo2_1.getMaxActiveConnectionAgentId(), "agent_id_7_40");
        JoinDataSourceBo joinDataSourceBo2_2 = joinDataSourceBoList2.get(1);
        assertEquals(joinDataSourceBo2_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo2_2.getUrl(), "jdbc:mssql");
        assertEquals(joinDataSourceBo2_2.getAvgActiveConnectionSize(), 286);
        assertEquals(joinDataSourceBo2_2.getMinActiveConnectionSize(), 100);
        assertEquals(joinDataSourceBo2_2.getMinActiveConnectionAgentId(), "agent_id_2_-50");
        assertEquals(joinDataSourceBo2_2.getMaxActiveConnectionSize(), 840);
        assertEquals(joinDataSourceBo2_2.getMaxActiveConnectionAgentId(), "agent_id_7_40");

        JoinDataSourceListBo joinDataSourceListBo3 = joinDataSourceListBoList.get(2);
        assertEquals(joinDataSourceListBo3.getId(), "id1");
        assertEquals(joinDataSourceListBo3.getTimestamp(), 1487149810000L);
        List<JoinDataSourceBo> joinDataSourceBoList3 = joinDataSourceListBo3.getJoinDataSourceBoList();
        assertEquals(joinDataSourceBoList3.size(), 2);
        JoinDataSourceBo joinDataSourceBo3_1 = joinDataSourceBoList3.get(0);
        assertEquals(joinDataSourceBo3_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo3_1.getUrl(), "jdbc:mysql");
        assertEquals(joinDataSourceBo3_1.getAvgActiveConnectionSize(), 486);
        assertEquals(joinDataSourceBo3_1.getMinActiveConnectionSize(), 100);
        assertEquals(joinDataSourceBo3_1.getMinActiveConnectionAgentId(), "agent_id_3_-50");
        assertEquals(joinDataSourceBo3_1.getMaxActiveConnectionSize(), 940);
        assertEquals(joinDataSourceBo3_1.getMaxActiveConnectionAgentId(), "agent_id_8_40");
        JoinDataSourceBo joinDataSourceBo3_2 = joinDataSourceBoList3.get(1);
        assertEquals(joinDataSourceBo3_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo3_2.getUrl(), "jdbc:mssql");
        assertEquals(joinDataSourceBo3_2.getAvgActiveConnectionSize(), 586);
        assertEquals(joinDataSourceBo3_2.getMinActiveConnectionSize(), 200);
        assertEquals(joinDataSourceBo3_2.getMinActiveConnectionAgentId(), "agent_id_3_-50");
        assertEquals(joinDataSourceBo3_2.getMaxActiveConnectionSize(), 1040);
        assertEquals(joinDataSourceBo3_2.getMaxActiveConnectionAgentId(), "agent_id_8_40");

        JoinDataSourceListBo joinDataSourceListBo4 = joinDataSourceListBoList.get(3);
        assertEquals(joinDataSourceListBo4.getId(), "id1");
        assertEquals(joinDataSourceListBo4.getTimestamp(), 1487149815000L);
        List<JoinDataSourceBo> joinDataSourceBoList4 = joinDataSourceListBo4.getJoinDataSourceBoList();
        assertEquals(joinDataSourceBoList4.size(), 2);
        JoinDataSourceBo joinDataSourceBo4_1 = joinDataSourceBoList4.get(0);
        assertEquals(joinDataSourceBo4_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo4_1.getUrl(), "jdbc:mysql");
        assertEquals(joinDataSourceBo4_1.getAvgActiveConnectionSize(), 386);
        assertEquals(joinDataSourceBo4_1.getMinActiveConnectionSize(), 500);
        assertEquals(joinDataSourceBo4_1.getMinActiveConnectionAgentId(), "agent_id_4_-50");
        assertEquals(joinDataSourceBo4_1.getMaxActiveConnectionSize(), 640);
        assertEquals(joinDataSourceBo4_1.getMaxActiveConnectionAgentId(), "agent_id_9_40");
        JoinDataSourceBo joinDataSourceBo4_2 = joinDataSourceBoList4.get(1);
        assertEquals(joinDataSourceBo4_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo4_2.getUrl(), "jdbc:mssql");
        assertEquals(joinDataSourceBo4_2.getAvgActiveConnectionSize(), 486);
        assertEquals(joinDataSourceBo4_2.getMinActiveConnectionSize(), 600);
        assertEquals(joinDataSourceBo4_2.getMinActiveConnectionAgentId(), "agent_id_4_-50");
        assertEquals(joinDataSourceBo4_2.getMaxActiveConnectionSize(), 740);
        assertEquals(joinDataSourceBo4_2.getMaxActiveConnectionAgentId(), "agent_id_9_40");

        JoinDataSourceListBo joinDataSourceListBo5 = joinDataSourceListBoList.get(4);
        assertEquals(joinDataSourceListBo5.getId(), "id1");
        assertEquals(joinDataSourceListBo5.getTimestamp(), 1487149820000L);
        List<JoinDataSourceBo> joinDataSourceBoList5 = joinDataSourceListBo5.getJoinDataSourceBoList();
        assertEquals(joinDataSourceBoList5.size(), 2);
        JoinDataSourceBo joinDataSourceBo5_1 = joinDataSourceBoList5.get(0);
        assertEquals(joinDataSourceBo5_1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo5_1.getUrl(), "jdbc:mysql");
        assertEquals(joinDataSourceBo5_1.getAvgActiveConnectionSize(), 86);
        assertEquals(joinDataSourceBo5_1.getMinActiveConnectionSize(), 700);
        assertEquals(joinDataSourceBo5_1.getMinActiveConnectionAgentId(), "agent_id_5_-50");
        assertEquals(joinDataSourceBo5_1.getMaxActiveConnectionSize(), 840);
        assertEquals(joinDataSourceBo5_1.getMaxActiveConnectionAgentId(), "agent_id_10_40");
        JoinDataSourceBo joinDataSourceBo5_2 = joinDataSourceBoList5.get(1);
        assertEquals(joinDataSourceBo5_2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo5_2.getUrl(), "jdbc:mssql");
        assertEquals(joinDataSourceBo5_2.getAvgActiveConnectionSize(), 186);
        assertEquals(joinDataSourceBo5_2.getMinActiveConnectionSize(), 800);
        assertEquals(joinDataSourceBo5_2.getMinActiveConnectionAgentId(), "agent_id_5_-50");
        assertEquals(joinDataSourceBo5_2.getMaxActiveConnectionSize(), 940);
        assertEquals(joinDataSourceBo5_2.getMaxActiveConnectionAgentId(), "agent_id_10_40");

    }

    private class ComparatorImpl6 implements Comparator<JoinDataSourceListBo> {
        @Override
        public int compare(JoinDataSourceListBo bo1, JoinDataSourceListBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private JoinApplicationStatBo createJoinApplicationStatBo6(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinDataSourceListBoList(createJoinDataSourceListBoList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinDataSourceListBo> createJoinDataSourceListBoList(String id, long currentTime, int plus) {
        final List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();

        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300 + plus, 250 + plus, "agent_id_1_" + plus, 600 + plus, "agent_id_6_" + plus));
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 400 + plus, 350 + plus, "agent_id_1_" + plus, 700 + plus, "agent_id_6_" + plus));
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo(id, joinDataSourceBoList1, currentTime);

        List<JoinDataSourceBo> joinDataSourceBoList2 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200 + plus, 50 + plus, "agent_id_2_" + plus, 700 + plus, "agent_id_7_" + plus));
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 300 + plus, 150 + plus, "agent_id_2_" + plus, 800 + plus, "agent_id_7_" + plus));
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo(id, joinDataSourceBoList2, currentTime + 5000);

        List<JoinDataSourceBo> joinDataSourceBoList3 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500 + plus, 150 + plus, "agent_id_3_" + plus, 900 + plus, "agent_id_8_" + plus));
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 600 + plus, 250 + plus, "agent_id_3_" + plus, 1000 + plus, "agent_id_8_" + plus));
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo(id, joinDataSourceBoList3, currentTime + 10000);

        List<JoinDataSourceBo> joinDataSourceBoList4 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400 + plus, 550 + plus, "agent_id_4_" + plus, 600 + plus, "agent_id_9_" + plus));
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 500 + plus, 650 + plus, "agent_id_4_" + plus, 700 + plus, "agent_id_9_" + plus));
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo(id, joinDataSourceBoList4, currentTime + 15000);

        List<JoinDataSourceBo> joinDataSourceBoList5 = new ArrayList<JoinDataSourceBo>();
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
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo7("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinFileDescriptorBo> joinFileDescriptorBoList = resultJoinApplicationStatBo.getJoinFileDescriptorBoList();
        Collections.sort(joinFileDescriptorBoList, new ComparatorImpl7());
        assertJoinFileDescriptorBoList(joinFileDescriptorBoList);
    }
    @Test
    public void joinApplicationStatBoByTimeSlice14Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();

        List<JoinFileDescriptorBo> joinFileDescriptorBoList1 = new ArrayList<JoinFileDescriptorBo>();
        JoinFileDescriptorBo joinFileDescriptorBo1_1 = new JoinFileDescriptorBo("agent1", 440, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo1_2 = new JoinFileDescriptorBo("agent1", 330, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo1_3 = new JoinFileDescriptorBo("agent1", 550, 600, "agent1", 70, "agent1", 1498462555000L);
        joinFileDescriptorBoList1.add(joinFileDescriptorBo1_1);
        joinFileDescriptorBoList1.add(joinFileDescriptorBo1_2);
        joinFileDescriptorBoList1.add(joinFileDescriptorBo1_3);
        JoinApplicationStatBo joinApplicationStatBo1 = new JoinApplicationStatBo();
        joinApplicationStatBo1.setId("test_app");
        joinApplicationStatBo1.setJoinFileDescriptorBoList(joinFileDescriptorBoList1);
        joinApplicationStatBo1.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo1);

        List<JoinFileDescriptorBo> joinFileDescriptorBoList2 = new ArrayList<JoinFileDescriptorBo>();
        JoinFileDescriptorBo joinFileDescriptorBo2_1 = new JoinFileDescriptorBo("agent1", 330, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo2_2 = new JoinFileDescriptorBo("agent1", 220, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo2_3 = new JoinFileDescriptorBo("agent1", 110, 600, "agent1", 70, "agent1", 1498462555000L);
        JoinFileDescriptorBo joinFileDescriptorBo2_4 = new JoinFileDescriptorBo("agent1", 770, 600, "agent1", 70, "agent1", 1498462560000L);
        joinFileDescriptorBoList2.add(joinFileDescriptorBo2_1);
        joinFileDescriptorBoList2.add(joinFileDescriptorBo2_2);
        joinFileDescriptorBoList2.add(joinFileDescriptorBo2_3);
        joinFileDescriptorBoList2.add(joinFileDescriptorBo2_4);
        JoinApplicationStatBo joinApplicationStatBo2 = new JoinApplicationStatBo();
        joinApplicationStatBo2.setId("test_app");
        joinApplicationStatBo2.setJoinFileDescriptorBoList(joinFileDescriptorBoList2);
        joinApplicationStatBo2.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo2);

        List<JoinFileDescriptorBo> joinFileDescriptorBoList3 = new ArrayList<JoinFileDescriptorBo>();
        JoinFileDescriptorBo joinFileDescriptorBo3_1 = new JoinFileDescriptorBo("agent1", 220, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo3_2 = new JoinFileDescriptorBo("agent1", 110, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo3_3 = new JoinFileDescriptorBo("agent1", 880, 600, "agent1", 70, "agent1", 1498462565000L);
        joinFileDescriptorBoList3.add(joinFileDescriptorBo3_1);
        joinFileDescriptorBoList3.add(joinFileDescriptorBo3_2);
        joinFileDescriptorBoList3.add(joinFileDescriptorBo3_3);
        JoinApplicationStatBo joinApplicationStatBo3 = new JoinApplicationStatBo();
        joinApplicationStatBo3.setId("test_app");
        joinApplicationStatBo3.setJoinFileDescriptorBoList(joinFileDescriptorBoList3);
        joinApplicationStatBo3.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo3);


        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinFileDescriptorBo> joinFileDescriptorBoList = joinApplicationStatBo.getJoinFileDescriptorBoList();
        Collections.sort(joinFileDescriptorBoList, new ComparatorImpl7());

        assertEquals(joinFileDescriptorBoList.size(), 5);
        assertEquals(joinFileDescriptorBoList.get(0).getAvgOpenFDCount(), 330,0);
        assertEquals(joinFileDescriptorBoList.get(1).getAvgOpenFDCount(), 220,0);
        assertEquals(joinFileDescriptorBoList.get(2).getAvgOpenFDCount(), 330,0);
        assertEquals(joinFileDescriptorBoList.get(3).getAvgOpenFDCount(), 770,0);
        assertEquals(joinFileDescriptorBoList.get(4).getAvgOpenFDCount(), 880,0);
    }

    private class ComparatorImpl7 implements Comparator<JoinFileDescriptorBo> {
        @Override
        public int compare(JoinFileDescriptorBo bo1, JoinFileDescriptorBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private void assertJoinFileDescriptorBoList(List<JoinFileDescriptorBo> joinFileDescriptorBoList) {
        assertEquals(joinFileDescriptorBoList.size(), 5);
        JoinFileDescriptorBo joinFileDescriptorBo1 = joinFileDescriptorBoList.get(0);
        assertEquals(joinFileDescriptorBo1.getId(), "id1");
        assertEquals(joinFileDescriptorBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinFileDescriptorBo1.getAvgOpenFDCount(), 486, 0);
        assertEquals(joinFileDescriptorBo1.getMinOpenFDCount(), 220, 0);
        assertEquals(joinFileDescriptorBo1.getMinOpenFDCountAgentId(), "id5_2");
        assertEquals(joinFileDescriptorBo1.getMaxOpenFDCount(), 910, 0);
        assertEquals(joinFileDescriptorBo1.getMaxOpenFDCountAgentId(), "id4_1");

        JoinFileDescriptorBo joinFileDescriptorBo2 = joinFileDescriptorBoList.get(1);
        assertEquals(joinFileDescriptorBo2.getId(), "id1");
        assertEquals(joinFileDescriptorBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinFileDescriptorBo2.getAvgOpenFDCount(), 386, 0);
        assertEquals(joinFileDescriptorBo2.getMinOpenFDCount(), 350, 0);
        assertEquals(joinFileDescriptorBo2.getMinOpenFDCountAgentId(), "id5_2");
        assertEquals(joinFileDescriptorBo2.getMaxOpenFDCount(), 810, 0);
        assertEquals(joinFileDescriptorBo2.getMaxOpenFDCountAgentId(), "id4_1");

        JoinFileDescriptorBo joinFileDescriptorBo3 = joinFileDescriptorBoList.get(2);
        assertEquals(joinFileDescriptorBo3.getId(), "id1");
        assertEquals(joinFileDescriptorBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinFileDescriptorBo3.getAvgOpenFDCount(), 286, 0);
        assertEquals(joinFileDescriptorBo3.getMinOpenFDCount(), 220, 0);
        assertEquals(joinFileDescriptorBo3.getMinOpenFDCountAgentId(), "id5_2");
        assertEquals(joinFileDescriptorBo3.getMaxOpenFDCount(), 710, 0);
        assertEquals(joinFileDescriptorBo3.getMaxOpenFDCountAgentId(), "id4_1");

        JoinFileDescriptorBo joinFileDescriptorBo4 = joinFileDescriptorBoList.get(3);
        assertEquals(joinFileDescriptorBo4.getId(), "id1");
        assertEquals(joinFileDescriptorBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinFileDescriptorBo4.getAvgOpenFDCount(), 186, 0);
        assertEquals(joinFileDescriptorBo4.getMinOpenFDCount(), 120, 0);
        assertEquals(joinFileDescriptorBo4.getMinOpenFDCountAgentId(), "id5_2");
        assertEquals(joinFileDescriptorBo4.getMaxOpenFDCount(), 610, 0);
        assertEquals(joinFileDescriptorBo4.getMaxOpenFDCountAgentId(), "id4_1");

        JoinFileDescriptorBo joinFileDescriptorBo5 = joinFileDescriptorBoList.get(4);
        assertEquals(joinFileDescriptorBo5.getId(), "id1");
        assertEquals(joinFileDescriptorBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinFileDescriptorBo5.getAvgOpenFDCount(), 86, 0);
        assertEquals(joinFileDescriptorBo5.getMinOpenFDCount(), 20, 0);
        assertEquals(joinFileDescriptorBo5.getMinOpenFDCountAgentId(), "id5_2");
        assertEquals(joinFileDescriptorBo5.getMaxOpenFDCount(), 930, 0);
        assertEquals(joinFileDescriptorBo5.getMaxOpenFDCountAgentId(), "id4_1");
    }

    private JoinApplicationStatBo createJoinApplicationStatBo7(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinFileDescriptorBoList(createJoinFileDescriptorBoList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinFileDescriptorBo> createJoinFileDescriptorBoList(final String id, final long currentTime, int plus) {
        final List<JoinFileDescriptorBo> joinFileDescriptorBoList = new ArrayList<JoinFileDescriptorBo>();
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
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id1", currentTime, 10));
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id2", currentTime + 1000, -40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id3", currentTime + 2000, -30));
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id4", currentTime + 3000, 40));
        joinApplicationStatBoList.add(createJoinApplicationStatBo8("id5", currentTime + 4000, -50));
        JoinApplicationStatBo resultJoinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        List<JoinDirectBufferBo> joinDirectBufferBoList = resultJoinApplicationStatBo.getJoinDirectBufferBoList();
        Collections.sort(joinDirectBufferBoList, new ComparatorImpl8());
        assertJoinDirectBufferBoList(joinDirectBufferBoList);
    }
    @Test
    public void joinApplicationStatBoByTimeSlice16Test() {
        List<JoinApplicationStatBo> joinApplicationStatBoList = new ArrayList<JoinApplicationStatBo>();

        List<JoinDirectBufferBo> joinDirectBufferBoList1 = new ArrayList<JoinDirectBufferBo>();
        JoinDirectBufferBo joinDirectBufferBo1_1 = new JoinDirectBufferBo("agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", 440, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinDirectBufferBo joinDirectBufferBo1_2 = new JoinDirectBufferBo("agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", 330, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinDirectBufferBo joinDirectBufferBo1_3 = new JoinDirectBufferBo("agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", 550, 600, "agent1", 70, "agent1", 1498462555000L);
        joinDirectBufferBoList1.add(joinDirectBufferBo1_1);
        joinDirectBufferBoList1.add(joinDirectBufferBo1_2);
        joinDirectBufferBoList1.add(joinDirectBufferBo1_3);
        JoinApplicationStatBo joinApplicationStatBo1 = new JoinApplicationStatBo();
        joinApplicationStatBo1.setId("test_app");
        joinApplicationStatBo1.setJoinDirectBufferBoList(joinDirectBufferBoList1);
        joinApplicationStatBo1.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo1);

        List<JoinDirectBufferBo> joinDirectBufferBoList2 = new ArrayList<JoinDirectBufferBo>();
        JoinDirectBufferBo joinDirectBufferBo2_1 = new JoinDirectBufferBo("agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", 330, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinDirectBufferBo joinDirectBufferBo2_2 = new JoinDirectBufferBo("agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", 220, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinDirectBufferBo joinDirectBufferBo2_3 = new JoinDirectBufferBo("agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", 110, 600, "agent1", 70, "agent1", 1498462555000L);
        JoinDirectBufferBo joinDirectBufferBo2_4 = new JoinDirectBufferBo("agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", 770, 600, "agent1", 70, "agent1", 1498462560000L);
        joinDirectBufferBoList2.add(joinDirectBufferBo2_1);
        joinDirectBufferBoList2.add(joinDirectBufferBo2_2);
        joinDirectBufferBoList2.add(joinDirectBufferBo2_3);
        joinDirectBufferBoList2.add(joinDirectBufferBo2_4);
        JoinApplicationStatBo joinApplicationStatBo2 = new JoinApplicationStatBo();
        joinApplicationStatBo2.setId("test_app");
        joinApplicationStatBo2.setJoinDirectBufferBoList(joinDirectBufferBoList2);
        joinApplicationStatBo2.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo2);

        List<JoinDirectBufferBo> joinDirectBufferBoList3 = new ArrayList<JoinDirectBufferBo>();
        JoinDirectBufferBo joinDirectBufferBo3_1 = new JoinDirectBufferBo("agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", 220, 700, "agent1", 300, "agent1", 1498462545000L);
        JoinDirectBufferBo joinDirectBufferBo3_2 = new JoinDirectBufferBo("agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", 110, 400, "agent1", 100, "agent1", 1498462550000L);
        JoinDirectBufferBo joinDirectBufferBo3_3 = new JoinDirectBufferBo("agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", 880, 600, "agent1", 70, "agent1", 1498462565000L);
        joinDirectBufferBoList3.add(joinDirectBufferBo3_1);
        joinDirectBufferBoList3.add(joinDirectBufferBo3_2);
        joinDirectBufferBoList3.add(joinDirectBufferBo3_3);
        JoinApplicationStatBo joinApplicationStatBo3 = new JoinApplicationStatBo();
        joinApplicationStatBo3.setId("test_app");
        joinApplicationStatBo3.setJoinDirectBufferBoList(joinDirectBufferBoList3);
        joinApplicationStatBo3.setTimestamp(1498462545000L);
        joinApplicationStatBoList.add(joinApplicationStatBo3);


        JoinApplicationStatBo joinApplicationStatBo = JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicationStatBoList);
        assertEquals(joinApplicationStatBo.getId(), "test_app");
        assertEquals(joinApplicationStatBo.getTimestamp(), 1498462545000L);
        List<JoinDirectBufferBo> joinDirectBufferBoList = joinApplicationStatBo.getJoinDirectBufferBoList();
        Collections.sort(joinDirectBufferBoList, new ComparatorImpl8());

        assertEquals(joinDirectBufferBoList.size(), 5);
        assertEquals(joinDirectBufferBoList.get(0).getAvgDirectCount(), 330,0);
        assertEquals(joinDirectBufferBoList.get(0).getAvgDirectMemoryUsed(), 330,0);
        assertEquals(joinDirectBufferBoList.get(0).getAvgMappedCount(), 330,0);
        assertEquals(joinDirectBufferBoList.get(0).getAvgMappedMemoryUsed(), 330,0);

        assertEquals(joinDirectBufferBoList.get(1).getAvgDirectCount(), 220,0);
        assertEquals(joinDirectBufferBoList.get(1).getAvgDirectMemoryUsed(), 220,0);
        assertEquals(joinDirectBufferBoList.get(1).getAvgMappedCount(), 220,0);
        assertEquals(joinDirectBufferBoList.get(1).getAvgMappedMemoryUsed(), 220,0);

        assertEquals(joinDirectBufferBoList.get(2).getAvgDirectCount(), 330,0);
        assertEquals(joinDirectBufferBoList.get(2).getAvgDirectMemoryUsed(), 330,0);
        assertEquals(joinDirectBufferBoList.get(2).getAvgMappedCount(), 330,0);
        assertEquals(joinDirectBufferBoList.get(2).getAvgMappedMemoryUsed(), 330,0);

        assertEquals(joinDirectBufferBoList.get(3).getAvgDirectCount(), 770,0);
        assertEquals(joinDirectBufferBoList.get(3).getAvgDirectMemoryUsed(), 770,0);
        assertEquals(joinDirectBufferBoList.get(3).getAvgMappedCount(), 770,0);
        assertEquals(joinDirectBufferBoList.get(3).getAvgMappedMemoryUsed(), 770,0);

        assertEquals(joinDirectBufferBoList.get(4).getAvgDirectCount(), 880,0);
        assertEquals(joinDirectBufferBoList.get(4).getAvgDirectMemoryUsed(), 880,0);
        assertEquals(joinDirectBufferBoList.get(4).getAvgMappedCount(), 880,0);
        assertEquals(joinDirectBufferBoList.get(4).getAvgMappedMemoryUsed(), 880,0);
    }

    private class ComparatorImpl8 implements Comparator<JoinDirectBufferBo> {
        @Override
        public int compare(JoinDirectBufferBo bo1, JoinDirectBufferBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private void assertJoinDirectBufferBoList(List<JoinDirectBufferBo> joinDirectBufferBoList) {
        assertEquals(joinDirectBufferBoList.size(), 5);
        JoinDirectBufferBo joinDirectBufferBo1 = joinDirectBufferBoList.get(0);
        assertEquals(joinDirectBufferBo1.getId(), "id1");
        //1
        assertEquals(joinDirectBufferBo1.getTimestamp(), 1487149800000L);
        assertEquals(joinDirectBufferBo1.getAvgDirectCount(), 486, 0);
        assertEquals(joinDirectBufferBo1.getMinDirectCount(), 220, 0);
        assertEquals(joinDirectBufferBo1.getMinDirectCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo1.getMaxDirectCount(), 910, 0);
        assertEquals(joinDirectBufferBo1.getMaxDirectCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo1.getAvgDirectMemoryUsed(), 486, 0);
        assertEquals(joinDirectBufferBo1.getMinDirectMemoryUsed(), 220, 0);
        assertEquals(joinDirectBufferBo1.getMinDirectMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo1.getMaxDirectMemoryUsed(), 910, 0);
        assertEquals(joinDirectBufferBo1.getMaxDirectMemoryUsedAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo1.getAvgMappedCount(), 486, 0);
        assertEquals(joinDirectBufferBo1.getMinMappedCount(), 220, 0);
        assertEquals(joinDirectBufferBo1.getMinMappedCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo1.getMaxMappedCount(), 910, 0);
        assertEquals(joinDirectBufferBo1.getMaxMappedCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo1.getAvgMappedMemoryUsed(), 486, 0);
        assertEquals(joinDirectBufferBo1.getMinMappedMemoryUsed(), 220, 0);
        assertEquals(joinDirectBufferBo1.getMinMappedMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo1.getMaxMappedMemoryUsed(), 910, 0);
        assertEquals(joinDirectBufferBo1.getMaxMappedMemoryUsedAgentId(), "id4_1");

        //2
        JoinDirectBufferBo joinDirectBufferBo2 = joinDirectBufferBoList.get(1);
        assertEquals(joinDirectBufferBo2.getId(), "id1");
        assertEquals(joinDirectBufferBo2.getTimestamp(), 1487149805000L);
        assertEquals(joinDirectBufferBo2.getAvgDirectCount(), 386, 0);
        assertEquals(joinDirectBufferBo2.getMinDirectCount(), 350, 0);
        assertEquals(joinDirectBufferBo2.getMinDirectCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo2.getMaxDirectCount(), 810, 0);
        assertEquals(joinDirectBufferBo2.getMaxDirectCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo2.getAvgDirectMemoryUsed(), 386, 0);
        assertEquals(joinDirectBufferBo2.getMinDirectMemoryUsed(), 350, 0);
        assertEquals(joinDirectBufferBo2.getMinDirectMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo2.getMaxDirectMemoryUsed(), 810, 0);
        assertEquals(joinDirectBufferBo2.getMaxDirectMemoryUsedAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo2.getAvgMappedCount(), 386, 0);
        assertEquals(joinDirectBufferBo2.getMinMappedCount(), 350, 0);
        assertEquals(joinDirectBufferBo2.getMinMappedCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo2.getMaxMappedCount(), 810, 0);
        assertEquals(joinDirectBufferBo2.getMaxMappedCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo2.getAvgMappedMemoryUsed(), 386, 0);
        assertEquals(joinDirectBufferBo2.getMinMappedMemoryUsed(), 350, 0);
        assertEquals(joinDirectBufferBo2.getMinMappedMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo2.getMaxMappedMemoryUsed(), 810, 0);
        assertEquals(joinDirectBufferBo2.getMaxMappedMemoryUsedAgentId(), "id4_1");

        //3
        JoinDirectBufferBo joinDirectBufferBo3 = joinDirectBufferBoList.get(2);
        assertEquals(joinDirectBufferBo3.getId(), "id1");
        assertEquals(joinDirectBufferBo3.getTimestamp(), 1487149810000L);
        assertEquals(joinDirectBufferBo3.getAvgDirectCount(), 286, 0);
        assertEquals(joinDirectBufferBo3.getMinDirectCount(), 220, 0);
        assertEquals(joinDirectBufferBo3.getMinDirectCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo3.getMaxDirectCount(), 710, 0);
        assertEquals(joinDirectBufferBo3.getMaxDirectCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo3.getAvgDirectMemoryUsed(), 286, 0);
        assertEquals(joinDirectBufferBo3.getMinDirectMemoryUsed(), 220, 0);
        assertEquals(joinDirectBufferBo3.getMinDirectMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo3.getMaxDirectMemoryUsed(), 710, 0);
        assertEquals(joinDirectBufferBo3.getMaxDirectMemoryUsedAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo3.getAvgMappedCount(), 286, 0);
        assertEquals(joinDirectBufferBo3.getMinMappedCount(), 220, 0);
        assertEquals(joinDirectBufferBo3.getMinMappedCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo3.getMaxMappedCount(), 710, 0);
        assertEquals(joinDirectBufferBo3.getMaxMappedCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo3.getAvgMappedMemoryUsed(), 286, 0);
        assertEquals(joinDirectBufferBo3.getMinMappedMemoryUsed(), 220, 0);
        assertEquals(joinDirectBufferBo3.getMinMappedMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo3.getMaxMappedMemoryUsed(), 710, 0);
        assertEquals(joinDirectBufferBo3.getMaxMappedMemoryUsedAgentId(), "id4_1");

        //4
        JoinDirectBufferBo joinDirectBufferBo4 = joinDirectBufferBoList.get(3);
        assertEquals(joinDirectBufferBo4.getId(), "id1");
        assertEquals(joinDirectBufferBo4.getTimestamp(), 1487149815000L);
        assertEquals(joinDirectBufferBo4.getAvgDirectCount(), 186, 0);
        assertEquals(joinDirectBufferBo4.getMinDirectCount(), 120, 0);
        assertEquals(joinDirectBufferBo4.getMinDirectCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo4.getMaxDirectCount(), 610, 0);
        assertEquals(joinDirectBufferBo4.getMaxDirectCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo4.getAvgDirectMemoryUsed(), 186, 0);
        assertEquals(joinDirectBufferBo4.getMinDirectMemoryUsed(), 120, 0);
        assertEquals(joinDirectBufferBo4.getMinDirectMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo4.getMaxDirectMemoryUsed(), 610, 0);
        assertEquals(joinDirectBufferBo4.getMaxDirectMemoryUsedAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo4.getAvgMappedCount(), 186, 0);
        assertEquals(joinDirectBufferBo4.getMinMappedCount(), 120, 0);
        assertEquals(joinDirectBufferBo4.getMinMappedCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo4.getMaxMappedCount(), 610, 0);
        assertEquals(joinDirectBufferBo4.getMaxMappedCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo4.getAvgMappedMemoryUsed(), 186, 0);
        assertEquals(joinDirectBufferBo4.getMinMappedMemoryUsed(), 120, 0);
        assertEquals(joinDirectBufferBo4.getMinMappedMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo4.getMaxMappedMemoryUsed(), 610, 0);
        assertEquals(joinDirectBufferBo4.getMaxMappedMemoryUsedAgentId(), "id4_1");

        //5
        JoinDirectBufferBo joinDirectBufferBo5 = joinDirectBufferBoList.get(4);
        assertEquals(joinDirectBufferBo5.getId(), "id1");
        assertEquals(joinDirectBufferBo5.getTimestamp(), 1487149820000L);
        assertEquals(joinDirectBufferBo5.getAvgDirectCount(), 86, 0);
        assertEquals(joinDirectBufferBo5.getMinDirectCount(), 20, 0);
        assertEquals(joinDirectBufferBo5.getMinDirectCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo5.getMaxDirectCount(), 930, 0);
        assertEquals(joinDirectBufferBo5.getMaxDirectCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo5.getAvgDirectMemoryUsed(), 86, 0);
        assertEquals(joinDirectBufferBo5.getMinDirectMemoryUsed(), 20, 0);
        assertEquals(joinDirectBufferBo5.getMinDirectMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo5.getMaxDirectMemoryUsed(), 930, 0);
        assertEquals(joinDirectBufferBo5.getMaxDirectMemoryUsedAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo5.getAvgMappedCount(), 86, 0);
        assertEquals(joinDirectBufferBo5.getMinMappedCount(), 20, 0);
        assertEquals(joinDirectBufferBo5.getMinMappedCountAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo5.getMaxMappedCount(), 930, 0);
        assertEquals(joinDirectBufferBo5.getMaxMappedCountAgentId(), "id4_1");

        assertEquals(joinDirectBufferBo5.getAvgMappedMemoryUsed(), 86, 0);
        assertEquals(joinDirectBufferBo5.getMinMappedMemoryUsed(), 20, 0);
        assertEquals(joinDirectBufferBo5.getMinMappedMemoryUsedAgentId(), "id5_2");
        assertEquals(joinDirectBufferBo5.getMaxMappedMemoryUsed(), 930, 0);
        assertEquals(joinDirectBufferBo5.getMaxMappedMemoryUsedAgentId(), "id4_1");

    }

    private JoinApplicationStatBo createJoinApplicationStatBo8(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinDirectBufferBoList(createJoinDirectBufferBoList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_STST);
        return joinApplicationStatBo;
    }

    private List<JoinDirectBufferBo> createJoinDirectBufferBoList(final String id, final long currentTime, int plus) {
        final List<JoinDirectBufferBo> joinDirectBufferBoList = new ArrayList<JoinDirectBufferBo>();
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

        List<JoinActiveTraceBo> JoinActiveTraceBoList = new ArrayList<JoinActiveTraceBo>();
        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462565000L);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462570000L);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462575000L);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462580000L);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462585000L);
        JoinActiveTraceBoList.add(joinActiveTraceBo1);
        JoinActiveTraceBoList.add(joinActiveTraceBo2);
        JoinActiveTraceBoList.add(joinActiveTraceBo3);
        JoinActiveTraceBoList.add(joinActiveTraceBo4);
        JoinActiveTraceBoList.add(joinActiveTraceBo5);
        joinAgentStatBo.setJoinActiveTraceBoList(JoinActiveTraceBoList);

        List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo("agent1", 1498462565000L, 3000, 2, "app_1_1", 6000, "app_1_2");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo("agent1", 1498462570000L, 4000, 200, "app_2_1", 9000, "app_2_2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo("agent1", 1498462575000L, 2000, 20, "app_3_1", 7000, "app_3_2");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo("agent1", 1498462580000L, 5000, 20, "app_4_1", 8000, "app_4_2");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo("agent1", 1498462585000L, 1000, 10, "app_5_1", 6600, "app_5_2");
        joinResponseTimeBoList.add(joinResponseTimeBo1);
        joinResponseTimeBoList.add(joinResponseTimeBo2);
        joinResponseTimeBoList.add(joinResponseTimeBo3);
        joinResponseTimeBoList.add(joinResponseTimeBo4);
        joinResponseTimeBoList.add(joinResponseTimeBo5);
        joinAgentStatBo.setJoinResponseTimeBoList(joinResponseTimeBoList);

        final List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();

        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"));
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6"));
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462565000L);

        List<JoinDataSourceBo> joinDataSourceBoList2 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"));
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7"));
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, 1498462570000L);

        List<JoinDataSourceBo> joinDataSourceBoList3 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"));
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8"));
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, 1498462575000L);

        List<JoinDataSourceBo> joinDataSourceBoList4 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"));
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9"));
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, 1498462580000L);

        List<JoinDataSourceBo> joinDataSourceBoList5 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"));
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10"));
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, 1498462585000L);

        joinDataSourceListBoList.add(joinDataSourceListBo1);
        joinDataSourceListBoList.add(joinDataSourceListBo2);
        joinDataSourceListBoList.add(joinDataSourceListBo3);
        joinDataSourceListBoList.add(joinDataSourceListBo4);
        joinDataSourceListBoList.add(joinDataSourceListBo5);
        joinAgentStatBo.setJoinDataSourceListBoList(joinDataSourceListBoList);

        List<JoinFileDescriptorBo> joinFileDescriptorBoList = new ArrayList<JoinFileDescriptorBo>();
        JoinFileDescriptorBo joinFileDescriptorBo1 = new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", 1498462565000L);
        JoinFileDescriptorBo joinFileDescriptorBo2 = new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", 1498462570000L);
        JoinFileDescriptorBo joinFileDescriptorBo3 = new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", 1498462575000L);
        JoinFileDescriptorBo joinFileDescriptorBo4 = new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", 1498462580000L);
        JoinFileDescriptorBo joinFileDescriptorBo5 = new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", 1498462585000L);
        joinFileDescriptorBoList.add(joinFileDescriptorBo1);
        joinFileDescriptorBoList.add(joinFileDescriptorBo2);
        joinFileDescriptorBoList.add(joinFileDescriptorBo3);
        joinFileDescriptorBoList.add(joinFileDescriptorBo4);
        joinFileDescriptorBoList.add(joinFileDescriptorBo5);
        joinAgentStatBo.setJoinFileDescriptorBoList(joinFileDescriptorBoList);

        List<JoinDirectBufferBo> joinDirectBufferBoList = new ArrayList<JoinDirectBufferBo>();
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
        joinDirectBufferBoList.add(joinDirectBufferBo1);
        joinDirectBufferBoList.add(joinDirectBufferBo2);
        joinDirectBufferBoList.add(joinDirectBufferBo3);
        joinDirectBufferBoList.add(joinDirectBufferBo4);
        joinDirectBufferBoList.add(joinDirectBufferBo5);
        joinAgentStatBo.setJoinDirectBufferBoList(joinDirectBufferBoList);

        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", joinAgentStatBo, 60000);
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
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo("agent2", 5000, 300, 10, "agent2", 400, "agent1", 1498462550000L);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo("agent3", 5000, 30, 5, "agent3", 100, "agent3", 1498462555000L);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo("agent4", 5000, 30, 5, "agent4", 100, "agent4", 1498462560000L);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo("agent5", 5000, 30, 5, "agent5", 100, "agent5", 1498462565000L);
        joinTransactionBoList.add(joinTransactionBo1);
        joinTransactionBoList.add(joinTransactionBo2);
        joinTransactionBoList.add(joinTransactionBo3);
        joinTransactionBoList.add(joinTransactionBo4);
        joinTransactionBoList.add(joinTransactionBo5);
        joinAgentStatBo.setJoinTransactionBoList(joinTransactionBoList);

        List<JoinActiveTraceBo> JoinActiveTraceBoList = new ArrayList<JoinActiveTraceBo>();
        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462555000L);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462560000L);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462565000L);
        JoinActiveTraceBoList.add(joinActiveTraceBo1);
        JoinActiveTraceBoList.add(joinActiveTraceBo2);
        JoinActiveTraceBoList.add(joinActiveTraceBo3);
        JoinActiveTraceBoList.add(joinActiveTraceBo4);
        JoinActiveTraceBoList.add(joinActiveTraceBo5);
        joinAgentStatBo.setJoinActiveTraceBoList(JoinActiveTraceBoList);

        List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo("agent1", 1498462545000L, 3000, 2, "app_1_1", 6000, "app_1_2");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo("agent1", 1498462550000L, 4000, 200, "app_2_1", 9000, "app_2_2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo("agent1", 1498462555000L, 2000, 20, "app_3_1", 7000, "app_3_2");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo("agent1", 1498462560000L, 5000, 20, "app_4_1", 8000, "app_4_2");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo("agent1", 1498462565000L, 1000, 10, "app_5_1", 6600, "app_5_2");
        joinResponseTimeBoList.add(joinResponseTimeBo1);
        joinResponseTimeBoList.add(joinResponseTimeBo2);
        joinResponseTimeBoList.add(joinResponseTimeBo3);
        joinResponseTimeBoList.add(joinResponseTimeBo4);
        joinResponseTimeBoList.add(joinResponseTimeBo5);
        joinAgentStatBo.setJoinResponseTimeBoList(joinResponseTimeBoList);

        final List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();

        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"));
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6"));
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462545000L);

        List<JoinDataSourceBo> joinDataSourceBoList2 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"));
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7"));
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, 1498462550000L);

        List<JoinDataSourceBo> joinDataSourceBoList3 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"));
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8"));
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, 1498462555000L);

        List<JoinDataSourceBo> joinDataSourceBoList4 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"));
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9"));
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, 1498462560000L);

        List<JoinDataSourceBo> joinDataSourceBoList5 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"));
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10"));
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, 1498462565000L);

        joinDataSourceListBoList.add(joinDataSourceListBo1);
        joinDataSourceListBoList.add(joinDataSourceListBo2);
        joinDataSourceListBoList.add(joinDataSourceListBo3);
        joinDataSourceListBoList.add(joinDataSourceListBo4);
        joinDataSourceListBoList.add(joinDataSourceListBo5);
        joinAgentStatBo.setJoinDataSourceListBoList(joinDataSourceListBoList);

        List<JoinFileDescriptorBo> joinFileDescriptorBoList = new ArrayList<JoinFileDescriptorBo>();
        JoinFileDescriptorBo joinFileDescriptorBo1 = new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo2 = new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo3 = new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", 1498462555000L);
        JoinFileDescriptorBo joinFileDescriptorBo4 = new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", 1498462560000L);
        JoinFileDescriptorBo joinFileDescriptorBo5 = new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", 1498462565000L);
        joinFileDescriptorBoList.add(joinFileDescriptorBo1);
        joinFileDescriptorBoList.add(joinFileDescriptorBo2);
        joinFileDescriptorBoList.add(joinFileDescriptorBo3);
        joinFileDescriptorBoList.add(joinFileDescriptorBo4);
        joinFileDescriptorBoList.add(joinFileDescriptorBo5);
        joinAgentStatBo.setJoinFileDescriptorBoList(joinFileDescriptorBoList);

        List<JoinDirectBufferBo> joinDirectBufferBoList = new ArrayList<JoinDirectBufferBo>();
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
        joinDirectBufferBoList.add(joinDirectBufferBo1);
        joinDirectBufferBoList.add(joinDirectBufferBo2);
        joinDirectBufferBoList.add(joinDirectBufferBo3);
        joinDirectBufferBoList.add(joinDirectBufferBo4);
        joinDirectBufferBoList.add(joinDirectBufferBo5);
        joinAgentStatBo.setJoinDirectBufferBoList(joinDirectBufferBoList);

        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", joinAgentStatBo, 60000);
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

        List<JoinActiveTraceBo> JoinActiveTraceBoList = new ArrayList<JoinActiveTraceBo>();
        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462545000L);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462550000L);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462555000L);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462560000L);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo("agent1", 1, (short)2, 30, 15, "app_1_1", 40, "app_1_2", 1498462565000L);
        JoinActiveTraceBoList.add(joinActiveTraceBo1);
        JoinActiveTraceBoList.add(joinActiveTraceBo2);
        JoinActiveTraceBoList.add(joinActiveTraceBo3);
        JoinActiveTraceBoList.add(joinActiveTraceBo4);
        JoinActiveTraceBoList.add(joinActiveTraceBo5);
        joinAgentStatBo.setJoinActiveTraceBoList(JoinActiveTraceBoList);

        List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo("agent1", 1498462545000L, 3000, 2, "app_1_1", 6000, "app_1_2");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo("agent1", 1498462550000L, 4000, 200, "app_2_1", 9000, "app_2_2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo("agent1", 1498462555000L, 2000, 20, "app_3_1", 7000, "app_3_2");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo("agent1", 1498462560000L, 5000, 20, "app_4_1", 8000, "app_4_2");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo("agent1", 1498462565000L, 1000, 10, "app_5_1", 6600, "app_5_2");
        joinResponseTimeBoList.add(joinResponseTimeBo1);
        joinResponseTimeBoList.add(joinResponseTimeBo2);
        joinResponseTimeBoList.add(joinResponseTimeBo3);
        joinResponseTimeBoList.add(joinResponseTimeBo4);
        joinResponseTimeBoList.add(joinResponseTimeBo5);
        joinAgentStatBo.setJoinResponseTimeBoList(joinResponseTimeBoList);

        final List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();

        List<JoinDataSourceBo> joinDataSourceBoList1 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 300, 250, "agent_id_1", 600, "agent_id_6"));
        joinDataSourceBoList1.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 400, 350, "agent_id_1", 700, "agent_id_6"));
        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo("agent1", joinDataSourceBoList1, 1498462545000L);

        List<JoinDataSourceBo> joinDataSourceBoList2 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 200, 50, "agent_id_2", 700, "agent_id_7"));
        joinDataSourceBoList2.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 300, 150, "agent_id_2", 800, "agent_id_7"));
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo("agent1", joinDataSourceBoList2, 1498462550000L);

        List<JoinDataSourceBo> joinDataSourceBoList3 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 500, 150, "agent_id_3", 900, "agent_id_8"));
        joinDataSourceBoList3.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 600, 250, "agent_id_3", 1000, "agent_id_8"));
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo("agent1", joinDataSourceBoList3, 1498462555000L);

        List<JoinDataSourceBo> joinDataSourceBoList4 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 400, 550, "agent_id_4", 600, "agent_id_9"));
        joinDataSourceBoList4.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 500, 650, "agent_id_4", 700, "agent_id_9"));
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo("agent1", joinDataSourceBoList4, 1498462560000L);

        List<JoinDataSourceBo> joinDataSourceBoList5 = new ArrayList<JoinDataSourceBo>();
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)1000, "jdbc:mysql", 100, 750, "agent_id_5", 800, "agent_id_10"));
        joinDataSourceBoList5.add(new JoinDataSourceBo((short)2000, "jdbc:mssql", 200, 850, "agent_id_5", 900, "agent_id_10"));
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo("agent1", joinDataSourceBoList5, 1498462565000L);

        joinDataSourceListBoList.add(joinDataSourceListBo1);
        joinDataSourceListBoList.add(joinDataSourceListBo2);
        joinDataSourceListBoList.add(joinDataSourceListBo3);
        joinDataSourceListBoList.add(joinDataSourceListBo4);
        joinDataSourceListBoList.add(joinDataSourceListBo5);
        joinAgentStatBo.setJoinDataSourceListBoList(joinDataSourceListBoList);

        List<JoinFileDescriptorBo> joinFileDescriptorBoList = new ArrayList<JoinFileDescriptorBo>();
        JoinFileDescriptorBo joinFileDescriptorBo1 = new JoinFileDescriptorBo("agent1", 44, 70, "agent1", 30, "agent1", 1498462545000L);
        JoinFileDescriptorBo joinFileDescriptorBo2 = new JoinFileDescriptorBo("agent1", 33, 40, "agent1", 10, "agent1", 1498462550000L);
        JoinFileDescriptorBo joinFileDescriptorBo3 = new JoinFileDescriptorBo("agent1", 55, 60, "agent1", 7, "agent1", 1498462555000L);
        JoinFileDescriptorBo joinFileDescriptorBo4 = new JoinFileDescriptorBo("agent1", 11, 80, "agent1", 8, "agent1", 1498462560000L);
        JoinFileDescriptorBo joinFileDescriptorBo5 = new JoinFileDescriptorBo("agent1", 22, 70, "agent1", 12, "agent1", 1498462565000L);
        joinFileDescriptorBoList.add(joinFileDescriptorBo1);
        joinFileDescriptorBoList.add(joinFileDescriptorBo2);
        joinFileDescriptorBoList.add(joinFileDescriptorBo3);
        joinFileDescriptorBoList.add(joinFileDescriptorBo4);
        joinFileDescriptorBoList.add(joinFileDescriptorBo5);
        joinAgentStatBo.setJoinFileDescriptorBoList(joinFileDescriptorBoList);

        List<JoinDirectBufferBo> joinDirectBufferBoList = new ArrayList<JoinDirectBufferBo>();
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
        joinDirectBufferBoList.add(joinDirectBufferBo1);
        joinDirectBufferBoList.add(joinDirectBufferBo2);
        joinDirectBufferBoList.add(joinDirectBufferBo3);
        joinDirectBufferBoList.add(joinDirectBufferBo4);
        joinDirectBufferBoList.add(joinDirectBufferBo5);
        joinAgentStatBo.setJoinDirectBufferBoList(joinDirectBufferBoList);

        List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo("test_app", joinAgentStatBo, 10000);
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


