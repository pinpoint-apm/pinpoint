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

package com.navercorp.pinpoint.user.vo;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String number;
    private String userId;
    private String name;
    private String department;
    private int phoneCountryCode;
    private String phoneNumber;
    private String email;
    
    public User() {
    }
    
    public User(String userId, String name, String department, int phoneCountryCode, String phoneNumber, String email) {
        this.userId = userId;
        this.name = name;
        this.department = department;
        this.phoneCountryCode = phoneCountryCode;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public User(String number, String userId, String name, String department, int phoneCountryCode, String phoneNumber, String email) {
        this.number = number;
        this.userId = userId;
        this.name = name;
        this.department = department;
        this.phoneCountryCode = phoneCountryCode;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public int getPhoneCountryCode() {
        return phoneCountryCode;
    }

    public void setPhoneCountryCode(int phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }

    public static List<String> removeHyphenForPhoneNumberList(List<String> phoneNumberList) {
        if (CollectionUtils.isEmpty(phoneNumberList)) {
            return phoneNumberList;
        }

        List<String> editedPhoneNumberList = new ArrayList<>(phoneNumberList.size());

        for (String phoneNumber : phoneNumberList) {
            if (phoneNumber == null) {
                continue;
            } else if (phoneNumber.contains("-")) {
                editedPhoneNumberList.add(removeHyphenForPhoneNumber(phoneNumber));
            } else {
                editedPhoneNumberList.add(phoneNumber);
            }
        }

        return editedPhoneNumberList;
    }

    public static String removeHyphenForPhoneNumber(String phoneNumber) {
        return StringUtils.remove(phoneNumber, '-');
    }

    @Override
    public String toString() {
        return "User{" +
                "number='" + number + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", phoneCountryCode=" + phoneCountryCode +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
