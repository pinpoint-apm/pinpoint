/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.collector;

import com.navercorp.pinpoint.batch.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class FileDescriptorDataCollectorTest {

    @Test
    public void collect() {
        String agentId1 = "testAgent1";
        String agentId2 = "testAgent2";

        List<String> agentList = new ArrayList<>();
        agentList.add(agentId1);
        agentList.add(agentId2);

        AgentStatDao<FileDescriptorBo> fileDescriptorDao = (AgentStatDao<FileDescriptorBo>) mock(AgentStatDao.class);
        long timeStamp = 1558936971494L;
        Range range = Range.newUncheckedRange(timeStamp - DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN, timeStamp);

        List<FileDescriptorBo> fileDescriptorBoList1 = new ArrayList<>();
        FileDescriptorBo fileDescriptorBo1_1 = new FileDescriptorBo();
        fileDescriptorBo1_1.setOpenFileDescriptorCount(200);
        FileDescriptorBo fileDescriptorBo1_2 = new FileDescriptorBo();
        fileDescriptorBo1_2.setOpenFileDescriptorCount(300);
        FileDescriptorBo fileDescriptorBo1_3 = new FileDescriptorBo();
        fileDescriptorBo1_3.setOpenFileDescriptorCount(400);
        fileDescriptorBoList1.add(fileDescriptorBo1_1);
        fileDescriptorBoList1.add(fileDescriptorBo1_2);
        fileDescriptorBoList1.add(fileDescriptorBo1_3);
        when(fileDescriptorDao.getAgentStatList(agentId1, range)).thenReturn(fileDescriptorBoList1);

        List<FileDescriptorBo> fileDescriptorBoList2 = new ArrayList<>();
        FileDescriptorBo fileDescriptorBo2_1 = new FileDescriptorBo();
        fileDescriptorBo2_1.setOpenFileDescriptorCount(250);
        FileDescriptorBo fileDescriptorBo2_2 = new FileDescriptorBo();
        fileDescriptorBo2_2.setOpenFileDescriptorCount(350);
        FileDescriptorBo fileDescriptorBo2_3 = new FileDescriptorBo();
        fileDescriptorBo2_3.setOpenFileDescriptorCount(450);
        fileDescriptorBoList2.add(fileDescriptorBo2_1);
        fileDescriptorBoList2.add(fileDescriptorBo2_2);
        fileDescriptorBoList2.add(fileDescriptorBo2_3);
        when(fileDescriptorDao.getAgentStatList(agentId2, range)).thenReturn(fileDescriptorBoList2);

        FileDescriptorDataCollector fileDescriptorDataCollector = new FileDescriptorDataCollector(
                DataCollectorCategory.FILE_DESCRIPTOR,
                fileDescriptorDao,
                agentList,
                timeStamp,
                DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN
        );

        fileDescriptorDataCollector.collect();
        Map<String, Long> fileDescriptorCount = fileDescriptorDataCollector.getFileDescriptorCount();
        assertEquals(fileDescriptorCount.size(), 2);
        assertEquals(fileDescriptorCount.get(agentId1), Long.valueOf(300L));
        assertEquals(fileDescriptorCount.get(agentId2), Long.valueOf(350L));
    }
}