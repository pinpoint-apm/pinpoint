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

import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@RunWith(MockitoJUnitRunner.class)
public class UserGroupServiceImplTest {

    @Mock
    UserGroupDao userGroupDao;

    @Mock
    UserInfoDecoder userInfoDecoder;

    UserGroupServiceImpl userGroupService = new UserGroupServiceImpl();

    @Test
    public void selectPhoneNumberOfMemberTest() {
        final String groupId = "test_group";
        List<String> phoneNumberWithHyphenList = new ArrayList<>();
        phoneNumberWithHyphenList.add("010-1111-1111");
        phoneNumberWithHyphenList.add("-010-2222-2222-");

        ReflectionTestUtils.setField(userGroupService, "userGroupDao", userGroupDao);
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

        ReflectionTestUtils.setField(userGroupService, "userInfoDecoder", userInfoDecoder);
        ReflectionTestUtils.setField(userGroupService, "userGroupDao", userGroupDao);
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

        ReflectionTestUtils.setField(userGroupService, "userInfoDecoder", userInfoDecoder);
        ReflectionTestUtils.setField(userGroupService, "userGroupDao", userGroupDao);
        when(userGroupDao.selectPhoneNumberOfMember(groupId)).thenReturn(encodedPhoneNumberList);
        when(userInfoDecoder.decodePhoneNumberList(encodedPhoneNumberList)).thenReturn(decodedPhoneNumberList);
        List<String> phoneNumberList = userGroupService.selectPhoneNumberOfMember(groupId);

        assertEquals(2, phoneNumberList.size());
        assertEquals(phoneNumberList.get(0), "01011111111");
        assertEquals(phoneNumberList.get(1), "01022222222");
    }

}