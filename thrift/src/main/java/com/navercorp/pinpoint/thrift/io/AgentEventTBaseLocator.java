/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.io.util.BodyFactory;
import com.navercorp.pinpoint.io.util.TypeLocator;
import com.navercorp.pinpoint.io.util.TypeLocatorBuilder;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import org.apache.thrift.TBase;

/**
 * @author Taejin Koo
 */
public class AgentEventTBaseLocator {

    private static final short DEADLOCK = 910;

    private static final TypeLocator<TBase<?, ?>> typeLocator = build();

    public static TypeLocator<TBase<?, ?>>build() {

        TypeLocatorBuilder<TBase<?, ?>> builder = new TypeLocatorBuilder<TBase<?, ?>>();
        addBodyFactory(builder);
        return builder.build();
    }

    public static void addBodyFactory(TypeLocatorBuilder<TBase<?, ?>> builder) {
        builder.addBodyFactory(DEADLOCK, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TDeadlock();
            }
        });

    }

    public static TypeLocator<TBase<?, ?>> getTypeLocator() {
        return typeLocator;
    }
}
