/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Predicate;

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
        final AnnotationBo annotationBo = findAnnotation(list, annotationKey.getCode());
        if (annotationBo != null) {
            final Object value = annotationBo.getValue();
            if (type.isInstance(value)) {
                return type.cast(value);
            }
        }
        return null;
    }


    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static AnnotationBo findAnnotation(List<AnnotationBo> list, int key) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        if (list instanceof RandomAccess) {
            for (int i = 0; i < list.size(); i++) {
                final AnnotationBo annotationBo = list.get(i);
                if (key == annotationBo.getKey()) {
                    return annotationBo;
                }
            }
            return null;
        } else {
            for (AnnotationBo annotationBo : list) {
                if (key == annotationBo.getKey()) {
                    return annotationBo;
                }
            }
            return null;
        }
    }

    /**
     * @deprecated use {@link #findAnnotation(List, AnnotationKey)}
     */
    @Deprecated
    public static AnnotationBo findAnnotationBo(List<AnnotationBo> list, AnnotationKey annotationKey) {
        return findAnnotation(list, annotationKey.getCode());
    }

    public static AnnotationBo findAnnotation(List<AnnotationBo> list, AnnotationKey annotationKey) {
        return findAnnotation(list, annotationKey.getCode());
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static List<AnnotationBo> findAnnotations(List<AnnotationBo> list, Predicate<AnnotationBo> annotationPredicate) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<AnnotationBo> annotationList = null;
        if (list instanceof RandomAccess) {
            for (int i = 0; i < list.size(); i++) {
                final AnnotationBo annotationBo = list.get(i);
                if (annotationPredicate.test(annotationBo)) {
                    if (annotationList == null) {
                        annotationList = new ArrayList<>(2);
                    }
                    annotationList.add(annotationBo);
                }
            }
        } else {
            for (AnnotationBo annotationBo : list) {
                if (annotationPredicate.test(annotationBo)) {
                    if (annotationList == null) {
                        annotationList = new ArrayList<>(2);
                    }
                    annotationList.add(annotationBo);
                }
            }
        }
        if (annotationList != null) {
            return annotationList;
        }
        return Collections.emptyList();
    }


}
