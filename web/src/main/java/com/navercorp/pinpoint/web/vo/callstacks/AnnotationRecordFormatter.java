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

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.dao.StringMetaDataDao;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
public class AnnotationRecordFormatter {
    private static final long DAY = TimeUnit.DAYS.toMillis(1);
    private static final long HOUR = TimeUnit.HOURS.toMillis(1);
    private static final long MINUTE = TimeUnit.MINUTES.toMillis(1);
    private static final long SECOND = TimeUnit.SECONDS.toMillis(1);
    private static final String PROXY_TITLE_PREFIX = "PROXY(";
    private static final String PROXY_TITLE_SUFFIX = ")";

    private final StringMetaDataDao stringMetaDataDao;

    public AnnotationRecordFormatter(final StringMetaDataDao stringMetaDataDao) {
        this.stringMetaDataDao = stringMetaDataDao;
    }

    public String formatTitle(final AnnotationKey annotationKey, final AnnotationBo annotationBo, SpanAlign align) {
        if (annotationKey.getCode() == AnnotationKey.PROXY_HTTP_HEADER.getCode()) {
            if (!(annotationBo.getValue() instanceof LongIntIntByteByteStringValue)) {
                return PROXY_TITLE_PREFIX + PROXY_TITLE_SUFFIX;
            }

            final LongIntIntByteByteStringValue value = (LongIntIntByteByteStringValue) annotationBo.getValue();
            final List<StringMetaDataBo> list = this.stringMetaDataDao.getStringMetaData(align.getAgentId(), align.getAgentStartTime(), value.getIntValue1());
            if (list.size() == 0) {
                return PROXY_TITLE_PREFIX + "STRING-META-DATA-NOT-FOUND" + PROXY_TITLE_SUFFIX;
            }
            return PROXY_TITLE_PREFIX + list.get(0).getStringValue() + PROXY_TITLE_SUFFIX;
        }
        return annotationKey.getName();
    }

    String formatArguments(final AnnotationKey annotationKey, final AnnotationBo annotationBo, final SpanAlign spanAlign) {
        if (annotationKey.getCode() == AnnotationKey.PROXY_HTTP_HEADER.getCode()) {
            if (annotationBo.getValue() instanceof LongIntIntByteByteStringValue) {
                final LongIntIntByteByteStringValue value = (LongIntIntByteByteStringValue) annotationBo.getValue();
                return buildProxyHttpHeaderAnnotationArguments(value, spanAlign.getStartTime());
            } else {
                return "Unsupported type(collector server needs to be upgraded)";
            }
        } else if (annotationKey.getCode() == AnnotationKey.HTTP_IO.getCode()) {
            if (annotationBo.getValue() instanceof IntBooleanIntBooleanValue) {
                final IntBooleanIntBooleanValue value = (IntBooleanIntBooleanValue) annotationBo.getValue();
                return buildHttpIoArguments(value);
            }
        }
        return annotationBo.getValue().toString();
    }

    String buildProxyHttpHeaderAnnotationArguments(final LongIntIntByteByteStringValue value, final long startTimeMillis) {
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
        if (value.getStringValue() != null) {
            appendComma(sb);
            sb.append("app: ").append(value.getStringValue());
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

        if (difference > 0) {
            buffer.append(" ago");
        } else {
            buffer.append(" from now");
        }

        buffer.append('(');
        if (TimeUnit.MILLISECONDS.toDays(proxyTimeMillis) == TimeUnit.MILLISECONDS.toDays(startTimeMillis)) {
            buffer.append(DateUtils.longToDateStr(proxyTimeMillis, "HH:mm:ss SSS"));
        } else {
            buffer.append(DateUtils.longToDateStr(proxyTimeMillis));
        }
        buffer.append(')');
        return buffer.toString();
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
            buffer.append(toMillis(millis)).append('.');
            buffer.append(micros).append("ms");
        } else if (millis > SECOND) {
            final long seconds = toSecond(millis);
            if (seconds > 0) {
                buffer.append(seconds).append("s ");
            }
            buffer.append(toMillis(millis)).append('.');
            buffer.append(micros).append("ms");
        } else {
            buffer.append(toMillis(millis)).append('.');
            buffer.append(micros).append("ms");
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