/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.callstacks;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.HttpMethod;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.service.ProxyRequestTypeRegistryService;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class AnnotationRecordFormatter {

    private final IntObjectMap<AnnotationHandler> titleHandlers;
    private final IntObjectMap<AnnotationHandler> argumentHandlers;

    AnnotationRecordFormatter(IntObjectMap<AnnotationHandler> titleHandlers, IntObjectMap<AnnotationHandler> argumentHandlers) {
        this.titleHandlers = Objects.requireNonNull(titleHandlers, "titleHandlers");
        this.argumentHandlers = Objects.requireNonNull(argumentHandlers, "argumentHandlers");
    }

    public String formatTitle(final AnnotationKey annotationKey, final AnnotationBo annotationBo, Align align) {
        final AnnotationHandler handler = titleHandlers.get(annotationKey.getCode());
        if (handler != null) {
            String title = handler.format(annotationKey, annotationBo, align);
            if (title != null) {
                return title;
            }
        }
        return annotationKey.getName();
    }

    String formatArguments(final AnnotationKey annotationKey, final AnnotationBo annotationBo, final Align align) {
        final AnnotationHandler handler = argumentHandlers.get(annotationKey.getCode());
        if (handler != null) {
            String argument = handler.format(annotationKey, annotationBo, align);
            if (argument != null) {
                return argument;
            }
        }

        // complex-type formatting
        final Object value = annotationBo.getValue();
        if (value instanceof StringStringValue stringStringValue) {
            return formatStringStringValue(stringStringValue);
        }

        return Objects.toString(annotationBo.getValue(), "");
    }

    private String formatStringStringValue(StringStringValue value) {
        return value.getStringValue1() + '=' + value.getStringValue2();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private final MutableIntObjectMap<AnnotationHandler> titleHandlers = IntObjectMaps.mutable.of();
        private final MutableIntObjectMap<AnnotationHandler> argumentHandlers = IntObjectMaps.mutable.of();

        public void addTitleHandler(int code, AnnotationHandler handler) {
            Objects.requireNonNull(handler, "handler");
            AnnotationHandler exist = titleHandlers.put(code, handler);
            if (exist != null) {
                throw new IllegalArgumentException("code already exist : " + code);
            }
        }

        public void addArgumentHandler(int code, AnnotationHandler handler) {
            Objects.requireNonNull(handler, "handler");
            AnnotationHandler exist = argumentHandlers.put(code, handler);
            if (exist != null) {
                throw new IllegalArgumentException("code already exist : " + code);
            }
        }

        public void addArgumentHandlers(List<Integer> codes, AnnotationHandler handler) {
            codes.forEach(code -> addArgumentHandler(code, handler));
        }

        public AnnotationRecordFormatter build() {
            IntObjectMap<AnnotationHandler> titleHandlers = IntObjectMaps.mutable.ofAll(this.titleHandlers);
            IntObjectMap<AnnotationHandler> argumentHandlers = IntObjectMaps.mutable.ofAll(this.argumentHandlers);
            return new AnnotationRecordFormatter(titleHandlers, argumentHandlers);
        }

        public void addProxyHeaderAnnotationHeader(ProxyRequestTypeRegistryService proxyRequestTypeRegistryService) {
            addTitleHandler(AnnotationKey.PROXY_HTTP_HEADER.getCode(), new ProxyHeaderAnnotationTitleHandler(proxyRequestTypeRegistryService));
            addArgumentHandler(AnnotationKey.PROXY_HTTP_HEADER.getCode(), new ProxyHeaderAnnotationArgumentHandler(proxyRequestTypeRegistryService));
        }

        public void addDefaultHandlers() {
            addArgumentHandlers(List.of(AnnotationKey.HTTP_IO.getCode(), AnnotationKey.REDIS_IO.getCode()), new AnnotationHandler() {
                @Override
                public String format(AnnotationKey annotationKey, AnnotationBo annotationBo, Align align) {
                    if (annotationBo.getValue() instanceof IntBooleanIntBooleanValue value) {
                        return buildHttpIoArguments(value);
                    }
                    return null;
                }

                private String buildHttpIoArguments(final IntBooleanIntBooleanValue value) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("write: ").append(value.getIntValue1()).append("ms");
                    if (value.isBooleanValue1()) {
                        sb.append("(FAILED)");
                    }
                    sb.append(", read: ").append(value.getIntValue2()).append("ms");
                    if (value.isBooleanValue2()) {
                        sb.append("(FAILED)");
                    }
                    return sb.toString();
                }
            });

            addArgumentHandler(AnnotationKey.HTTP_STATUS_CODE.getCode(), new AnnotationHandler() {
                @Override
                public String format(AnnotationKey annotationKey, AnnotationBo annotationBo, Align align) {
                    if (annotationBo.getValue() instanceof Integer statusCode) {
                        if (statusCode < 0) {
                            return "UNKNOWN(invalid status code)";
                        }
                        return String.valueOf(statusCode);
                    }
                    return null;
                }
            });
            addArgumentHandler(AnnotationKey.HTTP_METHOD.getCode(), new AnnotationHandler() {
                @Override
                public String format(AnnotationKey annotationKey, AnnotationBo annotationBo, Align align) {
                    if (annotationBo.getValue() instanceof String method) {
                        HttpMethod httpMethod = HttpMethod.valueOf(method);
                        if (httpMethod != HttpMethod.UNKNOWN) {
                            return httpMethod.name();
                        }
                    }
                    return "UNKNOWN(invalid method)";
                }
            });

            addArgumentHandler(AnnotationKey.HTTP_SECURE.getCode(), new AnnotationHandler() {
                @Override
                public String format(AnnotationKey annotationKey, AnnotationBo annotationBo, Align align) {
                    if (annotationBo.getValue() instanceof Boolean secure) {
                        return secure ? "HTTPS" : "HTTP";
                    }
                    return "UNKNOWN";
                }
            });
        }
    }

}