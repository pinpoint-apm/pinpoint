package com.navercorp.pinpoint.web.dao.memory;

import static org.junit.Assert.*;

import org.junit.Test;

import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

public class MemoryUserGroupDaoTest {

    @Test
    public void selectUserGroupByUserId() {
        MemoryUserGroupDao userGroupDao = new MemoryUserGroupDao();
        userGroupDao.createUserGroup(new UserGroup("1", "userGroup1"));
        userGroupDao.createUserGroup(new UserGroup("2", "userGroup2"));
        userGroupDao.insertMember(new UserGroupMember("userGroup1", "user1"));
        userGroupDao.insertMember(new UserGroupMember("userGroup2", "user1"));
        
        assertEquals(userGroupDao.selectUserGroupByUserId("user1").size(), 2);
    }
    
    @Test
    public void selectUserGroupByUserGroupId() {
        MemoryUserGroupDao userGroupDao = new MemoryUserGroupDao();
        userGroupDao.createUserGroup(new UserGroup("1", "userGroup1"));
        userGroupDao.createUserGroup(new UserGroup("2", "userGroup2"));
        userGroupDao.insertMember(new UserGroupMember("userGroup1", "user1"));
        userGroupDao.insertMember(new UserGroupMember("userGroup2", "user1"));
        
        assertEquals(userGroupDao.selectUserGroupByUserGroupId("Group").size(), 2);
    }

}
