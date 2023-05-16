/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.elasticsearch8.interceptor;

import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.elasticsearch8.accessor.EndPointAccessor;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;

import java.util.ArrayList;
import java.util.List;

public class ElasticsearchClientConstructorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public ElasticsearchClientConstructorInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logBeforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logAfterInterceptor(target, args, result, throwable);
        }

        if (args == null) {
            return;
        }

        getEndPoint(args, target);
    }

    private void getEndPoint(Object[] args, Object target) {
        final RestClientTransport restClientTransport = ArrayArgumentUtils.getArgument(args, 0, RestClientTransport.class);
        final List<String> hostList = getHostList(restClientTransport.restClient());

        if (target instanceof EndPointAccessor) {
            if (((EndPointAccessor) target)._$PINPOINT$_getEndPoint() == null) {
                ((EndPointAccessor) target)._$PINPOINT$_setEndPoint(merge(hostList));
            }
        }
    }

    private List<String> getHostList(RestClient restClient) {
        final List<String> hostList = new ArrayList<>();

        for (Node node : restClient.getNodes()) {
            final String hostAddress = HostAndPort.toHostAndPortString(node.getHost().getHostName(), node.getHost().getPort());
            hostList.add(hostAddress);
        }

        return hostList;
    }

    private String merge(List<String> host) {
        if (host.isEmpty()) {
            return "";
        }
        String single = host.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(single);
        for (int i = 1; i < host.size(); i++) {
            sb.append(',');
            sb.append(host.get(i));
        }
        return sb.toString();
    }

    private void logBeforeInterceptor(Object target, Object[] args) {
        logger.beforeInterceptor(target, args);
    }

    private void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, args, result, throwable);
    }

}
