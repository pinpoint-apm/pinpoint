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

import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

import java.nio.charset.Charset;

/**
 * @author Younsung Hwang
 */
public class PahoMqttConstants {

    public static final ServiceType PAHO_MQTT_CLIENT = ServiceTypeProvider.getByCode(8501);
    public static final ServiceType PAHO_MQTT_CLIENT_INTERNAL = ServiceTypeProvider.getByCode(8502);

    public static final AnnotationKey MQTT_TOPIC_ANNOTATION_KEY = AnnotationKeyProvider.getByCode(191);
    public static final AnnotationKey MQTT_BROKER_URI_ANNOTATION_KEY = AnnotationKeyProvider.getByCode(192);
    public static final AnnotationKey MQTT_MESSAGE_PAYLOAD_ANNOTATION_KEY = AnnotationKeyProvider.getByCode(193);
    public static final AnnotationKey MQTT_QOS_ANNOTATION_KEY = AnnotationKeyProvider.getByCode(194);

    public static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

    public static final String UNKNOWN = "Unknown";
}
