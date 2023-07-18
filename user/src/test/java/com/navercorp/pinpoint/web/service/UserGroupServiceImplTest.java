/*
 * Copyright 2017 NAVER Corp.
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
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.config.UserConfigProperties;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserPhoneInfo;
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
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@ExtendWith(MockitoExtension.class)
public class UserGroupServiceImplTest {

    @Mock
    private UserGroupDao userGroupDao;
    @Mock
    private UserInfoDecoder userInfoDecoder;
    @Mock
    private UserService userService;

    UserGroupServiceImpl userGroupService;

    @BeforeEach
    public void before() throws Exception {
        userGroupService = new UserGroupServiceImpl(userGroupDao, Optional.of(userInfoDecoder), new UserConfigProperties(), userService);
    }

    @Test
    public void selectPhoneNumberOfMemberTest() {
        final String groupId = "test_group";
        List<String> phoneNumberWithHyphenList = List.of(
                "010-1111-1111",
                "010-2222-2222"
        );


        userGroupService = new UserGroupServiceImpl(userGroupDao, Optional.empty(), new UserConfigProperties(), userService);
        when(userGroupDao.selectPhoneNumberOfMember(groupId)).thenReturn(phoneNumberWithHyphenList);
        List<String> phoneNumberList = userGroupService.selectPhoneNumberOfMember(groupId);

        assertThat(phoneNumberList).hasSize(2)
                .containsExactly("01011111111", "01022222222");
    }

    @Test
    public void selectPhoneNumberOfMember2Test() {
        final String groupId = "test_group";
        List<String> encodedPhoneNumberList = List.of(
                "ASDFG@#$%T",
                "ASDF@#%$HG"
        );

        List<String> decodedPhoneNumberList = List.of(
                "010-1111-1111",
                "010-2222-2222"
        );

        when(userGroupDao.selectPhoneNumberOfMember(groupId)).thenReturn(encodedPhoneNumberList);
        when(userInfoDecoder.decodePhoneNumberList(encodedPhoneNumberList)).thenReturn(decodedPhoneNumberList);
        List<String> phoneNumberList = userGroupService.selectPhoneNumberOfMember(groupId);

        assertThat(phoneNumberList).hasSize(2)
                .containsExactly("01011111111", "01022222222");
    }


    @Test
    public void selectPhoneNumberOfMember3Test() {
        final String groupId = "test_group";
        List<String> encodedPhoneNumberList = List.of(
                "ASDFG@#$%T",
                "ASDF@#%$HG"
        );

        List<String> decodedPhoneNumberList = List.of(
                "01011111111",
                "01022222222"
        );

        when(userGroupDao.selectPhoneNumberOfMember(groupId)).thenReturn(encodedPhoneNumberList);
        when(userInfoDecoder.decodePhoneNumberList(encodedPhoneNumberList)).thenReturn(decodedPhoneNumberList);
        List<String> phoneNumberList = userGroupService.selectPhoneNumberOfMember(groupId);

        assertThat(phoneNumberList).hasSize(2)
                .containsExactly("01011111111", "01022222222");
    }

    @Test
    public void selectPhoneInfoOfMemberTest() {

        UserGroupService userGroupService = new UserGroupServiceImpl(userGroupDao, Optional.of(new CustomUserInfoDecoder()), new UserConfigProperties(), userService);

        String groupId = "groupId";
        List<UserPhoneInfo> userPhoneInfoList = List.of(
                new UserPhoneInfo(82, "ASDFG@#$%T"),
                new UserPhoneInfo(82, "ASDF@#%$HG"),
                new UserPhoneInfo(82, null)
        );

        when(userGroupDao.selectPhoneInfoOfMember(groupId)).thenReturn(userPhoneInfoList);

        List<UserPhoneInfo> decodedUserPhoneInfoList = userGroupService.selectPhoneInfoOfMember("groupId");

        for (UserPhoneInfo userPhoneInfo : decodedUserPhoneInfoList) {
            assertEquals(userPhoneInfo.getPhoneCountryCode(), 82);
            assertEquals(userPhoneInfo.getPhoneNumber(), REMOVED_HYPHEN_CHANGED_PHONE_NUMBER);
        }
    }

    @Test
    public void selectEmailOfMemberTest() {
        UserGroupService userGroupService = new UserGroupServiceImpl(userGroupDao, Optional.of(new CustomUserInfoDecoder()), new UserConfigProperties(), userService);

        String groupId = "groupId";
        List<String> encodedEmailList = List.of(
                "ASDFG@#$%T",
                "ASDF@#%$HG"
        );

        when(userGroupDao.selectEmailOfMember(groupId)).thenReturn(encodedEmailList);

        List<String> decodedEmailList = userGroupService.selectEmailOfMember("groupId");

        for (String email : decodedEmailList) {
            assertEquals(email, DECODED_EMAIL);
        }
    }

    @Test
    public void selectEmailOfMember2Test() {
        final String groupId = "test_group";
        List<String> encodedEmailList = List.of(
                "ASDFG@#$%T",
                "ASDF@#%$HG"
        );

        List<String> decodedEmailList = List.of(
                "user01@navercorp.com",
                "user02@navercorp.com"
        );

        when(userGroupDao.selectEmailOfMember(groupId)).thenReturn(encodedEmailList);
        when(userInfoDecoder.decodeEmailList(encodedEmailList)).thenReturn(decodedEmailList);
        List<String> phoneNumberList = userGroupService.selectEmailOfMember(groupId);

        assertThat(phoneNumberList).hasSize(2)
                .containsExactly("user01@navercorp.com", "user02@navercorp.com");
    }

    private final static String CHANGED_PHONE_NUMBER = "123-4567-8900";
    private final static String REMOVED_HYPHEN_CHANGED_PHONE_NUMBER = "12345678900";
    private final static String DECODED_EMAIL = "user@navercorp.com";

    private static class CustomUserInfoDecoder implements UserInfoDecoder {

        @Override
        public List<String> decodePhoneNumberList(List<String> phoneNumberList) {
            List<String> changedPhoneNumberList = new ArrayList<>(phoneNumberList.size());
            for (int i = 0; i < phoneNumberList.size(); i++) {
                changedPhoneNumberList.add(CHANGED_PHONE_NUMBER);
            }

            return changedPhoneNumberList;
        }

        @Override
        public String decodePhoneNumber(String phoneNumber) {
            return CHANGED_PHONE_NUMBER;
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
                return null;
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


}