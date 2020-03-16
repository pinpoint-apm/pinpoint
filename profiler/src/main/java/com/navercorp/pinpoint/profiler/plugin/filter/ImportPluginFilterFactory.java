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

package com.navercorp.pinpoint.profiler.plugin.filter;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ImportPluginFilterFactory implements PluginFilterFactory {

    private final List<String> importPluginIds;

    public ImportPluginFilterFactory(List<String> importPluginIds) {
        Assert.requireNonNull(importPluginIds, "importPluginIds");
        this.importPluginIds = importPluginIds;
    }

    @Override
    public PluginFilter newPluginFilter() {
        if (CollectionUtils.isEmpty(importPluginIds)) {
            return new JavaVersionFilter();
        }
        PluginFilter javaVersionFilter = new JavaVersionFilter();
        PluginFilter enablePluginFilter = new ImportPluginFilter(importPluginIds);
        return new PluginFilters(enablePluginFilter, javaVersionFilter);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnablePluginJarFilterFactory{");
        sb.append("importPluginIds=").append(importPluginIds);
        sb.append('}');
        return sb.toString();
    }
}
