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

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.AlarmPartitioner;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import java.util.Map;
import java.util.Optional;

public class AlarmPartitionerTest {

    private static ApplicationIndexDao dao;
    
    @Test
    public void partitionTest() {
        AlarmPartitioner partitioner = new AlarmPartitioner(Optional.empty());
        Map<String, ExecutionContext> partitions = partitioner.partition(0);
        Assert.assertEquals(1, partitions.size());
    }
}
