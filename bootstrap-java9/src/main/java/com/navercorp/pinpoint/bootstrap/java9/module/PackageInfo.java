/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.java9.module;

import com.navercorp.pinpoint.bootstrap.module.Providers;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PackageInfo {
    private final Set<String> packageSet;
    private final List<Providers> providersList;

    public PackageInfo(Set<String> packageSet, List<Providers> providersList) {
        this.packageSet = Assert.requireNonNull(packageSet, "packageSet");
        this.providersList = Assert.requireNonNull(providersList, "providersList");
    }

    public Set<String> getPackage() {
        return packageSet;
    }

    public List<Providers> getProviders() {
        return providersList;
    }

    @Override
    public String toString() {
        return "PackageInfo{" +
                "packageSet=" + packageSet +
                ", providersList=" + providersList +
                '}';
    }
}
