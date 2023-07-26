package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDaoProxy;
import com.navercorp.pinpoint.web.dao.ApplicationIndexPerTimeDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class HbaseApplicationIndexDaoProxy implements ApplicationIndexDaoProxy {

    private ApplicationIndexDao applicationIndexDao;
    private ApplicationIndexPerTimeDao applicationIndexPerTimeDao;
    private static final Range WHOLE_RANGE = Range.between(0, Long.MAX_VALUE);

    @Value("${web.enableApplicationIndexPerTimeTable:false}")
    private boolean useIndexPerTime;

    public HbaseApplicationIndexDaoProxy(ApplicationIndexDao applicationIndexDao,
                                         ApplicationIndexPerTimeDao applicationIndexPerTimeDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationIndexPerTimeDao = Objects.requireNonNull(applicationIndexPerTimeDao, "applicationIndexPerTimeDao");
    }

    @Override
    public List<Application> selectAllApplicationNames() {
        return applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    public List<Application> selectApplicationName(String applicationName) {
        return applicationIndexDao.selectApplicationName(applicationName);
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        return selectAgentIds(applicationName, WHOLE_RANGE);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, Range range) {
        if (useIndexPerTime && !range.equals(WHOLE_RANGE)) {
            List<String> agentIds = applicationIndexPerTimeDao.selectAgentIds(applicationName, range);
            if (agentIds.isEmpty()) {
                // FIXME: need to clarify the case when agentIds is empty
                return applicationIndexDao.selectAgentIds(applicationName);
            }
            return agentIds;
        } else {
            return applicationIndexDao.selectAgentIds(applicationName);
        }
    }


    @Override
    public void deleteApplicationName(String applicationName) {
        applicationIndexDao.deleteApplicationName(applicationName);
    }

    @Override
    public void deleteAgentIds(Map<String, List<String>> applicationAgentIdMap) {
        applicationIndexDao.deleteAgentIds(applicationAgentIdMap);
    }

    @Override
    public void deleteAgentId(String applicationName, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
    }

    public boolean usesIndexPerTime() {
        return useIndexPerTime;
    }

    public void setUseIndexPerTime(boolean useIndexPerTime) {
        this.useIndexPerTime = useIndexPerTime;
    }
}
