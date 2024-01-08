package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.DefaultShared;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultSqlCountServiceTest {
    private static final int SQL_ERROR_COUNT = 100;

    private DefaultSqlCountService sut = new DefaultSqlCountService(SQL_ERROR_COUNT);

    @Mock
    private TraceRoot traceRoot;

    private Shared shared;

    @BeforeEach
    void setUp() {
        shared = new DefaultShared();
        when(traceRoot.getShared()).thenReturn(shared);
    }

    @Test
    void recordSqlCount() {
        // when
        for (int i = 0; i < SQL_ERROR_COUNT; i++) {
            sut.recordSqlCount(traceRoot);
        }

        // then
        assertThat(shared.getErrorCode()).isNotZero();
    }
}