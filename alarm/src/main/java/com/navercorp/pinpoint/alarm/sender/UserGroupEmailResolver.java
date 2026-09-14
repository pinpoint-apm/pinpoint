package com.navercorp.pinpoint.alarm.sender;

import java.util.List;

/**
 * Resolves email addresses from user_group_id using existing user_group/puser tables.
 * Implemented in each deployment module.
 */
public interface UserGroupEmailResolver {

    List<String> resolveEmails(String userGroupId);
}
