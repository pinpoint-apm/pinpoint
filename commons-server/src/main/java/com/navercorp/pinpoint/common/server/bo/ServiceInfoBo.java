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
public class ServiceInfoBo {

    private final String serviceName;
    private final List<String> serviceLibs;
    
    private ServiceInfoBo(Builder builder) {
        this.serviceName = builder.serviceName;
        this.serviceLibs = builder.serviceLibs;
    }
    
    public String getServiceName() {
        return this.serviceName;
    }
    
    public List<String> getServiceLibs() {
        return this.serviceLibs;
    }
    
    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.put2PrefixedString(this.serviceName);
        int numServiceLibs = this.serviceLibs == null ? 0 : this.serviceLibs.size();
        buffer.putVInt(numServiceLibs);
        for (int i = 0; i < numServiceLibs; ++i) {
            buffer.put2PrefixedString(this.serviceLibs.get(i));
        }
        return buffer.getBuffer();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServiceInfoBo{");
        sb.append("serviceName='").append(this.serviceName).append('\'');
        sb.append(", serviceLibs=").append(this.serviceLibs).append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serviceLibs == null) ? 0 : serviceLibs.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
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
        ServiceInfoBo other = (ServiceInfoBo)obj;
        if (serviceLibs == null) {
            if (other.serviceLibs != null)
                return false;
        } else if (!serviceLibs.equals(other.serviceLibs))
            return false;
        if (serviceName == null) {
            if (other.serviceName != null)
                return false;
        } else if (!serviceName.equals(other.serviceName))
            return false;
        return true;
    }

    public static class Builder {
        private String serviceName;
        private List<String> serviceLibs;
        
        public Builder() {}
        
        public Builder(final byte[] value) {
            final Buffer buffer = new FixedBuffer(value);
            this.serviceName = buffer.read2PrefixedString();
            final int numServiceLibs = buffer.readVInt();
            this.serviceLibs = new ArrayList<String>(numServiceLibs);
            for (int i = 0; i < numServiceLibs; ++i) {
                this.serviceLibs.add(buffer.read2PrefixedString());
            }
        }
        
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public Builder serviceLibs(List<String> serviceLibs) {
            this.serviceLibs = serviceLibs;
            return this;
        }
        
        public ServiceInfoBo build() {
            if (this.serviceName == null) {
                this.serviceName = "";
            }
            if (this.serviceLibs == null) {
                this.serviceLibs = Collections.<String>emptyList();
            }
            return new ServiceInfoBo(this);
        }
    }
}
