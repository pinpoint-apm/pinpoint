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

package com.navercorp.pinpoint.bootstrap.plugin.request.util;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultNameSpaceChecker<T> implements NameSpaceChecker<T> {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final RequestAdaptor<T> requestAdaptor;
    private final String applicationNamespace;


    public DefaultNameSpaceChecker(RequestAdaptor<T> requestAdaptor, String applicationNamespace) {
        this.requestAdaptor = Assert.requireNonNull(requestAdaptor, "requestAdaptor");

        if (StringUtils.isEmpty(applicationNamespace)) {
            throw new IllegalArgumentException("applicationNamespace must not be empty");
        }
        this.applicationNamespace = applicationNamespace;
    }

    @Override
    public boolean checkNamespace(T request) {

        final String parentApplicationNamespace = requestAdaptor.getHeader(request, Header.HTTP_PARENT_APPLICATION_NAMESPACE.toString());
        // If parentApplicationNamespace is null, it is ignored for backwards compatibility.
        if (parentApplicationNamespace == null) {
            return true;
        }
        if (this.applicationNamespace.equals(parentApplicationNamespace)) {
            // collision.
            return true;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Collision namespace. applicationNamespace={}, parentApplicationNamespace={}", this.applicationNamespace, parentApplicationNamespace);
        }
        return false;
    }
}
