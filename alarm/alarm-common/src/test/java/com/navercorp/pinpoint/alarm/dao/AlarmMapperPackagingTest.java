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
package com.navercorp.pinpoint.alarm.dao;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What this module packages for the deployables that read the alarm tables.
 *
 * <p>The mappers live here and the configurations that load them live in the modules that
 * serve or evaluate, so nothing on either side notices a mapper that stopped being packaged
 * -- an unmatched pattern builds an empty SqlSessionFactory rather than failing, and the
 * first query dies far from the cause. The complete set is asserted here, where it is owned.
 */
class AlarmMapperPackagingTest {

    private static final String MAPPER_LOCATION = "classpath*:alarm/mapper/*Mapper.xml";

    @Test
    void everyMapperThisModuleOwnsIsPackaged() throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources(MAPPER_LOCATION);

        List<String> names = Arrays.stream(resources).map(Resource::getFilename).sorted().toList();

        assertEquals(List.of(
                "AlarmChannelBindingMapper.xml",
                "AlarmHistoryV2Mapper.xml",
                "AlarmNotificationChannelMapper.xml",
                "AlarmNotificationOutboxMapper.xml",
                "AlarmRuleLocalConfigMapper.xml",
                "AlarmRuleV2Mapper.xml",
                "AlarmStateMapper.xml",
                "AlarmTemplateItemMapper.xml",
                "AlarmTemplateMapper.xml"
        ), names);
    }
}
