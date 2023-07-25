package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.cache.UidCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SqlUidMetaDataServiceTest {
    private SqlUidMetaDataService sut;

    @Mock
    private EnhancedDataSender<MetaDataType, ResponseMessage> dataSender;

    AutoCloseable autoCloseable;

    @BeforeEach
    public void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);

        sut = new SqlUidMetaDataService(dataSender, new UidCache(100));
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void sendDataOnce() {
        String sql = "select * from A";

        ParsingResult parsingResult = sut.parseSql(sql);

        assertTrue(sut.cacheSql(parsingResult));
        verify(dataSender).request(any(SqlUidMetaData.class));

        assertFalse(sut.cacheSql(parsingResult));
        verifyNoMoreInteractions(dataSender);
    }

    @Test
    public void sameSql() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.parseSql(sql);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.parseSql(sql);

        assertTrue(sut.cacheSql(parsingResult1));
        assertFalse(sut.cacheSql(parsingResult2));

        assertArrayEquals(parsingResult1.getId(), parsingResult2.getId());
        verify(dataSender, times(1)).request(any(SqlUidMetaData.class));
    }

    @Test
    public void sameSql_clearCache() {
        String sql = "select * from A";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.parseSql(sql);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.parseSql(sql);

        assertTrue(sut.cacheSql(parsingResult1));
        sut = new SqlUidMetaDataService(dataSender, new UidCache(100));
        assertTrue(sut.cacheSql(parsingResult2));

        assertArrayEquals(parsingResult1.getId(), parsingResult2.getId());
        verify(dataSender, times(2)).request(any(SqlUidMetaData.class));
    }

    @Test
    public void differentSql() {
        String sql1 = "select * from A";
        String sql2 = "select * from B";

        UidParsingResult parsingResult1 = (UidParsingResult) sut.parseSql(sql1);
        UidParsingResult parsingResult2 = (UidParsingResult) sut.parseSql(sql2);

        assertTrue(sut.cacheSql(parsingResult1));
        assertTrue(sut.cacheSql(parsingResult2));

        assertFalse(Arrays.equals(parsingResult1.getId(), parsingResult2.getId()));
    }
}