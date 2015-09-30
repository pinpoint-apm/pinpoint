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

/**
 * AnnotationKey sandbox is from 900 to 999. These values will not be assigned to anything.
 * 
 * <table>
 * <tr><td>-1</td><td>args[0]</td></tr>
 * <tr><td>-2</td><td>args[1]</td></tr>
 * <tr><td>-3</td><td>args[2]</td></tr>
 * <tr><td>-4</td><td>args[3]</td></tr>
 * <tr><td>-5</td><td>args[4]</td></tr>
 * <tr><td>-6</td><td>args[5]</td></tr>
 * <tr><td>-7</td><td>args[6]</td></tr>
 * <tr><td>-8</td><td>args[7]</td></tr>
 * <tr><td>-9</td><td>args[8]</td></tr>
 * <tr><td>-10</td><td>args[9]</td></tr>
 * <tr><td>-11</td><td>args[N]</td></tr>
 * <tr><td>-30</td><td>cached_args[0]</td></tr>
 * <tr><td>-31</td><td>cached_args[1]</td></tr>
 * <tr><td>-32</td><td>cached_args[2]</td></tr>
 * <tr><td>-33</td><td>cached_args[3]</td></tr>
 * <tr><td>-34</td><td>cached_args[4]</td></tr>
 * <tr><td>-35</td><td>cached_args[5]</td></tr>
 * <tr><td>-36</td><td>cached_args[6]</td></tr>
 * <tr><td>-37</td><td>cached_args[7]</td></tr>
 * <tr><td>-38</td><td>cached_args[8]</td></tr>
 * <tr><td>-39</td><td>cached_args[9]</td></tr>
 * <tr><td>-40</td><td>cached_args[N]</td></tr>
 * <tr><td>-50</td><td>Exception</td></tr>
 * <tr><td>-51</td><td>ExceptionClass</td></tr>
 * <tr><td>-100</td><td>Asynchronous Invocation</td></tr>
 * <tr><td>-9999</td><td>UNKNOWN</td></tr>
 * 
 * <tr><td>12</td><td>API</td></tr>
 * <tr><td>13</td><td>API_METADATA</td></tr>
 * <tr><td>14</td><td>RETURN_DATA</td></tr>
 * <tr><td>15</td><td><i>RESERVED</i></td></tr>
 * <tr><td>16</td><td><i>RESERVED</i></td></tr>
 * <tr><td>17</td><td><i>RESERVED</i></td></tr>
 * <tr><td>20</td><td>SQL-ID</td></tr>
 * <tr><td>21</td><td>SQL</td></tr>
 * <tr><td>22</td><td>SQL-METADATA</td></tr>
 * <tr><td>23</td><td>SQL-PARAM</td></tr>
 * <tr><td>24</td><td>SQL-BindValue</td></tr>
 * <tr><td>30</td><td>STRING_ID</td></tr>
 * <tr><td>40</td><td>http.url</td></tr>
 * <tr><td>41</td><td>http.param</td></tr>
 * <tr><td>42</td><td>http.entity</td></tr>
 * <tr><td>45</td><td>http.cookie</td></tr>
 * <tr><td>46</td><td>http.status.code</td></tr>
 * <tr><td>48</td><td>http.internal.display</td></tr>
 * <tr><td>49</td><td>http.io</td></tr>
 * <tr><td>50</td><td>arcus.command</td></tr>
 * <tr><td>60</td><td><i>RESERVED</i></td></tr>
 * <tr><td>61</td><td><i>RESERVED</i></td></tr>
 * <tr><td>62</td><td><i>RESERVED</i></td></tr>
 * <tr><td>70</td><td><i>RESERVED</i></td></tr>
 * <tr><td>71</td><td><i>RESERVED</i></td></tr>
 * <tr><td>72</td><td><i>RESERVED</i></td></tr>
 * <tr><td>73</td><td><i>RESERVED</i></td></tr>
 * <tr><td>80</td><td>thrift.url</td></tr>
 * <tr><td>81</td><td>thrift.args</td></tr>
 * <tr><td>82</td><td>thrift.result</td></tr>
 * <tr><td>9000</td><td>gson.json.length</td></tr>
 * <tr><td>9001</td><td>jackson.json.length</td></tr>
 * <tr><td>9002</td><td>json-lib.json.length</td></tr>
 * <tr><td>10015</td><td>API-TAG</td></tr>
 * <tr><td>10000010</td><td>API-METADATA-ERROR</td></tr>
 * <tr><td>10000011</td><td>API-METADATA-AGENT-INFO-NOT-FOUND</td></tr>
 * <tr><td>10000012</td><td>API-METADATA-IDENTIFIER-CHECK_ERROR</td></tr>
 * <tr><td>10000013</td><td>API-METADATA-NOT-FOUND</td></tr>
 * <tr><td>10000014</td><td>API-METADATA-DID-COLLSION</td></tr>
 * </table>
 * 
 * 
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
    public static final AnnotationKey HTTP_INTERNAL_DISPLAY = new AnnotationKey(48, "http.internal.display");
    public static final AnnotationKey HTTP_IO = new AnnotationKey(49, "http.io", VIEW_IN_RECORD_SET);
    // post method parameter of httpclient

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