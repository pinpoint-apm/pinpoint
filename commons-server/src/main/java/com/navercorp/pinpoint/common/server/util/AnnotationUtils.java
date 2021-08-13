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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author emeroad
 */
public final class AnnotationUtils {
    private AnnotationUtils() {
    }

    public static String findApiAnnotation(List<AnnotationBo> list) {
        return findAnnotationValue(list, AnnotationKey.API, String.class);
    }
    
    public static String findApiTagAnnotation(List<AnnotationBo> list) {
        return findAnnotationValue(list, AnnotationKey.API_TAG, String.class);
    }

    private static <T> T findAnnotationValue(List<AnnotationBo> list, AnnotationKey annotationKey, Class<T> type) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        final AnnotationBo annotationBo = findAnnotationBo(list, annotationKey);
        if (annotationBo != null) {
            final Object value = annotationBo.getValue();
            if (type.isInstance(value)) {
                return type.cast(value);
            }
        }
        return null;
    }


    public static AnnotationBo findAnnotationBo(List<AnnotationBo> annotationBoList, AnnotationKey annotationKey) {
        for (AnnotationBo annotation : annotationBoList) {
            final int key = annotation.getKey();
            if (annotationKey.getCode() == key) {
                return annotation;
            }
        }
        return null;
    }

}
