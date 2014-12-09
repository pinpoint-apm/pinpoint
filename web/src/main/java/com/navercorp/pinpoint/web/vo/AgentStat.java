package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.navercorp.pinpoint.common.bo.AgentStatMemoryGcBo;

/**
 * @author hyungil.jeong
 */
public class AgentStat {

    private AgentStatMemoryGcBo memoryGc;
    private AgentStatCpuLoadBo cpuLoad;

    public AgentStatMemoryGcBo getMemoryGc() {
        return memoryGc;
    }

    public void setMemoryGc(AgentStatMemoryGcBo memoryGc) {
        this.memoryGc = memoryGc;
    }

    public AgentStatCpuLoadBo getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(AgentStatCpuLoadBo cpuLoad) {
        this.cpuLoad = cpuLoad;
    }
}
