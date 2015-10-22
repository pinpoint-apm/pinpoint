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

package com.navercorp.pinpoint.common.trace;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;

import java.util.*;

/**
 * @author emeroad
 */
public class AnnotationKeyRegistry {

    private final IntHashMap<AnnotationKey> codeLookupTable;

    private final HashMap<String, AnnotationKey> nameLookupTable;

    private final IntHashMap<AnnotationKey> apiErrorLookupTable;

    public AnnotationKeyRegistry(HashMap<Integer, AnnotationKey> buildMap) {
        if (buildMap == null) {
            throw new NullPointerException("buildMap must not be null");
        }
        this.codeLookupTable = IntHashMapUtils.copy(buildMap);
        this.nameLookupTable = buildNameTable(buildMap.values());
        this.apiErrorLookupTable = buildApiMetaDataError(buildMap.values());
    }

    private HashMap<String, AnnotationKey> buildNameTable(Collection<AnnotationKey> buildMap) {

        final HashMap<String, AnnotationKey> nameLookupTable = new HashMap<String, AnnotationKey>();

        for (AnnotationKey annotationKey : buildMap) {
            final AnnotationKey exist = nameLookupTable.put(annotationKey.getName(), annotationKey);
            if (exist != null) {
                throwDuplicatedAnnotationKey(annotationKey, exist);
            }
        }

        return nameLookupTable;
    }

    private static void throwDuplicatedAnnotationKey(AnnotationKey annotationKey, AnnotationKey exist) {
        throw new IllegalStateException("already exist. annotationKey:" + annotationKey + ", exist:" + exist);
    }


    private IntHashMap<AnnotationKey> buildApiMetaDataError(Collection<AnnotationKey> buildMap) {
        final IntHashMap<AnnotationKey> table = new IntHashMap<AnnotationKey>();

        for (AnnotationKey annotationKey : buildMap) {
            if (annotationKey.isErrorApiMetadata()) {
                table.put(annotationKey.getCode(), annotationKey);
            }
        }
        return table;
    }


    public AnnotationKey findAnnotationKey(int code) {
        final AnnotationKey annotationKey = codeLookupTable.get(code);
        if (annotationKey == null) {
            return AnnotationKey.UNKNOWN;
        }
        return annotationKey;
    }

    public AnnotationKey findAnnotationKeyByName(String keyName) {
        final AnnotationKey annotationKey = nameLookupTable.get(keyName);
        if (annotationKey == null) {
            throw new NoSuchElementException(keyName);
        }

        return annotationKey;
    }

    public AnnotationKey findApiErrorCode(int annotationCode) {
        return this.apiErrorLookupTable.get(annotationCode);
    }

    public static class Builder {

        private final HashMap<Integer, AnnotationKey> buildMap = new HashMap<Integer, AnnotationKey>();

        public void addAnnotationKey(AnnotationKey annotationKey) {
            if (annotationKey == null) {
                throw new NullPointerException("annotationKey must not be null");
            }
            int code = annotationKey.getCode();
            final AnnotationKey exist = this.buildMap.put(code, annotationKey);
            if (exist != null) {
                throwDuplicatedAnnotationKey(annotationKey, exist);
            }
        }


        public AnnotationKeyRegistry build() {
            return new AnnotationKeyRegistry(buildMap);
        }
    }

}
