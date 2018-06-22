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

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.util.BodyFactory;
import com.navercorp.pinpoint.io.util.TypeLocator;
import com.navercorp.pinpoint.io.util.TypeLocatorBuilder;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author koo.taejin
 */
public class TCommandRegistry {


    public static TypeLocator<TBase<?, ?>> build(TCommandTypeVersion version) {
        return build(version.getSupportCommandList());
    }
    public static TypeLocator<TBase<?, ?>> build(List<TCommandType> supportCommandList) {
        TypeLocatorBuilder<TBase<?, ?>> builder = new TypeLocatorBuilder<TBase<?, ?>>();

        for (final TCommandType commandType : supportCommandList) {
            builder.addBodyFactory(commandType.getCode(), commandType.getBodyFactory());
        }

        TypeLocator<TBase<?, ?>> typeLocator = builder.build();
        return typeLocator;
    }
}
