/*
 * Copyright 2025 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.user.dao.memory;

import com.navercorp.pinpoint.user.dao.UserGroupDao;
import com.navercorp.pinpoint.user.vo.User;
import com.navercorp.pinpoint.user.vo.UserGroupMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author hyeran.lee
 */
@ExtendWith(MockitoExtension.class)
public class MemoryUserDaoTest {
    
    @Mock
    private UserGroupDao userGroupDao;
    
    @InjectMocks
    private MemoryUserDao memoryUserDao;
    
    private User createUser(String userID, String dep, String name) {
        return new User(userID, name, dep, 82, "1012345678", "pinpoint@naver.com");
    }
    
    @Test
    public void insertUser() {
        User inputUser = createUser("user1", "dep", "name");
        memoryUserDao.insertUser(inputUser);
        User selectedUser = memoryUserDao.selectUserByUserId("user1");
        assertEquals(inputUser.getUserId(), selectedUser.getUserId());
        assertEquals(inputUser.getName(), selectedUser.getName());
        assertEquals(inputUser.getDepartment(), selectedUser.getDepartment());
        assertEquals(inputUser.getEmail(), selectedUser.getEmail());
        assertEquals(inputUser.getPhoneCountryCode(), selectedUser.getPhoneCountryCode());
        assertEquals("0", selectedUser.getNumber());
    }

    @Test
    public void insertUserList() {
        List<User> userList = List.of(
                createUser("user1", "dep1", "name1"),
                createUser("user2", "dep2", "name2")
        );

        memoryUserDao.insertUserList(userList);

        List<User> selectedUserList = memoryUserDao.selectUser();
        assertThat(selectedUserList).hasSize(2);
    }
    
    @Test
    public void selectUserByDepartment() {
        User inputUser1 = createUser("user1", "dep1", "name1");
        User inputUser2 = createUser("user2", "dep2", "name2");
    
        memoryUserDao.insertUser(inputUser1);
        memoryUserDao.insertUser(inputUser2);
    
        List<User> selectedUserList = memoryUserDao.selectUserByDepartment("dep1");
        assertThat(selectedUserList).hasSize(1);
    }
    
    @Test
    public void selectUserByUserName() {
        User inputUser1 = createUser("user1", "dep1", "name1");
        User inputUser2 = createUser("user2", "dep2", "name2");
    
        memoryUserDao.insertUser(inputUser1);
        memoryUserDao.insertUser(inputUser2);
        
        List<User> selectedUserList = memoryUserDao.selectUserByUserName("name1");
        assertThat(selectedUserList).hasSize(1);
    }
    
    @Test
    public void selectUserByUserGroupId() {
        User inputUser1 = createUser("user1", "dep1", "name1");
        User inputUser2 = createUser("user2", "dep2", "name2");
        memoryUserDao.insertUser(inputUser1);
        memoryUserDao.insertUser(inputUser2);

        UserGroupMember userGroupMember1 = new UserGroupMember();
        userGroupMember1.setName("name1");
        userGroupMember1.setDepartment("dep1");
        userGroupMember1.setMemberId("user1");
        userGroupMember1.setUserGroupId("userGroupId");

        List<UserGroupMember> userGroupMembers = List.of(userGroupMember1);

        when(userGroupDao.selectMember("userGroupId")).thenReturn(userGroupMembers);
        List<User> selectedUserList = memoryUserDao.selectUserByUserGroupId("userGroupId");

        assertEquals(userGroupMember1.getMemberId(), selectedUserList.get(0).getUserId());
        assertEquals(userGroupMember1.getName(), selectedUserList.get(0).getName());
        assertThat(selectedUserList).hasSize(1);
    }
    
    @Test
    public void dropAndCreateUserTable() {
        memoryUserDao.dropAndCreateUserTable();
        List<User> selectedUserList = memoryUserDao.selectUser();
        assertThat(selectedUserList).isEmpty();
    }
}
