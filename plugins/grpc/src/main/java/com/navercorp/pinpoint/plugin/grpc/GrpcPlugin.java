/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

public class GrpcPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final GrpcConfig config = new GrpcConfig(context.getConfig());

        if (config.isClientEnable()) {
            addClientInterceptor();
        }

        if (config.isServerEnable()) {
            addServerInterceptor(config);
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    private void addClientInterceptor() {
        GrpcClientPlugin grpcClientPlugin = new GrpcClientPlugin(transformTemplate);
        grpcClientPlugin.addInterceptor();
    }

    private void addServerInterceptor(GrpcConfig config) {
        GrpcServerPlugin grpcServerPlugin = new GrpcServerPlugin(transformTemplate, config);
        grpcServerPlugin.addInterceptor();
    }

}
