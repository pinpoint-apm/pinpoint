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

package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultLinkFilter implements LinkFilter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Application outApplication;
    private final Application inApplication;

    public DefaultLinkFilter(Application outApplication, Application inApplication) {
        this.outApplication = Objects.requireNonNull(outApplication, "outApplication");
        this.inApplication = Objects.requireNonNull(inApplication, "inApplication");
    }

    public boolean filter(Application foundApplication) {
        Objects.requireNonNull(foundApplication, "foundApplication");

        if (this.inApplication.serviceType().isWas() && this.outApplication.serviceType().isWas()) {
            logger.debug("check was to was.");
            // if not from same source, drop
            if (!this.outApplication.equals(foundApplication)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("  DROP THE ROW,1, DIFFERENT SRC. fetched={} , params={}", foundApplication, inApplication);
                }
                return true;
            }
        } else if (this.outApplication.serviceType().isUser()) {
            logger.debug("check client to was");
            // if dest not equals to that WAS, drop
            if (!this.inApplication.name().equals(foundApplication.name())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("  DROP THE ROW,2, DIFFERENT DEST. fetched={}, params={}", foundApplication, this.inApplication);
                }
                return true;
            }
        } else {
            logger.debug("check any to any.");
            if (this.inApplication.serviceType().isUnknown()) {
                //  compare just only application name when dest is unknown.
                // TODO need more nice way to compare
                if (!this.inApplication.name().equals(foundApplication.name())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("  DROP THE ROW,3, DIFFERENT DEST. fetched={}, params={}", foundApplication, inApplication);
                    }
                    return true;
                }
            } else {
                // compare all of application name and type when dest is not unknown.
                if (!this.inApplication.equals(foundApplication)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("  DROP THE ROW,4, DIFFERENT DEST. fetched={}, params={}", foundApplication, this.inApplication);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
