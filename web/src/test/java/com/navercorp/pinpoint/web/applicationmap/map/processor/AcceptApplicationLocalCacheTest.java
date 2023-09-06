/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class AcceptApplicationLocalCacheTest {

    AcceptApplication localhost = new AcceptApplication("localhost:8080", new Application("LOCALHOST", ServiceType.STAND_ALONE));

    @Test
    public void testFind() {
        AcceptApplicationLocalCache cache = new AcceptApplicationLocalCache();

        Application tomcat = new Application("Tomcat", ServiceType.STAND_ALONE);
        RpcApplication rpc = new RpcApplication("localhost:8080", tomcat);
        // find the application that accept the rpc request of calling to localhost:8080 at tomcat itself

        Set<AcceptApplication> findSet =  createAcceptApplication();

        cache.put(rpc, findSet);

        // found
        Set<AcceptApplication> acceptApplications = cache.get(rpc);
        assertThat(acceptApplications).hasSize(1);
        Assertions.assertEquals(acceptApplications.iterator().next(), localhost);

        // not found
        Set<AcceptApplication> unknown = cache.get(new RpcApplication("unknown:8080", tomcat));
        assertThat(unknown).isEmpty();
        Assertions.assertFalse(unknown.iterator().hasNext());

    }


    private Set<AcceptApplication> createAcceptApplication() {
        AcceptApplication naver = new AcceptApplication("www.naver.com", new Application("Naver", ServiceType.STAND_ALONE));
        AcceptApplication daum = new AcceptApplication("www.daum.com", new Application("Daum", ServiceType.STAND_ALONE));
        AcceptApplication nate = new AcceptApplication("www.nate.com", new Application("Nate", ServiceType.STAND_ALONE));

        return Set.of(naver, daum, nate, localhost);
    }
}