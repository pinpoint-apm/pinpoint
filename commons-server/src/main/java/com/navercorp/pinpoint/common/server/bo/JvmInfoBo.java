/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.util.ByteUtils;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class JvmInfoBo {

    private final byte version;
    private final String jvmVersion;
    private final String gcTypeName;

    public JvmInfoBo(int version, String jvmVersion, String gcTypeName) {
        this.version = ByteUtils.toUnsignedByte(version);

        this.jvmVersion = Objects.requireNonNull(jvmVersion, "jvmVersion");
        this.gcTypeName = Objects.requireNonNull(gcTypeName, "gcTypeName");
    }

    public static JvmInfoBo readJvmInfo(byte[] serializedJvmInfoBo) {
        final Buffer buffer = new FixedBuffer(serializedJvmInfoBo);
        final int version = Byte.toUnsignedInt(buffer.readByte());
        if (version == 0) {
            String jvmVersion = buffer.readPrefixedString();
            String gcTypeName = buffer.readPrefixedString();
            return new JvmInfoBo(version, jvmVersion, gcTypeName);
        }
        return new JvmInfoBo(version, "", "");
    }

    public int getVersion() {
        return Byte.toUnsignedInt(version);
    }

    public byte getRawVersion() {
        return version;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }


    public String getGcTypeName() {
        return gcTypeName;
    }


    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putByte(this.version);
        final int version = this.version & 0xFF;
        if (version == 0) {
            buffer.putPrefixedString(this.jvmVersion);
            buffer.putPrefixedString(this.gcTypeName);
        }
        return buffer.getBuffer();
    }

    @Override
    public String toString() {
        return "JvmInfoBo{" +
                "version=" + version +
                ", jvmVersion='" + jvmVersion + '\'' +
                ", gcTypeName='" + gcTypeName + '\'' +
                '}';
    }
}
