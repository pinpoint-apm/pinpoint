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
import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.dao.hbase.stat.compatibility.HbaseSampledAgentStatDualReadDao;
import com.navercorp.pinpoint.web.dao.stat.SampledActiveTraceDao;
import com.navercorp.pinpoint.web.dao.stat.SampledCpuLoadDao;
import com.navercorp.pinpoint.web.dao.stat.SampledDataSourceDao;
import com.navercorp.pinpoint.web.dao.stat.SampledDeadlockDao;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDao;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDetailedDao;
import com.navercorp.pinpoint.web.dao.stat.SampledResponseTimeDao;
import com.navercorp.pinpoint.web.dao.stat.SampledTransactionDao;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
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
abstract class SampledAgentStatDaoFactory<S extends SampledAgentStatDataPoint, D extends SampledAgentStatDao<S>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected D v1;
    protected D v2;

    @Autowired
    private HBaseAdminTemplate adminTemplate;

    @Value("#{pinpointWebProps['web.stat.format.compatibility.version'] ?: 'v2'}")
    private String mode = "v2";

    D getDao() throws Exception {
        logger.info("SampledAgentStatDao Compatibility {}", mode);

        final TableName v1TableName = HBaseTables.AGENT_STAT;
        final TableName v2TableName = HBaseTables.AGENT_STAT_VER2;

        if (mode.equalsIgnoreCase("v2")) {
            if (this.adminTemplate.tableExists(v2TableName)) {
                return v2;
            } else {
                logger.error("SampledAgentStatDao configured for v2, but {} table does not exist", v2TableName);
                throw new IllegalStateException(v2TableName + " table does not exist");
            }
        } else if (mode.equalsIgnoreCase("compatibilityMode")) {
            boolean v1TableExists = this.adminTemplate.tableExists(v1TableName);
            boolean v2TableExists = this.adminTemplate.tableExists(v2TableName);
            if (v1TableExists && v2TableExists) {
                return getCompatibilityDao(this.v1, this.v2);
            } else {
                logger.error("SampledAgentStatDao configured for compatibilityMode, but {} and {} tables do not exist", v1TableName, v2TableName);
                throw new IllegalStateException(v1TableName + ", " + v2TableName + " tables do not exist");
            }
        } else {
            throw new IllegalStateException("Unknown SampledAgentStatDao configuration : " + mode);
        }
    }

    abstract D getCompatibilityDao(D v1, D v2);

    @Repository("sampledJvmGcDaoFactory")
    public static class SampledJvmGcDaoFactory extends SampledAgentStatDaoFactory<SampledJvmGc, SampledJvmGcDao> implements FactoryBean<SampledJvmGcDao> {

        @Autowired
        public void setV1(@Qualifier("sampledJvmGcDaoV1") SampledJvmGcDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("sampledJvmGcDaoV2") SampledJvmGcDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledJvmGcDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledJvmGcDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        SampledJvmGcDao getCompatibilityDao(SampledJvmGcDao v1, SampledJvmGcDao v2) {
            return new HbaseSampledAgentStatDualReadDao.SampledJvmGcDualReadDao(v2, v1);
        }
    }

    @Repository("sampledJvmGcDetailedDaoFactory")
    public static class SampledJvmGcDetailedDaoFactory extends SampledAgentStatDaoFactory<SampledJvmGcDetailed, SampledJvmGcDetailedDao> implements FactoryBean<SampledJvmGcDetailedDao> {

        @Autowired
        public void setV1(@Qualifier("sampledJvmGcDetailedDaoV1") SampledJvmGcDetailedDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("sampledJvmGcDetailedDaoV2") SampledJvmGcDetailedDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledJvmGcDetailedDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledJvmGcDetailedDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        SampledJvmGcDetailedDao getCompatibilityDao(SampledJvmGcDetailedDao v1, SampledJvmGcDetailedDao v2) {
            return new HbaseSampledAgentStatDualReadDao.SampledJvmGcDetailedDualReadDao(v2, v1);
        }
    }

    @Repository("sampledCpuLoadDaoFactory")
    public static class SampledCpuLoadDaoFactory extends SampledAgentStatDaoFactory<SampledCpuLoad, SampledCpuLoadDao> implements FactoryBean<SampledCpuLoadDao> {

        @Autowired
        public void setV1(@Qualifier("sampledCpuLoadDaoV1") SampledCpuLoadDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("sampledCpuLoadDaoV2") SampledCpuLoadDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledCpuLoadDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledCpuLoadDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        SampledCpuLoadDao getCompatibilityDao(SampledCpuLoadDao v1, SampledCpuLoadDao v2) {
            return new HbaseSampledAgentStatDualReadDao.SampledCpuLoadDualReadDao(v2, v1);
        }
    }

    @Repository("sampledTransactionDaoFactory")
    public static class SampledTransactionDaoFactory extends SampledAgentStatDaoFactory<SampledTransaction, SampledTransactionDao> implements FactoryBean<SampledTransactionDao> {

        @Autowired
        public void setV1(@Qualifier("sampledTransactionDaoV1") SampledTransactionDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("sampledTransactionDaoV2") SampledTransactionDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledTransactionDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledTransactionDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        SampledTransactionDao getCompatibilityDao(SampledTransactionDao v1, SampledTransactionDao v2) {
            return new HbaseSampledAgentStatDualReadDao.SampledTransactionDualReadDao(v2, v1);
        }
    }

    @Repository("sampledActiveTraceDaoFactory")
    public static class SampledActiveTraceDaoFactory extends SampledAgentStatDaoFactory<SampledActiveTrace, SampledActiveTraceDao> implements FactoryBean<SampledActiveTraceDao> {

        @Autowired
        public void setV1(@Qualifier("sampledActiveTraceDaoV1") SampledActiveTraceDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("sampledActiveTraceDaoV2") SampledActiveTraceDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledActiveTraceDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledActiveTraceDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        SampledActiveTraceDao getCompatibilityDao(SampledActiveTraceDao v1, SampledActiveTraceDao v2) {
            return new HbaseSampledAgentStatDualReadDao.SampledActiveTraceDualReadDao(v2, v1);
        }
    }

    @Repository("sampledDataSourceDaoFactory")
    public static class SampledDataSourceDaoFactory extends SampledAgentStatDaoFactory<SampledDataSourceList, SampledDataSourceDao> implements FactoryBean<SampledDataSourceDao> {

        @Autowired
        public void setV1(@Qualifier("sampledDataSourceDaoV1") SampledDataSourceDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("sampledDataSourceDaoV2") SampledDataSourceDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledDataSourceDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledDataSourceDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        SampledDataSourceDao getCompatibilityDao(SampledDataSourceDao v1, SampledDataSourceDao v2) {
            return new HbaseSampledAgentStatDualReadDao.SampledDataSourceDualReadDao(v2, v1);
        }
    }

    @Repository("sampledResponseTimeDaoFactory")
    public static class SampledResponseTimeDaoFactory extends SampledAgentStatDaoFactory<SampledResponseTime, SampledResponseTimeDao> implements FactoryBean<SampledResponseTimeDao> {

        @Autowired
        public void setV1(@Qualifier("sampledResponseTimeDaoV1") SampledResponseTimeDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("sampledResponseTimeDaoV2") SampledResponseTimeDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledResponseTimeDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledResponseTimeDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        SampledResponseTimeDao getCompatibilityDao(SampledResponseTimeDao v1, SampledResponseTimeDao v2) {
            return new HbaseSampledAgentStatDualReadDao.SampledResponseTimeDualReadDao(v2, v1);
        }
    }

    @Repository("sampledDeadlockDaoFactory")
    public static class SampledDeadlockDaoFactory extends SampledAgentStatDaoFactory<SampledDeadlock, SampledDeadlockDao> implements FactoryBean<SampledDeadlockDao> {

        @Autowired
        public void setV1(@Qualifier("sampledDeadlockDaoV1") SampledDeadlockDao v1) {
            this.v1 = v1;
        }

        @Autowired
        public void setV2(@Qualifier("sampledDeadlockDaoV2") SampledDeadlockDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledDeadlockDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledDeadlockDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        SampledDeadlockDao getCompatibilityDao(SampledDeadlockDao v1, SampledDeadlockDao v2) {
            return new HbaseSampledAgentStatDualReadDao.SampledDeadlockDualReadDao(v2, v1);
        }
    }

}
