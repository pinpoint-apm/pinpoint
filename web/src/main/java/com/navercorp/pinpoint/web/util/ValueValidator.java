/*
 * Copyright 2019 NAVER Corp.
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
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author minwoo.jung
 */
public class ValueValidator {

    private static final int USER_ID_MAX_LENGTH = 24;
    private static final int USER_ID_MIN_LENGTH = 4;
    private static final String USER_ID_PATTERN_EXPRESSION = "[a-z0-9\\-_]+";
    private static final Pattern USER_ID_PATTERN = Pattern.compile(USER_ID_PATTERN_EXPRESSION);

    private static final int PASSWORD_MAX_LENGTH = 30;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final String PASSWORD_PATTERN_EXPRESSION = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%\\^&*\\(\\)])[A-Za-z\\d!@#$%\\^&*\\(\\)]+$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_PATTERN_EXPRESSION);

    private static final int NAME_MAX_LENGTH = 30;
    private static final int NAME_MIN_LENGTH = 1;
    private static final String NAME_PATTERN_EXPRESSION = "[가-힣A-Za-z0-9\\.\\-_]+";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_EXPRESSION);

    private static final int DEPARTMENT_MAX_LENGTH = 40;
    private static final int DEPARTMENT_MIN_LENGTH = 3;
    private static final String DEPARTMENT_PATTERN_EXPRESSION = "[가-힣A-Za-z0-9\\.\\-_]+";
    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile(DEPARTMENT_PATTERN_EXPRESSION);

    private static final int ROLE_ID_MAX_LENGTH = 24;
    private static final int ROLE_ID_MIN_LENGTH = 3;
    private static final String ROLE_ID_PATTERN_EXPRESSION = "[A-Za-z0-9\\-_]+";
    private static final Pattern ROLE_ID_PATTERN = Pattern.compile(ROLE_ID_PATTERN_EXPRESSION);

    private static final int USER_GROUP_ID_MAX_LENGTH = 30;
    private static final int USER_GROUP_ID_MIN_LENGTH = 4;
    private static final String USER_GROUP_ID_PATTERN_EXPRESSION = "[A-Za-z0-9\\-_]+";
    private static final Pattern USER_GROUP_ID_PATTERN = Pattern.compile(USER_GROUP_ID_PATTERN_EXPRESSION);

    private static final int PHONENUMBER_MAX_LENGTH = 24;
    private static final int PHONENUMBER_MIN_LENGTH = 3;
    private static final String PHONENUMBER_PATTERN_EXPRESSION = "[0-9]+";
    private static final Pattern PHONENUMBER_PATTERN = Pattern.compile(PHONENUMBER_PATTERN_EXPRESSION);

    private static final int EMAIL_MAX_LENGTH = 60;
    private static final int EMAIL_MIN_LENGTH = 3;
    private static final String EMAIL_PATTERN_EXPRESSION = "^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_PATTERN_EXPRESSION);

    public static boolean validateUserId(String userId) {
        if (validateLength(userId, USER_ID_MAX_LENGTH, USER_ID_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = USER_ID_PATTERN.matcher(userId);
        return matcher.matches();
    }

    protected static boolean validateLength(final String value, final int max, final int min) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }

        if (value.length() > max) {
            return false;
        }

        if (value.length() < min) {
            return false;
        }

        return true;
    }

    public static boolean validatePassword(String password) {
        if (validateLength(password, PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }

    public static boolean validateDepartment(String department) {
        if (validateLength(department, DEPARTMENT_MAX_LENGTH, DEPARTMENT_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = DEPARTMENT_PATTERN.matcher(department);
        return matcher.matches();
    }

    public static boolean validateName(String name) {
        if (validateLength(name, NAME_MAX_LENGTH, NAME_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = NAME_PATTERN.matcher(name);
        return matcher.matches();
    }

    public static boolean validateRoleId(String roleId) {
        if (validateLength(roleId, ROLE_ID_MAX_LENGTH, ROLE_ID_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = ROLE_ID_PATTERN.matcher(roleId);
        return matcher.matches();
    }

    public static boolean validateUserGroupId(String userGroupId) {
        if (validateLength(userGroupId, USER_GROUP_ID_MAX_LENGTH, USER_GROUP_ID_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = USER_GROUP_ID_PATTERN.matcher(userGroupId);
        return matcher.matches();
    }

    public static boolean validatePhonenumber(String phonenumber) {
        if (validateLength(phonenumber, PHONENUMBER_MAX_LENGTH, PHONENUMBER_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = PHONENUMBER_PATTERN.matcher(phonenumber);
        return matcher.matches();
    }

    public static boolean validateEmail(String email) {
        if (validateLength(email, EMAIL_MAX_LENGTH, EMAIL_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    public static boolean validateUser(User user) {
        if (validateUserId(user.getUserId()) == false) {
            return false;
        }
        if (validateName(user.getName()) == false) {
            return false;
        }
        if (StringUtils.hasLength(user.getDepartment())) {
            if (validateDepartment(user.getDepartment()) == false) {
                return false;
            }
        }
        if (StringUtils.hasLength(user.getPhoneNumber())) {
            if (validatePhonenumber(user.getPhoneNumber()) == false) {
                return false;
            }
        }
        if (StringUtils.hasLength(user.getEmail())) {
            if (validateEmail(user.getEmail()) == false) {
                return false;
            }
        }

        return true;
    }
}
