/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.core.agentstat.dao.pinot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That the mapper is where the deployables look for it and still parses.
 *
 * <p>A mapper that has moved, or a statement whose name no longer matches the method that
 * calls it, fails at the first query rather than at startup -- and for an alarm that means an
 * evaluation that never worked and a rule that never fires. Both deployables resolve the
 * mapper by the pattern used here, so the pattern is the thing under test as much as the file.
 */
class AgentStatMapperTest {

    private static final String NAMESPACE = PinotAgentStatAlarmDao.class.getName() + ".";

    private static final List<String> STATEMENTS = List.of(
            "selectSumGroupByField",
            "selectAvgGroupByField",
            "selectSumCount",
            "selectAvg",
            "selectTagInfo",
            "selectTagInfoContainedSpecificTag");

    @Test
    void everyStatementTheDaoCallsIsDefinedWhereTheDeployablesLook() throws IOException {
        Resource[] mappers = new PathMatchingResourcePatternResolver()
                .getResources(PinotAgentStatAlarmDao.MAPPER_LOCATION);
        assertEquals(1, mappers.length,
                "expected the agent stat mapper at " + PinotAgentStatAlarmDao.MAPPER_LOCATION);

        Configuration config = new Configuration();
        new AgentStatRegistryHandler(new ObjectMapper()).registerHandlers(config);

        for (Resource mapper : mappers) {
            try (InputStream stream = mapper.getInputStream()) {
                new XMLMapperBuilder(stream, config, mapper.getDescription(),
                        config.getSqlFragments()).parse();
            }
        }

        for (String statement : STATEMENTS) {
            assertTrue(config.hasStatement(NAMESPACE + statement),
                    "the dao calls " + statement + " but the mapper does not define it");
        }
    }
}
