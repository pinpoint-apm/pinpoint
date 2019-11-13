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

import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AnnotationVisitor<T extends Annotation> {


    private final Class<T> targetAnnotation;

    public AnnotationVisitor(Class<T> targetAnnotation) {
        this.targetAnnotation = Objects.requireNonNull(targetAnnotation, "targetAnnotation");
    }

//    public void visit(Class<?> clazz, Object object, FieldVisitor fieldVisitor) {
//        // spring cglib
//    }

    public void visit(Object object, FieldVisitor fieldVisitor) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(fieldVisitor, "fieldVisitor");

        final Class<?> clazz = getClazz(object);
        final Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            final boolean annotation = findAnnotation(field);
            if (!annotation) {
                continue;
            }
            final Object fieldValue = getFiledValue(object, field);
            fieldVisitor.visit(field, fieldValue);
        }
    }

    private boolean findAnnotation(Field field) {
        final Annotation annotation = field.getAnnotation(targetAnnotation);
        return annotation != null;
    }

    protected Class<?> getClazz(Object object) {
        // spring cglib workaround
        return ClassUtils.getUserClass(object);
    }


    private Object getFiledValue(Object object, Field field) {
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, object);
    }

    public interface FieldVisitor {
        void visit(Field field, Object value);
    }

}
