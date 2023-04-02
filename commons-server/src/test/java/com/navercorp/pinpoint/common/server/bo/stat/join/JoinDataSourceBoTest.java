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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinDataSourceBoTest {

    @Test
    public void joinDataSourceBoListTest() {

        List<JoinDataSourceBo> joinDataSourceBoList = List.of(
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 30, 25, "agent_id_1", 60, "agent_id_6"),
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 20, 5, "agent_id_2", 30, "agent_id_7"),
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 10, 25, "agent_id_3", 50, "agent_id_8"),
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 40, 4, "agent_id_4", 70, "agent_id_9"),
                new JoinDataSourceBo((short) 1000, "jdbc:mysql", 50, 25, "agent_id_5", 80, "agent_id_10")
        );

        JoinDataSourceBo joinDataSourceBo = JoinDataSourceBo.joinDataSourceBoList(joinDataSourceBoList);

        assertEquals(joinDataSourceBo.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo.getUrl(), "jdbc:mysql");
        assertEquals(new JoinIntFieldBo(30, 4, "agent_id_4", 80, "agent_id_10"), joinDataSourceBo.getActiveConnectionSizeJoinValue());
    }

    @Test
    public void joinDataSourceBoList2Test() {
        JoinDataSourceBo joinDataSourceBo = JoinDataSourceBo.joinDataSourceBoList(List.of());

        assertEquals(joinDataSourceBo, JoinDataSourceBo.EMPTY_JOIN_DATA_SOURCE_BO);
    }

}