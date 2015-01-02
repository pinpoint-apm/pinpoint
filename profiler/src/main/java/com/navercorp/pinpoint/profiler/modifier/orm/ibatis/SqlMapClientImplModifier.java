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

package com.navercorp.pinpoint.profiler.modifier.orm.ibatis;

import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.filter.SqlMapClientMethodFilter;

/**
 * iBatis SqlMapClientImpl Modifier
 * <p/>
 * Hooks onto <i>com.ibatis.sqlmap.engine.SqlMapClientImpl
 * <p/>
 * 
 * @author Hyun Jeong
 */
public final class SqlMapClientImplModifier extends IbatisClientModifier {

    private static final MethodFilter sqlMapClientMethodFilter = new SqlMapClientMethodFilter();

    public static final String TARGET_CLASS_NAME = "com/ibatis/sqlmap/engine/impl/SqlMapClientImpl";

    public SqlMapClientImplModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public String getTargetClass() {
        return TARGET_CLASS_NAME;
    }


    @Override
    protected MethodFilter getIbatisApiMethodFilter() {
        return sqlMapClientMethodFilter;
    }

}
