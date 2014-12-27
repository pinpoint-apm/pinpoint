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

package com.navercorp.pinpoint.common;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;

/**
 * @author netspider
 * @author emeroad
 */
public enum AnnotationKey {

    // because of using variable-length encoding,
    // a small number should be used mainly for data contained in network packets and a big number for internal used code.

//    2147483647
//    -2147483648

//    @Deprecated  // moved apiId to spanEvent and span. dump by int
//    API_DID(10, "API-DID"),
//    @Deprecated  // you should remove static API code. Use only API-DID. dump by int
//    API_ID(11, "API-ID"),
    // used for developing the annotation that dumps api by string. you also consider to remove it later.
    API(12, "API"),
    API_METADATA(13, "API-METADATA"),
    RETURN_DATA(14, "RETRUN_DATA", true),
    
    CAll_URL(15, "CALL_URL"),
    CAll_PARAM(16, "CALL_PARAM", true),
    PROTOCAL(17, "PROTOCAL", true),

    // when you don't know the correct cause of errors.
    ERROR_API_METADATA_ERROR(10000010, "API-METADATA-ERROR"),
    // when agentInfo not found
    ERROR_API_METADATA_AGENT_INFO_NOT_FOUND(10000011, "API-METADATA-AGENT-INFO-NOT-FOUND"),
    // when checksum is not correct even if agentInfo exists
    ERROR_API_METADATA_IDENTIFIER_CHECK_ERROR(10000012, "API-METADATA-IDENTIFIER-CHECK_ERROR"),
    // when  meta data itself not found
    ERROR_API_METADATA_NOT_FOUND(10000013, "API-METADATA-NOT-FOUND"),
    // when the same hashId of meta data exists
    ERROR_API_METADATA_DID_COLLSION(10000014, "API-METADATA-DID-COLLSION"),

    // it's not clear to handle a error code.  so ApiMetaDataError with searching ERROR_API_META_DATA has been used.
    // automatically generated id

    SQL_ID(20, "SQL-ID"),
    SQL(21, "SQL", true),
    SQL_METADATA(22, "SQL-METADATA"),
    SQL_PARAM(23, "SQL-PARAM"),
    SQL_BINDVALUE(24, "SQL-BindValue", true),

    STRING_ID(30, "STRING_ID"),

    // HTTP_URL is replaced by argument. So viewInRecordSet parameter value is not true.
    HTTP_URL(40, "http.url"),
    HTTP_PARAM(41, "http.param", true),
    HTTP_PARAM_ENTITY(42, "http.entity", true),
    HTTP_COOKIE(45, "http.cookie", true),
    HTTP_CALL_RETRY_COUNT(48, "retryCount"),
    // post method parameter of httpclient

    // ARCUS_COMMAND(50, "arcus.command"),
    
    NPC_URL(60, "npc.url"),
    NPC_PARAM(61, "npc.param"),
    NPC_CONNECT_OPTION(62, "npc.connect.options"),

    NIMM_OBJECT_NAME(70, "nimm.objectName"),
    NIMM_METHOD_NAME(71, "nimm.methodName"),
    NIMM_PARAM(72, "nimm.param"),
    NIMM_CONNECT_OPTION(73, "nimm.connect.options"),
    
    ARGS0(-1, "args[0]"),
    ARGS1(-2, "args[1]"),
    ARGS2(-3, "args[2]"),
    ARGS3(-4, "args[3]"),
    ARGS4(-5, "args[4]"),
    ARGS5(-6, "args[5]"),
    ARGS6(-7, "args[6]"),
    ARGS7(-8, "args[7]"),
    ARGS8(-9, "args[8]"),
    ARGS9(-10, "args[9]"),
    ARGSN(-11, "args[N]"),

