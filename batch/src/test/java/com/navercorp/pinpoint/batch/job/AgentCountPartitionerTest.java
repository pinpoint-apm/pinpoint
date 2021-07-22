/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.batch.job.AgentCountPartitioner;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class AgentCountPartitionerTest {

    @Test
    public void partition() throws Exception {
        AgentCountPartitioner partitioner = new AgentCountPartitioner();
        Map<String, ExecutionContext> partition = partitioner.partition(0);
        assertEquals(1, partition.size());
    }

}