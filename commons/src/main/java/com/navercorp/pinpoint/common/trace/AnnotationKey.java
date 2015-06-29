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

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.*;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.navercorp.pinpoint.common.util.StaticFieldLookUp;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;

/**
 * @author netspider
 * @author emeroad
 * @author Jongho Moon
 */
public class AnnotationKey {
    
    public AnnotationKey(int code, String name, AnnotationKeyProperty... properties) {
        this.code = code;
        this.name = name;
        
        boolean viewInRecordSet = false;
        boolean errorApiMetadata = false;
        
        for (AnnotationKeyProperty property : properties) {
            switch (property) {
            case VIEW_IN_RECORD_SET:
                viewInRecordSet = true;
                break;
            case ERROR_API_METADATA:
                errorApiMetadata = true;
                break;
            }
        }
        
        this.viewInRecordSet = viewInRecordSet;
        this.errorApiMetadata = errorApiMetadata;
    }
    

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
    
    public boolean isErrorApiMetadata() {
        return errorApiMetadata;
    }

    public boolean isViewInRecordSet() {
        return viewInRecordSet;
    }

    
    // because of using variable-length encoding,
    // a small number should be used mainly for data contained in network packets and a big number for internal used code.

//    2147483647
//    -2147483648

//    @Deprecated  // moved apiId to spanEvent and span. dump by int
//    API_DID(10, "API-DID"),
//    @Deprecated  // you should remove static API code. Use only API-DID. dump by int
//    API_ID(11, "API-ID"),
    // used for developing the annotation that dumps api by string. you also consider to remove it later.
    public static final AnnotationKey API = new AnnotationKey(12, "API");
    public static final AnnotationKey API_METADATA = new AnnotationKey(13, "API-METADATA");
    public static final AnnotationKey RETURN_DATA = new AnnotationKey(14, "RETURN_DATA", VIEW_IN_RECORD_SET);
    public static final AnnotationKey API_TAG = new AnnotationKey(10015, "API-TAG");
    
    // when you don't know the correct cause of errors.
    public static final AnnotationKey ERROR_API_METADATA_ERROR = new AnnotationKey(10000010, "API-METADATA-ERROR", ERROR_API_METADATA);
    // when agentInfo not found
    public static final AnnotationKey ERROR_API_METADATA_AGENT_INFO_NOT_FOUND = new AnnotationKey(10000011, "API-METADATA-AGENT-INFO-NOT-FOUND", ERROR_API_METADATA);
    // when checksum is not correct even if agentInfo exists
    public static final AnnotationKey ERROR_API_METADATA_IDENTIFIER_CHECK_ERROR = new AnnotationKey(10000012, "API-METADATA-IDENTIFIER-CHECK_ERROR", ERROR_API_METADATA);
    // when  meta data itself not found
    public static final AnnotationKey ERROR_API_METADATA_NOT_FOUND = new AnnotationKey(10000013, "API-METADATA-NOT-FOUND", ERROR_API_METADATA);
    // when the same hashId of meta data exists
    public static final AnnotationKey ERROR_API_METADATA_DID_COLLSION = new AnnotationKey(10000014, "API-METADATA-DID-COLLSION", ERROR_API_METADATA);

    // it's not clear to handle a error code.  so ApiMetaDataError with searching ERROR_API_META_DATA has been used.
    // automatically generated id

    public static final AnnotationKey SQL_ID = new AnnotationKey(20, "SQL-ID");
    public static final AnnotationKey SQL = new AnnotationKey(21, "SQL", VIEW_IN_RECORD_SET);
    public static final AnnotationKey SQL_METADATA = new AnnotationKey(22, "SQL-METADATA");
    public static final AnnotationKey SQL_PARAM = new AnnotationKey(23, "SQL-PARAM");
    public static final AnnotationKey SQL_BINDVALUE = new AnnotationKey(24, "SQL-BindValue", VIEW_IN_RECORD_SET);

    public static final AnnotationKey STRING_ID = new AnnotationKey(30, "STRING_ID");

