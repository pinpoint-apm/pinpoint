/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.config;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LoggingEvent implements AnnotationVisitor.FieldVisitor {

    private final Logger logger;

    public LoggingEvent(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void visit(Field field, Object value) {
        if (logger.isDebugEnabled()) {
            final Value annotation = field.getAnnotation(Value.class);
            logger.debug("{} {} @Value(\"{}\") = {}", field.getType().getSimpleName(), field.getName(), annotation.value(), value);
        }
    }
}

