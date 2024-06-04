package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.DefaultShared;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSqlCountServiceTest {
    private static final int SQL_ERROR_COUNT = 100;

    private DefaultSqlCountService sut = new DefaultSqlCountService(SQL_ERROR_COUNT);

    private Shared shared;

    @BeforeEach
    void setUp() {
        shared = new DefaultShared();
    }

    @Test
    void recordSqlCount() {
        // when
        for (int i = 0; i < SQL_ERROR_COUNT; i++) {
            sut.recordSqlCount(shared);
        }

        // then
        assertThat(shared.getErrorCode()).isNotZero();
    }
}