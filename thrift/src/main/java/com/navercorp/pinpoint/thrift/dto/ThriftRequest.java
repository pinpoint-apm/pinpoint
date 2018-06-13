/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.thrift.dto;

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.thrift.TBase;

/**
 * @author minwoo.jung
 */
public class ThriftRequest implements ServerRequest<TBase<?,?>> {
    private final Header header;
    private final TBase<?, ?> tbase;

    public ThriftRequest(Header header, TBase<?, ?> base) {
        if (header == null) {
            throw new NullPointerException("header must not be null");
        }
        if (base == null) {
            throw new NullPointerException("base must not be null");
        }

        this.header = header;
        this.tbase = base;
    }

    public TBase<?, ?> getData() {
        return tbase;
    }

    public Header getHeader() {
        return header;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThriftRequest that = (ThriftRequest) o;

        if (!header.equals(that.header)) return false;
        return tbase.equals(that.tbase);

    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + tbase.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ThriftRequest{" +
            "base=" + tbase +
            ", header=" + header +
            '}';
    }
}
