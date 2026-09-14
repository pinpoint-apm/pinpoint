package com.navercorp.pinpoint.alarm.dao;

import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmNotificationChannelDao {

    void insert(AlarmNotificationChannel channel);

    void update(AlarmNotificationChannel channel);

    void delete(Long id);

    AlarmNotificationChannel selectById(Long id);

    AlarmNotificationChannel selectByIdForUpdate(Long id);

    List<AlarmNotificationChannel> selectAll();

    List<AlarmNotificationChannel> selectByService(@Param("serviceName") String serviceName);

    List<AlarmNotificationChannel> selectByIds(List<Long> ids);

    List<AlarmNotificationChannel> selectByIdsWithUsage(List<Long> ids);

    List<AlarmNotificationChannel> selectByRuleId(Long ruleId);

    List<AlarmNotificationChannel> selectByServiceName(@Param("serviceName") String serviceName);

    boolean existsUserGroup(@Param("userGroupId") String userGroupId);

    List<AlarmNotificationChannel> selectByUserGroupDestinationForUpdate(
            @Param("userGroupId") String userGroupId);

    void deleteByUserGroupDestination(@Param("userGroupId") String userGroupId);
}
