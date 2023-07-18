package com.navercorp.pinpoint.web.dao.memory;

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MemoryUserGroupDaoTest {

    @Test
    public void selectUserGroupByUserId() {
        MemoryUserGroupDao userGroupDao = new MemoryUserGroupDao(mock(AlarmRule.class), mock(UserDao.class));
        userGroupDao.createUserGroup(new UserGroup("1", "userGroup1"));
        userGroupDao.createUserGroup(new UserGroup("2", "userGroup2"));
        userGroupDao.insertMember(new UserGroupMember("userGroup1", "user1"));
        userGroupDao.insertMember(new UserGroupMember("userGroup2", "user1"));

        assertThat(userGroupDao.selectUserGroupByUserId("user1")).hasSize(2);
    }
    
    @Test
    public void selectUserGroupByUserGroupId() {
        MemoryUserGroupDao userGroupDao = new MemoryUserGroupDao(mock(AlarmRule.class), mock(UserDao.class));
        userGroupDao.createUserGroup(new UserGroup("1", "userGroup1"));
        userGroupDao.createUserGroup(new UserGroup("2", "userGroup2"));
        userGroupDao.insertMember(new UserGroupMember("userGroup1", "user1"));
        userGroupDao.insertMember(new UserGroupMember("userGroup2", "user1"));

        assertThat(userGroupDao.selectUserGroupByUserGroupId("Group")).hasSize(2);
    }

}
