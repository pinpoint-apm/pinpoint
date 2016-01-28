/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.alarm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;

public class AlarmPartitionerTest {

    private static ApplicationIndexDao dao;
    
    @Test
    public void partitionTest() {
        AlarmPartitioner partitioner = new AlarmPartitioner(dao);
        Map<String, ExecutionContext> partitions = partitioner.partition(0);
        Assert.assertEquals(8, partitions.size());
    }
    
    @BeforeClass
    public static void beforeClass() {
        dao = new ApplicationIndexDao() {
            
            @Override
            public List<Application> selectAllApplicationNames() {
                List<Application> apps = new LinkedList<Application>();
                
                for(int i = 0; i <= 37; i++) {
                    apps.add(new Application("app" + i, ServiceType.STAND_ALONE));
                }
                
                return apps;
            }

            @Override
            public List<String> selectAgentIds(String applicationName) {
                return null;
            }
            
            @Override
            public void deleteApplicationName(String applicationName) {
            }

            @Override
            public void deleteAgentIds(Map<String, List<String>> applicationAgentIdMap) {
            }

            @Override
            public void deleteAgentId(String applicationName, String agentId) {
            }
        };
    }

}
