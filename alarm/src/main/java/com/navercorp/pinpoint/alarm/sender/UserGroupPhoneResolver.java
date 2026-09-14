package com.navercorp.pinpoint.alarm.sender;

import java.util.List;

/**
 * Resolves phone numbers from user_group_id using existing user_group/puser tables.
 * Implemented in each deployment module.
 */
public interface UserGroupPhoneResolver {

    List<String> resolvePhoneNumbers(String userGroupId);
}
