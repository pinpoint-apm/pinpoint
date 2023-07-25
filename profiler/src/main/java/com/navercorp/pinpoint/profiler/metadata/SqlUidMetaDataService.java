package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.cache.UidCache;

public class SqlUidMetaDataService extends AbstractSqlMetaDataService<byte[]> {
    public SqlUidMetaDataService(EnhancedDataSender<MetaDataType, ResponseMessage> enhancedDataSender, UidCache sqlCache) {
        super(enhancedDataSender, new UidCachingSqlNormalizer(sqlCache));
    }

    @Override
    protected MetaDataType prepareSqlMetaData(ParsingResultInternal<byte[]> parsingResult) {
        return new SqlUidMetaData(parsingResult.getId(), parsingResult.getSql());
    }
}
