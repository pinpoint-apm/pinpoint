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
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author minwoo.jung
 */
public class ValueValidatorTest {

    @Test
    public void testValidateUserId() {
        //length test
        assertFalse(ValueValidator.validateUserId(""));
        assertFalse(ValueValidator.validateUserId("uuu"));
        assertFalse(ValueValidator.validateUserId("1234567890123456789012345"));
        assertTrue(ValueValidator.validateUserId("123456789012345678901234"));

        //uppercase test
        assertFalse(ValueValidator.validateUserId("AAAaaa"));
        assertFalse(ValueValidator.validateUserId("aaaAAA"));
        assertFalse(ValueValidator.validateUserId("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateUserId("aaa."));
        assertFalse(ValueValidator.validateUserId("a(aa"));
        assertFalse(ValueValidator.validateUserId("!aaa"));

        //success test
        assertTrue(ValueValidator.validateUserId("pinpoint-userid"));
        assertTrue(ValueValidator.validateUserId("pinpoint-___"));
        assertTrue(ValueValidator.validateUserId("pinpoint--_userid"));
        assertTrue(ValueValidator.validateUserId("-pinpoint_userid-"));
    }

    @Test
    public void testValidatePassword() {
        //length test
        assertFalse(ValueValidator.validatePassword(""));
        assertFalse(ValueValidator.validatePassword("1234567"));
        assertFalse(ValueValidator.validatePassword("12345678901234567890123456789012"));
        assertTrue(ValueValidator.validatePassword("AAAAbbbb1234!@#"));

        //whitespace  character
        assertFalse(ValueValidator.validatePassword("aaa   aa"));
        assertFalse(ValueValidator.validatePassword("   aaa"));
        assertFalse(ValueValidator.validatePassword("aaa   "));

        //requirement
        assertFalse(ValueValidator.validatePassword("1234!@#"));
        assertFalse(ValueValidator.validatePassword("AAAAbbbb!@#"));
        assertFalse(ValueValidator.validatePassword("AAAAbbbb1234"));
        assertFalse(ValueValidator.validatePassword("bbbb1234!@#가나다"));
        assertTrue(ValueValidator.validatePassword("bbbb1234!@#"));
        assertTrue(ValueValidator.validatePassword("AAAA1234!@#"));
        assertTrue(ValueValidator.validatePassword("1234!@#bbb"));
    }

    @Test
    public void testValidateName() {
        //length test
        assertFalse(ValueValidator.validateName(""));
        assertTrue(ValueValidator.validateName("1"));
        assertFalse(ValueValidator.validateName("1234567890123456789012345678901234"));
        assertTrue(ValueValidator.validateName("123456789012345678901234567890"));

        //uppercase test
        assertTrue(ValueValidator.validateName("AAAaaa"));
        assertTrue(ValueValidator.validateName("aaaAAA"));
        assertTrue(ValueValidator.validateName("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateName("aaa!@#"));
        assertFalse(ValueValidator.validateName("a(aa"));
        assertFalse(ValueValidator.validateName("!aaa"));

        //success test
        assertTrue(ValueValidator.validateName("pinpoint-name"));
        assertTrue(ValueValidator.validateName("pinpoint-___"));
        assertTrue(ValueValidator.validateName("pinpoint--_name"));
        assertTrue(ValueValidator.validateName("-pinpoint_name-"));
        assertTrue(ValueValidator.validateName(".pinpoint.name."));
        assertTrue(ValueValidator.validateName("가나다"));
        assertTrue(ValueValidator.validateName("가.나-다"));
    }

    @Test
    public void testValidateDepartment() {
        //length test
        assertFalse(ValueValidator.validateDepartment(""));
        assertFalse(ValueValidator.validateDepartment("na"));
        assertFalse(ValueValidator.validateDepartment("12345678901234567890123456789012345678901"));
        assertTrue(ValueValidator.validateDepartment("1234567890123456789012345678901234567890"));

        //uppercase test
        assertTrue(ValueValidator.validateDepartment("AAAaaa"));
        assertTrue(ValueValidator.validateDepartment("aaaAAA"));
        assertTrue(ValueValidator.validateDepartment("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateDepartment("aaa!@#"));
        assertFalse(ValueValidator.validateDepartment("a(aa"));
        assertFalse(ValueValidator.validateDepartment("!aaa"));

        //success test
        assertTrue(ValueValidator.validateDepartment("pinpoint-department"));
        assertTrue(ValueValidator.validateDepartment("pinpoint-___"));
        assertTrue(ValueValidator.validateDepartment("pinpoint--department"));
        assertTrue(ValueValidator.validateDepartment("-pinpoint_department-"));
        assertTrue(ValueValidator.validateDepartment(".pinpoint.department."));
        assertTrue(ValueValidator.validateDepartment("가나다"));
        assertTrue(ValueValidator.validateDepartment("가.나-다"));
    }

    @Test
    public void testValidateRoleId() {
        //length test
        assertFalse(ValueValidator.validateRoleId(""));
        assertFalse(ValueValidator.validateRoleId("ro"));
        assertFalse(ValueValidator.validateRoleId("1234567890123456789012345"));
        assertTrue(ValueValidator.validateRoleId("123456789012345678901234"));

        //uppercase test
        assertTrue(ValueValidator.validateRoleId("AAAaaa"));
        assertTrue(ValueValidator.validateRoleId("aaaAAA"));
        assertTrue(ValueValidator.validateRoleId("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateRoleId("aaa!@#"));
        assertFalse(ValueValidator.validateRoleId("a(aa"));
        assertFalse(ValueValidator.validateRoleId("!aaa"));
        assertFalse(ValueValidator.validateRoleId(".aaa"));

        //success test
        assertTrue(ValueValidator.validateRoleId("role-id"));
        assertTrue(ValueValidator.validateRoleId("roleid-___"));
        assertTrue(ValueValidator.validateRoleId("role--_id"));
        assertTrue(ValueValidator.validateRoleId("-role_id-"));
    }

    @Test
    public void testValidatePhonenumber() {
        //length test
        assertFalse(ValueValidator.validatePhonenumber(""));
        assertFalse(ValueValidator.validatePhonenumber("12"));
        assertFalse(ValueValidator.validatePhonenumber("1234567890123456789012345"));
        assertTrue(ValueValidator.validatePhonenumber("123456789012345678901234"));

        //character test
        assertFalse(ValueValidator.validatePhonenumber("AAA"));
        assertFalse(ValueValidator.validatePhonenumber("aaa"));
        assertFalse(ValueValidator.validatePhonenumber("!#$"));
        assertFalse(ValueValidator.validatePhonenumber("123AABB"));
        assertFalse(ValueValidator.validatePhonenumber("123!@bb"));

        //success test
        assertTrue(ValueValidator.validatePhonenumber("123455667"));
    }

    @Test
    public void testValidateEmail() {
        //length test
        assertFalse(ValueValidator.validateEmail(""));
        assertFalse(ValueValidator.validateEmail("mail"));
        assertFalse(ValueValidator.validateEmail("1234567890123456789012345678901234567890123456789012345678901"));
        assertTrue(ValueValidator.validateEmail("12345678901234567@naver.com"));

        //character test
        assertFalse(ValueValidator.validateEmail("AAA!@#@naver.com"));
        assertFalse(ValueValidator.validateEmail("AAA.com"));
        assertFalse(ValueValidator.validateEmail("@@@@naver.com"));
        assertFalse(ValueValidator.validateEmail("pinpoint_dev@navercorp.!@#"));
        assertFalse(ValueValidator.validateEmail("pinpoint_dev@naver!!corp.com"));
        assertFalse(ValueValidator.validateEmail("pinpoint##dev@navercorp.com"));

        //success test
        assertTrue(ValueValidator.validateEmail("pinpoint_dev@navercorp.com"));
        assertTrue(ValueValidator.validateEmail("pinpoint-dev@navercorp.com"));
        assertTrue(ValueValidator.validateEmail("pinpoint.dev@navercorp.com"));
        assertTrue(ValueValidator.validateEmail("pinpoint.dev@naver.corp.com"));
        assertTrue(ValueValidator.validateEmail("pinpoint.dev@naver-corp.com"));
    }

    @Test
    public void testValidateUserGroupId() {
        //length test
        assertFalse(ValueValidator.validateUserGroupId(""));
        assertFalse(ValueValidator.validateUserGroupId("use"));
        assertFalse(ValueValidator.validateUserGroupId("1234567890123456789012345678901"));
        assertTrue(ValueValidator.validateUserGroupId("123456789012345678901234567890"));

        //uppercase test
        assertTrue(ValueValidator.validateUserGroupId("AAAaaa"));
        assertTrue(ValueValidator.validateUserGroupId("aaaAAA"));
        assertTrue(ValueValidator.validateUserGroupId("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateUserGroupId("aaa!@#"));
        assertFalse(ValueValidator.validateUserGroupId("a(aa"));
        assertFalse(ValueValidator.validateUserGroupId("!aaa"));
        assertFalse(ValueValidator.validateUserGroupId(".aaa"));

        //success test
        assertTrue(ValueValidator.validateUserGroupId("userGroup-id"));
        assertTrue(ValueValidator.validateUserGroupId("userGroupId-___"));
        assertTrue(ValueValidator.validateUserGroupId("userGroup--_id"));
        assertTrue(ValueValidator.validateUserGroupId("-userGroup_id-"));
    }

    @Test
    public void testValidateUser() {
        assertTrue(ValueValidator.validateUser(new User("userid", "name", "", 82,"", "")));
        assertTrue(ValueValidator.validateUser(new User("userid", "name", null, 82,null, null)));
        assertTrue(ValueValidator.validateUser(new User("userid", "name", "AAA", 82,null, null)));
        assertTrue(ValueValidator.validateUser(new User("userid", "name", "AAA", 82,"01012341234", "naver@naver.com")));
        assertFalse(ValueValidator.validateUser(new User("", "", "", 82,"", "")));
        assertFalse(ValueValidator.validateUser(new User("", "name", "", 82,"", "")));
        assertFalse(ValueValidator.validateUser(new User("userid", "", "", 82,"", "")));
    }
}