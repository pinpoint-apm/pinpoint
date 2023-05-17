/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.pubsub.endpoint;

/**
 * @author youngjin.kim2
 */
public class PubSubServiceDescriptor<D, S> {

    private final String name;
    private final Class<D> demandClass;
    private final Class<S> supplyClass;

    protected PubSubServiceDescriptor(String name, Class<D> demandClass, Class<S> supplyClass) {
        this.name = name;
        this.demandClass = demandClass;
        this.supplyClass = supplyClass;
    }

    public static <D, S> PubSubMonoServiceDescriptor<D, S> mono(String name, Class<D> demandClass, Class<S> supplyClass) {
        return new PubSubMonoServiceDescriptor<>(name, demandClass, supplyClass);
    }

    public static <D, S> PubSubFluxServiceDescriptor<D, S> flux(String name, Class<D> demandClass, Class<S> supplyClass) {
        return new PubSubFluxServiceDescriptor<>(name, demandClass, supplyClass);
    }

    public String getName() {
        return name;
    }

    public Class<D> getDemandClass() {
        return demandClass;
    }

    public Class<S> getSupplyClass() {
        return supplyClass;
    }

}
