/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.plugin;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PinpointProfilerPackageSkipFilter implements ClassNameFilter {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final String[] packageList;

    public PinpointProfilerPackageSkipFilter() {
        this(getPinpointPackageList());
    }

    public PinpointProfilerPackageSkipFilter(List<String> packageList) {
        Objects.requireNonNull(packageList, "packageList");
        this.packageList = packageList.toArray(new String[0]);
    }



    @Override
    public boolean accept(String className) {
        Objects.requireNonNull(className, "className");

        for (String packageName : packageList) {
            if (className.startsWith(packageName)) {
                if (logger.isDebugEnabled()) {
                    logger.info("skip ProfilerPackage:{} Class:{}", packageName, className);
                }
                return REJECT;
            }
        }
        return ACCEPT;
    }

    private static List<String> getPinpointPackageList() {
        List<String> pinpointPackageList = new ArrayList<>();
        pinpointPackageList.add("com.navercorp.pinpoint.bootstrap");
        pinpointPackageList.add("com.navercorp.pinpoint.profiler");
        pinpointPackageList.add("com.navercorp.pinpoint.common");
        pinpointPackageList.add("com.navercorp.pinpoint.exception");
        // TODO move test package
        pinpointPackageList.add("com.navercorp.pinpoint.test");
        return pinpointPackageList;
    }
}
