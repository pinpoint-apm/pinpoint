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
        for(JoinCpuLoadBo joinCpuLoadBo : joinCpuLoadBoList) {
            System.out.println(new Date(joinCpuLoadBo.getTimestamp()) + " : " + joinCpuLoadBo);
        }
    }

    private class ComparatorImpl implements Comparator<JoinCpuLoadBo> {
        @Override
        public int compare(JoinCpuLoadBo bo1, JoinCpuLoadBo bo2) {
            return bo1.getTimestamp() < bo2.getTimestamp() ? -1 : 1;
        }
    }

    private JoinApplicationStatBo createJoinApplicationStatBo(final String id, final long timestamp, final int plus) {
        final JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
        joinApplicationStatBo.setId(id);
        joinApplicationStatBo.setJoinCpuLoadBoList(createJoinCpuLoadBoList(id, timestamp, plus));
        joinApplicationStatBo.setTimestamp(timestamp);
        joinApplicationStatBo.setStatType(StatType.APP_CPU_LOAD);
        return joinApplicationStatBo;
    }

    private List<JoinCpuLoadBo> createJoinCpuLoadBoList(final String id, final long currentTime, int plus) {
        final List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo(id, 50 + plus, 97 + plus, 27 + plus, 80 + plus, 97 + plus, 46 + plus, currentTime);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo(id, 40 + plus, 87 + plus, 40 + plus, 70 + plus, 97 + plus, 40 + plus, currentTime + 5000);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo(id, 30 + plus, 77 + plus, 27 + plus, 60 + plus, 77 + plus, 27 + plus, currentTime + 10000);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo(id, 20 + plus, 67 + plus, 17 + plus, 40 + plus, 99 + plus, 18 + plus, currentTime + 15000);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo(id, 10 + plus, 99 + plus, 7 + plus, 30 + plus, 59 + plus, 8 + plus, currentTime + 20000);
        joinCpuLoadBoList.add(joinCpuLoadBo1);
        joinCpuLoadBoList.add(joinCpuLoadBo2);
        joinCpuLoadBoList.add(joinCpuLoadBo3);
        joinCpuLoadBoList.add(joinCpuLoadBo4);
        joinCpuLoadBoList.add(joinCpuLoadBo5);
        for(JoinCpuLoadBo joinCpuLoadBo : joinCpuLoadBoList) {
            System.out.println(new Date(joinCpuLoadBo.getTimestamp()) + " : " + joinCpuLoadBo);
        }
        return joinCpuLoadBoList;
    }
}

