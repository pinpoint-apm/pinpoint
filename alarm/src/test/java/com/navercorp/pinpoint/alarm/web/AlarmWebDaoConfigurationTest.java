package com.navercorp.pinpoint.alarm.web;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The mapper location the DAO configuration resolves.
 *
 * <p>A pattern that matches nothing does not fail: the SqlSessionFactory is built with an
 * empty mapper array and every DAO call then dies on a missing statement, far from the
 * configuration that caused it. So the pattern is checked against the packaged resources
 * rather than left to a deployment to discover.
 */
class AlarmWebDaoConfigurationTest {

    @Test
    void mapperPatternResolvesEveryPackagedMapper() throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(AlarmWebDaoConfiguration.MAPPER_LOCATION);

        assertFalse(resources.length == 0, "no mapper matched " + AlarmWebDaoConfiguration.MAPPER_LOCATION);

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
