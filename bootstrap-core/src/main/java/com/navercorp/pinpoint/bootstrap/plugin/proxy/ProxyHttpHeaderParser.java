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

package com.navercorp.pinpoint.bootstrap.plugin.proxy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author jaehong.kim
 */
public class ProxyHttpHeaderParser {
    private static final ThreadLocal<DateFormat> CACHE = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(FORMAT, Locale.ENGLISH);
        }
    };

    // sfttime "%A, %d-%b-%Y %H:%M:%S %Z" to EEEE, dd-MMM-yyyy HH:mm:ss ZZZ.
    // only GMT
    private static final String FORMAT = "EEEE, dd-MMM-yyyy HH:mm:ss ZZZ";

    public ProxyHttpHeader parse(final String value) {
        if (value == null) {
            final ProxyHttpHeader header = new ProxyHttpHeader();
            header.setValid(false);
            header.setCause("value is must not be null");
            return header;
        }

        // if apache
        if (isApacheHttpd(value)) {
            return parseApacheHttpd(value);
        } else if (isNginx(value)) {
            return parseNginx(value);
        } else if(isApp(value)) {
            return parseApp(value);
        }
        // others
        return parseTimestamp(value);
    }

    boolean isApacheHttpd(final String value) {
        return value.contains("t=");
    }

    ProxyHttpHeader parseApacheHttpd(final String value) {
        final ProxyHttpHeader header = new ProxyHttpHeader();
        final String[] tokens = value.split(" ");
        for (String token : tokens) {
            final String s = token.trim();
            if (s.isEmpty()) {
                continue;
            }

            if (s.startsWith("t=")) {
                try {
                    // convert to milliseconds from microseconds.
                    final int length = s.length() - 3;
                    if (length > 2) {
                        final long receivedTimeMillis = Long.parseLong(s.substring(2, length));
                        if (receivedTimeMillis > 0) {
                            header.setReceivedTimeMillis(receivedTimeMillis);
                            header.setValid(true);
                            continue;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
                header.setValid(false);
                header.setCause("invalid received time " + s);
                return header;
            } else if (s.startsWith("D=")) {
                try {
                    final long durationTimeMicroseconds = Long.parseLong(s.substring(2));
                    if (durationTimeMicroseconds > 0) {
                        header.setDurationTimeMicroseconds((int) durationTimeMicroseconds);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
                header.setValid(false);
                header.setCause("invalid duration time " + s);
                return header;
            } else if (s.startsWith("i=")) {
                try {
                    final int idlePercent = Integer.parseInt(s.substring(2));
                    if (idlePercent >= 0 && idlePercent <= 100) {
                        header.setIdlePercent((byte) idlePercent);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
                header.setValid(false);
                header.setCause("invalid idle percent " + s);
                return header;
            } else if (s.startsWith("b=")) {
                try {
                    int busyPercent = Integer.parseInt(s.substring(2));
                    if (busyPercent >= 0 && busyPercent <= 100) {
                        header.setBusyPercent((byte) busyPercent);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
                header.setValid(false);
                header.setCause("invalid busy percent " + s);
                return header;
            }
        }
        return header;
    }

    boolean isNginx(final String value) {
        return value.contains(", ");
    }

    ProxyHttpHeader parseNginx(final String value) {
        final ProxyHttpHeader header = new ProxyHttpHeader();
        try {
            final DateFormat dateFormat = CACHE.get();
            final Date date = dateFormat.parse(value);
            header.setReceivedTimeMillis(date.getTime());
            header.setValid(true);
        } catch (ParseException ignored) {
        }
        return header;
    }

    boolean isApp(final String value) {
        return value.contains("ts=");
    }

    ProxyHttpHeader parseApp(final String value) {
        final ProxyHttpHeader header = new ProxyHttpHeader();
        final String[] tokens = value.split(" ");
        for (String token : tokens) {
            final String s = token.trim();
            if (s.isEmpty()) {
                continue;
            }

            if (s.startsWith("ts=")) {
                try {
                    // convert to milliseconds from microseconds.
                    final long receivedTimeMillis = Long.parseLong(s.substring(3));
                    if (receivedTimeMillis > 0) {
                        header.setReceivedTimeMillis(receivedTimeMillis);
                        header.setValid(true);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
                header.setValid(false);
                header.setCause("invalid received time " + s);
                return header;
            } else if (s.startsWith("app=")) {
                final String appName = s.substring(4);
                header.setApp(appName);
            }
        }
        return header;
    }

    ProxyHttpHeader parseTimestamp(final String value) {
        final ProxyHttpHeader header = new ProxyHttpHeader();
        try {
            final long timestamp = Long.parseLong(value);
            if(timestamp > 0) {
                header.setReceivedTimeMillis(timestamp);
                header.setValid(true);
                return header;
            }
        } catch (NumberFormatException ignored) {
        }
        header.setValid(false);
        header.setCause("invalid received time");
        return header;
    }
}