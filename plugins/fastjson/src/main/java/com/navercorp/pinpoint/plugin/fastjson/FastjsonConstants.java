/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * The type Fastjson constants.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/17
 */
public final class FastjsonConstants {

    private FastjsonConstants() {
    }

    /**
     * The constant SERVICE_TYPE.
     */
    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(5013, "FASTJSON");
    /**
     * The constant ANNOTATION_KEY_JSON_LENGTH.
     */
    public static final AnnotationKey ANNOTATION_KEY_JSON_LENGTH = AnnotationKeyFactory.of(9003, "fastjson.json.length");

    /**
     * The constant SCOPE.
     */
    public static final String SCOPE = "FASTJSON_SCOPE";

    /**
     * The constant CONFIG.
     */
    public static final String CONFIG = "profiler.json.fastjson";
}
