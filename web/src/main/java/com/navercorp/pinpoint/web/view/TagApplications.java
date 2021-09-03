/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;

public class TagApplications {
    private final String tag;
    private final List<Application> applicationList;

    public TagApplications(String tag, List<Application> applicationList) {
        this.tag = tag;
        this.applicationList = applicationList;
    }

    public String getTag() {
        return tag;
    }

    public List<Application> getApplicationList() {
        return applicationList;
    }
}
