package com.nhn.pinpoint.web.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class MappingJsonpJacksonHttpMessageConverter extends
        MappingJacksonHttpMessageConverter {

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

    public MappingJsonpJacksonHttpMessageConverter() {
        setSupportedMediaTypes(DEFAULT_MEDIA_TYPES);
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        JsonGenerator jsonGenerator = getJsonGenerator(outputMessage);

        try {
            String callbackParam = getRequestParam(DEFAULT_CALLBACK_PARAMETER);

            if (callbackParam == null || "".equals(callbackParam)) {
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

    private JsonGenerator getJsonGenerator(HttpOutputMessage outputMessage)
            throws IOException {
        JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders()
                .getContentType());
        return getObjectMapper().getJsonFactory().createJsonGenerator(
                outputMessage.getBody(), encoding);
    }

    private String getRequestParam(String paramName) {
        return getServletRequest().getParameter(paramName);
    }

    private HttpServletRequest getServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
    }

}
