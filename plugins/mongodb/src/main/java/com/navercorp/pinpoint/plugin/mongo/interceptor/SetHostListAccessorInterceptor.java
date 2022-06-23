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

package com.navercorp.pinpoint.plugin.mongo.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.mongo.HostListAccessor;

import java.util.List;
import java.util.Objects;

public class SetHostListAccessorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public SetHostListAccessorInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        Objects.requireNonNull(traceContext, "traceContext");
        Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (throwable != null) {
            // If an error occurs, it is ignored.
            return;
        }

        if (Boolean.FALSE == (target instanceof HostListAccessor)) {
            logger.info("Unexpected target. The target is not a HostListAccessor implementation. target={}", target);
            return;
        }

        final List<String> hostList = ((HostListAccessor) target)._$PINPOINT$_getHostList();
        if (hostList == null) {
            logger.info("Invalid hostList. target={}", target);
            return;
        }

        if (Boolean.FALSE == (result instanceof HostListAccessor)) {
            logger.info("Unexpected result. The result is not a HostListAccessor implementation. result={}", result);
            return;
        }

        ((HostListAccessor) result)._$PINPOINT$_setHostList(hostList);
        if (isDebug) {
            logger.debug("Set hostList hostList={}", hostList);
        }
    }
}
