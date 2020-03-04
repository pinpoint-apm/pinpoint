/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.common.hbase;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author minwoo.jung
 */
public class HbaseSecurityInterceptorFactory implements FactoryBean<HbaseSecurityInterceptor> {

    @Autowired(required = false)
    private HbaseSecurityInterceptor hbaseSecurityInterceptor = new EmptyHbaseSecurityInterceptor();

    public HbaseSecurityInterceptor getHbaseSecurityInterceptor() {
        return hbaseSecurityInterceptor;
    }

    @Override
    public HbaseSecurityInterceptor getObject() throws Exception {
        return hbaseSecurityInterceptor;
    }

    @Override
    public Class<?> getObjectType() {
        return HbaseSecurityInterceptor.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
