package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.dao.hbase.HbaseApplicationTraceIndexDao;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HbaseTableConfiguration {

    @Bean
    @Primary
    ApplicationTraceIndexDao hbaseApplicationTraceIndexDao(HbasePutWriter putWriter, TableNameProvider tableNameProvider,
                                                           @Qualifier("applicationIndexRowKeyEncoder") RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoder) {
        return new HbaseApplicationTraceIndexDao(HbaseTables.APPLICATION_TRACE_INDEX_TRACE, HbaseTables.APPLICATION_TRACE_INDEX_META,
                putWriter, tableNameProvider, applicationIndexRowKeyEncoder);
    }


    @Bean
    ApplicationTraceIndexDao hbaseApplicationTraceIndexDaoV2(HbasePutWriter putWriter, TableNameProvider tableNameProvider,
                                                           @Qualifier("applicationIndexRowKeyEncoderV2") RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoder) {
        return new HbaseApplicationTraceIndexDao(HbaseTables.TRACE_INDEX, HbaseTables.TRACE_INDEX_META,
                putWriter, tableNameProvider, applicationIndexRowKeyEncoder);
    }

}
