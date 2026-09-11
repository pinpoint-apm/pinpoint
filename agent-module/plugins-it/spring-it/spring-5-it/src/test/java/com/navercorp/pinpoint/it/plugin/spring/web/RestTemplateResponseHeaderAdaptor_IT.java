/*
 * Copyright 2026 NAVER Corp.
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
 */

package com.navercorp.pinpoint.it.plugin.spring.web;

import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseAdaptor;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.plugin.resttemplate.RestTemplateResponseHeaderAdaptor;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Calls the resttemplate plugin's {@link RestTemplateResponseHeaderAdaptor} directly against a real
 * {@code ClientHttpResponse} of every Spring Framework 5 release in range - no interceptor, no HTTP call.
 *
 * <p>{@code @PluginTest} loads the plugin class through the agent class loader and links its
 * {@code HttpHeaders} calls against the spring-web version of the current case, so a descriptor that a
 * newer Spring release dropped (issue #14276: {@code containsKey}/{@code get(Object)}/{@code keySet}
 * compiled against Spring 5.3) fails here as the same {@code NoSuchMethodError} the interceptor would hit,
 * with the offending method and version in the report.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.springframework:spring-web:[5.0.0.RELEASE,5.max]",
        // same version as spring-web: MockClientHttpResponse
        "org.springframework:spring-test"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-resttemplate-plugin"})
public class RestTemplateResponseHeaderAdaptor_IT {

    private final ResponseAdaptor<ClientHttpResponse> adaptor = new RestTemplateResponseHeaderAdaptor();

    @Test
    public void readsHeadersOfEveryShape() {
        ClientHttpResponse response = response();
        response.getHeaders().add("X-Test-Echo", "echo");
        response.getHeaders().add("X-Test-Multi", "one");
        response.getHeaders().add("X-Test-Multi", "two");

        Assertions.assertTrue(adaptor.containsHeader(response, "X-Test-Echo"));
        Assertions.assertTrue(adaptor.containsHeader(response, "x-test-echo"), "header names are case-insensitive");
        Assertions.assertFalse(adaptor.containsHeader(response, "X-Missing"));

        Assertions.assertEquals("echo", adaptor.getHeader(response, "X-Test-Echo"));
        Assertions.assertEquals("one", adaptor.getHeader(response, "X-Test-Multi"), "first value wins");
        Assertions.assertNull(adaptor.getHeader(response, "X-Missing"));

        Assertions.assertEquals(Arrays.asList("one", "two"), new ArrayList<>(adaptor.getHeaders(response, "X-Test-Multi")));
        Assertions.assertEquals(Arrays.asList("one", "two"), new ArrayList<>(adaptor.getHeaders(response, "x-test-multi")));
        Assertions.assertTrue(adaptor.getHeaders(response, "X-Missing").isEmpty());

        Collection<String> names = adaptor.getHeaderNames(response);
        Assertions.assertEquals(2, names.size(), "names: " + names);
        Assertions.assertTrue(containsIgnoreCase(names, "X-Test-Echo"), "names: " + names);
        Assertions.assertTrue(containsIgnoreCase(names, "X-Test-Multi"), "names: " + names);
    }

    @Test
    public void setReplaces_addAppends() {
        ClientHttpResponse response = response();

        adaptor.setHeader(response, "X-Set", "first");
        adaptor.setHeader(response, "X-Set", "second");
        Assertions.assertEquals(Arrays.asList("second"), new ArrayList<>(adaptor.getHeaders(response, "X-Set")));

        adaptor.addHeader(response, "X-Add", "first");
        adaptor.addHeader(response, "X-Add", "second");
        Assertions.assertEquals(Arrays.asList("first", "second"), new ArrayList<>(adaptor.getHeaders(response, "X-Add")));
        Assertions.assertEquals("first", adaptor.getHeader(response, "X-Add"));
    }

    private static ClientHttpResponse response() {
        // HttpStatus.OK: (byte[], HttpStatus) on 5.x and (byte[], HttpStatusCode) on 6.x/7.x - each IT module compiles
        // against its own Spring line, so the descriptor matches every version in range. The (byte[], int) overload
        // only exists from spring-test 5.3.17.
        return new MockClientHttpResponse("body".getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
    }

    private static boolean containsIgnoreCase(Collection<String> names, String expected) {
        for (String name : names) {
            if (expected.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
