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
