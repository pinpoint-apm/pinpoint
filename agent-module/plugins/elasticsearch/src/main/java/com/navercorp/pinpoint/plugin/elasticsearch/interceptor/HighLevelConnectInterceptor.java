/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.elasticsearch.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.EndPointAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.HttpHostInfoAccessor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Roy Kim
 */
public class HighLevelConnectInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public HighLevelConnectInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (args == null || args.length == 0) {
            return;
        }

        try {
            getEndPoint(args, target);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private void getEndPoint(Object[] args, Object target) {
        final List<String> hostList = getHostList(args[0]);

        if (target instanceof EndPointAccessor) {
            if (((EndPointAccessor) target)._$PINPOINT$_getEndPoint() == null) {
                ((EndPointAccessor) target)._$PINPOINT$_setEndPoint(merge(hostList));
            }
        }
    }

    private String merge(List<String> host) {
        if (host.isEmpty()) {
            return "";
        }
        return String.join(",", host);
    }

    private List<String> getHostList(Object arg) {
        if (!(arg instanceof RestClient)) {
            return Collections.emptyList();
        }

        final List<String> hostList = new ArrayList<>();

        HttpHost[] httpHosts = null;
        if (arg instanceof HttpHostInfoAccessor) {
            httpHosts = ((HttpHostInfoAccessor) arg)._$PINPOINT$_getHttpHostInfo();
        }

        //v6.4 ~
        if (httpHosts == null) {
            for (Node node : ((RestClient) arg).getNodes()) {
                final String hostAddress = HostAndPort.toHostAndPortString(node.getHost().getHostName(), node.getHost().getPort());
                hostList.add(hostAddress);
            }
        } else {
            //v6.0 ~ 6.3
            for (HttpHost httpHost : httpHosts) {
                final String hostAddress = HostAndPort.toHostAndPortString(httpHost.getHostName(), httpHost.getPort());
                hostList.add(hostAddress);
            }
        }

        return hostList;
    }
}
