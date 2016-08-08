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

/**
 * @author HyunGil Jeong
 */
public class JvmInfoBo {

    private final byte version;
    private String jvmVersion;
    private String gcTypeName;

    public JvmInfoBo(int version) {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("version out of range (0~255)");
        }
        this.version = (byte) (version & 0xFF);
    }

    public JvmInfoBo(byte[] serializedJvmInfoBo) {
        final Buffer buffer = new FixedBuffer(serializedJvmInfoBo);
        this.version = buffer.readByte();
        int version = this.version & 0xFF;
        switch (version) {
            case 0:
                this.jvmVersion = buffer.readPrefixedString();
                this.gcTypeName = buffer.readPrefixedString();
                break;
            default:
                this.jvmVersion = "";
                this.gcTypeName = "";
                break;
        }
    }

    public int getVersion() {
        return version & 0xFF;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public String getGcTypeName() {
        return gcTypeName;
    }

    public void setGcTypeName(String gcTypeName) {
        this.gcTypeName = gcTypeName;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putByte(this.version);
        int version = this.version & 0xFF;
        switch (version) {
            case 0:
                buffer.putPrefixedString(this.jvmVersion);
                buffer.putPrefixedString(this.gcTypeName);
                break;
            default:
                break;
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
