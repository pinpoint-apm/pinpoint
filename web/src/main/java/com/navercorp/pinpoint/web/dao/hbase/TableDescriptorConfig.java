/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.hbase.TableDescriptors;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration()
//@Import(TableDescriptors.class)
public class TableDescriptorConfig extends TableDescriptors {

    public TableDescriptorConfig(TableNameProvider tableNameProvider) {
        super(tableNameProvider);
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.AgentInfo> getAgentInfo() {
        return super.getAgentInfo();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.AgentEvent> getAgentEvent() {
        return super.getAgentEvent();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.AgentLifeCycleStatus> getAgentLifeCycleStatus() {
        return super.getAgentLifeCycleStatus();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.AgentStatStatistics> getAgentStatStatus() {
        return super.getAgentStatStatus();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.ApiMetadata> getApiMetadata() {
        return super.getApiMetadata();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.ApplicationIndex> getApplicationIndex() {
        return super.getApplicationIndex();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.ApplicationStatStatistics> getApplicationStatStatistics() {
        return super.getApplicationStatStatistics();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.ApplicationTraceIndexTrace> getApplicationTraceIndexTrace() {
        return super.getApplicationTraceIndexTrace();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.HostStatMap> getHostStatMap() {
        return super.getHostStatMap();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.CalleeStatMap> getCalleeStatMap() {
        return super.getCalleeStatMap();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.CallerStatMap> getCallerStatMap() {
        return super.getCallerStatMap();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.SelfStatMap> getSelfStatMap() {
        return super.getSelfStatMap();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.SqlMetadataV2> getSqlMetadataV2() {
        return super.getSqlMetadataV2();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.StringMetadataStr> getStringMetadataStr() {
        return super.getStringMetadataStr();
    }

    @Bean
    @Override
    public TableDescriptor<HbaseColumnFamily.Trace> getTrace() {
        return super.getTrace();
    }
}