    // HTTP_URL is replaced by argument. So viewInRecordSet parameter name is not true.
    public static final AnnotationKey HTTP_URL = new AnnotationKey(40, "http.url");
    public static final AnnotationKey HTTP_PARAM = new AnnotationKey(41, "http.param", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HTTP_PARAM_ENTITY = new AnnotationKey(42, "http.entity", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HTTP_COOKIE = new AnnotationKey(45, "http.cookie", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HTTP_STATUS_CODE = new AnnotationKey(46, "http.status.code", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HTTP_CALL_RETRY_COUNT = new AnnotationKey(48, "retryCount");
    // post method parameter of httpclient


    // ARCUS_COMMAND(50, "arcus.command");
    
//    public static final AnnotationKey NPC_URL = new AnnotationKey(60, "npc.url");
//    public static final AnnotationKey NPC_PARAM = new AnnotationKey(61, "npc.param");
//    public static final AnnotationKey NPC_CONNECT_OPTION = new AnnotationKey(62, "npc.connect.options");

    public static final AnnotationKey NIMM_OBJECT_NAME = new AnnotationKey(70, "nimm.objectName");
    public static final AnnotationKey NIMM_METHOD_NAME = new AnnotationKey(71, "nimm.methodName");
    public static final AnnotationKey NIMM_PARAM = new AnnotationKey(72, "nimm.param");
    public static final AnnotationKey NIMM_CONNECT_OPTION = new AnnotationKey(73, "nimm.connect.options");

    
    // 9000 gson.json.length
    // 9001 jackson.json.length
    // 9002 json-lib.json.length

  
    public static final AnnotationKey ARGS0 = new AnnotationKey(-1, "args[0]");
    public static final AnnotationKey ARGS1 = new AnnotationKey(-2, "args[1]");
    public static final AnnotationKey ARGS2 = new AnnotationKey(-3, "args[2]");
    public static final AnnotationKey ARGS3 = new AnnotationKey(-4, "args[3]");
    public static final AnnotationKey ARGS4 = new AnnotationKey(-5, "args[4]");
    public static final AnnotationKey ARGS5 = new AnnotationKey(-6, "args[5]");
    public static final AnnotationKey ARGS6 = new AnnotationKey(-7, "args[6]");
    public static final AnnotationKey ARGS7 = new AnnotationKey(-8, "args[7]");
    public static final AnnotationKey ARGS8 = new AnnotationKey(-9, "args[8]");
    public static final AnnotationKey ARGS9 = new AnnotationKey(-10, "args[9]");
    public static final AnnotationKey ARGSN = new AnnotationKey(-11, "args[N]");

    public static final AnnotationKey CACHE_ARGS0 = new AnnotationKey(-30, "cached_args[0]");
    public static final AnnotationKey CACHE_ARGS1 = new AnnotationKey(-31, "cached_args[1]");
    public static final AnnotationKey CACHE_ARGS2 = new AnnotationKey(-32, "cached_args[2]");
    public static final AnnotationKey CACHE_ARGS3 = new AnnotationKey(-33, "cached_args[3]");
    public static final AnnotationKey CACHE_ARGS4 = new AnnotationKey(-34, "cached_args[4]");
    public static final AnnotationKey CACHE_ARGS5 = new AnnotationKey(-35, "cached_args[5]");
    public static final AnnotationKey CACHE_ARGS6 = new AnnotationKey(-36, "cached_args[6]");
    public static final AnnotationKey CACHE_ARGS7 = new AnnotationKey(-37, "cached_args[7]");
    public static final AnnotationKey CACHE_ARGS8 = new AnnotationKey(-38, "cached_args[8]");
    public static final AnnotationKey CACHE_ARGS9 = new AnnotationKey(-39, "cached_args[9]");
    public static final AnnotationKey CACHE_ARGSN = new AnnotationKey(-40, "cached_args[N]");
    @Deprecated
    public static final AnnotationKey EXCEPTION = new AnnotationKey(-50, "Exception", VIEW_IN_RECORD_SET);
    @Deprecated
    public static final AnnotationKey EXCEPTION_CLASS = new AnnotationKey(-51, "ExceptionClass");
    public static final AnnotationKey UNKNOWN = new AnnotationKey(-9999, "UNKNOWN");

    public static final AnnotationKey ASYNC = new AnnotationKey(-100, "Asynchronous Invocation", VIEW_IN_RECORD_SET);
    
    private final int code;
    private final String name;
    private final boolean viewInRecordSet;
    private final boolean errorApiMetadata;

    public final static int MAX_ARGS_SIZE = 10;




    ////////////////////////////////
    // Arguments
    ////////////////////////////////
    
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
        // you have to - (minus) operation because of negative name
        return index - cachedIndex;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnnotationKey{");
        sb.append("code=").append(code);
        sb.append(", name='").append(name);
        sb.append('}');
        return sb.toString();
    }
}