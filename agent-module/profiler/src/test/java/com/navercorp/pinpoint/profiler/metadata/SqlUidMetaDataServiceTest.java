package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
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

class SqlUidMetaDataServiceTest {
    SqlUidMetaDataService sut;

    @Mock
    EnhancedDataSender<MetaDataType> dataSender;

    AutoCloseable autoCloseable;

    SqlCacheService<byte[]> sqlCacheService;

    static int LENGTH_LIMIT = 100;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);

        this.sqlCacheService = newSqlCacheService();

        sut = new SqlUidMetaDataService(sqlCacheService);
    }

    SqlCacheService<byte[]> newSqlCacheService() {
        UidCachingSqlNormalizer simpleCachingSqlNormalizer = new UidCachingSqlNormalizer(100, LENGTH_LIMIT);
        return new SqlCacheService<>(dataSender, simpleCachingSqlNormalizer, 1000);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void sameSql() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql);

        assertTrue(sqlCacheService.cacheSql(parsingResult1, SqlUidMetaDataService::newSqlUidMetaData));
        assertFalse(sqlCacheService.cacheSql(parsingResult2, SqlUidMetaDataService::newSqlUidMetaData));

        assertArrayEquals(parsingResult1.getId(), parsingResult2.getId());
        verify(dataSender, times(1)).request(any(SqlUidMetaData.class));
    }

    @Test
    void sameSql_clearCache() {
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
    void differentSql() {
        String sql1 = "select * from A";
        String sql2 = "select * from B";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql1);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql2);

        assertTrue(sqlCacheService.cacheSql(parsingResult1, SqlUidMetaDataService::newSqlUidMetaData));
        assertTrue(sqlCacheService.cacheSql(parsingResult2, SqlUidMetaDataService::newSqlUidMetaData));

        assertFalse(Arrays.equals(parsingResult1.getId(), parsingResult2.getId()));
    }

    @Test
    void bypassCache() {
        String veryLongSql = veryLongSql();

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(veryLongSql);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(veryLongSql);

        assertTrue(sqlCacheService.cacheSql(parsingResult1, SqlUidMetaDataService::newSqlUidMetaData));
        assertTrue(sqlCacheService.cacheSql(parsingResult2, SqlUidMetaDataService::newSqlUidMetaData));

        assertArrayEquals(parsingResult1.getId(), parsingResult2.getId());
    }

    String veryLongSql() {
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < LENGTH_LIMIT + 1; i++) {
            a.append("a");
        }
        return a.toString();
    }
}