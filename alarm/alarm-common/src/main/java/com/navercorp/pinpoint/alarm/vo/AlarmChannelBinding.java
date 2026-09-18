/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.vo;

public class AlarmChannelBinding {

    private AlarmChannelOwnerType ownerType;
    private Long ownerId;
    private Long channelId;

    public AlarmChannelBinding() {
    }

    public AlarmChannelBinding(AlarmChannelOwnerType ownerType, Long ownerId, Long channelId) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.channelId = channelId;
    }

    public AlarmChannelOwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(AlarmChannelOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }
}
