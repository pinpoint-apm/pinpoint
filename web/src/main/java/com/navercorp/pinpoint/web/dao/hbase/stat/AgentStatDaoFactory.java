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


import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.web.dao.stat.ActiveTraceDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.dao.stat.CpuLoadDao;
import com.navercorp.pinpoint.web.dao.stat.DataSourceDao;
import com.navercorp.pinpoint.web.dao.stat.DeadlockDao;
import com.navercorp.pinpoint.web.dao.stat.DirectBufferDao;
import com.navercorp.pinpoint.web.dao.stat.FileDescriptorDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDetailedDao;
import com.navercorp.pinpoint.web.dao.stat.ResponseTimeDao;
import com.navercorp.pinpoint.web.dao.stat.TransactionDao;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * @author HyunGil Jeong
 */
abstract class AgentStatDaoFactory<T extends AgentStatDataPoint, D extends AgentStatDao<T>> {

    protected D v2;

    D getDao() throws Exception {
        return v2;
    }

    @Repository("jvmGcDaoFactory")
    public static class JvmGcDaoFactory extends AgentStatDaoFactory<JvmGcBo, JvmGcDao> implements FactoryBean<JvmGcDao> {

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
    }

    @Repository("jvmGcDetailedDaoFactory")
    public static class JvmGcDetailedDaoFactory extends AgentStatDaoFactory<JvmGcDetailedBo, JvmGcDetailedDao> implements FactoryBean<JvmGcDetailedDao> {

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
    }

    @Repository("cpuLoadDaoFactory")
    public static class CpuLoadDaoFactory extends AgentStatDaoFactory<CpuLoadBo, CpuLoadDao> implements FactoryBean<CpuLoadDao> {

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
    }

    @Repository("transactionDaoFactory")
    public static class TransactionDaoFactory extends AgentStatDaoFactory<TransactionBo, TransactionDao> implements FactoryBean<TransactionDao> {

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
    }

    @Repository("activeTraceDaoFactory")
    public static class ActiveTraceDaoFactory extends AgentStatDaoFactory<ActiveTraceBo, ActiveTraceDao> implements FactoryBean<ActiveTraceDao> {

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
    }

    @Repository("dataSourceDaoFactory")
    public static class DataSourceDaoFactory extends AgentStatDaoFactory<DataSourceListBo, DataSourceDao> implements FactoryBean<DataSourceDao> {

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
    }

    @Repository("responseTimeDaoFactory")
    public static class ResponseTimeDaoFactory extends AgentStatDaoFactory<ResponseTimeBo, ResponseTimeDao> implements FactoryBean<ResponseTimeDao> {

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
    }

    @Repository("deadlockDaoFactory")
    public static class DeadlockDaoFactory extends AgentStatDaoFactory<DeadlockThreadCountBo, DeadlockDao> implements FactoryBean<DeadlockDao> {

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
    }

    @Repository("fileDescriptorDaoFactory")
    public static class FileDescriptorDaoFactory extends AgentStatDaoFactory<FileDescriptorBo, FileDescriptorDao> implements FactoryBean<FileDescriptorDao> {

        @Autowired
        public void setV2(@Qualifier("fileDescriptorDaoV2") FileDescriptorDao v2) {
            this.v2 = v2;
        }

        @Override
        public FileDescriptorDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return FileDescriptorDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }

    @Repository("directBufferDaoFactory")
    public static class DirectBufferDaoFactory extends AgentStatDaoFactory<DirectBufferBo, DirectBufferDao> implements FactoryBean<DirectBufferDao> {

        @Autowired
        public void setV2(@Qualifier("directBufferDaoV2") DirectBufferDao v2) {
            this.v2 = v2;
        }

        @Override
        public DirectBufferDao getObject() throws Exception {
            return super.getDao();
        }

        @Override
        public Class<?> getObjectType() {
            return DirectBufferDao.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}
