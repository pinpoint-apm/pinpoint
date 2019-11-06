/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.common.servlet.util;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServletArgumentValidator implements ArgumentValidator {
    private final PLogger logger;

    private final int requestIndex;
    private final Class<?> requestClass;

    private final int responseIndex;
    private final Class<?> responseClass;

    private final int minArgsSize;

    public ServletArgumentValidator(PLogger logger, int requestIndex, Class<?> requestClass, int responseIndex, Class<?> responseClass) {
        this(logger, requestIndex, requestClass, responseIndex, responseClass, defaultArgumentMin(requestIndex, responseIndex));
    }

    public ServletArgumentValidator(PLogger logger, int requestIndex, Class<?> requestClass, int responseIndex, Class<?> responseClass, int minArgsSize) {
        this.logger = Assert.requireNonNull(logger, "logger");

        Assert.isTrue(requestIndex >= 0, "requestIndex must be positive");
        this.requestIndex = requestIndex;
        this.requestClass = Assert.requireNonNull(requestClass, "requestClass");

        Assert.isTrue(responseIndex >= 0, "responseIndex must be positive");
        this.responseIndex = responseIndex;
        this.responseClass = Assert.requireNonNull(responseClass, "responseClass");

        Assert.isTrue(requestIndex != responseIndex, "requestIndex==responseIndex");
        this.minArgsSize = minArgsSize;
    }

    private static int defaultArgumentMin(int requestIndex, int responseIndex) {
        return Math.max(requestIndex, responseIndex) + 1;
    }

    @Override
    public boolean validate(Object[] args) {
        if (args == null) {
            return false;
        }
        if (args.length < minArgsSize) {
            return false;
        }

        final Object request = args[requestIndex];
        if (!(requestClass.isInstance(request))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid args[{}] object, Not implemented of {}. args[{}]={}", requestIndex, requestClass, requestIndex, request);
            }
            return false;
        }
        final Object response = args[responseIndex];
        if (!(responseClass.isInstance(response))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid args[{}] object, Not implemented of {}. args[{}]={}.", responseIndex, responseClass, responseIndex, response);
            }
            return false;
        }
        return true;
    }
}
