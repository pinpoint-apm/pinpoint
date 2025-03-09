/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.RequestId;

/**
 * @author yjqg6666
 */
public final class DefaultRequestId implements RequestId {

    private final String requestId;

    public DefaultRequestId() {
        this(null);
    }

    public DefaultRequestId(String id) {
        this.requestId = id;
    }

    @Override
    public String toId() {
        return requestId;
    }

    @Override
    public boolean isSet() {
        return requestId != null;
    }

    @Override
    public String toString() {
        return "DefaultRequestId{" +
                "requestId='" + requestId + '\'' +
                '}';
    }

}