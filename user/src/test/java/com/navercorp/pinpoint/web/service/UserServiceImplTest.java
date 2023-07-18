package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.dao.memory.MemoryUserDao;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import com.navercorp.pinpoint.web.util.UserInfoEncoder;
import com.navercorp.pinpoint.web.vo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private final static String DECODED_PHONE_NUMBER = "0000000000";
    private final static String DECODED_EMAIL = "name@navercorp.com";
    private final static String ENCODED_PHONE_NUMBER = "0000000000asdf";
    private final static String ENCODED_EMAIL = "asdf@asdfasdf.com";

    @Mock
    private UserDao userDao;

    private final UserInfoDecoder userInfoDecoder = new CustomUserInfoDecoder();

    private final UserInfoEncoder userInfoEncoder = new CustomUserInfoEncoder();

    private UserService userService;

    @BeforeEach
    public void before() throws Exception {
        userService = new UserServiceImpl(userDao, Optional.of(userInfoDecoder), Optional.of(userInfoEncoder));
    }

    @Test
    public void insertUserTest() {
        UserDao userDao = new MemoryUserDao(mock(UserGroupDao.class));
        UserService userService = new UserServiceImpl(userDao, Optional.of(userInfoDecoder), Optional.of(userInfoEncoder));

        String userId = "userId01";
        User user = new User("0", userId, "name01", "departmentName", 82, "01012341234", "name01@pinpoint.com");
        userService.insertUser(user);

        User decodedUser = userDao.selectUserByUserId(userId);

        assertNotEquals(user, decodedUser);
        assertEquals(decodedUser.getNumber(), user.getNumber());
        assertEquals(decodedUser.getUserId(), user.getUserId());
        assertEquals(decodedUser.getName(), user.getName());
        assertEquals(decodedUser.getPhoneCountryCode(), user.getPhoneCountryCode());
        assertEquals(decodedUser.getPhoneNumber(), ENCODED_PHONE_NUMBER);
        assertEquals(decodedUser.getEmail(), ENCODED_EMAIL);
    }

    @Test
    public void insertUserList() {
        UserDao userDao = new MemoryUserDao(mock(UserGroupDao.class));
        UserService userService = new UserServiceImpl(userDao, Optional.of(userInfoDecoder), Optional.of(userInfoEncoder));

        List<User> userList = List.of(
                new User("1", "userId01", "name01", "departmentName01", 82, "01012341234", "name01@pinpoint.com"),
                new User("2", "userId02", "name02", "departmentName01", 82, "01012341234", "name02@pinpoint.com"),
                new User("3", "userId03", "name03", "departmentName01", 82, "01012341234", "name03@pinpoint.com"),
                new User("4", "userId04", "name04", "departmentName01", 82, "01012341234", "name04@pinpoint.com"),
                new User("5", "userId05", "name05", "departmentName01", 82, "01012341234", "name05@pinpoint.com")
        );

        userService.insertUserList(userList);

        List<User> selectUserList = userDao.selectUser();
        assertThat(selectUserList).hasSize(5);
        for (User user : selectUserList) {
            assertTrue(user.getUserId().startsWith("userId0"));
            assertTrue(user.getName().startsWith("name0"));
            assertTrue(user.getDepartment().startsWith("departmentName"));
            assertEquals(user.getPhoneCountryCode(), user.getPhoneCountryCode());
            assertEquals(user.getPhoneNumber(), ENCODED_PHONE_NUMBER);
            assertEquals(user.getEmail(), ENCODED_EMAIL);
        }
    }

    @Test
    public void updateUserTest() {
        UserDao userDao = new MemoryUserDao(mock(UserGroupDao.class));
        UserService userService = new UserServiceImpl(userDao, Optional.of(userInfoDecoder), Optional.of(userInfoEncoder));

        String userId = "userId01";
        User user = new User("0", userId, "name01", "departmentName", 82, "01012341234", "name01@pinpoint.com");
        userService.insertUser(user);
        User updatedUser = new User("0", userId, "name01", "departmentName2", 83, "01012341234", "name01@pinpoint.com");
        userService.updateUser(updatedUser);

        User decodedUser = userDao.selectUserByUserId(userId);

        assertNotEquals(user, decodedUser);
        assertEquals(decodedUser.getNumber(), updatedUser.getNumber());
        assertEquals(decodedUser.getUserId(), updatedUser.getUserId());
        assertEquals(decodedUser.getName(), updatedUser.getName());
        assertEquals(decodedUser.getPhoneCountryCode(), updatedUser.getPhoneCountryCode());
        assertEquals(decodedUser.getPhoneNumber(), ENCODED_PHONE_NUMBER);
        assertEquals(decodedUser.getEmail(), ENCODED_EMAIL);
    }

    @Test
    public void selectUser() {
        List<User> userList = List.of(
                new User("1", "userId01", "name01", "departmentName01", 82, "01012341234", "name01@pinpoint.com"),
                new User("2", "userId02", "name02", "departmentName01", 82, "01012341234", "name02@pinpoint.com"),
                new User("3", "userId03", "name03", "departmentName01", 82, "01012341234", "name03@pinpoint.com"),
                new User("4", "userId04", "name04", "departmentName01", 82, "01012341234", "name04@pinpoint.com"),
                new User("5", "userId05", "name05", "departmentName01", 82, "01012341234", "name05@pinpoint.com")
        );

        when(userDao.selectUser()).thenReturn(userList);
        List<User> result = userService.selectUser();

        assertThat(userList).hasSize(5);
        for (User user : result) {
            assertTrue(user.getUserId().startsWith("userId0"));
            assertTrue(user.getName().startsWith("name0"));
            assertTrue(user.getDepartment().startsWith("departmentName"));
            assertEquals(user.getPhoneCountryCode(), user.getPhoneCountryCode());
            assertEquals(user.getPhoneNumber(), DECODED_PHONE_NUMBER);
            assertEquals(user.getEmail(), DECODED_EMAIL);
        }
    }


    @Test
    public void selectUserByUserName() {
        String name = "name01";

        List<User> userList = List.of(
                new User("1", "userId01", name, "departmentName01", 82, "01012341234", "name01@pinpoint.com"),
                new User("2", "userId02", name, "departmentName01", 82, "01012341234", "name02@pinpoint.com"),
                new User("3", "userId03", name, "departmentName01", 82, "01012341234", "name03@pinpoint.com"),
                new User("4", "userId04", name, "departmentName01", 82, "01012341234", "name04@pinpoint.com"),
                new User("5", "userId05", name, "departmentName01", 82, "01012341234", "name05@pinpoint.com")
        );

        when(userDao.selectUserByUserName(name)).thenReturn(userList);
        List<User> result = userService.selectUserByUserName("name01");

        assertThat(result).hasSize(5);
        for (User user : result) {
            assertTrue(user.getUserId().startsWith("userId"));
            assertTrue(user.getName().startsWith("name"));
            assertEquals(user.getPhoneCountryCode(), 82);
            assertEquals(user.getPhoneNumber(), DECODED_PHONE_NUMBER);
            assertEquals(user.getEmail(), DECODED_EMAIL);
        }
    }

    @Test
    public void selectUserByUserId() {
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
        assertEquals(decodedUser.getPhoneNumber(), DECODED_PHONE_NUMBER);
        assertEquals(decodedUser.getEmail(), DECODED_EMAIL);
    }

    @Test
    public void selectUserByDepartment() {
        String departmentName = "departmentName";

        List<User> userList = List.of(
                new User("1", "userId01", "name01", departmentName, 82, "01012341234", "name01@pinpoint.com"),
                new User("2", "userId02", "name02", departmentName, 82, "01012341234", "name02@pinpoint.com"),
                new User("3", "userId03", "name03", departmentName, 82, "01012341234", "name03@pinpoint.com"),
                new User("4", "userId04", "name04", departmentName, 82, "01012341234", "name04@pinpoint.com"),
                new User("5", "userId05", "name05", departmentName, 82, "01012341234", "name05@pinpoint.com")
        );

        when(userDao.selectUserByDepartment(departmentName)).thenReturn(userList);
        List<User> result = userService.selectUserByDepartment(departmentName);

        assertThat(result).hasSize(5);
        for (User user : result) {
            assertTrue(user.getUserId().startsWith("userId"));
            assertTrue(user.getName().startsWith("name"));
            assertEquals(user.getPhoneCountryCode(), 82);
            assertEquals(user.getPhoneNumber(), DECODED_PHONE_NUMBER);
            assertEquals(user.getEmail(), DECODED_EMAIL);
        }
    }

    @Test
    public void searchUser() {
        String condition = "part";

        List<User> userList = List.of(
                new User("1", "userId01", "name01", "departmentName", 82, "01012341234", "name01@pinpoint.com"),
                new User("2", "userId02", "name02", "departmentName", 82, "01012341234", "name02@pinpoint.com"),
                new User("3", "userId03", "name03", "departmentName", 82, "01012341234", "name03@pinpoint.com"),
                new User("4", "userId04", "name04", "departmentName", 82, "01012341234", "name04@pinpoint.com"),
                new User("5", "userId05", "name05", "departmentName", 82, "01012341234", "name05@pinpoint.com")
        );

        when(userDao.searchUser(condition)).thenReturn(userList);
        List<User> result = userService.searchUser(condition);

        assertThat(result).hasSize(5);
        for (User user : result) {
            assertTrue(user.getUserId().startsWith("userId"));
            assertTrue(user.getName().startsWith("name"));
            assertEquals(user.getPhoneCountryCode(), 82);
            assertEquals(user.getPhoneNumber(), DECODED_PHONE_NUMBER);
            assertEquals(user.getEmail(), DECODED_EMAIL);
        }
    }

    private class CustomUserInfoDecoder implements UserInfoDecoder {

        @Override
        public List<String> decodePhoneNumberList(List<String> phoneNumberList) {
            List<String> changedPhoneNumberList = new ArrayList<>(phoneNumberList.size());
            for (int i = 0; i < phoneNumberList.size(); i++) {
                changedPhoneNumberList.add(DECODED_PHONE_NUMBER);
            }

            return changedPhoneNumberList;
        }

        @Override
        public String decodePhoneNumber(String phoneNumber) {
            return DECODED_PHONE_NUMBER;
        }

        @Override
        public List<User> decodeUserInfoList(List<User> userList) {
            if (CollectionUtils.isEmpty(userList)) {
                return userList;
            }

            List<User> decodedUserList = new ArrayList<>(userList.size());
            for (User user : userList) {
                decodedUserList.add(decodeUserInfo(user));
            }

            return decodedUserList;
        }

        @Override
        public User decodeUserInfo(User user) {
            if (user == null) {
                return user;
            }

            String phoneNumber = decodePhoneNumber(user.getPhoneNumber());
            String email = decodeEmail(user.getEmail());
            return new User(user.getNumber(), user.getUserId(), user.getName(), user.getDepartment(), user.getPhoneCountryCode(), phoneNumber, email);
        }

        @Override
        public List<String> decodeEmailList(List<String> emailList) {
            List<String> encodedEmailList = new ArrayList<>(emailList.size());
            for (int i = 0; i < emailList.size(); i++) {
                encodedEmailList.add(DECODED_EMAIL);
            }

            return encodedEmailList;
        }

        private String decodeEmail(String email) {
            return DECODED_EMAIL;
        }
    }

    private class CustomUserInfoEncoder implements UserInfoEncoder {

        @Override
        public User encodeUserInfo(User user) {
            return new User(user.getNumber(), user.getUserId(), user.getName(), user.getDepartment(), user.getPhoneCountryCode(), ENCODED_PHONE_NUMBER, ENCODED_EMAIL);
        }

        @Override
        public List<User> encodeUserInfoList(List<User> userList) {
            List<User> encodedUserList = new ArrayList<>(userList.size());

            for (User user : userList) {
                User encodedUser = new User(user.getNumber(), user.getUserId(), user.getName(), user.getDepartment(), user.getPhoneCountryCode(), ENCODED_PHONE_NUMBER, ENCODED_EMAIL);
                encodedUserList.add(encodedUser);
            }

            return encodedUserList;
        }
    }

}