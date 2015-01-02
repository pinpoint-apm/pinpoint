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

package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.common.util.DelegateEnumeration;
import com.navercorp.pinpoint.common.util.EmptyEnumeration;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public enum Header {

    HTTP_TRACE_ID("Pinpoint-TraceID"    ,
	HTTP_SPAN_ID("Pinpoint-Span    D"),
	HTTP_PARENT_SPAN_ID("Pinpoint-pS    anID"),
	HTTP_SAMPLED("Pinpoint    Sampled"),
	HTTP_FLAGS("Pin    oint-Flags"),
	HTTP_PARENT_APPLICATION_NAME("Pin    oint-pAppName"),
	HTTP_PARENT_APPLICATION_TYPE("P    npoint-pAppType");
    	private String nam       ;

	Header(St        ng name) {
		this.name =       name;
	}        	public String toString() {
		return name;
	}

	private static     inal Map<String, Header> NAME_SET = createMap(       ;

	private static Map<Stri       g, Header> createMap() {
		Header[] headerList = val       es();
		Map<String, Header> ma           = new HashMap<String,              eader        );
		for (Header header : headerList) {
			       ap.put(header.n          me, he             der);
		}
		return map;
	}

	pu          lic st             tic Header getHeader    St    ing name) {
		if (name == null) {
			return        ull;
		}
		if (!startWithPi        ointHeader(name)) {
			return null;
		}
		return        AME_SET.get(nam          );
	}


	public static boolean hasHead       r(String name) {
          	retur              getHeader(name        != null;
	}

	public stat         Enumeration getHeaders(String name) {
		if (name == null) {
			return null;       		}
		final Header header = getHeader(name);
		i        (header == null) {
			return null;
		}
		// if pinpoint header
		return new EmptyEn       merat       on();
	}

	public static Enum          ration filteredHeade             Names(final Enumerati                   n e             umeration) {
		return new DelegateEnumeration(enumeration,        ILTER);
	}

	private static Dele    ateEnumeration.Filter FILTER = new DelegateEnumeration.Filter() {
		@Override
		public boolean filter(Object o) {
			if (o instanceof String) {
				return hasHeader((String )o);
			}
			return false;
		}
	};

	private static boolean startWithPinpointHeader(String name) {
		return name.startsWith("Pinpoint-");
	}
}
