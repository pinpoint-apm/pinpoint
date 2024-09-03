package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqlUidMetaDataServiceTest {
    static int LENGTH_LIMIT = 100;

    SqlUidMetaDataService sut;
    SqlCacheService<byte[]> sqlCacheService;

    @Mock
    EnhancedDataSender<MetaDataType> dataSender;

    @BeforeEach
    void setUp() {
        UidCachingSqlNormalizer simpleCachingSqlNormalizer = new UidCachingSqlNormalizer(100, LENGTH_LIMIT);
        sqlCacheService = new SqlCacheService<>(dataSender, simpleCachingSqlNormalizer, 1000);
        sut = new SqlUidMetaDataService(sqlCacheService);
    }

    @Test
    void sameSqlSameId() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql);
        assertNew(parsingResult1);

        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql);
        assertCached(parsingResult2);

        assertSameId(parsingResult1, parsingResult2);
        verify(dataSender, times(1)).request(any(SqlUidMetaData.class));
    }

    @Test
    void sameSqlSameId_clearCache() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql);
        assertNew(parsingResult1);

        setUp();

        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql);
        assertNew(parsingResult2);

        assertSameId(parsingResult1, parsingResult2);
        verify(dataSender, times(2)).request(any(SqlUidMetaData.class));
    }

    @Test
    void differentSqlDifferentId() {
        String sql1 = "select * from A";
        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql1);
        assertNew(parsingResult1);

        String sql2 = "select * from B";
        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql2);
        assertNew(parsingResult2);

        assertDifferentId(parsingResult1, parsingResult2);
        verify(dataSender, times(2)).request(any(SqlUidMetaData.class));
    }

    @Test
    void bypassCache() {
        String veryLongSql = veryLongSql();

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(veryLongSql);
        assertNew(parsingResult1);

        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(veryLongSql);
        assertNew(parsingResult2);

        assertSameId(parsingResult1, parsingResult2);
    }

    String veryLongSql() {
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < LENGTH_LIMIT + 1; i++) {
            a.append("a");
        }
        return a.toString();
    }

    void assertNew(UidParsingResult parsingResult) {
        assertTrue(sqlCacheService.cacheSql(parsingResult, SqlUidMetaDataService::newSqlUidMetaData));
    }

    void assertCached(UidParsingResult parsingResult) {
        assertFalse(sqlCacheService.cacheSql(parsingResult, SqlUidMetaDataService::newSqlUidMetaData));
    }

    static void assertSameId(UidParsingResult parsingResult1, UidParsingResult parsingResult2) {
        assertArrayEquals(parsingResult1.getId(), parsingResult2.getId());
    }

    static void assertDifferentId(UidParsingResult parsingResult1, UidParsingResult parsingResult2) {
        assertFalse(Arrays.equals(parsingResult1.getId(), parsingResult2.getId()));
    }
}