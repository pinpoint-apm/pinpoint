/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.filedescriptor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.UnsupportedMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.filedescriptor.DefaultFileDescriptorMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetricSnapshot;

/**
 * @author Roy Kim
 */
public class FileDescriptorMetricCollectorProvider implements Provider<AgentStatMetricCollector<FileDescriptorMetricSnapshot>> {

    private final FileDescriptorMetric fileDescriptorMetric;

    @Inject
    public FileDescriptorMetricCollectorProvider(FileDescriptorMetric fileDescriptorMetric) {
        this.fileDescriptorMetric = Assert.requireNonNull(fileDescriptorMetric, "fileDescriptorMetric");
    }

    @Override
    public AgentStatMetricCollector<FileDescriptorMetricSnapshot> get() {
        if (fileDescriptorMetric == FileDescriptorMetric.UNSUPPORTED_FILE_DESCRIPTOR_METRIC) {
            return new UnsupportedMetricCollector<FileDescriptorMetricSnapshot>();
        }
        return new DefaultFileDescriptorMetricCollector(fileDescriptorMetric);
    }
}
