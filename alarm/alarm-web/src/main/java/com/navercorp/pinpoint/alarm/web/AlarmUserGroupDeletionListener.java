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
package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.user.service.UserGroupDeletionListener;
import com.navercorp.pinpoint.user.vo.UserGroup;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AlarmUserGroupDeletionListener implements UserGroupDeletionListener {
    private final AlarmChannelService channelService;

    public AlarmUserGroupDeletionListener(AlarmChannelService channelService) {
        this.channelService = Objects.requireNonNull(channelService, "channelService");
    }

    @Override
    public void onUserGroupDeleted(UserGroup userGroup) {
        channelService.deleteChannelsByUserGroupId(userGroup.getId());
    }
}
