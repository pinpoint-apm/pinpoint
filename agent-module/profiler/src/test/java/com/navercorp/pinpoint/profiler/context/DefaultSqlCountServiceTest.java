package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.profiler.context.id.DefaultShared;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

class DefaultSqlCountServiceTest {
    private static final int SQL_ERROR_COUNT = 100;

    private final DefaultSqlCountService sut = new DefaultSqlCountService(SQL_ERROR_COUNT);

    private Shared shared;

    ErrorRecorder errorRecorder;

    @BeforeEach
    void setUp() {
        shared = new DefaultShared();
        errorRecorder = Mockito.mock(ErrorRecorder.class);
    }

    @Test
    void recordSqlCount() {
        // when
        for (int i = 0; i < SQL_ERROR_COUNT; i++) {
            sut.recordSqlCount(shared, errorRecorder);
        }

        // then
        verify(errorRecorder).recordError(ErrorCategory.SQL);
    }
}