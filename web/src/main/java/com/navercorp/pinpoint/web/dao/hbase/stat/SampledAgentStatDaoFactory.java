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

import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;

import com.navercorp.pinpoint.web.dao.stat.SampledActiveTraceDao;
import com.navercorp.pinpoint.web.dao.stat.SampledCpuLoadDao;
import com.navercorp.pinpoint.web.dao.stat.SampledDataSourceDao;
import com.navercorp.pinpoint.web.dao.stat.SampledDeadlockDao;
import com.navercorp.pinpoint.web.dao.stat.SampledDirectBufferDao;
import com.navercorp.pinpoint.web.dao.stat.SampledFileDescriptorDao;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDao;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDetailedDao;
import com.navercorp.pinpoint.web.dao.stat.SampledResponseTimeDao;
import com.navercorp.pinpoint.web.dao.stat.SampledTransactionDao;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.SampledDirectBuffer;
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * @author HyunGil Jeong
 */
abstract class SampledAgentStatDaoFactory<S extends SampledAgentStatDataPoint, D extends SampledAgentStatDao<S>> {

    protected D v2;

    D getDao() throws Exception {
        return v2;
    }

    @Repository("sampledJvmGcDaoFactory")
    public static class SampledJvmGcDaoFactory extends SampledAgentStatDaoFactory<SampledJvmGc, SampledJvmGcDao> implements FactoryBean<SampledJvmGcDao> {

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
    }

    @Repository("sampledJvmGcDetailedDaoFactory")
    public static class SampledJvmGcDetailedDaoFactory extends SampledAgentStatDaoFactory<SampledJvmGcDetailed, SampledJvmGcDetailedDao> implements FactoryBean<SampledJvmGcDetailedDao> {

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
    }

    @Repository("sampledCpuLoadDaoFactory")
    public static class SampledCpuLoadDaoFactory extends SampledAgentStatDaoFactory<SampledCpuLoad, SampledCpuLoadDao> implements FactoryBean<SampledCpuLoadDao> {

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
    }

    @Repository("sampledTransactionDaoFactory")
    public static class SampledTransactionDaoFactory extends SampledAgentStatDaoFactory<SampledTransaction, SampledTransactionDao> implements FactoryBean<SampledTransactionDao> {

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
    }

    @Repository("sampledActiveTraceDaoFactory")
    public static class SampledActiveTraceDaoFactory extends SampledAgentStatDaoFactory<SampledActiveTrace, SampledActiveTraceDao> implements FactoryBean<SampledActiveTraceDao> {

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
    }

    @Repository("sampledDataSourceDaoFactory")
    public static class SampledDataSourceDaoFactory extends SampledAgentStatDaoFactory<SampledDataSourceList, SampledDataSourceDao> implements FactoryBean<SampledDataSourceDao> {

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
    }

    @Repository("sampledResponseTimeDaoFactory")
    public static class SampledResponseTimeDaoFactory extends SampledAgentStatDaoFactory<SampledResponseTime, SampledResponseTimeDao> implements FactoryBean<SampledResponseTimeDao> {

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
    }

    @Repository("sampledDeadlockDaoFactory")
    public static class SampledDeadlockDaoFactory extends SampledAgentStatDaoFactory<SampledDeadlock, SampledDeadlockDao> implements FactoryBean<SampledDeadlockDao> {

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
    }

    @Repository("sampledFileDescriptorDaoFactory")
    public static class SampledFileDescriptorDaoFactory extends SampledAgentStatDaoFactory<SampledFileDescriptor, SampledFileDescriptorDao> implements FactoryBean<SampledFileDescriptorDao> {

        @Autowired
        public void setV2(@Qualifier("sampledFileDescriptorDaoV2") SampledFileDescriptorDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledFileDescriptorDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledFileDescriptorDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }

    @Repository("sampledDirectBufferDaoFactory")
    public static class SampledDirectBufferDaoFactory extends SampledAgentStatDaoFactory<SampledDirectBuffer, SampledDirectBufferDao> implements FactoryBean<SampledDirectBufferDao> {

        @Autowired
        public void setV2(@Qualifier("sampledDirectBufferDaoV2") SampledDirectBufferDao v2) {
            this.v2 = v2;
        }

        @Override
        public SampledDirectBufferDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return SampledDirectBufferDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}
