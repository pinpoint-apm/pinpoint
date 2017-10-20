/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase.stat;

import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.web.dao.hbase.stat.compatibility.HbaseAgentStatDualReadDao;
import com.navercorp.pinpoint.web.dao.stat.ActiveTraceDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.dao.stat.CpuLoadDao;
import com.navercorp.pinpoint.web.dao.stat.DataSourceDao;
import com.navercorp.pinpoint.web.dao.stat.DeadlockDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDetailedDao;
import com.navercorp.pinpoint.web.dao.stat.ResponseTimeDao;
import com.navercorp.pinpoint.web.dao.stat.TransactionDao;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * @author HyunGil Jeong
 */
abstract class AgentStatDaoFactory<T extends AgentStatDataPoint, D extends AgentStatDao<T>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected D v1;
    protected D v2;

    @Autowired
    private HBaseAdminTemplate adminTemplate;

    @Value("#{pinpointWebProps['web.stat.format.compatibility.version'] ?: 'v2'}")
    private String mode = "v2";

    D getDao() throws Exception {
        logger.info("AgentStatDao Compatibility {}", mode);

        final TableName v1TableName = HBaseTables.AGENT_STAT;
        final TableName v2TableName = HBaseTables.AGENT_STAT_VER2;

        if (mode.equalsIgnoreCase("v2")) {
            if (this.adminTemplate.tableExists(v2TableName)) {
                return v2;
            } else {
                logger.error("AgentStatDao configured for v2, but {} table does not exist", v2TableName);
                throw new IllegalStateException(v2TableName + " table does not exist");
            }
        } else if (mode.equalsIgnoreCase("compatibilityMode")) {
            boolean v1TableExists = this.adminTemplate.tableExists(v1TableName);
            boolean v2TableExists = this.adminTemplate.tableExists(v2TableName);
            if (v1TableExists && v2TableExists) {
                return getCompatibilityDao(this.v1, this.v2);
            } else {
                logger.error("AgentStatDao configured for compatibilityMode, but {} and {} tables do not exist", v1TableName, v2TableName);
                throw new IllegalStateException(v1TableName + ", " + v2TableName + " tables do not exist");
            }
        } else {
            throw new IllegalStateException("Unknown AgentStatDao configuration : " + mode);
        }
    }

    abstract D getCompatibilityDao(D v1, D v2);

    @Repository("jvmGcDaoFactory")
    public static class JvmGcDaoFactory extends AgentStatDaoFactory<JvmGcBo, JvmGcDao> implements FactoryBean<JvmGcDao> {

        @Autowired
        public void setV1(@Qualifier("jvmGcDaoV1") JvmGcDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("jvmGcDaoV2") JvmGcDao v2) {
            this.v2 = v2;
        }

        @Override
        public JvmGcDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return JvmGcDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        JvmGcDao getCompatibilityDao(JvmGcDao v1, JvmGcDao v2) {
            return new HbaseAgentStatDualReadDao.JvmGcDualReadDao(v2, v1);
        }
    }

    @Repository("jvmGcDetailedDaoFactory")
    public static class JvmGcDetailedDaoFactory extends AgentStatDaoFactory<JvmGcDetailedBo, JvmGcDetailedDao> implements FactoryBean<JvmGcDetailedDao> {

        @Autowired
        public void setV1(@Qualifier("jvmGcDetailedDaoV1") JvmGcDetailedDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("jvmGcDetailedDaoV2") JvmGcDetailedDao v2) {
            this.v2 = v2;
        }

        @Override
        public JvmGcDetailedDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return JvmGcDetailedDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        JvmGcDetailedDao getCompatibilityDao(JvmGcDetailedDao v1, JvmGcDetailedDao v2) {
            return new HbaseAgentStatDualReadDao.JvmGcDetailedDualReadDao(v2, v1);
        }
    }

    @Repository("cpuLoadDaoFactory")
    public static class CpuLoadDaoFactory extends AgentStatDaoFactory<CpuLoadBo, CpuLoadDao> implements FactoryBean<CpuLoadDao> {

        @Autowired
        public void setV1(@Qualifier("cpuLoadDaoV1") CpuLoadDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("cpuLoadDaoV2") CpuLoadDao v2) {
            this.v2 = v2;
        }

        @Override
        public CpuLoadDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return CpuLoadDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        CpuLoadDao getCompatibilityDao(CpuLoadDao v1, CpuLoadDao v2) {
            return new HbaseAgentStatDualReadDao.CpuLoadDualReadDao(v2, v1);
        }
    }

    @Repository("transactionDaoFactory")
    public static class TransactionDaoFactory extends AgentStatDaoFactory<TransactionBo, TransactionDao> implements FactoryBean<TransactionDao> {

        @Autowired
        public void setV1(@Qualifier("transactionDaoV1") TransactionDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("transactionDaoV2") TransactionDao v2) {
            this.v2 = v2;
        }

        @Override
        public TransactionDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return TransactionDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        TransactionDao getCompatibilityDao(TransactionDao v1, TransactionDao v2) {
            return new HbaseAgentStatDualReadDao.TransactionDualReadDao(v2, v1);
        }
    }

    @Repository("activeTraceDaoFactory")
    public static class ActiveTraceDaoFactory extends AgentStatDaoFactory<ActiveTraceBo, ActiveTraceDao> implements FactoryBean<ActiveTraceDao> {

        @Autowired
        public void setV1(@Qualifier("activeTraceDaoV1") ActiveTraceDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("activeTraceDaoV2") ActiveTraceDao v2) {
            this.v2 = v2;
        }

        @Override
        public ActiveTraceDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return ActiveTraceDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        ActiveTraceDao getCompatibilityDao(ActiveTraceDao v1, ActiveTraceDao v2) {
            return new HbaseAgentStatDualReadDao.ActiveTraceDualReadDao(v2, v1);
        }
    }

    @Repository("dataSourceDaoFactory")
    public static class DataSourceDaoFactory extends AgentStatDaoFactory<DataSourceListBo, DataSourceDao> implements FactoryBean<DataSourceDao> {

        @Autowired
        public void setV1(@Qualifier("dataSourceDaoV1") DataSourceDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("dataSourceDaoV2") DataSourceDao v2) {
            this.v2 = v2;
        }

        @Override
        public DataSourceDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return DataSourceDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        DataSourceDao getCompatibilityDao(DataSourceDao v1, DataSourceDao v2) {
            return new HbaseAgentStatDualReadDao.DataSourceDualReadDao(v2, v1);
        }
    }

    @Repository("responseTimeDaoFactory")
    public static class ResponseTimeDaoFactory extends AgentStatDaoFactory<ResponseTimeBo, ResponseTimeDao> implements FactoryBean<ResponseTimeDao> {

        @Autowired
        public void setV1(@Qualifier("responseTimeDaoV1") ResponseTimeDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("responseTimeDaoV2") ResponseTimeDao v2) {
            this.v2 = v2;
        }

        @Override
        public ResponseTimeDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return ResponseTimeDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        ResponseTimeDao getCompatibilityDao(ResponseTimeDao v1, ResponseTimeDao v2) {
            return new HbaseAgentStatDualReadDao.ResponseTimeDualReadDao(v2, v1);
        }
    }

    @Repository("deadlockDaoFactory")
    public static class DeadlockDaoFactory extends AgentStatDaoFactory<DeadlockBo, DeadlockDao> implements FactoryBean<DeadlockDao> {

        @Autowired
        public void setV1(@Qualifier("deadlockDaoV1") DeadlockDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("deadlockDaoV2") DeadlockDao v2) {
            this.v2 = v2;
        }

        @Override
        public DeadlockDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return ResponseTimeDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        DeadlockDao getCompatibilityDao(DeadlockDao v1, DeadlockDao v2) {
            return new HbaseAgentStatDualReadDao.DeadlockDualReadDao(v2, v1);
        }
    }

}
