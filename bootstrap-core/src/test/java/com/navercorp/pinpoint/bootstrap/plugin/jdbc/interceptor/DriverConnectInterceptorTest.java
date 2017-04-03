/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import org.junit.Test;

import java.sql.Driver;
import java.sql.SQLException;

import static com.navercorp.pinpoint.common.util.VarArgs.va;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DriverConnectInterceptorTest {

    @Test
    public void driverConnect() throws SQLException {
        TraceContext traceContext = mock(TraceContext.class);
        MethodDescriptor methodDescriptor = mock(MethodDescriptor.class);
        JdbcUrlParser parser = mock(JdbcUrlParser.class);
        when(parser.parse(anyString())).thenReturn(UnKnownDatabaseInfo.INSTANCE);

        String invalidJdbcUrl = "invalidUrl";

        Driver driver = mock(Driver.class);
        DatabaseInfoAccessor setAccessor = mock(DatabaseInfoAccessor.class);
        DatabaseInfoAccessor getAccessor = mock(DatabaseInfoAccessor.class);
        SpanEventRecorder spanEventRecorder = mock(SpanEventRecorder.class);

        DriverConnectInterceptor driverConnectInterceptor = new DriverConnectInterceptor(traceContext, methodDescriptor, parser);

        driverConnectInterceptor.prepareAfterTrace(driver, va(invalidJdbcUrl), setAccessor, null);
        driverConnectInterceptor.doInAfterTrace(spanEventRecorder, driver, va(invalidJdbcUrl), getAccessor, null);

        verify(setAccessor, times(1))._$PINPOINT$_setDatabaseInfo(UnKnownDatabaseInfo.INSTANCE);
        verify(getAccessor, times(1))._$PINPOINT$_getDatabaseInfo();
    }

    @Test
    public void driverConnect_return_Null_NPEtest() throws SQLException {
        TraceContext traceContext = mock(TraceContext.class);
        MethodDescriptor methodDescriptor = mock(MethodDescriptor.class);
        JdbcUrlParser parser = mock(JdbcUrlParser.class);
        when(parser.parse(anyString())).thenReturn(UnKnownDatabaseInfo.INSTANCE);

        String invalidJdbcUrl = "invalidUrl";

        final Driver driver = mock(Driver.class);
        SpanEventRecorder spanEventRecorder = mock(SpanEventRecorder.class);

        DriverConnectInterceptor driverConnectInterceptor = new DriverConnectInterceptor(traceContext, methodDescriptor, parser);

        driverConnectInterceptor.prepareAfterTrace(driver, va(invalidJdbcUrl), null, null);
        driverConnectInterceptor.doInAfterTrace(spanEventRecorder, driver, va(invalidJdbcUrl), null, null);

    }



}