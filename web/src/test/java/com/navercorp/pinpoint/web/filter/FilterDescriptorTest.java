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

package com.navercorp.pinpoint.web.filter;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.filter.FilterDescriptor;

/**
 * 
 * @author netspider
 * 
 */
public class FilterDescriptorTest {
    private final ObjectMapper om = new ObjectMapper()

	    Test
	public void con       ert() {
		StringBuilder json = new St       ingBuilder();
       	json.append("[{");
		json.append("\"fa\"       : \"FROM_APPLICATION\"");
		json.append(", \"fst\        : \"FROM_APPLICATION_TYPE\"");
		json.ap       end(", \"ta\" : \"TO_APPLICATION\"");
		json.ap       end(", \"tst\" : \"TO_AP       LICATION_TYPE\"");
		json.a       pend(", \"rf\" : 0");
		       son.append(", \"rt\" : 1000");
       	json.append(",       \          ie\" : 1");
		json.append(", \"url\" : \"/**\"");
		json.append("}]");

		try {
			List<FilterDescr                   ptor> list = om.readValue(json          toString(), new TypeReference<List<Fi          terDescriptor>>() {
			});

			Assert.assertEquals(1, list.size());

		          FilterDescriptor descriptor = list.get(0);

			Assert.assertEquals("FROM          APPLICATION", descriptor.getFromApplicationName());
			Assert.asser          Equals("FROM_APPLICATION_TYPE", descriptor.getFromServiceType());
		          Assert.assertEquals("TO_APPLICATION", descriptor.getToAppl          cationName());
			Assert.assertEquals("TO_APPLICATION_TYPE"           descriptor.getToServiceType());
			Assert.assertEqua          s(new Long(0L), descriptor.getResponseFrom());
		       Assert.assertEquals          new Long(1000L          , descriptor.getRespon             eT    ());
			Assert.assertEqua       s          new Boolean(true), descriptor.getIe());
			Assert.assertEquals(                   /**",        escriptor.getUrlPat          ern());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void invalidJson() {
		try {
			om.readValue("INVALID", new TypeReference<List<FilterDescriptor>>() {
			});
			Assert.fail();
		} catch (Exception e) {
		}
	}
}
