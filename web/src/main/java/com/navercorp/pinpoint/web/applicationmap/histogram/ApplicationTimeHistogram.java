/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.navercorp.pinpoint.web.vo.Application;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
public class ApplicationTimeHistogram {
    private final Application application;
    private final List<TimeHistogram> histogramList;

    public ApplicationTimeHistogram(Application application) {
        this(application, Collections.emptyList());
    }

    public ApplicationTimeHistogram(Application application, List<TimeHistogram> histogramList) {
        this.application = Objects.requireNonNull(application, "application");
        this.histogramList = Objects.requireNonNull(histogramList, "histogramList");
    }

    public Application getApplication() {
        return application;
    }

    public List<TimeHistogram> getHistogramList() {
        return histogramList;
    }
}