/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

/**
 * @author emeroad
 */
public abstract class ServiceTypeFactory {

    private static final ServiceTypeFactory DEFAULT_FACTORY = new DefaultServiceTypeFactory();

    public static ServiceType of(int code, String name, ServiceTypeProperty... properties) {
        return of(code, name, name, properties);
    }

    public static ServiceType of(int code, String name, String desc, ServiceTypeProperty... properties) {
        return DEFAULT_FACTORY.createServiceType(code, name, desc, properties);
    }

    ServiceTypeFactory() {
    }



    abstract ServiceType createServiceType(int code, String name, String desc, ServiceTypeProperty... properties);
}
