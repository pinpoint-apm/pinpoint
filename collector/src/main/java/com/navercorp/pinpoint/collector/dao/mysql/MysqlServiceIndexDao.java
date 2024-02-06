/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.mysql;

import com.navercorp.pinpoint.collector.dao.ServiceIndexDao;
import com.navercorp.pinpoint.collector.dao.mysql.vo.AgentIndex;
import com.navercorp.pinpoint.collector.dao.mysql.vo.ApplicationHasAgent;
import com.navercorp.pinpoint.collector.dao.mysql.vo.ApplicationIndexDto;
import com.navercorp.pinpoint.collector.dao.mysql.vo.ServiceIdAndApplicationName;
import com.navercorp.pinpoint.collector.dao.mysql.vo.ServiceIndexDto;
import com.navercorp.pinpoint.collector.vo.ApplicationIndex;
import com.navercorp.pinpoint.collector.vo.ServiceHasApplication;
import com.navercorp.pinpoint.collector.vo.ServiceIndex;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Repository
@ConditionalOnProperty(name = "pinpoint.experimental.service-index", havingValue = "mysql")
public class MysqlServiceIndexDao implements ServiceIndexDao {

    private static final String NAMESPACE = ServiceIndexDao.class.getName() + '.';

    private final SqlSessionTemplate template;

    public MysqlServiceIndexDao(SqlSessionTemplate template) {
        this.template = Objects.requireNonNull(template, "sqlSessionTemplate");
    }

    @Override
    public List<ServiceIndex> selectAllServices() {
        return this.template.selectList(NAMESPACE + "selectAllServices");
    }

    @Override
    public List<ApplicationIndex> selectAllApplications() {
        return this.template.selectList(NAMESPACE + "selectAllApplications");
    }

    @Override
    public List<ServiceHasApplication> selectAllServiceHasApplications() {
        return this.template.selectList(NAMESPACE + "selectAllServiceHasApplications");
    }

    @Override
    public Long selectServiceIdByName(String serviceId, boolean writeLock) {
        return this.template.selectOne(applyWriteLock(NAMESPACE + "selectServiceIdByName", writeLock), serviceId);
    }

    @Override
    public List<Long> selectApplicationIdByServiceIdAndApplicationName(Long serviceId, String applicationName, boolean writeLock) {
        ServiceIdAndApplicationName parameter = new ServiceIdAndApplicationName(serviceId, applicationName);
        return this.template.selectList(applyWriteLock(NAMESPACE + "selectApplicationIdByServiceIdAndApplicationName", writeLock), parameter);
    }

    @Override
    public Long insertService(String serviceId) {
        ServiceIndexDto parameter = new ServiceIndexDto(null, serviceId);
        this.template.insert(NAMESPACE + "insertService", parameter);
        return parameter.getId();
    }

    @Override
    public Long insertApplication(String applicationName) {
        ApplicationIndexDto parameter = new ApplicationIndexDto(null, applicationName);
        this.template.insert(NAMESPACE + "insertApplication", parameter);
        return parameter.getId();
    }

    @Override
    public void insertAgent(UUID agentId, String agentName) {
        this.template.insert(NAMESPACE + "insertAgent", new AgentIndex(agentId, agentName));
    }

    @Override
    public void insertServiceHasApplication(Long serviceId, Long applicationId) {
        this.template.insert(NAMESPACE + "insertServiceHasApplication", new ServiceHasApplication(serviceId, applicationId));
    }

    @Override
    public void insertApplicationHasAgent(Long applicationId, UUID agentId) {
        this.template.insert(NAMESPACE + "insertApplicationHasAgent", new ApplicationHasAgent(applicationId, agentId));
    }

    private String applyWriteLock(String statement, boolean writeLock) {
        if (writeLock) {
            return statement + "ForUpdate";
        }
        return statement;
    }
}
