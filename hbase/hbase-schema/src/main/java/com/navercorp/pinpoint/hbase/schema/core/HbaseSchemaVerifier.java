/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.core;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public interface HbaseSchemaVerifier<T> {

    /**
     * Returns {@code true} if the schema definitions specified by {@code expectedSchemas} matches those
     * specified by {@code actualSchemas}.
     *
     * @param expectedSchemas expected schema definitions
     * @param actualSchemas actual schema definitions
     * @return {@code true} if the actual schema matches the expected schema
     */
    boolean verifySchemas(List<T> expectedSchemas, List<T> actualSchemas);
}
