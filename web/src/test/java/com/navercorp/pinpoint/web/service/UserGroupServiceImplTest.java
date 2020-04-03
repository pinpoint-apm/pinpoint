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

import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import com.navercorp.pinpoint.web.vo.UserPhoneInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@RunWith(MockitoJUnitRunner.class)
public class UserGroupServiceImplTest {

    @Mock
    private UserGroupDao userGroupDao;
    @Mock
    private UserInfoDecoder userInfoDecoder;
    @Mock
    private UserService userService;
    @Mock
    private AlarmService alarmService;

    UserGroupServiceImpl userGroupService;

    @Before
    public void before() throws Exception {
        userGroupService = new UserGroupServiceImpl(userGroupDao, Optional.of(userInfoDecoder), alarmService, new ConfigProperties(), userService);
    }

    @Test
    public void selectPhoneNumberOfMemberTest() {
        final String groupId = "test_group";
        List<String> phoneNumberWithHyphenList = new ArrayList<>();
        phoneNumberWithHyphenList.add("010-1111-1111");
        phoneNumberWithHyphenList.add("-010-2222-2222-");


        userGroupService = new UserGroupServiceImpl(userGroupDao, Optional.empty(), alarmService, new ConfigProperties(), userService);
        when(userGroupDao.selectPhoneNumberOfMember(groupId)).thenReturn(phoneNumberWithHyphenList);
        List<String> phoneNumberList = userGroupService.selectPhoneNumberOfMember(groupId);

        assertEquals(2, phoneNumberList.size());
        assertEquals(phoneNumberList.get(0), "01011111111");
        assertEquals(phoneNumberList.get(1), "01022222222");
    }

    @Test
    public void selectPhoneNumberOfMember2Test() {
        final String groupId = "test_group";
        List<String> encodedPhoneNumberList = new ArrayList<>();
        encodedPhoneNumberList.add("ASDFG@#$%T");
        encodedPhoneNumberList.add("ASDF@#%$HG");

        List<String> decodedPhoneNumberList = new ArrayList<>();
        decodedPhoneNumberList.add("010-1111-1111");
        decodedPhoneNumberList.add("010-2222-2222");


        when(userGroupDao.selectPhoneNumberOfMember(groupId)).thenReturn(encodedPhoneNumberList);
        when(userInfoDecoder.decodePhoneNumberList(encodedPhoneNumberList)).thenReturn(decodedPhoneNumberList);
        List<String> phoneNumberList = userGroupService.selectPhoneNumberOfMember(groupId);

        assertEquals(2, phoneNumberList.size());
        assertEquals(phoneNumberList.get(0), "01011111111");
        assertEquals(phoneNumberList.get(1), "01022222222");
    }


    @Test
    public void selectPhoneNumberOfMember3Test() {
        final String groupId = "test_group";
        List<String> encodedPhoneNumberList = new ArrayList<>();
        encodedPhoneNumberList.add("ASDFG@#$%T");
        encodedPhoneNumberList.add("ASDF@#%$HG");

        List<String> decodedPhoneNumberList = new ArrayList<>();
        decodedPhoneNumberList.add("01011111111");
        decodedPhoneNumberList.add("01022222222");

        when(userGroupDao.selectPhoneNumberOfMember(groupId)).thenReturn(encodedPhoneNumberList);
        when(userInfoDecoder.decodePhoneNumberList(encodedPhoneNumberList)).thenReturn(decodedPhoneNumberList);
        List<String> phoneNumberList = userGroupService.selectPhoneNumberOfMember(groupId);

        assertEquals(2, phoneNumberList.size());
        assertEquals(phoneNumberList.get(0), "01011111111");
        assertEquals(phoneNumberList.get(1), "01022222222");
    }

    @Test
    public void selectPhoneInfoOfMemberTest() {

        UserGroupService userGroupService = new UserGroupServiceImpl(userGroupDao, Optional.of(new CustomUserInfoDecoder()), alarmService, new ConfigProperties(), userService);

        UserInfoDecoder userInfoDecoder = new CustomUserInfoDecoder();

        String groupId = "groupId";
        List<UserPhoneInfo> userPhoneInfoList = new ArrayList<>(2);
        userPhoneInfoList.add(new UserPhoneInfo(82, "ASDFG@#$%T"));
        userPhoneInfoList.add(new UserPhoneInfo(82, "ASDF@#%$HG"));

        when(userGroupDao.selectPhoneInfoOfMember(groupId)).thenReturn(userPhoneInfoList);

        List<UserPhoneInfo> decodedUserPhoneInfoList = userGroupService.selectPhoneInfoOfMember("groupId");

        for (UserPhoneInfo userPhoneInfo : decodedUserPhoneInfoList) {
            assertEquals(userPhoneInfo.getPhoneCountryCode(), 82);
            assertEquals(userPhoneInfo.getPhoneNumber(), REMOVED_HYPHEN_CHANGED_PHONE_NUMBER);
        }
    }

    private final static String CHANGED_PHONE_NUMBER = "123-4567-8900";
    private final static String REMOVED_HYPHEN_CHANGED_PHONE_NUMBER = "12345678900";

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