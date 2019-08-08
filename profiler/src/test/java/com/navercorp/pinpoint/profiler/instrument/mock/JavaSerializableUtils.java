/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.mock;

import com.navercorp.pinpoint.common.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class JavaSerializableUtils {
    private JavaSerializableUtils() {
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bout = null;
        ObjectOutputStream oout = null;
        try {
            bout = new ByteArrayOutputStream();
            oout = new ObjectOutputStream(bout);
            oout.writeObject(obj);
            return bout.toByteArray();
        } finally {
            IOUtils.closeQuietly(oout);
            IOUtils.closeQuietly(bout);
        }
    }

}
