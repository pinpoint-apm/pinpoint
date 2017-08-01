/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jaehong.kim
 */
public class ExceptionRecord extends BaseRecord {

    public ExceptionRecord(final int tab, final int id, final int parentId, final SpanAlign align) {
        this.tab = tab;
        this.id = id;
        this.parentId = parentId;
        this.title = toSimpleExceptionName(align.getExceptionClass());
        this.arguments = buildArgument(align.getExceptionMessage());
        this.isAuthorized = true;
    }

    String toSimpleExceptionName(String exceptionClass) {
        if (exceptionClass == null) {
            return "";
        }
        final int index = exceptionClass.lastIndexOf('.');
        if (index != -1) {
            exceptionClass = exceptionClass.substring(index + 1, exceptionClass.length());
        }
        return exceptionClass;
    }

    String buildArgument(String exceptionMessage) {
        return StringUtils.defaultString(exceptionMessage, "");
    }
}
