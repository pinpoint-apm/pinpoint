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

package com.navercorp.pinpoint.plugin.tomcat.aspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.plugin.tomcat.aspect.RequestFacadeAspect;

/**
 * @author jaehong.kim
 */
public class RequestFacadeAspectTest {
    public static class RequestFacadeAspectMock extends RequestFacadeAspect {
        @Override
        Enumeration __getHeaderNames() {
            Hashtable<String, String> hashtable = new Hashtable<String, String>();
            hashtable.put("b", "bb");
            hashtable.put("c", "cc");
            hashtable.put("d", Header.HTTP_SPAN_ID.toString());
            return hashtable.elements();
        }
    }

    @Test
    public void getHeaderNames() {
        RequestFacadeAspect mock = new RequestFacadeAspectMock();
        Enumeration isNull = mock.getHeaderNames();

        ArrayList list = Collections.list(isNull);
        Assert.assertEquals(list.size(), 2);
    }

}