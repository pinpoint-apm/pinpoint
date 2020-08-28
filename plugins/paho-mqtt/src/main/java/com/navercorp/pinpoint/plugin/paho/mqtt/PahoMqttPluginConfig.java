/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.paho.mqtt;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Yousung Hwang
 */
public class PahoMqttPluginConfig {

    private final boolean tracePahoMqttClient;
    private final boolean tracePahoMqttClientV3;
    private final boolean tracePahoMqttClientV5;
    private final boolean tracePahoMqttClientPublisher;
    private final boolean tracePahoMqttClientSubscriber;

    public PahoMqttPluginConfig(ProfilerConfig config) {
        this.tracePahoMqttClient = config.readBoolean("profiler.paho.mqtt.client.enable", true);
        this.tracePahoMqttClientV3 = config.readBoolean("profiler.paho.mqtt.client.v3.enable", true);
        this.tracePahoMqttClientV5 = config.readBoolean("profiler.paho.mqtt.client.v5.enable", true);
        this.tracePahoMqttClientPublisher = config.readBoolean("profiler.paho.mqtt.client.publisher.enable", true);
        this.tracePahoMqttClientSubscriber = config.readBoolean("profiler.paho.mqtt.client.subscriber.enable", true);
    }

    public boolean isEnableTracePahoMqttClient() {
        return this.tracePahoMqttClient;
    }
    public boolean isEnableTracePahoMqttClientV3() {
        return this.tracePahoMqttClientV3;
    }
    public boolean isEnableTracePahoMqttClientV5() {
        return this.tracePahoMqttClientV5;
    }
    public boolean isEnableTracePahoMqttClientPublisher() {
        return this.tracePahoMqttClientPublisher;
    }
    public boolean isEnableTracePahoMqttClientSubscriber() {
        return this.tracePahoMqttClientSubscriber;
    }

    @Override
    public String toString() {
        return "PahoMqttConfig{" +
                "tracePahoMqttClient=" + tracePahoMqttClient +
                ", tracePahoMqttClientPublisher=" + tracePahoMqttClientPublisher +
                ", tracePahoMqttClientSubscriber=" + tracePahoMqttClientSubscriber +
                '}';
    }
}
