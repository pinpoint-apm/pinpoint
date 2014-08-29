package com.nhn.pinpoint.profiler.modifier.tomcat;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Enumeration;
import java.util.List;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardHost;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nhn.pinpoint.bootstrap.context.Header;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.util.TransactionIdUtils;
import com.nhn.pinpoint.profiler.junit4.BasePinpointTest;
import com.nhn.pinpoint.profiler.junit4.IsRootSpan;

/**
 * @author hyungil.jeong
 */
public class StandardHostValveInvokeModifierTest extends BasePinpointTest {

    private static final ServiceType SERVICE_TYPE = ServiceType.TOMCAT;
    private static final String REQUEST_URI = "testRequestUri";
    private static final String SERVER_NAME = "serverForTest";
    private static final int SERVER_PORT = 19999;
    private static final String REMOTE_ADDRESS = "1.1.1.1";
    private static final Enumeration<String> EMPTY_PARAM_KEYS = new Enumeration<String>() {
        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public String nextElement() {
            return null;
        }
    };

    private StandardHost host;

    @Mock
    private Request mockRequest;
    @Mock
    private Response mockResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initMockRequest();
        // StandardHost's default constructor sets StandardHostValve as the first item in the pipeline.
        host = new StandardHost();
    }

    private void initMockRequest() {
        when(mockRequest.getRequestURI()).thenReturn(REQUEST_URI);
        when(mockRequest.getServerName()).thenReturn(SERVER_NAME);
        when(mockRequest.getServerPort()).thenReturn(SERVER_PORT);
        when(mockRequest.getRemoteAddr()).thenReturn(REMOTE_ADDRESS);
        when(mockRequest.getParameterNames()).thenReturn(EMPTY_PARAM_KEYS);
    }

    @Test
    @IsRootSpan
    public void invokeShouldBeTraced() throws Exception {
        // Given
        // When
        host.invoke(mockRequest, mockResponse);
        // Then
        final List<SpanBo> rootSpans = getCurrentRootSpans();
        assertEquals(rootSpans.size(), 1);

        final SpanBo rootSpan = rootSpans.get(0);
        assertEquals(rootSpan.getParentSpanId(), -1);
        assertEquals(rootSpan.getServiceType(), SERVICE_TYPE);
        assertEquals(rootSpan.getRpc(), REQUEST_URI);
        assertEquals(rootSpan.getEndPoint(), SERVER_NAME + ":" + SERVER_PORT);
        assertEquals(rootSpan.getRemoteAddr(), REMOTE_ADDRESS);
    }

    @Test
    @IsRootSpan
    public void invokeShouldTraceExceptions() throws Exception {
        // Given
        when(mockRequest.getContext()).thenThrow(new RuntimeException("expected exception."));
        // When
        try {
            host.invoke(mockRequest, mockResponse);
            assertTrue(false);
        } catch (RuntimeException e) {
            // Then
            final List<SpanBo> rootSpans = getCurrentRootSpans();
            assertEquals(rootSpans.size(), 1);

            final SpanBo rootSpan = rootSpans.get(0);
            assertEquals(rootSpan.getParentSpanId(), -1);
            assertEquals(rootSpan.getServiceType(), SERVICE_TYPE);
            assertTrue(rootSpan.hasException());
        }
    }
    
    @Test
    @IsRootSpan
    public void invokeShouldContinueTracingFromRequest() throws Exception {
        // Given
        // Set Transaction ID from remote source.
        final String sourceAgentId = "agentId";
        final long sourceAgentStartTime = 1234567890123L;
        final long sourceTransactionSequence = 12345678L;
        final String sourceTransactionId = TransactionIdUtils.formatString(sourceAgentId, sourceAgentStartTime, sourceTransactionSequence);
        when(mockRequest.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn(sourceTransactionId);
        // Set parent Span ID from remote source.
        final long sourceParentId = 99999;
        when(mockRequest.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn(String.valueOf(sourceParentId));
        // When
        host.invoke(mockRequest, mockResponse);
        // Then
        final List<SpanBo> rootSpans = getCurrentRootSpans();
        assertEquals(rootSpans.size(), 1);
        
        final SpanBo rootSpan = rootSpans.get(0);
        // Check Transaction ID from remote source.
        assertEquals(rootSpan.getTransactionId(), sourceTransactionId);
        assertEquals(rootSpan.getTraceAgentId(), sourceAgentId);
        assertEquals(rootSpan.getTraceAgentStartTime(), sourceAgentStartTime);
        assertEquals(rootSpan.getTraceTransactionSequence(), sourceTransactionSequence);
        // Check parent Span ID from remote source.
        assertEquals(rootSpan.getParentSpanId(), sourceParentId);
    }

}
