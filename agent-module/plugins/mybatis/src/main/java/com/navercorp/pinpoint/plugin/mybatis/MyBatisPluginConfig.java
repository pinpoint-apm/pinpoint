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
 *
 */

package com.navercorp.pinpoint.plugin.mybatis;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MyBatisPluginConfig {

    private final boolean myBatisEnabled;

    private final boolean markError;

    public MyBatisPluginConfig(ProfilerConfig profilerConfig) {
        this.myBatisEnabled = profilerConfig.readBoolean("profiler.orm.mybatis", true);
        this.markError = profilerConfig.readBoolean("profiler.orm.mybatis.markerror", false);
    }

    public boolean isMyBatisEnabled() {
        return myBatisEnabled;
    }

    public boolean isMarkError() {
        return markError;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MyBatisPluginConfig{");
        sb.append("myBatisEnabled=").append(myBatisEnabled);
        sb.append('}');
        return sb.toString();
    }
}
