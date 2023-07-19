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
package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.web.vo.User;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class DefaultUserInfoDecoder implements UserInfoDecoder {

    public static final DefaultUserInfoDecoder EMPTY_USER_INFO_DECODER = new DefaultUserInfoDecoder();

    private DefaultUserInfoDecoder() {
    }

    @Override
    public List<String> decodePhoneNumberList(List<String> phoneNumberList) {
        return phoneNumberList;
    }

    @Override
    public String decodePhoneNumber(String phoneNumber) {
        return phoneNumber;
    }

    @Override
    public List<User> decodeUserInfoList(List<User> userList) {
        return userList;
    }

    @Override
    public User decodeUserInfo(User user) {
        return user;
    }

    @Override
    public List<String> decodeEmailList(List<String> emailList) {
        return emailList;
    }
}
