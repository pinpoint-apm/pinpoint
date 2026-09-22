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
package com.navercorp.pinpoint.alarm.batch.starter;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mapper location the DAO configuration resolves.
 *
 * <p>A pattern that matches nothing does not fail: the SqlSessionFactory is built with an
 * empty mapper array and every DAO call then dies on a missing statement, far from the
 * configuration that caused it. The configuration's javadoc says a test resolves the pattern;
 * this is that test.
 *
 * <p>It also pins the pattern to the one the web resolves. The two processes read the same
 * tables through the same mappers, so a mapper reachable from one and not the other is a
 * process that fails on a call its sibling serves.
 */
class AlarmBatchDaoConfigurationTest {

    /** One per dao the sweep and the outbox read through. */
    private static final List<String> MAPPERS_THE_JOB_NEEDS = List.of(
            "AlarmChannelBindingMapper.xml",
            "AlarmHistoryV2Mapper.xml",
            "AlarmNotificationChannelMapper.xml",
            "AlarmNotificationOutboxMapper.xml",
            "AlarmRuleLocalConfigMapper.xml",
            "AlarmRuleV2Mapper.xml",
            "AlarmStateMapper.xml",
            "AlarmTemplateItemMapper.xml",
            "AlarmTemplateMapper.xml");

    // The list below is satisfied by a narrower pattern too -- every mapper there happens to
    // start with Alarm -- so the pattern itself is pinned as well, or narrowing it back would
    // stay green until someone adds a mapper named otherwise.
    @Test
    void mapperPatternIsTheOneTheWebResolves() {
        assertEquals("classpath*:alarm/mapper/*Mapper.xml", AlarmBatchDaoConfiguration.MAPPER_LOCATION);
    }

    @Test
    void mapperPatternResolvesTheMappersTheJobNeeds() throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources(AlarmBatchDaoConfiguration.MAPPER_LOCATION);

        assertFalse(resources.length == 0,
                "no mapper matched " + AlarmBatchDaoConfiguration.MAPPER_LOCATION);

        List<String> names = Arrays.stream(resources).map(Resource::getFilename).sorted().toList();
        assertTrue(names.containsAll(MAPPERS_THE_JOB_NEEDS),
                "the pattern misses a mapper the job reads through, resolved " + names);
    }
}
