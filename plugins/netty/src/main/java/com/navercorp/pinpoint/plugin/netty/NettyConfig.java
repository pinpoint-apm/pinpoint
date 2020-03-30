/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.netty;

import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Taejin Koo
 */
public class NettyConfig {

    static final String PLUGIN_ENABLE = "profiler.netty";

    static final String HTTP_CODEC_ENABLE = "profiler.netty.http";
    static final String NETTY_CHANNEL_CLOSE_ENABLE = "profiler.netty.channel.close";

    private final boolean pluginEnable;

    private final boolean httpCodecEnable;
    private final boolean param;
    private final HttpDumpConfig httpDumpConfig;
    private final boolean channelClose;

    public NettyConfig(ProfilerConfig config) {
        pluginEnable = config.readBoolean(PLUGIN_ENABLE, false);
        httpCodecEnable = config.readBoolean(HTTP_CODEC_ENABLE, false);
        param = config.readBoolean("profiler.netty.http.param", false);
        channelClose = config.readBoolean(NETTY_CHANNEL_CLOSE_ENABLE, true);
        httpDumpConfig = HttpDumpConfig.getDefault();
    }

    boolean isPluginEnable() {
        return pluginEnable;
    }

    boolean isHttpCodecEnable() {
        return httpCodecEnable;
    }

    public boolean isParam() {
        return param;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    public boolean isChannelClose() {
        return channelClose;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NettyConfig{");
        sb.append("pluginEnable=").append(pluginEnable);
        sb.append(", httpCodecEnable=").append(httpCodecEnable);
        sb.append(", param=").append(param);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append(", channelClose=").append(channelClose);
        sb.append('}');
        return sb.toString();
    }
}
