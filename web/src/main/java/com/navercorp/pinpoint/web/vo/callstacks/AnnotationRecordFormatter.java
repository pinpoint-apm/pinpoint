/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.callstacks;

import com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestType;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.service.ProxyRequestTypeRegistryService;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
public class AnnotationRecordFormatter {
    private static final long DAY = TimeUnit.DAYS.toMillis(1);
    private static final long HOUR = TimeUnit.HOURS.toMillis(1);
    private static final long MINUTE = TimeUnit.MINUTES.toMillis(1);
    private static final long SECOND = TimeUnit.SECONDS.toMillis(1);
    private final ProxyRequestTypeRegistryService proxyRequestTypeRegistryService;

    public AnnotationRecordFormatter(final ProxyRequestTypeRegistryService proxyRequestTypeRegistryService) {
        this.proxyRequestTypeRegistryService = proxyRequestTypeRegistryService;
    }

    public String formatTitle(final AnnotationKey annotationKey, final AnnotationBo annotationBo, Align align) {
        if (annotationKey.getCode() == AnnotationKey.PROXY_HTTP_HEADER.getCode()) {
            if (!(annotationBo.getValue() instanceof LongIntIntByteByteStringValue)) {
                return proxyRequestTypeRegistryService.unknown().getDisplayName();
            }

            final LongIntIntByteByteStringValue value = (LongIntIntByteByteStringValue) annotationBo.getValue();
            final ProxyRequestType type = this.proxyRequestTypeRegistryService.findByCode(value.getIntValue1());
            return type.getDisplayName(value.getStringValue());
        }
        return annotationKey.getName();
    }

    String formatArguments(final AnnotationKey annotationKey, final AnnotationBo annotationBo, final Align align) {
        if (annotationKey.getCode() == AnnotationKey.PROXY_HTTP_HEADER.getCode()) {
            if (annotationBo.getValue() instanceof LongIntIntByteByteStringValue) {
                final LongIntIntByteByteStringValue value = (LongIntIntByteByteStringValue) annotationBo.getValue();
                return buildProxyHttpHeaderAnnotationArguments(value, align.getStartTime());
            } else {
                return "Unsupported type(collector server needs to be upgraded)";
            }
        } else if (annotationKey.getCode() == AnnotationKey.HTTP_IO.getCode() || annotationKey.getCode() == AnnotationKey.REDIS_IO.getCode()) {
            if (annotationBo.getValue() instanceof IntBooleanIntBooleanValue) {
                final IntBooleanIntBooleanValue value = (IntBooleanIntBooleanValue) annotationBo.getValue();
                return buildHttpIoArguments(value);
            }
        }
        // TODO complext-type formatting
        final Object value = annotationBo.getValue();
        if (value instanceof StringStringValue) {
            return formatStringStringValue((StringStringValue) value);
        }

        return Objects.toString(annotationBo.getValue(), "");
    }

    private String formatStringStringValue(StringStringValue value) {
        StringBuilder sb = new StringBuilder(value.getStringValue1());
        sb.append('=');
        sb.append(value.getStringValue2());
        return sb.toString();
    }

    String buildProxyHttpHeaderAnnotationArguments(final LongIntIntByteByteStringValue value, final long startTimeMillis) {
        final ProxyRequestType type = this.proxyRequestTypeRegistryService.findByCode(value.getIntValue1());
        final StringBuilder sb = new StringBuilder(150);
        if (value.getLongValue() != 0) {
            sb.append(toDifferenceTimeFormat(value.getLongValue(), startTimeMillis));
        }
        if (value.getIntValue2() != -1) {
            appendComma(sb);
            sb.append(toDurationTimeFormat(value.getIntValue2()));
        }
        if (value.getByteValue1() != -1) {
            appendComma(sb);
            sb.append("idle: ").append(value.getByteValue1()).append("%");
        }
        if (value.getByteValue2() != -1) {
            appendComma(sb);
            sb.append("busy: ").append(value.getByteValue2()).append("%");
        }

        if (type.useApp()) {
            if (StringUtils.hasLength(value.getStringValue())) {
                appendComma(sb);
                sb.append("app: ").append(value.getStringValue());
            }
        }

        return sb.toString();
    }

