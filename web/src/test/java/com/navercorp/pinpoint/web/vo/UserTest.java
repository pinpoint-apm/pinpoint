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

package com.navercorp.pinpoint.web.vo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class UserTest {

    @Test
    public void testRemoveHyphenForPhoneNumberList() {
        List<String> phoneNumberList = new ArrayList<>();
        phoneNumberList.add("010-1111-1111");
        phoneNumberList.add("010-2222-2222");

        List<String> editedPhoneNumberList = User.removeHyphenForPhoneNumberList(phoneNumberList);
        assertEquals(editedPhoneNumberList.size(), 2);
        assertEquals(editedPhoneNumberList.get(0), "01011111111");
        assertEquals(editedPhoneNumberList.get(1), "01022222222");
    }

}