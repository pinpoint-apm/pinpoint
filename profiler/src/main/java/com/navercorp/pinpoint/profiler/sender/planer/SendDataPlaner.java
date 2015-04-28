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

package com.navercorp.pinpoint.profiler.sender.planer;

import java.io.IOException;
import java.util.Iterator;

import com.navercorp.pinpoint.profiler.sender.SpanStreamSendData;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;

/**
 * @author Taejin Koo
 */
public interface SendDataPlaner {

    Iterator<SpanStreamSendData> getSendDataIterator() throws IOException;

    Iterator<SpanStreamSendData> getSendDataIterator(SpanStreamSendData spanStreamSendData) throws IOException;

    Iterator<SpanStreamSendData> getSendDataIterator(SpanStreamSendData spanStreamSendData, HeaderTBaseSerializer serializer) throws IOException;
    
}
