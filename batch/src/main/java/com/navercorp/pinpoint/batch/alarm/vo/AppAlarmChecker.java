/*
 * Copyright 2022 NAVER Corp.
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
package com.navercorp.pinpoint.batch.alarm.vo;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youngjin.kim2
 */
public class AppAlarmChecker {

    private final String applicationId;
    private final List<AlarmChecker<?>> children;

    public AppAlarmChecker(List<AlarmChecker<?>> children) {
        this.children = haveSameApplication(children);
        this.applicationId = this.children.get(0).getRule().getApplicationName();
    }

    private List<AlarmChecker<?>> haveSameApplication(List<AlarmChecker<?>> children) {
        if (children == null || children.isEmpty()) {
            throw new IllegalArgumentException("children should not be empty");
        }

        final String applicationName = children.get(0).getRule().getApplicationName();
        for (AlarmChecker<?> child : children) {
            if (!applicationName.equals(child.getRule().getApplicationName())) {
                throw new IllegalArgumentException("All children should have the same application: " + applicationName + " != " + child.getRule().getApplicationName());
            }
        }
        return children;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public List<AlarmChecker<?>> getChildren() {
        return this.children;
    }

    public void check() {
        for (AlarmChecker<?> child : this.children) {
            child.check();
        }
    }

    public static List<AlarmChecker<?>> flatten(List<? extends AppAlarmChecker> appAlarmCheckers) {
        List<AlarmChecker<?>> checkers = new ArrayList<>();
        for (AppAlarmChecker appAlarmChecker : appAlarmCheckers) {
            checkers.addAll(appAlarmChecker.getChildren());
        }
        return checkers;
    }
}
