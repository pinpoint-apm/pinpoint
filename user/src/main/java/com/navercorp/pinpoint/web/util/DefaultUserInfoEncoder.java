package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.web.vo.User;

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
