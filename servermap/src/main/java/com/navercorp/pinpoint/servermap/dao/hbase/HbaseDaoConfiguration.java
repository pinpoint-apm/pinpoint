/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.servermap.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author intr3p1d
 */
@Configuration
public class HbaseDaoConfiguration {

    @Bean
    public HbaseDao hbaseInboundDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            HbaseBatchWriter hbaseBatchWriter,
            @Qualifier("applicationMapInboundRowKeyDistributor")
            RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix
    ) {
        return new HbaseDao(
                HbaseColumnFamily.MAP_STATISTICS_INBOUND_SERVICE_GROUP_COUNTER,
                hbaseTemplate,
                tableNameProvider,
                hbaseBatchWriter,
                rowKeyDistributorByHashPrefix
        );
    }

    @Bean
    public HbaseDao hbaseOutboundDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            HbaseBatchWriter hbaseBatchWriter,
            @Qualifier("applicationMapOutboundRowKeyDistributor")
            RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix
    ) {
        return new HbaseDao(
                HbaseColumnFamily.MAP_STATISTICS_OUTBOUND_SERVICE_GROUP_COUNTER,
                hbaseTemplate,
                tableNameProvider,
                hbaseBatchWriter,
                rowKeyDistributorByHashPrefix
        );
    }


    @Bean
    public HbaseDao hbaseSelfDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            HbaseBatchWriter hbaseBatchWriter,
            @Qualifier("applicationMapSelfRowKeyDistributor")
            RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix
    ) {
        return new HbaseDao(
                HbaseColumnFamily.MAP_STATISTICS_SELF_SERVICE_GROUP_COUNTER,
                hbaseTemplate,
                tableNameProvider,
                hbaseBatchWriter,
                rowKeyDistributorByHashPrefix
        );
    }


}
