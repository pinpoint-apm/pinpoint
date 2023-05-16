/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.*;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import com.navercorp.pinpoint.profiler.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class BufferedStorage implements Storage {
    private static final Logger logger = LoggerFactory.getLogger(BufferedStorage.class);
    private static final boolean isDebug = logger.isDebugEnabled();

    private static final int DEFAULT_BUFFER_SIZE = 20;

    private final int bufferSize;

    private final SpanChunkFactory spanChunkFactory;
    private List<SpanEvent> storage;
    private final DataSender<Object> dataSender;


    /**
     * 报文异常判断相关
     */
    private final boolean responseJudge;
    private final String responseJudgeSign;
    private final String responseJudgeCode;


    public BufferedStorage(SpanChunkFactory spanChunkFactory, DataSender<Object> dataSender, int bufferSize,
                           boolean responseJudge, String responseJudgeSign, String responseJudgeCode) {
        this.spanChunkFactory = Assert.requireNonNull(spanChunkFactory, "spanChunkFactory");
        this.dataSender = Assert.requireNonNull(dataSender, "dataSender");
        this.bufferSize = bufferSize;
        this.storage = allocateBuffer();
        this.responseJudge = responseJudge;
        this.responseJudgeSign = responseJudgeSign;
        this.responseJudgeCode = responseJudgeCode;
    }

    @Override
    public void store(SpanEvent spanEvent) {
        final List<SpanEvent> storage = getBuffer();
        storage.add(spanEvent);

        if (overflow(storage)) {
            final List<SpanEvent> flushData = clearBuffer();
            sendSpanChunk(flushData);
        }
    }

    private boolean overflow(List<SpanEvent> storage) {
        return storage.size() >= bufferSize;
    }


    private List<SpanEvent> allocateBuffer() {
        return new ArrayList<SpanEvent>(this.bufferSize);
    }

    private List<SpanEvent> getBuffer() {
        List<SpanEvent> copy = this.storage;
        if (copy == null) {
            copy = allocateBuffer();
            this.storage = copy;
        }
        return copy;
    }

    private List<SpanEvent> clearBuffer() {
        final List<SpanEvent> copy = this.storage;
        this.storage = null;
        return copy;
    }

    @Override
    public void store(Span span) {
        final List<SpanEvent> spanEventList = clearBuffer();
        span.setSpanEventList(spanEventList);
        span.finish();

        if (isDebug) {
            logger.debug("Flush {}", span);
        }
        // 报文采集信息发送
        sendWebInfo(span);
        final boolean success = this.dataSender.send(span);
        if (!success) {
            // WARN : Do not call span.toString ()
            // concurrentmodificationexceptionr may occur in spanProcessV2
            logger.debug("send fail");
        }
    }

    @Override
    public void sendWebInfo(Span span) {
        if (span.getWebInfo().isFlag()) {
            WebInfo webInfo = span.getWebInfo();
            span.setWebInfo(null);
            // 增加采集耗时
            webInfo.setElapsedTime(span.getElapsedTime());
            // 增加上游服务名称
            webInfo.setParentApplicationName(span.getParentApplicationName());
            sendSpanWebInfo(webInfo);
        }
    }


    @Override
    public void flush() {
        final List<SpanEvent> spanEventList = clearBuffer();
        if (CollectionUtils.hasLength(spanEventList)) {
            sendSpanChunk(spanEventList);
        }
    }

    private void sendSpanChunk(List<SpanEvent> spanEventList) {
        final SpanChunk spanChunk = this.spanChunkFactory.newSpanChunk(spanEventList);

        if (isDebug) {
            logger.debug("Flush {}", spanChunk);
        }
        final boolean success = this.dataSender.send(spanChunk);
        if (!success) {
            // WARN : Do not call span.toString ()
            // concurrentmodificationexceptionr may occur in spanProcessV2
            logger.debug("send fail");
        }
    }

    private void sendSpanWebInfo(WebInfo webInfo) {

        final SpanWebInfo spanWebInfo = this.spanChunkFactory.newSpanWebInfo(webInfo);

        // 对webinfo的相关字段做前置处理，长度截取、异常判断等，若无需发送，则直接return
        if (sendBefore(spanWebInfo)) {
            return;
        }

        if (isDebug) {
            logger.debug("Flush {}", spanWebInfo);
        }
        final boolean success = this.dataSender.send(spanWebInfo);
        if (!success) {
            // WARN : Do not call span.toString ()
            // concurrentmodificationexceptionr may occur in spanProcessV2
            logger.debug("send fail");
        }
    }

    /**
     * 对webinfo的相关字段做前置处理，长度截取、异常判断等
     *
     * @param spanWebInfo
     */
    private boolean sendBefore(SpanWebInfo spanWebInfo) {
        WebInfo webInfo = spanWebInfo.getWebInfo();

        // 1. 对报文做正常异常判断
        // ===== ①. 先判断响应报文关键字；②. 判断响应码； ③. 判断是否有异常堆栈 =====
        // 对响应做正常异常判断
        if (responseJudge) {
            baseRspCheck(webInfo);
        }
        // 如果请求状态已经标记为1（异常），说明在响应体的位置已经判断过了，此处响应码不可重复判断。
        if (PinpointConstants.WEBINFO_STATUS_ABNORMAL != webInfo.getStatus()) {
            // 标记请求状态（正常/异常），2xx正常，其余异常
            if (PinpointConstants.STATUS_CODE_200 <= webInfo.getStatusCode() && webInfo.getStatusCode() <= PinpointConstants.STATUS_CODE_299) {
                webInfo.setStatus(PinpointConstants.STRATEGY_0);
            } else {
                webInfo.setStatus(PinpointConstants.STRATEGY_1);
            }
        }
        // 判断方法栈是否有异常，如果有，则标记
        if (spanWebInfo.getTraceRoot().getShared().isException() && PinpointConstants.WEBINFO_STATUS_ABNORMAL != webInfo.getStatus()) {
            webInfo.setStatus(PinpointConstants.STRATEGY_1);
        }
        // 2. 再判断报文是否需要发送（只有不在采样率内的报文会有此逻辑）
        // 此判断表示：不被采样的报文舍弃
        if (webInfo.isDisabled() && PinpointConstants.STRATEGY_2 == webInfo.getWebBodyStrategy()) {
            return true;
        }

        // 此判断表示：异常全量，不被采样的正常报文舍弃
        if (webInfo.isDisabled() && PinpointConstants.STRATEGY_1 == webInfo.getWebBodyStrategy() && PinpointConstants.WEBINFO_STATUS_NORMAL == webInfo.getStatus()) {
            return true;
        }
        // 3. 最后对请求/响应头/体做json转换
        try {
            webInfo.setRequestHeader(JacksonUtil.toJsonString(webInfo.getRequestHeader()));
        } catch (Throwable e) {
            webInfo.setRequestHeader(PinpointConstants.EMPTY_STRING);
        }
        try {
            String requestBody = bodyToString(webInfo.getRequestBody());
            String body = sizeLimit(PinpointConstants.REQUEST_BODY_LENGTH_LIMIT,
                    requestBody,
                    PinpointConstants.REQUEST_BODY_LENGTH_LIMIT_MAP,
                    PinpointConstants.REQUEST_BODY);
            webInfo.setRequestBody(body);
        } catch (Throwable e) {
            webInfo.setRequestBody(PinpointConstants.EMPTY_STRING);
        }
        try {
            webInfo.setResponseHeader(JacksonUtil.toJsonString(webInfo.getResponseHeader()));
        } catch (Throwable e) {
            webInfo.setResponseHeader(PinpointConstants.EMPTY_STRING);
        }
        try {
            // 响应长度限制
            String responseBody = bodyToString(webInfo.getResponseBody());
            String body = sizeLimit(PinpointConstants.RESPONSE_BODY_LENGTH_LIMIT,
                    responseBody,
                    PinpointConstants.RESPONSE_BODY_LENGTH_LIMIT_MAP,
                    PinpointConstants.RESPONSE_BODY);
            webInfo.setResponseBody(body);
        } catch (Throwable e) {
            webInfo.setResponseBody(PinpointConstants.EMPTY_STRING);
        }
        return false;
    }

    private String bodyToString(Object body) throws JsonProcessingException {
        if (body instanceof String) {
            return (String) body;
        } else {
            return JacksonUtil.toJsonString(body);
        }
    }

    /**
     * // 对响应做正常异常判断
     *
     * @param webInfo 星舟规范响应示例：
     *                {
     *                "STATUS": "0000",
     *                "MSG": "服务调用成功！",
     *                "TXID": "TxidError0000!",
     *                "RSP": {
     *                "RSP_CODE": "0000",
     *                "RSP_DESC": "执行成功!",
     *                "SUB_CODE": "0000",
     *                "SUB_DESC": "执行成功!",
     *                "DATA": [
     *                "HELLO"
     *                ],
     *                "ATTACH": "GET"
     *                }
     *                }
     */
    private void baseRspCheck(WebInfo webInfo) {
        Object responseBody = webInfo.getResponseBody();
        if (null == responseBody) {
            return;
        }
        Class<?> clazz = responseBody.getClass();

        try {
            Field[] declaredFields = clazz.getDeclaredFields();
            if (declaredFields.length == 0) {
                return;
            }
            for (Field declaredField : declaredFields) {
                if (responseJudgeSign.equals(declaredField.getName())) {
                    judgeCodeCheck(declaredField, responseBody, webInfo);
                    break;
                }
            }
        } catch (Throwable t) {
            // 捕获所有异常
        }



    }

    private void judgeCodeCheck(Field judgeSign, Object responseBody, WebInfo webInfo) throws IllegalAccessException {
        judgeSign.setAccessible(true);
        Object judgeCode = judgeSign.get(responseBody);
        if (null != judgeCode && responseJudgeCode.equals(judgeCode.toString())) {
            webInfo.setStatus(PinpointConstants.STRATEGY_0);
        } else {
            webInfo.setStatus(PinpointConstants.STRATEGY_1);
        }
    }

    /**
     * 对报文做长度判断及截取
     *
     * @param limit    长度限制
     * @param param    报文
     * @param limitMap 限制话术
     * @param key      报文类别
     */
    private String sizeLimit(Integer limit, String param, Map<String, String> limitMap, String key) throws JsonProcessingException {
        if (limit < param.length()) {
            Map<String, String> map = new HashMap<String, String>(2);
            map.putAll(limitMap);
            map.put(key, StringUtils.abbreviate(param, PinpointConstants.BODY_LIMIT_LENGTH));
            return JacksonUtil.toJsonString(map);
        }
        return param;
    }


    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "BufferedStorage{" + "bufferSize=" + bufferSize + ", dataSender=" + dataSender + '}';
    }
}