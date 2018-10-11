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

package com.navercorp.pinpoint.profiler.receiver;

import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;

/**
 * @author Taejin Koo
 */
public class CommandSerializer {

    public static final SerializerFactory<HeaderTBaseSerializer> SERIALIZER_FACTORY = new CommandHeaderTBaseSerializerFactory();
    public static final DeserializerFactory<HeaderTBaseDeserializer> DESERIALIZER_FACTORY = new CommandHeaderTBaseDeserializerFactory();

}