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

package com.navercorp.pinpoint.profiler.modifier.arcus.interceptor;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.ApiInterceptor;
import com.navercorp.pinpoint.test.BaseInterceptorTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiInterceptorTest extends BaseInterceptorTest {

    static final Logger logger = LoggerFactory.getLogger(ApiInterceptorTest.class);

    ApiInterceptor interceptor = new ApiInterceptor();
    MemcachedClient client = mock(MockMemcachedClient.class);

    @Before
    public void beforeEach() {
        setInterceptor(interceptor);
        MethodDescriptor methodDescriptor = new DefaultMethodDescriptor(
                MockMemcachedClient.class.getName(), "set", new String[] {
                        "java.lang.String", "int", "java.lang.Object" },
                new String[] { "key", "exptime", "value" });
        /* FIXME NPE. Skip for now.
        setMethodDescriptor(methodDescriptor);
        */
        super.beforeEach();
    }

    @Test
    public void testAround() {
        Object[] args = new Object[] {"key", 10, "my_value"};
        interceptor.before(client, args);
        interceptor.after(client, args, null, null);
    }

    /**
     * Fake MemcachedClient
     */
    class MockMemcachedClient extends MemcachedClient {
        public MockMemcachedClient(ConnectionFactory cf,
                List<InetSocketAddress> addrs) throws IOException {
            super(cf, addrs);
        }

        public String __getServiceCode() {
            return "MEMCACHED";
        }
    }

}