//--------------------------------------------------
//    Wed Feb 15 18:10:00 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=51.0, maxJvmCpuLoad=98.0, minJvmCpuLoad=28.0, systemCpuLoad=81.0, maxSystemCpuLoad=98.0, minSystemCpuLoad=47.0, timestamp=1487149800000}
//    Wed Feb 15 18:10:01 KST 2017 : JoinCpuLoadBo{version=1, id='id2', jvmCpuLoad=46.0, maxJvmCpuLoad=93.0, minJvmCpuLoad=23.0, systemCpuLoad=76.0, maxSystemCpuLoad=93.0, minSystemCpuLoad=42.0, timestamp=1487149801000}
//    Wed Feb 15 18:10:02 KST 2017 : JoinCpuLoadBo{version=1, id='id3', jvmCpuLoad=47.0, maxJvmCpuLoad=94.0, minJvmCpuLoad=24.0, systemCpuLoad=77.0, maxSystemCpuLoad=94.0, minSystemCpuLoad=43.0, timestamp=1487149802000}
//    Wed Feb 15 18:10:03 KST 2017 : JoinCpuLoadBo{version=1, id='id4', jvmCpuLoad=54.0, maxJvmCpuLoad=101.0, minJvmCpuLoad=31.0, systemCpuLoad=84.0, maxSystemCpuLoad=101.0, minSystemCpuLoad=50.0, timestamp=1487149803000}
//    Wed Feb 15 18:10:04 KST 2017 : JoinCpuLoadBo{version=1, id='id5', jvmCpuLoad=45.0, maxJvmCpuLoad=92.0, minJvmCpuLoad=22.0, systemCpuLoad=75.0, maxSystemCpuLoad=92.0, minSystemCpuLoad=41.0, timestamp=1487149804000}
//
//    Wed Feb 15 18:10:00 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=48.6, maxJvmCpuLoad=101.0, minJvmCpuLoad=22.0, systemCpuLoad=78.6, maxSystemCpuLoad=101.0, minSystemCpuLoad=41.0, timestamp=1487149800000}
//    --------------------------------------------------
//    Wed Feb 15 18:10:05 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=41.0, maxJvmCpuLoad=88.0, minJvmCpuLoad=41.0, systemCpuLoad=71.0, maxSystemCpuLoad=98.0, minSystemCpuLoad=41.0, timestamp=1487149805000}
//    Wed Feb 15 18:10:06 KST 2017 : JoinCpuLoadBo{version=1, id='id2', jvmCpuLoad=36.0, maxJvmCpuLoad=83.0, minJvmCpuLoad=36.0, systemCpuLoad=66.0, maxSystemCpuLoad=93.0, minSystemCpuLoad=36.0, timestamp=1487149806000}
//    Wed Feb 15 18:10:07 KST 2017 : JoinCpuLoadBo{version=1, id='id3', jvmCpuLoad=37.0, maxJvmCpuLoad=84.0, minJvmCpuLoad=37.0, systemCpuLoad=67.0, maxSystemCpuLoad=94.0, minSystemCpuLoad=37.0, timestamp=1487149807000}
//    Wed Feb 15 18:10:08 KST 2017 : JoinCpuLoadBo{version=1, id='id4', jvmCpuLoad=44.0, maxJvmCpuLoad=91.0, minJvmCpuLoad=44.0, systemCpuLoad=74.0, maxSystemCpuLoad=101.0, minSystemCpuLoad=44.0, timestamp=1487149808000}
//    Wed Feb 15 18:10:09 KST 2017 : JoinCpuLoadBo{version=1, id='id5', jvmCpuLoad=35.0, maxJvmCpuLoad=82.0, minJvmCpuLoad=35.0, systemCpuLoad=65.0, maxSystemCpuLoad=92.0, minSystemCpuLoad=35.0, timestamp=1487149809000}
//
//    Wed Feb 15 18:10:05 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=38.6, maxJvmCpuLoad=91.0, minJvmCpuLoad=35.0, systemCpuLoad=68.6, maxSystemCpuLoad=101.0, minSystemCpuLoad=35.0, timestamp=1487149805000}
//    --------------------------------------------------
//    Wed Feb 15 18:10:10 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=31.0, maxJvmCpuLoad=78.0, minJvmCpuLoad=28.0, systemCpuLoad=61.0, maxSystemCpuLoad=78.0, minSystemCpuLoad=28.0, timestamp=1487149810000}
//    Wed Feb 15 18:10:11 KST 2017 : JoinCpuLoadBo{version=1, id='id2', jvmCpuLoad=26.0, maxJvmCpuLoad=73.0, minJvmCpuLoad=23.0, systemCpuLoad=56.0, maxSystemCpuLoad=73.0, minSystemCpuLoad=23.0, timestamp=1487149811000}
//    Wed Feb 15 18:10:12 KST 2017 : JoinCpuLoadBo{version=1, id='id3', jvmCpuLoad=27.0, maxJvmCpuLoad=74.0, minJvmCpuLoad=24.0, systemCpuLoad=57.0, maxSystemCpuLoad=74.0, minSystemCpuLoad=24.0, timestamp=1487149812000}
//    Wed Feb 15 18:10:13 KST 2017 : JoinCpuLoadBo{version=1, id='id4', jvmCpuLoad=34.0, maxJvmCpuLoad=81.0, minJvmCpuLoad=31.0, systemCpuLoad=64.0, maxSystemCpuLoad=81.0, minSystemCpuLoad=31.0, timestamp=1487149813000}
//    Wed Feb 15 18:10:14 KST 2017 : JoinCpuLoadBo{version=1, id='id5', jvmCpuLoad=25.0, maxJvmCpuLoad=72.0, minJvmCpuLoad=22.0, systemCpuLoad=55.0, maxSystemCpuLoad=72.0, minSystemCpuLoad=22.0, timestamp=1487149814000}
//
//    Wed Feb 15 18:10:10 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=28.6, maxJvmCpuLoad=81.0, minJvmCpuLoad=22.0, systemCpuLoad=58.6, maxSystemCpuLoad=81.0, minSystemCpuLoad=22.0, timestamp=1487149810000}
//    --------------------------------------------------
//    Wed Feb 15 18:10:15 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=21.0, maxJvmCpuLoad=68.0, minJvmCpuLoad=18.0, systemCpuLoad=41.0, maxSystemCpuLoad=100.0, minSystemCpuLoad=19.0, timestamp=1487149815000}
//    Wed Feb 15 18:10:16 KST 2017 : JoinCpuLoadBo{version=1, id='id2', jvmCpuLoad=16.0, maxJvmCpuLoad=63.0, minJvmCpuLoad=13.0, systemCpuLoad=36.0, maxSystemCpuLoad=95.0, minSystemCpuLoad=14.0, timestamp=1487149816000}
//    Wed Feb 15 18:10:17 KST 2017 : JoinCpuLoadBo{version=1, id='id3', jvmCpuLoad=17.0, maxJvmCpuLoad=64.0, minJvmCpuLoad=14.0, systemCpuLoad=37.0, maxSystemCpuLoad=96.0, minSystemCpuLoad=15.0, timestamp=1487149817000}
//    Wed Feb 15 18:10:18 KST 2017 : JoinCpuLoadBo{version=1, id='id4', jvmCpuLoad=24.0, maxJvmCpuLoad=71.0, minJvmCpuLoad=21.0, systemCpuLoad=44.0, maxSystemCpuLoad=103.0, minSystemCpuLoad=22.0, timestamp=1487149818000}
//    Wed Feb 15 18:10:19 KST 2017 : JoinCpuLoadBo{version=1, id='id5', jvmCpuLoad=15.0, maxJvmCpuLoad=62.0, minJvmCpuLoad=12.0, systemCpuLoad=35.0, maxSystemCpuLoad=94.0, minSystemCpuLoad=13.0, timestamp=1487149819000}
//
//    Wed Feb 15 18:10:15 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=18.6, maxJvmCpuLoad=71.0, minJvmCpuLoad=12.0, systemCpuLoad=38.6, maxSystemCpuLoad=103.0, minSystemCpuLoad=13.0, timestamp=1487149815000}
//    --------------------------------------------------
//    Wed Feb 15 18:10:20 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=11.0, maxJvmCpuLoad=100.0, minJvmCpuLoad=8.0, systemCpuLoad=31.0, maxSystemCpuLoad=60.0, minSystemCpuLoad=9.0, timestamp=1487149820000}
//    Wed Feb 15 18:10:21 KST 2017 : JoinCpuLoadBo{version=1, id='id2', jvmCpuLoad=6.0, maxJvmCpuLoad=95.0, minJvmCpuLoad=3.0, systemCpuLoad=26.0, maxSystemCpuLoad=55.0, minSystemCpuLoad=4.0, timestamp=1487149821000}
//    Wed Feb 15 18:10:22 KST 2017 : JoinCpuLoadBo{version=1, id='id3', jvmCpuLoad=7.0, maxJvmCpuLoad=96.0, minJvmCpuLoad=4.0, systemCpuLoad=27.0, maxSystemCpuLoad=56.0, minSystemCpuLoad=5.0, timestamp=1487149822000}
//    Wed Feb 15 18:10:23 KST 2017 : JoinCpuLoadBo{version=1, id='id4', jvmCpuLoad=14.0, maxJvmCpuLoad=103.0, minJvmCpuLoad=11.0, systemCpuLoad=34.0, maxSystemCpuLoad=63.0, minSystemCpuLoad=12.0, timestamp=1487149823000}
//    Wed Feb 15 18:10:24 KST 2017 : JoinCpuLoadBo{version=1, id='id5', jvmCpuLoad=5.0, maxJvmCpuLoad=94.0, minJvmCpuLoad=2.0, systemCpuLoad=25.0, maxSystemCpuLoad=54.0, minSystemCpuLoad=3.0, timestamp=1487149824000}
//
//    Wed Feb 15 18:10:20 KST 2017 : JoinCpuLoadBo{version=1, id='id1', jvmCpuLoad=8.6, maxJvmCpuLoad=103.0, minJvmCpuLoad=2.0, systemCpuLoad=28.6, maxSystemCpuLoad=63.0, minSystemCpuLoad=3.0, timestamp=1487149820000}
