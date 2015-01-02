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

package com.navercorp.pinpoint.profiler.modifier.tomcat.aspect;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.profiler.modifier.tomcat.aspect.RequestFacadeAspect;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;


public class RequestFacadeAspectTest {
    public static class RequestFacadeAspectMock extends RequestFacadeAspect       {
		@       verride
		String __getHeader(          tring name              {
	       	return "header";
		}

		@Override
          	Enumeration __getHeaders(String name) {
			Hashtable<String, St          ing> hashtable = ne           Hashtable<String,           tring>();
			hashtable             put(       a", "aa");
			hashtable.put(          b", "bb");
			return hashtable.elements();
		}

		@Override
		Enum          ration __getHeaderN          mes() {
			Hashtabl          <String, String>  hashtable = new Hashtable<S          ring, String> ();
			h             sh    able.put("b", "bb");
		       hashtable.put("c", "cc");
			hashtable.put("d", Header       HTTP_SPAN_ID.toString());
			return hashtable.elements();
	       }
	}

	@Test
	public vo       d getHeader() {
		RequestFacadeAspe       t mock = new RequestFacadeAspectMo    k    );
    		String isNull = mock.g       tHeader(Header.HTTP_SPAN_ID.toString());
		Assert.ass       rtNull(isNull);

		String header = mock.getHeader("test");
		Asser       .assertEquals(header, "header");
	}


       @Test
	public void getHeaders() {       		RequestFacadeAspect mock = new RequestF       cadeAspectMock();
		Enumeration isNull = mock.getHea    er    (He    der.HTTP_SPAN_ID.toString())

		ArrayList list = Collections.list(isNull);
		Asse       t.assertEquals(list.size(), 0);

		Enume       ation header = mock.getHeaders("test")
		Assert.assertEquals(Collectio    s.list(header).size(), 2);
	}



	@Test
	public void getHeaderNames() {
		RequestFacadeAspect mock = new RequestFacadeAspectMock();
		Enumeration isNull = mock.getHeaderNames();

		ArrayList list = Collections.list(isNull);
		Assert.assertEquals(list.size(), 2);
	}

}