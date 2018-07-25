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

package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.request.body.multipart.ByteArrayPart;
import org.asynchttpclient.request.body.multipart.FilePart;
import org.asynchttpclient.request.body.multipart.Part;
import org.asynchttpclient.request.body.multipart.StringPart;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NingEntityExtractorV2 implements EntityExtractor<Request> {

    public static final EntityExtractor<Request> INSTANCE = new NingEntityExtractorV2();

    private static final int MAX_READ_SIZE = 1024;
    @Override
    public String getEntity(Request httpRequest) {

        final String stringData = httpRequest.getStringData();
        if (stringData != null) {
            return stringData;
        }

        final byte[] byteData = httpRequest.getByteData();
        if (byteData != null) {
            return "BYTE_DATA";
        }

        final InputStream streamData = httpRequest.getStreamData();
        if (streamData != null) {
            return "STREAM_DATA";
        }

        List<Part> parts = httpRequest.getBodyParts();
        // bug fix : parts != null && ****!parts.isEmpty()
        if (CollectionUtils.hasLength(parts)) {
            StringBuilder sb = new StringBuilder();
            Iterator<Part> iterator = parts.iterator();
            while (iterator.hasNext()) {
                Part part = iterator.next();
                if (part instanceof ByteArrayPart) {
                    ByteArrayPart p = (ByteArrayPart) part;
                    sb.append(part.getName());
                    sb.append("=BYTE_ARRAY_");
                    sb.append(p.getBytes().length);
                } else if (part instanceof FilePart) {
                    FilePart p = (FilePart) part;
                    sb.append(part.getName());
                    sb.append("=FILE_");
                    sb.append(p.getContentType());
                } else if (part instanceof StringPart) {
                    StringPart p = (StringPart) part;
                    sb.append(part.getName());
                    sb.append("=STRING");
                }

                if (sb.length() >= MAX_READ_SIZE) {
                    break;
                }

                if (iterator.hasNext()) {
                    sb.append(',');
                }
            }
            return sb.toString();
        }

        return null;
    }
}
