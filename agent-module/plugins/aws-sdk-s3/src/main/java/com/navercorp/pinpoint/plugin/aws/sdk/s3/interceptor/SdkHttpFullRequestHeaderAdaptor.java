/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import software.amazon.awssdk.http.SdkHttpFullRequest;

public class SdkHttpFullRequestHeaderAdaptor implements ClientHeaderAdaptor<SdkHttpFullRequest.Builder> {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void setHeader(SdkHttpFullRequest.Builder header, String name, String value) {
        try {
            header.putHeader(name, value);
            if (isDebug) {
                logger.debug("Set header {}={}", name, value);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean contains(SdkHttpFullRequest.Builder header, String name) {
        try {
            return header.headers().containsKey(name);
        } catch (Exception ignored) {
        }
        return false;
    }
}
