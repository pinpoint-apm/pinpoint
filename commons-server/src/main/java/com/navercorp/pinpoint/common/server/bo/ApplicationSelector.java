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
package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public record ApplicationSelector(ServiceId serviceId, String name, short serviceTypeCode) {

    public ApplicationSelector(ServiceId serviceId, String name, short serviceTypeCode) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
        this.name = Objects.requireNonNull(name, "name");
        this.serviceTypeCode = serviceTypeCode;
    }

    public byte[] toBytes() {
        Buffer buffer = new AutomaticBuffer();
        buffer.putUUID(serviceId.value());
        buffer.putShort(serviceTypeCode);
        buffer.putBytes(BytesUtils.toBytes(name));
        return buffer.getBuffer();
    }

    public static ApplicationSelector fromBuffer(Buffer buffer) {
        final ServiceId serviceId = new ServiceId(buffer.readUUID());
        final short serviceTypeCode = buffer.readShort();
        final String name = buffer.readPadString(buffer.remaining());
        return new ApplicationSelector(serviceId, name, serviceTypeCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationSelector selector = (ApplicationSelector) o;
        return serviceTypeCode == selector.serviceTypeCode && Objects.equals(serviceId, selector.serviceId) && Objects.equals(name, selector.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, name, serviceTypeCode);
    }

    @Override
    public String toString() {
        return "ApplicationSelector{" +
                "serviceId=" + serviceId +
                ", name='" + name + '\'' +
                ", serviceTypeCode=" + serviceTypeCode +
                '}';
    }
}
