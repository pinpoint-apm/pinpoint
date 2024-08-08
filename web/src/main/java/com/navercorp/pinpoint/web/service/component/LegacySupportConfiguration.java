package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LegacySupportConfiguration {
    @Bean
    public LegacyAgentCompatibility legacyAgentCompatibility(AgentStatDao<JvmGcBo> jvmGcDao,
                                                             @Value("${pinpoint.web.agent-status.legacy-agent-support:true}")
                                                             boolean legacyAgentSupport) {
        if (legacyAgentSupport) {
            return new DefaultLegacyAgentCompatibility(jvmGcDao);
        }
        return new DisableAgentCompatibility();
    }
}
