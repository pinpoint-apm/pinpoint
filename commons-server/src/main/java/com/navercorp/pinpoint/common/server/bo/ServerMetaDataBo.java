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

package com.navercorp.pinpoint.common.server.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;

/**
 * @author hyungil.jeong
 */
public class ServerMetaDataBo {

    private final String serverInfo;
    private final List<String> vmArgs;
    private final List<ServiceInfoBo> serviceInfos;

    private ServerMetaDataBo(Builder builder) {
        this.serverInfo = builder.serverInfo;
        this.vmArgs = builder.vmArgs;
        this.serviceInfos = builder.serviceInfos;
    }

    public String getServerInfo() {
        return this.serverInfo;
    }

    public List<String> getVmArgs() {
        return this.vmArgs;
    }

    public List<ServiceInfoBo> getServiceInfos() {
        return this.serviceInfos;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.put2PrefixedString(this.serverInfo);
        final int numVmArgs = this.vmArgs == null ? 0 : this.vmArgs.size();
        buffer.putVInt(numVmArgs);
        if (this.vmArgs != null) {
            for (String vmArg : this.vmArgs) {
                buffer.put2PrefixedString(vmArg);
            }
        }
        final int numServiceInfos = this.serviceInfos == null ? 0 : this.serviceInfos.size();
        buffer.putVInt(numServiceInfos);
        if (this.serviceInfos != null) {
            for (ServiceInfoBo serviceInfo : this.serviceInfos) {
                buffer.putPrefixedBytes(serviceInfo.writeValue());
            }
        }
        return buffer.getBuffer();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServerMetaDataBo{");
        sb.append("serverInfo='").append(this.serverInfo).append('\'');
        sb.append(", vmArgs=").append(this.vmArgs);
        sb.append(", serviceInfos=").append(this.serviceInfos.toString());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serverInfo == null) ? 0 : serverInfo.hashCode());
        result = prime * result + ((serviceInfos == null) ? 0 : serviceInfos.hashCode());
        result = prime * result + ((vmArgs == null) ? 0 : vmArgs.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerMetaDataBo other = (ServerMetaDataBo)obj;
        if (serverInfo == null) {
            if (other.serverInfo != null)
                return false;
        } else if (!serverInfo.equals(other.serverInfo))
            return false;
        if (serviceInfos == null) {
            if (other.serviceInfos != null)
                return false;
        } else if (!serviceInfos.equals(other.serviceInfos))
            return false;
        if (vmArgs == null) {
            if (other.vmArgs != null)
                return false;
        } else if (!vmArgs.equals(other.vmArgs))
            return false;
        return true;
    }

    public static class Builder {
        private String serverInfo;
        private List<String> vmArgs;
        private List<ServiceInfoBo> serviceInfos;

        public Builder() {
        }

        public Builder(final byte[] value) {
            final Buffer buffer = new FixedBuffer(value);
            this.serverInfo = buffer.read2PrefixedString();
            final int numVmArgs = buffer.readVInt();
            this.vmArgs = new ArrayList<String>(numVmArgs);
            for (int i = 0; i < numVmArgs; ++i) {
                this.vmArgs.add(buffer.read2PrefixedString());
            }
            final int numServiceInfos = buffer.readVInt();
            this.serviceInfos = new ArrayList<ServiceInfoBo>(numServiceInfos);
            for (int i = 0; i < numServiceInfos; ++i) {
                ServiceInfoBo serviceInfoBo = new ServiceInfoBo.Builder(buffer.readPrefixedBytes()).build();
                this.serviceInfos.add(serviceInfoBo);
            }
        }

        public void serverInfo(String serverInfo) {
            this.serverInfo = serverInfo;
        }

        public void vmArgs(List<String> vmArgs) {
            this.vmArgs = vmArgs;
        }

        public void serviceInfos(List<ServiceInfoBo> serviceInfos) {
            this.serviceInfos = serviceInfos;
        }

        public ServerMetaDataBo build() {
            if (this.serverInfo == null) {
                this.serverInfo = "";
            }
            if (this.vmArgs == null) {
                this.vmArgs = Collections.emptyList();
            }
            if (this.serviceInfos == null) {
                this.serviceInfos = Collections.emptyList();
            }
            return new ServerMetaDataBo(this);
        }
    }
}
