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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinMemorySamplerTest {
    public static final String ID = "test_app";

    @Test
    public void sampleDataPointsTest() throws Exception {
        long currentTime = 1487149800000L;
        List<JoinMemoryBo> joinMemoryBoList = createJoinMemoryBoList(currentTime);
        AggreJoinMemoryBo aggreJoinMemoryBo = new JoinMemorySampler().sampleDataPoints(1, currentTime, joinMemoryBoList, JoinMemoryBo.EMPTY_JOIN_MEMORY_BO);
        assertEquals(aggreJoinMemoryBo.getId(), ID);
        assertEquals(aggreJoinMemoryBo.getTimestamp(), currentTime);
        assertEquals(aggreJoinMemoryBo.getHeapUsedJoinValue(), new JoinLongFieldBo(3000L, 100L, "app_4_1", 8000L, "app_3_2"));
        assertEquals(aggreJoinMemoryBo.getNonHeapUsedJoinValue(), new JoinLongFieldBo(300L, 50L, "app_1_3", 2900L, "app_5_4"));
    }

    private List<JoinMemoryBo> createJoinMemoryBoList(long currentTime) {
        return List.of(
                new JoinMemoryBo(ID, currentTime + 5000, 3000, 2000, 5000, "app_1_1", "app_1_2", 500, 50, 600, "app_1_3", "app_1_4"),
                new JoinMemoryBo(ID, currentTime + 10000, 4000, 1000, 7000, "app_2_1", "app_2_2", 400, 150, 600, "app_2_3", "app_2_4"),
                new JoinMemoryBo(ID, currentTime + 15000, 5000, 3000, 8000, "app_3_1", "app_3_2", 200, 100, 200, "app_3_3", "app_3_4"),
                new JoinMemoryBo(ID, currentTime + 20000, 1000, 100, 3000, "app_4_1", "app_4_2", 100, 900, 1000, "app_4_3", "app_4_4"),
                new JoinMemoryBo(ID, currentTime + 25000, 2000, 1000, 6000, "app_5_1", "app_5_2", 300, 100, 2900, "app_5_3", "app_5_4")
        );
    }

}