package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;

import java.util.List;
import java.util.Objects;

public class DefaultLegacyAgentCompatibility implements LegacyAgentCompatibility {


    // legacy node agent support
    private final LegacyAgent[] legacyAgents = {
            new LegacyAgent(List.of((short)1400, (short)1401), "0.8.0"),
    };

    private final AgentStatDao<JvmGcBo> jvmGcDao;

    public DefaultLegacyAgentCompatibility(AgentStatDao<JvmGcBo> jvmGcDao) {
        this.jvmGcDao = Objects.requireNonNull(jvmGcDao, "jvmGcDao");
    }

    @Override
    public boolean isLegacyAgent(short serviceType) {
        return isLegacyAgent(serviceType, null);
    }

    @Override
    public boolean isLegacyAgent(short serviceType, String version) {
        for (LegacyAgent legacyAgent : legacyAgents) {
            if (!legacyAgent.isLegacyType(serviceType)) {
                return false;
            }
            if (!legacyAgent.isLegacyVersion(version)) {
                return false;
            }
        }
        return true;
    }

//    @Override
//    public boolean isActiveAgent(Application agent, String version, Range range) {
//        if (!isLegacyAgent(agent.getServiceTypeCode(), version)) {
//            return false;
//        }
//        if (isActiveAgent(agent.getName(), range)) {
//            return true;
//        }
//        return false;
//    }

    @Override
    public boolean isActiveAgent(String agentId, Range range) {
        return this.jvmGcDao.agentStatExists(agentId, range);
    }

}
