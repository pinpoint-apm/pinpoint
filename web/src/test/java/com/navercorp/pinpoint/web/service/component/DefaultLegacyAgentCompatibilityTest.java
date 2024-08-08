package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semver4j.Semver;

@ExtendWith(MockitoExtension.class)
class DefaultLegacyAgentCompatibilityTest {
    @Mock
    AgentStatDao<JvmGcBo> jvmGcDao;

    @Test
    void semver() {
        Semver version = Semver.parse("0.7.0");
        Assertions.assertTrue(version.isLowerThan("0.8.0"));
    }

    private static final short node = 1400;
    private static final short java = 1010;

    @Test
    void legacy_serviceType() {
        LegacyAgentCompatibility compatibility = new DefaultLegacyAgentCompatibility(jvmGcDao);

        Assertions.assertTrue(compatibility.isLegacyAgent(node));

        Assertions.assertFalse(compatibility.isLegacyAgent(java));
    }

    @Test
    void legacy_semver() {

        LegacyAgentCompatibility compatibility = new DefaultLegacyAgentCompatibility(jvmGcDao);

        Assertions.assertTrue(compatibility.isLegacyAgent(node, "0.7.0"));
        Assertions.assertTrue(compatibility.isLegacyAgent(node, "0.7.1"));

        Assertions.assertTrue(compatibility.isLegacyAgent(node, "0.6.1"));

        Assertions.assertFalse(compatibility.isLegacyAgent(node, "0.8.0"));
        Assertions.assertFalse(compatibility.isLegacyAgent(node, "1.0.0"));

        Assertions.assertFalse(compatibility.isLegacyAgent(java, "0.7.0"));
    }
}