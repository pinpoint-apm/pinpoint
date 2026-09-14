package com.navercorp.pinpoint.alarm.dao;

import com.navercorp.pinpoint.alarm.vo.AlarmChannelBinding;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmChannelBindingDao {

    void insert(AlarmChannelBinding binding);

    void delete(@Param("ownerType") AlarmChannelOwnerType ownerType,
                @Param("ownerId") Long ownerId,
                @Param("channelId") Long channelId);

    void deleteByOwner(@Param("ownerType") AlarmChannelOwnerType ownerType,
                       @Param("ownerId") Long ownerId);

    void deleteByChannelId(@Param("channelId") Long channelId);

    void deleteRuleBindingsByRuleIds(@Param("ruleIds") List<Long> ruleIds);

    void deleteByUserGroupDestination(@Param("userGroupId") String userGroupId);

    boolean existsByChannelId(@Param("channelId") Long channelId);

    List<AlarmChannelBinding> selectByOwner(@Param("ownerType") AlarmChannelOwnerType ownerType,
                                            @Param("ownerId") Long ownerId);
}
