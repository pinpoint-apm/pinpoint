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
package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

/**
 * @author Seonghyun Oh
 */
public class OpenwhiskConstants {
    public static final ServiceType OPENWHISK_INTERNAL = ServiceTypeProvider.getByName("OPENWHISK_INTERNAL");
    public static final ServiceType OPENWHISK_CONTROLLER = ServiceTypeProvider.getByName("OPENWHISK_CONTROLLER");
    public static final ServiceType OPENWHISK_INVOKER = ServiceTypeProvider.getByName("OPENWHISK_INVOKER");
    public static final ServiceType OPENWHISK_CLIENT = ServiceTypeProvider.getByName("OPENWHISK_CLIENT");

    public static final ServiceType COUCHDB = ServiceTypeProvider.getByName("COUCHDB");
    public static final ServiceType COUCHDB_EXECUTE_QUERY = ServiceTypeProvider.getByName("COUCHDB_EXECUTE_QUERY");

    public static final AnnotationKey MARKER_MESSAGE = AnnotationKeyProvider.getByCode(923);
}
