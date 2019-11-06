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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.web.vo.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultLinkFilter implements LinkFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application callerApplication;
    private final Application calleeApplication;

    public DefaultLinkFilter(Application callerApplication, Application calleeApplication) {
        this.callerApplication = Objects.requireNonNull(callerApplication, "callerApplication");
        this.calleeApplication = Objects.requireNonNull(calleeApplication, "calleeApplication");
    }

    public boolean filter(Application foundApplication) {
        if (foundApplication == null) {
            throw new NullPointerException("foundApplication");
        }
        if (this.calleeApplication.getServiceType().isWas() && this.callerApplication.getServiceType().isWas()) {
            logger.debug("check was to was.");
            // if not from same source, drop
            if (!this.callerApplication.equals(foundApplication)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("  DROP THE ROW,1, DIFFERENT SRC. fetched={} , params={}", foundApplication, calleeApplication);
                }
                return true;
            }
        } else if (this.callerApplication.getServiceType().isUser()) {
            logger.debug("check client to was");
            // if dest not equals to that WAS, drop
            if (!this.calleeApplication.getName().equals(foundApplication.getName())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("  DROP THE ROW,2, DIFFERENT DEST. fetched={}, params={}", foundApplication, this.calleeApplication);
                }
                return true;
            }
        } else {
            logger.debug("check any to any.");
            if (this.calleeApplication.getServiceType().isUnknown()) {
                //  compare just only application name when dest is unknown.
                // TODO need more nice way to compare
                if (!this.calleeApplication.getName().equals(foundApplication.getName())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("  DROP THE ROW,3, DIFFERENT DEST. fetched={}, params={}", foundApplication, calleeApplication);
                    }
                    return true;
                }
            } else {
                // compare all of application name and type when dest is not unknown.
                if (!this.calleeApplication.equals(foundApplication)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("  DROP THE ROW,4, DIFFERENT DEST. fetched={}, params={}", foundApplication, this.calleeApplication);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
