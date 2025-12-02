/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.callstacks;

import com.navercorp.pinpoint.common.trace.ErrorCategory;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class ErrorCategoryRecord extends BaseRecord {
    public ErrorCategoryRecord(final int tab, final int id, final int parentId, final EnumSet<ErrorCategory> categories) {
        this.tab = tab;
        this.id = id;
        this.parentId = parentId;
        this.title = "ERROR_CATEGORY";
        this.arguments = categories.stream().map(Enum::name).collect(Collectors.joining(", "));
        this.hasException = true;
        this.isAuthorized = true;
    }
}
