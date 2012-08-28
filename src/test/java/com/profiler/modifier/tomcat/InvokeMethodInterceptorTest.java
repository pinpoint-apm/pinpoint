package com.profiler.modifier.tomcat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.profiler.context.Header;

public class InvokeMethodInterceptorTest {

	HttpServletRequest request;
	HttpServletResponse response;

	@Before
	public void beforeTest() {
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);

		when(request.getRequestURI()).thenReturn("/hellotest.nhn");
		when(request.getRemoteAddr()).thenReturn("10.0.0.1");
		when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn("TRACEID");
		when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("PARENTSPANID");
		when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("SPANID");
		when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("false");
		when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("0");

		Enumeration<?> enumeration = mock(Enumeration.class);
		when(request.getParameterNames()).thenReturn(enumeration);
	}

	@Test
	public void doTest() {
		InvokeMethodInterceptor interceptor = new InvokeMethodInterceptor();

		interceptor.before("target", "classname", "methodname", new Object[] { request, response });
		
		interceptor.after("target", "classname", "methodname", new Object[] { request, response }, new Object());
	}
}
