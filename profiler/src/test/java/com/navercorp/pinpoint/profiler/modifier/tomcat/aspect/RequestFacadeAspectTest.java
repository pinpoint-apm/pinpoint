package com.nhn.pinpoint.profiler.modifier.tomcat.aspect;

import com.nhn.pinpoint.bootstrap.context.Header;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;


public class RequestFacadeAspectTest {
	public static class RequestFacadeAspectMock extends RequestFacadeAspect {
		@Override
		String __getHeader(String name) {
			return "header";
		}

		@Override
		Enumeration __getHeaders(String name) {
			Hashtable<String, String> hashtable = new Hashtable<String, String>();
			hashtable.put("a", "aa");
			hashtable.put("b", "bb");
			return hashtable.elements();
		}

		@Override
		Enumeration __getHeaderNames() {
			Hashtable<String, String>  hashtable = new Hashtable<String, String> ();
			hashtable.put("b", "bb");
			hashtable.put("c", "cc");
			hashtable.put("d", Header.HTTP_SPAN_ID.toString());
			return hashtable.elements();
		}
	}

	@Test
	public void getHeader() {
		RequestFacadeAspect mock = new RequestFacadeAspectMock();

		String isNull = mock.getHeader(Header.HTTP_SPAN_ID.toString());
		Assert.assertNull(isNull);

		String header = mock.getHeader("test");
		Assert.assertEquals(header, "header");
	}


	@Test
	public void getHeaders() {
		RequestFacadeAspect mock = new RequestFacadeAspectMock();
		Enumeration isNull = mock.getHeaders(Header.HTTP_SPAN_ID.toString());

		ArrayList list = Collections.list(isNull);
		Assert.assertEquals(list.size(), 0);

		Enumeration header = mock.getHeaders("test");
		Assert.assertEquals(Collections.list(header).size(), 2);
	}



	@Test
	public void getHeaderNames() {
		RequestFacadeAspect mock = new RequestFacadeAspectMock();
		Enumeration isNull = mock.getHeaderNames();

		ArrayList list = Collections.list(isNull);
		Assert.assertEquals(list.size(), 2);
	}

}