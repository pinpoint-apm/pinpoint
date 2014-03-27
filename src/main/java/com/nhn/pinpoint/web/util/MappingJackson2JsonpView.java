package com.nhn.pinpoint.web.util;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 상속해서 할려고 했으나, objectMapper등이 private이라. 사용할수가 없어서 그냥 복사해서 고침..
 * renderMergedOutputModel만 수정함.
 * @author emeroad
 */
public class MappingJackson2JsonpView extends MappingJackson2JsonView {

    private static final String cbPrefix = "(";
    private static final String cbSuffix = ")";
    private static final String cbEnd = ";";

    private static final String DEFAULT_CALLBACK_PARAMETER = "_callback";

    /**
     * Default content type. Overridable as bean property.
     */
    public static final String DEFAULT_CONTENT_TYPE = "application/json";


    private ObjectMapper objectMapper = new ObjectMapper();

    private JsonEncoding encoding = JsonEncoding.UTF8;

    private boolean prefixJson = false;

    private Set<String> modelKeys;

    private boolean extractValueFromSingleKeyModel = false;

    private boolean disableCaching = true;


    /**
     * Construct a new {@code JacksonJsonView}, setting the content type to {@code application/json}.
     */
    public MappingJackson2JsonpView() {
        setContentType(DEFAULT_CONTENT_TYPE);
        setExposePathVariables(false);
    }


    /**
     * Sets the {@code ObjectMapper} for this view.
     * If not set, a default {@link ObjectMapper#ObjectMapper() ObjectMapper} is used.
     * <p>Setting a custom-configured {@code ObjectMapper} is one way to take further control
     * of the JSON serialization process. For example, an extended {@code SerializerFactory}
     * can be configured that provides custom serializers for specific types. The other option
     * for refining the serialization process is to use Jackson's provided annotations on the
     * types to be serialized, in which case a custom-configured ObjectMapper is unnecessary.
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "'objectMapper' must not be null");
        this.objectMapper = objectMapper;
    }

    /**
     * Set the {@code JsonEncoding} for this converter.
     * By default, {@linkplain JsonEncoding#UTF8 UTF-8} is used.
     */
    public void setEncoding(JsonEncoding encoding) {
        Assert.notNull(encoding, "'encoding' must not be null");
        this.encoding = encoding;
    }

    /**
     * Indicates whether the JSON output by this view should be prefixed with <tt>"{} && "</tt>.
     * Default is false.
     * <p>Prefixing the JSON string in this manner is used to help prevent JSON Hijacking.
     * The prefix renders the string syntactically invalid as a script so that it cannot be hijacked.
     * This prefix does not affect the evaluation of JSON, but if JSON validation is performed
     * on the string, the prefix would need to be ignored.
     */
    public void setPrefixJson(boolean prefixJson) {
        this.prefixJson = prefixJson;
    }

    /**
     * Set the attribute in the model that should be rendered by this view.
     * When set, all other model attributes will be ignored.
     */
    public void setModelKey(String modelKey) {
        this.modelKeys = Collections.singleton(modelKey);
    }

    /**
     * Set the attributes in the model that should be rendered by this view.
     * When set, all other model attributes will be ignored.
     */
    public void setModelKeys(Set<String> modelKeys) {
        this.modelKeys = modelKeys;
    }

    /**
     * Return the attributes in the model that should be rendered by this view.
     */
    public Set<String> getModelKeys() {
        return this.modelKeys;
    }

    /**
     * Set the attributes in the model that should be rendered by this view.
     * When set, all other model attributes will be ignored.
     * @deprecated use {@link #setModelKeys(Set)} instead
     */
    @Deprecated
    public void setRenderedAttributes(Set<String> renderedAttributes) {
        this.modelKeys = renderedAttributes;
    }

    /**
     * Return the attributes in the model that should be rendered by this view.
     * @deprecated use {@link #getModelKeys()} instead
     */
    @Deprecated
    public Set<String> getRenderedAttributes() {
        return this.modelKeys;
    }

    /**
     * Set whether to serialize models containing a single attribute as a map or whether to
     * extract the single value from the model and serialize it directly.
     * <p>The effect of setting this flag is similar to using {@code MappingJacksonHttpMessageConverter}
     * with an {@code @ResponseBody} request-handling method.
     * <p>Default is {@code false}.
     */
    public void setExtractValueFromSingleKeyModel(boolean extractValueFromSingleKeyModel) {
        this.extractValueFromSingleKeyModel = extractValueFromSingleKeyModel;
    }

    /**
     * Disables caching of the generated JSON.
     * <p>Default is {@code true}, which will prevent the client from caching the generated JSON.
     */
    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }


    @Override
    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(getContentType());
        response.setCharacterEncoding(this.encoding.getJavaName());
        if (this.disableCaching) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
            response.addDateHeader("Expires", 1L);
        }
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        Object value = filterModel(model);
        JsonGenerator generator = this.objectMapper.getFactory().createGenerator(response.getOutputStream(), this.encoding);
        if (this.prefixJson) {
            generator.writeRaw("{} && ");
        }
        final String callBackParameter = getCallBackParameter(request);
        if (StringUtils.isEmpty(callBackParameter)) {
            this.objectMapper.writeValue(generator, value);
        } else {
            generator.writeRaw(callBackParameter);
            generator.writeRaw(cbPrefix);
            this.objectMapper.writeValue(generator, value);
            generator.writeRaw(cbSuffix);
            generator.writeRaw(cbEnd);
        }
        generator.flush();
    }

    private String getCallBackParameter(HttpServletRequest request) {
        return request.getParameter(DEFAULT_CALLBACK_PARAMETER);
    }

    /**
     * Filters out undesired attributes from the given model.
     * The return value can be either another {@link Map} or a single value object.
     * <p>The default implementation removes {@link org.springframework.validation.BindingResult} instances and entries
     * not included in the {@link #setRenderedAttributes renderedAttributes} property.
     * @param model the model, as passed on to {@link #renderMergedOutputModel}
     * @return the object to be rendered
     */
    protected Object filterModel(Map<String, Object> model) {
        Map<String, Object> result = new HashMap<String, Object>(model.size());
        Set<String> renderedAttributes = (!CollectionUtils.isEmpty(this.modelKeys) ? this.modelKeys : model.keySet());
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            if (!(entry.getValue() instanceof BindingResult) && renderedAttributes.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return (this.extractValueFromSingleKeyModel && result.size() == 1 ? result.values().iterator().next() : result);
    }
}
