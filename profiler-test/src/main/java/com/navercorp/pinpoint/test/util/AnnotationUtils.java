/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.util;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.profiler.context.Annotation;

import java.util.List;

public final class AnnotationUtils {
    private AnnotationUtils() {
    }

    public static String toString(AnnotationKey key, ExpectedAnnotation anno) {
        return String.format("%s(%s)=%s", anno.getKeyName(), key.getCode(), anno.getValue());
    }

    public static String toString(Annotation<?> ann) {
        return ann.getKey() + "=" + ann.getValue();
    }


    public static void appendAnnotations(StringBuilder builder, List<Annotation<?>> annotations) {
        boolean first = true;

        if (annotations != null) {
            for (Annotation<?> annotation : annotations) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }

                builder.append(AnnotationUtils.toString(annotation));
            }
        }
    }
}
