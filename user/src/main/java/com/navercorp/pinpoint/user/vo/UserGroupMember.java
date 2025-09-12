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

public class UserGroupMember {
    
    private String number;
    private String userGroupId;
    private String memberId;
    private String name;
    private String department;
    
    public UserGroupMember() {
    }

    public UserGroupMember(String userGroupId, String memberId) {
        this.userGroupId = userGroupId;
        this.memberId = memberId;
    }

    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getUserGroupId() {
        return userGroupId;
    }
    
    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }
    
    public String getMemberId() {
        return memberId;
    }
    
    public void setMemberId(String memberId) {
        this.memberId = memberId;
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

    @Override
    public String toString() {
        return "UserGroupMember{" +
            "number='" + number + '\'' +
            ", userGroupId='" + userGroupId + '\'' +
            ", memberId='" + memberId + '\'' +
            ", name='" + name + '\'' +
            ", department='" + department + '\'' +
            '}';
    }
}