    private void appendComma(final StringBuilder buffer) {
        if (buffer.length() > 0) {
            buffer.append(", ");
        }
    }

    String toDifferenceTimeFormat(final long proxyTimeMillis, final long startTimeMillis) {
        final StringBuilder buffer = new StringBuilder(60);
        final long difference = startTimeMillis - proxyTimeMillis;
        final long absoluteDifference = Math.abs(difference);
        if (absoluteDifference > (DAY * 2)) {
            buffer.append("days");
        } else if (absoluteDifference > DAY) {
            buffer.append("a day");
        } else if (absoluteDifference > HOUR) {
            final long hours = toHours(absoluteDifference);
            if (hours > 0) {
                buffer.append(hours).append("h ");
            }
            final long minutes = toMinutes(absoluteDifference);
            if (minutes > 0) {
                buffer.append(minutes).append("m ");
            }
            final long seconds = toSecond(absoluteDifference);
            if (seconds > 0) {
                buffer.append(seconds).append("s ");
            }
            final long millis = toMillis(absoluteDifference);
            if (millis > 0) {
                buffer.append(millis).append("ms");
            }
        } else if (absoluteDifference > MINUTE) {
            final long minutes = toMinutes(absoluteDifference);
            if (minutes > 0) {
                buffer.append(minutes).append("m ");
            }
            final long seconds = toSecond(absoluteDifference);
            if (seconds > 0) {
                buffer.append(seconds).append("s ");
            }
            final long millis = toMillis(absoluteDifference);
            if (millis > 0) {
                buffer.append(millis).append("ms");
            }
        } else if (absoluteDifference > SECOND) {
            final long seconds = toSecond(absoluteDifference);
            if (seconds > 0) {
                buffer.append(seconds).append("s ");
            }
            final long millis = toMillis(absoluteDifference);
            if (millis > 0) {
                buffer.append(millis).append("ms");
            }
        } else {
            buffer.append(toMillis(absoluteDifference)).append("ms");
        }

        if (difference >= 0) {
            buffer.append(" ago");
        } else {
            buffer.append(" from now");
        }

        buffer.append('(');
        buffer.append(format(proxyTimeMillis, startTimeMillis));
        buffer.append(')');
        return buffer.toString();
    }

    private String format(long proxyTimeMillis, long startTimeMillis) {
        if (TimeUnit.MILLISECONDS.toDays(proxyTimeMillis) == TimeUnit.MILLISECONDS.toDays(startTimeMillis)) {
            return DateTimeFormatUtils.formatAbsolute(proxyTimeMillis);
        } else {
            return DateTimeFormatUtils.format(proxyTimeMillis);
        }
    }

    String toDurationTimeFormat(final int durationTimeMicroseconds) {
        StringBuilder buffer = new StringBuilder(30);
        buffer.append("duration: ");
        final long millis = durationTimeMicroseconds / 1000;
        final long micros = durationTimeMicroseconds % 1000;
        if (millis > HOUR) {
            buffer.append("over an hour");
        } else if (millis > MINUTE) {
            final long minutes = toMinutes(millis);
            if (minutes > 0) {
                buffer.append(minutes).append("m ");
            }
            final long seconds = toSecond(millis);
            if (seconds > 0) {
                buffer.append(seconds).append("s ");
            }
            buffer.append(toMillis(millis));
            if (micros > 0) {
                buffer.append('.').append(micros);
            }
            buffer.append("ms");
        } else if (millis > SECOND) {
            final long seconds = toSecond(millis);
            if (seconds > 0) {
                buffer.append(seconds).append("s ");
            }
            buffer.append(toMillis(millis));
            if (micros > 0) {
                buffer.append('.').append(micros);
            }
            buffer.append("ms");
        } else {
            buffer.append(toMillis(millis));
            if (micros > 0) {
                buffer.append('.').append(micros);
            }
            buffer.append("ms");
        }
        return buffer.toString();
    }

    long toHours(final long timeMillis) {
        return (timeMillis / HOUR) % 24;
    }

    long toMinutes(final long timeMillis) {
        return (timeMillis / MINUTE) % 60;
    }

    long toSecond(final long timeMillis) {
        return (timeMillis / SECOND) % 60;
    }

    long toMillis(final long timeMillis) {
        return timeMillis % 1000;
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
}