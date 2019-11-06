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

package com.navercorp.pinpoint.web.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class MappingJsonpJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final String cbPrefix = "(";
    private static final String cbSuffix = ")";
    private static final String cbEnd = ";";

    private static final String DEFAULT_CALLBACK_PARAMETER = "_";

    private static final List<MediaType> DEFAULT_MEDIA_TYPES = new ArrayList<MediaType>() {
        {
            add(new MediaType("application", "x-javascript"));
            add(new MediaType("application", "javascript"));
            add(new MediaType("text", "javascript"));
        }
    };

    public MappingJsonpJackson2HttpMessageConverter() {
        setSupportedMediaTypes(DEFAULT_MEDIA_TYPES);
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        JsonGenerator jsonGenerator = getJsonGenerator(outputMessage);

        try {
            String callbackParam = getRequestParam(DEFAULT_CALLBACK_PARAMETER);

            if (StringUtils.isEmpty(callbackParam)) {
                callbackParam = DEFAULT_CALLBACK_PARAMETER;
            }

            jsonGenerator.writeRaw(callbackParam);
            jsonGenerator.writeRaw(cbPrefix);
            getObjectMapper().writeValue(jsonGenerator, object);
            jsonGenerator.writeRaw(cbSuffix);
            jsonGenerator.writeRaw(cbEnd);
            jsonGenerator.flush();
        } catch (JsonProcessingException e) {
            throw new HttpMessageNotWritableException("Could not write JSON:"
                    + e.getMessage(), e);
        }
    }

    private JsonGenerator getJsonGenerator(HttpOutputMessage outputMessage) throws IOException {
        JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());
        return getObjectMapper().getFactory().createGenerator(outputMessage.getBody(), encoding);
    }

    private String getRequestParam(String paramName) {
        return getServletRequest().getParameter(paramName);
    }

    private HttpServletRequest getServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

}
