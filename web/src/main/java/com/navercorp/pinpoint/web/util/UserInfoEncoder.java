package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.web.vo.User;

import java.util.List;

public interface UserInfoEncoder {
    User encodeUserInfo(User user);

    List<User> encodeUserInfoList(List<User> userList);
}
