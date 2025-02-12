package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.profiler.message.DataConsumer;
import com.navercorp.pinpoint.profiler.cache.UidCache;
import com.navercorp.pinpoint.profiler.cache.UidGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqlUidMetaDataServiceTest {
    SqlUidMetaDataService sut;

    @Mock
    DataConsumer<MetaDataType> dataSender;

    @BeforeEach
    void setUp() {
        UidCache sqlCache = new UidCache(100, new UidGenerator.Murmur(), 1000);
        SqlCacheService<byte[]> sqlCacheService = new SqlCacheService<>(sqlCache, 1000);
        sut = new SqlUidMetaDataService(dataSender, sqlCacheService);
    }

    @Test
    void sameSqlSameId() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql);
        sut.newSqlAnnotation(parsingResult1, null);

        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql);
        sut.newSqlAnnotation(parsingResult2, null);

        assertSameId(parsingResult1, parsingResult2);
        verify(dataSender, times(1)).send(any(SqlUidMetaData.class));
    }

    @Test
    void sameSqlSameId_clearCache() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql);
        sut.newSqlAnnotation(parsingResult1, null);

        setUp();

        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql);
        sut.newSqlAnnotation(parsingResult2, null);

        assertSameId(parsingResult1, parsingResult2);
        verify(dataSender, times(2)).send(any(SqlUidMetaData.class));
    }

    @Test
    void differentSqlDifferentId() {
        String sql1 = "select * from A";
        UidParsingResult parsingResult1 = (UidParsingResult) sut.wrapSqlResult(sql1);
        sut.newSqlAnnotation(parsingResult1, null);

        String sql2 = "select * from B";
        UidParsingResult parsingResult2 = (UidParsingResult) sut.wrapSqlResult(sql2);
        sut.newSqlAnnotation(parsingResult2, null);

        assertDifferentId(parsingResult1, parsingResult2);
        verify(dataSender, times(2)).send(any(SqlUidMetaData.class));
    }

    static void assertSameId(UidParsingResult parsingResult1, UidParsingResult parsingResult2) {
        assertArrayEquals(parsingResult1.getId(), parsingResult2.getId());
    }

    static void assertDifferentId(UidParsingResult parsingResult1, UidParsingResult parsingResult2) {
        assertFalse(Arrays.equals(parsingResult1.getId(), parsingResult2.getId()));
    }
}