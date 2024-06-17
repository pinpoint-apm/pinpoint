/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author jaehong.kim
 */
public class StatusErrorsTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test
    public void throwable() {
        StatusError statusError = StatusErrors.throwable(new RuntimeException("test"));
        assertEquals("test", statusError.getMessage());
        assertFalse(statusError.isSimpleError());
    }


    @Test
    public void throwable_null_message() {
        StatusError statusError = StatusErrors.throwable(new RuntimeException());
        assertNull(statusError.getMessage());
        assertFalse(statusError.isSimpleError());
    }

    @Test
    public void throwable_status_cause_message_is_null() {
        Status unavailable = Status.UNAVAILABLE.withCause(new RuntimeException());
        StatusRuntimeException t = new StatusRuntimeException(unavailable);

        StatusError statusError = StatusErrors.throwable(t);
        assertEquals(Status.UNAVAILABLE.getCode().toString(), statusError.getMessage());
        assertFalse(statusError.isSimpleError());
    }

    @Test
    public void throwable_status_cause_message() {
        Status unavailable = Status.UNAVAILABLE.withCause(new RuntimeException("test"));
        StatusRuntimeException t = new StatusRuntimeException(unavailable);

        StatusError statusError = StatusErrors.throwable(t);
        assertEquals(Status.UNAVAILABLE.getCode().toString(), statusError.getMessage());
        assertFalse(statusError.isSimpleError());
    }

    @Test
    public void throwable_status_cause_connection_refuse() {
        RuntimeException cause = new RuntimeException(StatusErrors.CONNECTION_REFUSED_MESSAGE);
        Status unavailable = Status.UNAVAILABLE.withCause(cause);
        StatusRuntimeException t = new StatusRuntimeException(unavailable);

        StatusError statusError = StatusErrors.throwable(t);
        Assertions.assertThat(statusError.getMessage())
                .contains(StatusErrors.CONNECTION_REFUSED_MESSAGE );
        assertTrue(statusError.isSimpleError());
    }

    @Test
    public void throwable_status_cancel() {
        Status cancel = Status.CANCELLED.withDescription(StatusErrors.CANCELLED_BEFORE_RECEIVING_HALF_CLOSE);
        StatusRuntimeException t = new StatusRuntimeException(cancel);

        StatusError statusError = StatusErrors.throwable(t);
        Assertions.assertThat(statusError.getMessage())
                .contains(Status.CANCELLED.getCode().toString());
        assertTrue(statusError.isSimpleError());
    }


    @Test
    public void test_asException() {
        StatusError statusError = StatusErrors.throwable(Status.CANCELLED.asException());
        assertEquals("CANCELLED", statusError.getMessage());
        assertFalse(statusError.isSimpleError());
    }


    @Test
    public void simpleCode_null() {

        StatusException exception = Status.CANCELLED.withCause(null).withDescription(null).asException();
        StatusError statusError = StatusErrors.throwable(exception);
        assertEquals("CANCELLED", statusError.getMessage());
        assertFalse(statusError.isSimpleError());
    }

    @Test
    public void nonSimpleCode_null() {

        StatusException exception = Status.ABORTED.withCause(null).withDescription(null).asException();
        StatusError statusError = StatusErrors.throwable(exception);
        assertEquals("ABORTED", statusError.getMessage());
        assertFalse(statusError.isSimpleError());
    }
}