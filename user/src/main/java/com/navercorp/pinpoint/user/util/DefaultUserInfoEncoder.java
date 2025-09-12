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

package com.navercorp.pinpoint.user.util;

import com.navercorp.pinpoint.user.vo.User;

import java.util.List;

public class DefaultUserInfoEncoder implements UserInfoEncoder {

    public static final DefaultUserInfoEncoder EMPTY_USER_INFO_ENCODER = new DefaultUserInfoEncoder();

    @Override
    public User encodeUserInfo(User user) {
        return user;
    }

    @Override
    public List<User> encodeUserInfoList(List<User> userList) {
        return userList;
    }
}
