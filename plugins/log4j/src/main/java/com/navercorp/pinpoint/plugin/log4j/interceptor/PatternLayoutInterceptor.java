/*
 * Copyright 2021 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.log4j.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.log4j.Log4jConfig;
import org.apache.log4j.PatternLayout;

import java.util.List;

/**
 * @author yjqg6666
 */
public class PatternLayoutInterceptor implements AroundInterceptor1 {

    private static final String PATTERN_TRANSACTION_ID = "%X{PtxId}";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean debug = logger.isDebugEnabled();

    private final Log4jConfig config;

    public PatternLayoutInterceptor(TraceContext traceContext) {
        this.config = new Log4jConfig(traceContext.getProfilerConfig());
    }

    @Override
    public void before(Object target, Object arg0) {
    }

    @Override
    public void after(Object target, Object arg0, Object result, Throwable throwable) {
        if (!(arg0 instanceof String)) {
            return;
        }
        String oldPattern = (String) arg0;
        if (oldPattern.contains(PATTERN_TRANSACTION_ID)) {
            if (debug) {
                logger.debug("Log4j pattern already have pinpoint pattern, pattern:" + oldPattern);
            }
            return;
        }
        updatePattern(target, oldPattern, config.getPatternReplaceSearchList(), config.getPatternReplaceWith());
    }

    private void updatePattern(Object target, String oldPattern, List<String> searchList, String replace) {
        if (!(target instanceof PatternLayout)) {
            return;
        }
        String newPattern = oldPattern;
        boolean changed = false;
        for (String search : searchList) {
            newPattern = oldPattern.replace(search, replace);
            if (!oldPattern.contentEquals(newPattern)) {
                changed = true;
                if (debug) {
                    logger.debug("Log4j pattern replaced, old pattern(" + oldPattern + ") and new pattern(" +
                            newPattern + ").");
                }
                break;
            }
        }
        if (changed) {
            final PatternLayout layout = (PatternLayout) target;
            layout.setConversionPattern(newPattern);
        }
    }

}
