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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public final class PinpointConstants {

    public static final int APPLICATION_NAME_MAX_LEN = 24;

    public static final int AGENT_NAME_MAX_LEN = 24;

    public static final String EMPTY_STRING = "";

    /**
     * 正常响应码的取值区间
     */
    public static final int STATUS_CODE_200 = 200;
    public static final int STATUS_CODE_299 = 299;

    /**
     * 采样策略
     * # 0 代表全量采集；1 代表正常报文按照采样率，异常报文全量采集；2 代表报文采集按照采样率来走
     */
    public static final byte STRATEGY_0 = 0;
    public static final byte STRATEGY_1 = 1;
    public static final byte STRATEGY_2 = 2;


    /**
     * 报文状态标志：
     * 0代表正常，1代表异常，2代表未知
     */
    public static final byte WEBINFO_STATUS_NORMAL = 0;
    public static final byte WEBINFO_STATUS_ABNORMAL = 1;
    public static final byte WEBINFO_STATUS_UNKNOWN = 2;

    /**
     * 报文长度限制相关
     */
    public static final Map<String, String> RESPONSE_BODY_LENGTH_LIMIT_MAP = Collections.unmodifiableMap(unmodifiableMap("报文长度超出限制（52kB），采集截取前10240个字符。"));
    public static final Map<String, String> REQUEST_BODY_LENGTH_LIMIT_MAP = Collections.unmodifiableMap(unmodifiableMap("报文长度超出限制（20kB），采集截取前10240个字符。"));

    public static final String REQUEST_BODY = "requestBody";
    public static final String RESPONSE_BODY = "responseBody";

    public static final Integer REQUEST_BODY_LENGTH_LIMIT = 20480;
    public static final Integer RESPONSE_BODY_LENGTH_LIMIT = 53248;
    public static final Integer BODY_LIMIT_LENGTH = 10240;

    private static Map<String, String> unmodifiableMap(String limitInfo) {
        Map<String, String> map = new HashMap<String, String>(1);
        map.put("limitInfo", limitInfo);
        return map;
    }
}
