/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.common.id;

import com.navercorp.pinpoint.common.util.UuidUtils;

import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class ApplicationId extends UUIDPinpointIdentifier {
    public static final ApplicationId NOT_EXIST = new ApplicationId(UuidUtils.EMPTY);

    public ApplicationId(UUID value) {
        super(value);
    }

    public static ApplicationId of(UUID value) {
        return new ApplicationId(value);
    }

    public byte[] toBytes() {
        return UuidUtils.toBytes(this.value());
    }

    public static UUID unwrap(ApplicationId applicationId) {
        if (applicationId == null) {
            return null;
        }
        return applicationId.value();
    }

}
