/*
 * Copyright 2020 NAVER Corp.
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

/**
 * @author minwoo.jung
 */
public class UserPhoneInfo {

    private int phoneCountryCode;
    private String phoneNumber;

    public UserPhoneInfo() {
    }

    public UserPhoneInfo(int phoneCountryCode, String phoneNumber) {
        this.phoneCountryCode = phoneCountryCode;
        this.phoneNumber = phoneNumber;
    }

    public int getPhoneCountryCode() {
        return phoneCountryCode;
    }

    public void setPhoneCountryCode(int phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserPhoneInfo{");
        sb.append("phoneCountryCode=").append(phoneCountryCode);
        sb.append(", phoneNumber='").append(phoneNumber).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
