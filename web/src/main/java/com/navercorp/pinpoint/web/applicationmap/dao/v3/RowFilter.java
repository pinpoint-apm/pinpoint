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

package com.navercorp.pinpoint.web.applicationmap.dao.v3;

import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidRowKey;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;
import java.util.function.Predicate;

public class RowFilter<T extends UidRowKey> implements Predicate<T> {

    private final Application application;

    public RowFilter(Application application) {
        this.application = Objects.requireNonNull(application, "application");
    }

    public boolean test(UidRowKey row) {
        return this.application.getService().getUid() == row.getServiceUid()
                &&  application.getApplicationName().equals(row.getApplicationName())
                && application.getServiceTypeCode() == row.getServiceType();
    }
}
