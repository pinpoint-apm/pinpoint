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

package com.navercorp.pinpoint.common.server.bo;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AnnotationFactory<T> {

    private final AnnotationTypeHandler<T> typeHandler;

    public AnnotationFactory(AnnotationTypeHandler<T> typeHandler) {
        this.typeHandler = Objects.requireNonNull(typeHandler, "typeHandler");
    }

    public AnnotationBo buildAnnotation(T annotation) {
        Objects.requireNonNull(annotation, "annotation");
        int annotationkey = typeHandler.getKey(annotation);
        Object annotationValue = typeHandler.getValue(annotation);
        Object commonType = buildAnnotationValue(annotationValue);
        return new AnnotationBo(annotationkey, commonType);
    }

    public Object buildAnnotationValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return value;
        } else if (value instanceof Long) {
            return value;
        } else if (value instanceof Integer) {
            return value;
        } else if (value instanceof Boolean) {
            return value;
        } else if (value instanceof Byte) {
            return value;
        } else if (value instanceof Short) {
            return value;
        } else if (value instanceof Float) {
            // not supported by thrift
            return value;
        } else if (value instanceof Double) {
            return value;
        } else if (value instanceof byte[]) {
            return value;
        }

        // custom type
        final Object custom = typeHandler.buildCustomAnnotationValue(value);
        if (custom != null) {
            return custom;
        }

        return value.toString();
    }

    public interface AnnotationTypeHandler<T> {
        int getKey(T annotation);

        Object getValue(T annotation);

        Object buildCustomAnnotationValue(Object annotationValue);
    }

}
