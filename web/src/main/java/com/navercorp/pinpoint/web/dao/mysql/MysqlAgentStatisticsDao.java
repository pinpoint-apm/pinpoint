package com.navercorp.pinpoint.web.dao.mysql;

import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Repository
public class MysqlAgentStatisticsDao implements AgentStatisticsDao {

    private static final String NAMESPACE = MysqlAgentStatisticsDao.class.getPackage().getName() + "." + MysqlAgentStatisticsDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public boolean insertAgentCount(AgentCountStatistics agentCountStatistics) {
        int insert = sqlSessionTemplate.insert(NAMESPACE + "insertAgentCount", agentCountStatistics);
        return insert > 0;
    }

    @Override
    public List<AgentCountStatistics> selectAgentCount(Range range) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAgentCount", range);
    }

}
