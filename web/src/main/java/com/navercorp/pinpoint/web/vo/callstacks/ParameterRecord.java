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

import org.apache.commons.lang3.StringUtils;

/**
 * @author jaehong.kim
 */
public class ParameterRecord extends BaseRecord {

    public ParameterRecord(final int tab, final int id, final int parentId, String method, String argument) {
        this.tab = tab;
        this.id = id;
        this.parentId = parentId;
        this.title = method;
        this.arguments = buildArgument(argument);
        this.isAuthorized = true;
    }

    String buildArgument(String argument) {
        return StringUtils.defaultString(argument, "null");
    }
}
