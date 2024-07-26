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
package com.navercorp.pinpoint.plugin.log4j2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.plugin.log4j2.Log4j2Config;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.List;

/**
 * @author yjqg6666
 */
public class PatternLayoutInterceptor implements AroundInterceptor1 {

    private static final String PATTERN_TRANSACTION_ID = "%X{PtxId}";

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean debug = logger.isDebugEnabled();

    private final Log4j2Config config;

    public PatternLayoutInterceptor(TraceContext traceContext) {
        this.config = new Log4j2Config(traceContext.getProfilerConfig());
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
        if (config.isPatternFullReplace()) {
            updatePattern(target, oldPattern, config.getPatternFullReplaceWith());
            return;
        }
        if (oldPattern.contains(PATTERN_TRANSACTION_ID)) {
            if (debug) {
                logger.debug("Log4j2 pattern already have pinpoint pattern, pattern:{}", oldPattern);
            }
            return;
        }
        updatePattern(target, oldPattern, config.getPatternReplaceSearchList(), config.getPatternReplaceWith());
    }

    private void updatePattern(Object target, String old, String replace) {
        if (replace.contentEquals(old)) {
            return;
        }
        if (updatePattern(target, replace)) {
            logger.info("Log4j pattern fully-replaced, old pattern({}) and new pattern({}).", old, replace);
        }
    }

    private void updatePattern(Object target, String oldPattern, List<String> searchList, String replace) {
        String newPattern = oldPattern;
        boolean changed = false;
        for (String search : searchList) {
            newPattern = oldPattern.replace(search, replace);
            if (!oldPattern.contentEquals(newPattern)) {
                changed = true;
                break;
            }
        }
        if (changed) {
            if(updatePattern(target, newPattern)) {
                logger.info("Log4j2 pattern replaced, old pattern({}) and new pattern({})", oldPattern, newPattern);
            }
        }
    }

    private boolean updatePattern(Object target, String pattern) {
        if (!(target instanceof PatternLayout.SerializerBuilder)) {
            return false;
        }
        PatternLayout.SerializerBuilder builder = (PatternLayout.SerializerBuilder) target;
        builder.setPattern(pattern);
        return true;
    }

}
