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

package com.navercorp.pinpoint.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import com.navercorp.pinpoint.bootstrap.util.FixedByteArrayOutputStream;
import com.navercorp.pinpoint.common.Charsets;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;

import java.io.IOException;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpClient4EntityExtractor implements EntityExtractor<HttpRequest> {

    public static final EntityExtractor<HttpRequest> INSTANCE = new HttpClient4EntityExtractor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());


    @Override
    public String getEntity(HttpRequest httpRequest) {
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) httpRequest;
            try {
                final HttpEntity entity = entityRequest.getEntity();
                if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                    return entityUtilsToString(entity, Charsets.UTF_8_NAME, 1024);
                }
            } catch (Exception e) {
                logger.debug("Failed to get entity. httpRequest={}", httpRequest, e);
            }
        }
        return null;
    }

    /**
     * copy: EntityUtils Get the entity content as a String, using the provided default character set if none is found in the entity. If defaultCharset is null, the default "ISO-8859-1" is used.
     *
     * @param entity         must not be null
     * @param defaultCharset character set to be applied if none found in the entity
     * @return the entity content as a String. May be null if {@link HttpEntity#getContent()} is null.
     * @throws ParseException           if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null or if content length > Integer.MAX_VALUE
     * @throws IOException              if an error occurs reading the input stream
     */
    @SuppressWarnings("deprecation")
    private static String entityUtilsToString(final HttpEntity entity, final String defaultCharset, final int maxLength) throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity must not be null");
        }
        if (entity.getContentLength() > Integer.MAX_VALUE) {
            return "HTTP entity is too large to be buffered in memory length:" + entity.getContentLength();
        }
        if (entity.getContentType().getValue().startsWith("multipart/form-data")) {
            return "content type is multipart/form-data. content length:" + entity.getContentLength();
        }

        String charset = getContentCharSet(entity);
        if (charset == null) {
            charset = defaultCharset;
        }
        if (charset == null) {
            charset = HTTP.DEFAULT_CONTENT_CHARSET;
        }

        final FixedByteArrayOutputStream outStream = new FixedByteArrayOutputStream(maxLength);
        entity.writeTo(outStream);
        final String entityValue = outStream.toString(charset);
        if (entity.getContentLength() > maxLength) {
            final StringBuilder sb = new StringBuilder();
            sb.append(entityValue);
            sb.append("HTTP entity large length: ");
            sb.append(entity.getContentLength());
            sb.append(" )");
            return sb.toString();
        }

        return entityValue;
    }

    /**
     * copy: EntityUtils Obtains character set of the entity, if known.
     *
     * @param entity must not be null
     * @return the character set, or null if not found
     * @throws ParseException           if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null
     */
    private static String getContentCharSet(final HttpEntity entity) throws ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity must not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }
}