    CACHE_ARGS0(-30, "cached_args[0]"),
    CACHE_ARGS1(-31, "cached_args[1]"),
    CACHE_ARGS2(-32, "cached_args[2]"),
    CACHE_ARGS3(-33, "cached_args[3]"),
    CACHE_ARGS4(-34, "cached_args[4]"),
    CACHE_ARGS5(-35, "cached_args[5]"),
    CACHE_ARGS6(-36, "cached_args[6]"),
    CACHE_ARGS7(-37, "cached_args[7]"),
    CACHE_ARGS8(-38, "cached_args[8]"),
    CACHE_ARGS9(-39, "cached_args[9]"),
    CACHE_ARGSN(-40, "cached_args[N]"),
    @Deprecated
    EXCEPTION(-50, "Exception", true),
    @Deprecated
    EXCEPTION_CLASS(-51, "ExceptionClass"),
    UNKNOWN(-9999, "UNKNOWN");

    private final int code;
    private final String value;
    private final boolean viewInRecordSet;

    public final static int MAX_ARGS_SIZE = 10;

    private AnnotationKey(int code, String value) {
        this(code, value, false);
    }

    private AnnotationKey(int code, String value, boolean viewInRecordSet) {
        this.code = code;
        this.value = value;
        this.viewInRecordSet = viewInRecordSet;
    }

    public String getValue() {
        return value;
    }

    public int getCode() {
        return code;
    }

    public boolean isViewInRecordSet() {
        return viewInRecordSet;
    }

    private static final IntHashMap<AnnotationKey> CODE_LOOKUP_TABLE = new IntHashMap<AnnotationKey>();
    static {
        initializeLookupTable();
    }

    public static void initializeLookupTable() {
        AnnotationKey[] values = AnnotationKey.values();
        for (AnnotationKey name : values) {
            AnnotationKey check = CODE_LOOKUP_TABLE.put(name.getCode(), name);
            if (check != null) {
                throw new IllegalStateException("duplicated code found. code:" + name.getCode());
            }
        }
    }

    public static AnnotationKey findAnnotationKey(int code) {
        AnnotationKey annotationKey = CODE_LOOKUP_TABLE.get(code);
        if (annotationKey == null) {
            return UNKNOWN;
        }
        return annotationKey;
    }

    public static AnnotationKey getArgs(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("negative index:" + index);
        }
        switch (index) {
            case 0:
                return ARGS0;
            case 1:
                return ARGS1;
            case 2:
                return ARGS2;
            case 3:
                return ARGS3;
            case 4:
                return ARGS4;
            case 5:
                return ARGS5;
            case 6:
                return ARGS6;
            case 7:
                return ARGS7;
            case 8:
                return ARGS8;
            case 9:
                return ARGS9;
            default:
                return ARGSN;
        }
    }

    public static boolean isArgsKey(int index) {
        if (index <= ARGS0.getCode() && index >= ARGSN.getCode()) {
            return true;
        }
        return false;
    }

    public static AnnotationKey getCachedArgs(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("negative index:" + index);
        }
        switch (index) {
            case 0:
                return CACHE_ARGS0;
            case 1:
                return CACHE_ARGS1;
            case 2:
                return CACHE_ARGS2;
            case 3:
                return CACHE_ARGS3;
            case 4:
                return CACHE_ARGS4;
            case 5:
                return CACHE_ARGS5;
            case 6:
                return CACHE_ARGS6;
            case 7:
                return CACHE_ARGS7;
            case 8:
                return CACHE_ARGS8;
            case 9:
                return CACHE_ARGS9;
            default:
                return CACHE_ARGSN;
        }
    }

    public static boolean isCachedArgsKey(int index) {
        if (index <= CACHE_ARGS0.getCode() && index >= CACHE_ARGSN.getCode()) {
            return true;
        }
        return false;
    }

    public static int cachedArgsToArgs(int index) {
        if (!isCachedArgsKey(index)) {
            throw new IllegalArgumentException("non CACHED_ARGS:" + index);
        }

        final int cachedIndex = CACHE_ARGS0.getCode() - ARGS0.getCode();
        // you have to - (minus) operation because of negative value
        return index - cachedIndex;
    }
}
