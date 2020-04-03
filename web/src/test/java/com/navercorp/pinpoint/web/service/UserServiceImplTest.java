package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import com.navercorp.pinpoint.web.vo.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {


    private final static String CHANGED_PHONE_NUMBER = "0000000000";

    @Mock
    private UserDao userDao;

    private UserInfoDecoder userInfoDecoder = new CustomUserInfoDecoder();

    private UserService userService;

    @Before
    public void before() throws Exception {
        userService = new UserServiceImpl(userDao, Optional.of(userInfoDecoder));
    }

    @Test
    public void decodePhoneNumber() {
        String departmentName = "departmentName";


        User user = new User("1", "userId01", "name01", departmentName, 82, "01012341234", "name01@pinpoint.com");

        when(userDao.selectUserByUserId("userId01")).thenReturn(user);
        User decodedUser = userService.selectUserByUserId("userId01");

        System.out.println(user);
        assertNotEquals(user, decodedUser);
        assertEquals(decodedUser.getNumber(), user.getNumber());
        assertEquals(decodedUser.getUserId(), user.getUserId());
        assertEquals(decodedUser.getName(), user.getName());
        assertEquals(decodedUser.getPhoneCountryCode(), user.getPhoneCountryCode());
        assertEquals(decodedUser.getPhoneNumber(), CHANGED_PHONE_NUMBER);
        assertEquals(decodedUser.getEmail(), user.getEmail());
    }

    @Test
    public void decodePhoneNumberTest() {
        String departmentName = "departmentName";

        List<User> userList = new ArrayList<>(5);
        userList.add(new User("1", "userId01", "name01", departmentName, 82, "01012341234", "name01@pinpoint.com"));
        userList.add(new User("2", "userId02", "name02", departmentName, 82, "01012341234", "name02@pinpoint.com"));
        userList.add(new User("3", "userId03", "name03", departmentName, 82, "01012341234", "name03@pinpoint.com"));
        userList.add(new User("4","userId04", "name04", departmentName, 82, "01012341234", "name04@pinpoint.com"));
        userList.add(new User("5", "userId05", "name05", departmentName, 82, "01012341234", "name05@pinpoint.com"));

        when(userDao.selectUserByDepartment(departmentName)).thenReturn(userList);
        List<User> result = userService.selectUserByDepartment(departmentName);

        assertEquals(result.size(), 5);
        for (User user : result) {
            assertTrue(user.getUserId().startsWith("userId"));
            assertTrue(user.getName().startsWith("name"));
            assertEquals(user.getPhoneCountryCode(), 82);
            assertEquals(user.getPhoneNumber(), CHANGED_PHONE_NUMBER);
            assertTrue(user.getEmail().startsWith("name"));
            assertTrue(user.getEmail().endsWith("pinpoint.com"));
        }
    }

    private class CustomUserInfoDecoder implements UserInfoDecoder {

        @Override
        public List<String> decodePhoneNumberList(List<String> phoneNumberList) {
            List<String> changedPhoneNumberList = new ArrayList<>(phoneNumberList.size());
            for (int i = 0 ; i < phoneNumberList.size() ; i++) {
                changedPhoneNumberList.add(CHANGED_PHONE_NUMBER);
            }

            return changedPhoneNumberList;
        }

        @Override
        public String decodePhoneNumber(String phoneNumber) {
            return CHANGED_PHONE_NUMBER;
        }
    }

}