/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.service.map.processor;

import java.util.HashSet;
import java.util.Set;

import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;


public class AcceptApplicationLocalCacheTest {


    @Test
    public void testFind() throws Exception {
        AcceptApplicationLocalCache cache = new AcceptApplicationLocalCache();

        Application tomcat = new Application("Tomcat", ServiceType.STAND_ALONE);
        RpcApplication rpc = new RpcApplication("localhost:8080", tomcat);
        // find the application that accept the rpc request of calling to localhost:8080 at tomcat itself

        Set<AcceptApplication> findSet =  createAcceptApplication();

        cache.put(rpc, findSet);

        // found
        Set<AcceptApplication> acceptApplications = cache.get(rpc);
        Assert.assertEquals(acceptApplications.size(), 1);
        Assert.assertEquals(acceptApplications.iterator().next(), localhost);

        // not found
        Set<AcceptApplication> unknown = cache.get(new RpcApplication("unknown:8080", tomcat));
        Assert.assertTrue(unknown.isEmpty());
        Assert.assertFalse(unknown.iterator().hasNext());

    }

    AcceptApplication localhost = new AcceptApplication("localhost:8080", new Application("LOCALHOST", ServiceType.STAND_ALONE));

    private Set<AcceptApplication> createAcceptApplication() {
        AcceptApplication naver = new AcceptApplication("www.naver.com", new Application("Naver", ServiceType.STAND_ALONE));
        AcceptApplication daum = new AcceptApplication("www.daum.com", new Application("Daum", ServiceType.STAND_ALONE));
        AcceptApplication nate = new AcceptApplication("www.nate.com", new Application("Nate", ServiceType.STAND_ALONE));

        Set<AcceptApplication> result = new HashSet<AcceptApplication>();
        result.add(naver);
        result.add(daum);
        result.add(nate);
        result.add(localhost);

        return result;
    }
}