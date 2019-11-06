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

package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import com.navercorp.pinpoint.bootstrap.util.FixedByteArrayOutputStream;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpClient3EntityExtractor implements EntityExtractor<HttpMethod> {

    private static final int MAX_READ_SIZE = 1024;

    public static final EntityExtractor<HttpMethod> INSTANCE = new HttpClient3EntityExtractor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public String getEntity(HttpMethod httpMethod) {
        if (httpMethod instanceof EntityEnclosingMethod) {
            final EntityEnclosingMethod entityEnclosingMethod = (EntityEnclosingMethod) httpMethod;
            final RequestEntity entity = entityEnclosingMethod.getRequestEntity();
            if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                try {
                    String entityValue;
                    String charSet = entityEnclosingMethod.getRequestCharSet();
                    if (StringUtils.isEmpty(charSet)) {
                        charSet = HttpConstants.DEFAULT_CONTENT_CHARSET;
                    }
                    if (entity instanceof ByteArrayRequestEntity || entity instanceof StringRequestEntity) {
                        entityValue = entityUtilsToString(entity, charSet);
                    } else {
                        entityValue = entity.getClass() + " (ContentType:" + entity.getContentType() + ")";
                    }
                    return entityValue;
                } catch (Exception e) {
                    if (isDebug) {
                        logger.debug("Failed to get entity. httpMethod={}", httpMethod, e);
                    }
                }
            }
        }
        return null;
    }

    private static String entityUtilsToString(final RequestEntity entity, final String charSet) throws Exception {
        final FixedByteArrayOutputStream outStream = new FixedByteArrayOutputStream(MAX_READ_SIZE);
        entity.writeRequest(outStream);
        final String entityValue = outStream.toString(charSet);
        if (entity.getContentLength() > MAX_READ_SIZE) {
            StringBuilder sb = new StringBuilder();
            sb.append(entityValue);
            sb.append(" (HTTP entity is large. length: ");
            sb.append(entity.getContentLength());
            sb.append(" )");
            return sb.toString();
        }

        return entityValue;
    }
}
