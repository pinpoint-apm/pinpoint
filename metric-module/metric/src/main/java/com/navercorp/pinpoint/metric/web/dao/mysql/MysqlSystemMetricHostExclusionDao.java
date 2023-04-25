package com.navercorp.pinpoint.metric.web.dao.mysql;

import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostExclusionDao;
import com.navercorp.pinpoint.metric.web.dao.model.HostExclusionSearchKey;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Primary
@Repository
public class MysqlSystemMetricHostExclusionDao implements SystemMetricHostExclusionDao {

    private static final String NAMESPACE = MysqlSystemMetricHostExclusionDao.class.getName() + ".";

    private final SqlSessionTemplate sqlMetricSessionTemplate;

    public MysqlSystemMetricHostExclusionDao(@Qualifier("metricSqlSessionTemplate") SqlSessionTemplate sqlMetricSessionTemplate) {
        this.sqlMetricSessionTemplate = Objects.requireNonNull(sqlMetricSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public List<String> selectExcludedHostGroupNameList() {
        return sqlMetricSessionTemplate.selectList(NAMESPACE + "selectExcludedHostGroupNames");
    }

    @Override
    public void insertHostGroupExclusion(String hostGroupName) {
        sqlMetricSessionTemplate.insert(NAMESPACE + "insertHostGroupExclusion", hostGroupName);
    }

    @Override
    public void deleteHostGroupExclusion(String hostGroupName) {
        sqlMetricSessionTemplate.delete(NAMESPACE + "deleteHostGroupExclusion", hostGroupName);
    }

    @Override
    public List<String> selectExcludedHostNameList(String hostGroupName) {
        return sqlMetricSessionTemplate.selectList(NAMESPACE + "selectExcludedHostNames", hostGroupName);
    }

    @Override
    public void insertHostExclusion(String hostGroupName, String hostName) {
        sqlMetricSessionTemplate.insert(NAMESPACE + "insertHostExclusion", new HostExclusionSearchKey(hostGroupName, hostName));
    }

    @Override
    public void deleteHostExclusion(String hostGroupName, String hostName) {
        sqlMetricSessionTemplate.delete(NAMESPACE + "deleteHostExclusion", new HostExclusionSearchKey(hostGroupName, hostName));
    }

    @Override
    public void deleteHostExclusions(String hostGroupName) {
        sqlMetricSessionTemplate.delete(NAMESPACE + "deleteHostExclusions", hostGroupName);
    }

    @Override
    public List<String> selectGroupNameListFromHostExclusion() {
        return sqlMetricSessionTemplate.selectList(NAMESPACE + "selectGroupNamesFromHostExclusion");
    }
}
