package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.cache.UidCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class SqlUidMetaDataServiceTest {
    private SqlUidMetaDataService sut;

    @Mock
    private EnhancedDataSender<MetaDataType, ResponseMessage> dataSender;

    AutoCloseable autoCloseable;

    private SqlCacheService<byte[]> sqlCacheService;

    @BeforeEach
    public void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);

        this.sqlCacheService = newSqlCacheService();

        sut = new SqlUidMetaDataService(sqlCacheService);
    }

    private SqlCacheService<byte[]> newSqlCacheService() {
        UidCache uidCache = new UidCache(100);
        CachingSqlNormalizer<ParsingResultInternal<byte[]>> simpleCachingSqlNormalizer = new DefaultCachingSqlNormalizer<>(uidCache);
        return new SqlCacheService<>(dataSender, simpleCachingSqlNormalizer);
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void sendDataOnce() {
        String sql = "select * from A";

        UidParsingResult parsingResult = (UidParsingResult) sut.wrapSqlResult(sql);

        assertTrue(sqlCacheService.cacheSql(parsingResult, SqlUidMetaDataService::newSqlUidMetaData));
        verify(dataSender).request(any(SqlUidMetaData.class));

        assertFalse(sqlCacheService.cacheSql(parsingResult, SqlUidMetaDataService::newSqlUidMetaData));
        verifyNoMoreInteractions(dataSender);
    }

    @Test
    public void sameSql() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql);

        assertTrue(sqlCacheService.cacheSql(parsingResult1, SqlUidMetaDataService::newSqlUidMetaData));
        assertFalse(sqlCacheService.cacheSql(parsingResult2, SqlUidMetaDataService::newSqlUidMetaData));

        assertArrayEquals(parsingResult1.getId(), parsingResult2.getId());
        verify(dataSender, times(1)).request(any(SqlUidMetaData.class));
    }

    @Test
    public void sameSql_clearCache() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql);

        assertTrue(sqlCacheService.cacheSql(parsingResult1, SqlUidMetaDataService::newSqlUidMetaData));
        sqlCacheService = newSqlCacheService();
        assertTrue(sqlCacheService.cacheSql(parsingResult2, SqlUidMetaDataService::newSqlUidMetaData));

        assertArrayEquals(parsingResult1.getId(), parsingResult2.getId());
        verify(dataSender, times(2)).request(any(SqlUidMetaData.class));
    }

    @Test
    public void differentSql() {
        String sql1 = "select * from A";
        String sql2 = "select * from B";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql1);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql2);

        assertTrue(sqlCacheService.cacheSql(parsingResult1, SqlUidMetaDataService::newSqlUidMetaData));
        assertTrue(sqlCacheService.cacheSql(parsingResult2, SqlUidMetaDataService::newSqlUidMetaData));

        assertFalse(Arrays.equals(parsingResult1.getId(), parsingResult2.getId()));
    }
}