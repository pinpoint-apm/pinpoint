/*
 * Copyright 2019 NAVER Corp.
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

/**
 * Represents the hbase schema status.
 *
 * @author HyunGil Jeong
 */
public enum HbaseSchemaStatus {
    /**
     * Hbase schema does not exist.
     */
    NONE,
    /**
     * Hbase schema is valid, but is not up to date.
     */
    VALID_OUT_OF_DATE,
    /**
     * Hbase schema is valid and up to date.
     */
    VALID,
    /**
     * Hbase schema exists, but is not valid.
     */
    INVALID
}
