package com.nhn.pinpoint.web.alarm.collector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.dao.AgentStatDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

public class AgentStatDataCollector extends DataCollector {

    private final Application application;
    private final AgentStatDao agentStatDao;
    private final ApplicationIndexDao applicationIndexDao;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final AtomicBoolean init =new AtomicBoolean(false);// 동시에 checker들이 동작 되면 동시성 고려가 필요함
   
    private final Map<String, Long> agentHeapUsageRate = new HashMap<String, Long>();
    private final Map<String, Long> agentGcCount = new HashMap<String, Long>();
    private final Map<String, Long> agentJvmCpuUsageRate = new HashMap<String, Long>();
    
    public AgentStatDataCollector(DataCollectorCategory category, Application application, AgentStatDao agentStatDao, ApplicationIndexDao applicationIndexDao, long timeSlotEndTime, long slotInterval) {
        super(category);
        this.application = application;
        this.agentStatDao = agentStatDao;
        this.applicationIndexDao = applicationIndexDao;
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval; 
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }
        
        Range range = Range.createUncheckedRange(timeSlotEndTime - slotInterval, timeSlotEndTime);
        List<String> agentIds = applicationIndexDao.selectAgentIds(application.getName());
        
        for(String agentId : agentIds) {
            List<AgentStat> scanAgentStatList = agentStatDao.scanAgentStatList(agentId, range);
            int listSize = scanAgentStatList.size();
            long totalHeapSize = 0;
            long usedHeapSize = 0;
            long jvmCpuUsaged = 0;
            
            for (AgentStat agentStat : scanAgentStatList) {
                totalHeapSize += agentStat.getMemoryGc().getJvmMemoryHeapMax();
                usedHeapSize += agentStat.getMemoryGc().getJvmMemoryHeapUsed();
                
                jvmCpuUsaged += agentStat.getCpuLoad().getJvmCpuLoad();
            }
            
            long percent = 0;
            percent = calculatePercent(usedHeapSize, totalHeapSize);
            agentHeapUsageRate.put(agentId, percent);
            
            percent = calculatePercent(jvmCpuUsaged, 100*scanAgentStatList.size());
            agentJvmCpuUsageRate.put(agentId, percent);
            
            if(listSize > 0) {
                long accruedFirstGCcount = scanAgentStatList.get(0).getMemoryGc().getJvmGcOldCount();
                long accruedLastGCcount= scanAgentStatList.get(listSize - 1).getMemoryGc().getJvmGcOldCount();
                agentGcCount.put(agentId, accruedLastGCcount - accruedFirstGCcount);    
            }
            
        }

        init.set(true);
        
    }

    private long calculatePercent(long used, long total) {
        if (total == 0 || used == 0) {
            return 0;
        } else {
            return Math.round((used * 100) / total);
        }
    }

    public Map<String, Long> getHeapUsageRate() {
        return agentHeapUsageRate;
    }

    public Map<String, Long> getGCCount() {
        return agentGcCount;
    }

    public Map<String, Long> getJvmCpuUsageRate() {
        return agentJvmCpuUsageRate;
    }
    
}
