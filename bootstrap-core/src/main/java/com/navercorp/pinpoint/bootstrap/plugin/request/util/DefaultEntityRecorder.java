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

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultEntityRecorder<T> implements EntityRecorder<T> {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpDumpConfig httpDumpConfig;
    private final EntityExtractor<T> entityExtractor;

    public DefaultEntityRecorder(HttpDumpConfig httpDumpConfig, EntityExtractor<T> entityExtractor) {
        this.httpDumpConfig = Assert.requireNonNull(httpDumpConfig, "httpDumpConfig");
        this.entityExtractor = Assert.requireNonNull(entityExtractor, "entityExtractor");
    }

    @Override
    public void record(SpanEventRecorder recorder, T entity, Throwable throwable) {
        if (DumpType.ALWAYS == this.httpDumpConfig.getEntityDumpType()) {
            recordEntity(recorder, entity);
        } else if (DumpType.EXCEPTION == this.httpDumpConfig.getEntityDumpType() && InterceptorUtils.isThrowable(throwable)) {
            recordEntity(recorder, entity);
        }
    }

    private void recordEntity(final SpanEventRecorder recorder, final T entity) {
        if (this.httpDumpConfig.getEntitySampler().isSampling()) {
            final String entityValue = entityExtractor.getEntity(entity);
            if (entityValue != null) {
                final int entityDumpSize = this.httpDumpConfig.getEntityDumpSize();
                final String entityString = StringUtils.abbreviate(entityValue, entityDumpSize);
                recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, entityString);
                if (isDebug) {
                    logger.debug("Record entity={}", entityValue);
                }
            }
        }
    }
}
