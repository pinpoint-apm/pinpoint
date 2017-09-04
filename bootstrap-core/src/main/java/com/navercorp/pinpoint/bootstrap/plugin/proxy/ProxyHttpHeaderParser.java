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
        if (isReceivedTimeFormat(value)) {
            return parseReceivedTimeFormat(value);
        } else if (isNginxMsecFormat(value)) {
            return parseNginxMsecFormat(value);
        } else if (isNginxDateGmtFormat(value)) {
            return parseNginxDateGmtFormat(value);
        } else if (isApp(value)) {
            return parseApp(value);
        }
        // others
        return parseTimestamp(value);
    }

    boolean isReceivedTimeFormat(final String value) {
        return value.contains("t=");
    }

    ProxyHttpHeader parseReceivedTimeFormat(final String value) {
        final ProxyHttpHeader header = new ProxyHttpHeader();
        final String[] tokens = value.split(" ");
        for (String token : tokens) {
            final String s = token.trim();
            if (s.isEmpty()) {
                continue;
            }

            if (s.startsWith("t=")) {
                final long receivedTimeMillis = toReceivedTimeMillis(s.substring(2));
                if (receivedTimeMillis > 0) {
                    header.setReceivedTimeMillis(receivedTimeMillis);
                    header.setValid(true);
                    continue;
                }
                header.setValid(false);
                header.setCause("invalid received time");
                return header;
            } else if (s.startsWith("D=")) {
                final long durationTimeMicroseconds = toDurationTimeMicros(s.substring(2));
                if (durationTimeMicroseconds > 0) {
                    header.setDurationTimeMicroseconds((int) durationTimeMicroseconds);
                    continue;
                }
            } else if (s.startsWith("i=")) {
                try {
                    final int idlePercent = Integer.parseInt(s.substring(2));
                    if (idlePercent >= 0 && idlePercent <= 100) {
                        header.setIdlePercent((byte) idlePercent);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
            } else if (s.startsWith("b=")) {
                try {
                    int busyPercent = Integer.parseInt(s.substring(2));
                    if (busyPercent >= 0 && busyPercent <= 100) {
                        header.setBusyPercent((byte) busyPercent);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return header;
    }

    // for apache httpd & nginx
    long toReceivedTimeMillis(final String value) {
        if (value == null) {
            return 0;
        }

        final int length = value.length();
        final int millisPosition = value.lastIndexOf('.');
        if (millisPosition != -1) {
            // e.g. 1504230492.763
            if ((length - millisPosition) != 4) {
                // invalid format.
                return 0;
            }
            try {
                return Long.parseLong(value.substring(0, millisPosition) + value.substring(millisPosition + 1));
            } catch (NumberFormatException ignored) {
            }
        } else {
            // convert to milliseconds from microseconds.
            if (length > 3) {
                try {
                    return Long.parseLong(value.substring(0, length - 3));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    long toDurationTimeMicros(final String value) {
        if (value == null) {
            return 0;
        }

        final int length = value.length();
        final int millisPosition = value.lastIndexOf('.');
        if (millisPosition != -1) {
            // e.g. 0.000
            if ((length - millisPosition) != 4) {
                // invalid format.
                return 0;
            }
            try {
                // to microseconds
                return Long.parseLong(value.substring(0, millisPosition) + value.substring(millisPosition + 1)) * 1000;
            } catch (NumberFormatException ignored) {
            }
        } else {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    boolean isNginxMsecFormat(final String value) {
        return value.indexOf('.') != -1;
    }

    ProxyHttpHeader parseNginxMsecFormat(final String value) {
        final ProxyHttpHeader header = new ProxyHttpHeader();
        final long timestamp = toReceivedTimeMillis(value);
        if (timestamp > 0) {
            header.setReceivedTimeMillis(timestamp);
            header.setValid(true);
            return header;
        }
        header.setValid(false);
        header.setCause("invalid received time");
        return header;
    }

    boolean isNginxDateGmtFormat(final String value) {
        return value.contains(", ");
    }

    ProxyHttpHeader parseNginxDateGmtFormat(final String value) {
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
                    final long receivedTimeMillis = Long.parseLong(s.substring(3));
                    if (receivedTimeMillis > 0) {
                        header.setReceivedTimeMillis(receivedTimeMillis);
                        header.setValid(true);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
                header.setValid(false);
                header.setCause("invalid received time");
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
            if (timestamp > 0) {
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