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

package com.navercorp.pinpoint.web.vo.callstacks;

import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;

/**
 * each stack
 *
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 * @author minwoo.jung
 */
public interface Record {
    int getId();

    int getParentId();

    int getTab();

    String getTabspace();

    boolean isMethod();

    String getTitle();

    String getArguments();

    long getBegin();

    long getElapsed();

    long getGap();

    String getAgentId();

    String getAgentName();

    String getApplicationName();

    String getApplicationServiceType();

    String getApiType();

    boolean isExcludeFromTimeline();

    String getSimpleClassName();

    void setSimpleClassName(String simpleClassName);

    String getFullApiDescription();

    void setFullApiDescription(String fullApiDescription);

    boolean isFocused();

    void setFocused(boolean focused);

    boolean getHasChild();

    boolean getHasException();

    long getExceptionChainId();

    String getTransactionId();

    long getSpanId();

    long getExecutionMilliseconds();

    MethodTypeEnum getMethodTypeEnum();

    boolean isAuthorized();

    int getLineNumber();

    String getLocation();
}
