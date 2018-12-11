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

import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @deprecated As of release 1.8.2, replaced by ProxyRequestParser
 * @author jaehong.kim
 */
@Deprecated
public class ProxyHttpHeaderParser {
    private final ProxyTimeUnit nginxUnit = new NginxTimeUnit();
    private final ProxyTimeUnit apacheUnit = new ApacheTimeUnit();
    private final ProxyTimeUnit appUnit = new AppTimeUnit();

    public ProxyHttpHeader parse(final int type, final String value) {
        final ProxyHttpHeader header = new ProxyHttpHeader(type);
        if (value == null) {
            header.setValid(false);
            header.setCause("value is must not be null");
            return header;
        }

        if (type == ProxyHttpHeader.TYPE_APP) {
            parseFormat(header, value, appUnit);
        } else if (type == ProxyHttpHeader.TYPE_NGINX) {
            parseFormat(header, value, nginxUnit);
        } else if (type == ProxyHttpHeader.TYPE_APACHE) {
            parseFormat(header, value, apacheUnit);
        } else {
            header.setValid(false);
            header.setCause("unknown type");
        }

        return header;
    }

    void parseFormat(final ProxyHttpHeader header, final String value, final ProxyTimeUnit proxyTimeUnit) {
        for (String token : StringUtils.tokenizeToStringList(value, " ")) {
            if (token.startsWith("t=")) {
                // convert to milliseconds from microseconds.
                final long receivedTimeMillis = proxyTimeUnit.toReceivedTimeMillis(token.substring(2));
                if (receivedTimeMillis > 0) {
                    header.setReceivedTimeMillis(receivedTimeMillis);
                    header.setValid(true);
                } else {
                    // stop.
                    header.setValid(false);
                    header.setCause("invalid received time");
                    return;
                }
            } else if (token.startsWith("D=")) {
                final long durationTimeMicroseconds = proxyTimeUnit.toDurationTimeMicros(token.substring(2));
                if (durationTimeMicroseconds > 0) {
                    header.setDurationTimeMicroseconds((int) durationTimeMicroseconds);
                }
            } else if (token.startsWith("i=")) {
                try {
                    final int idlePercent = Integer.parseInt(token.substring(2));
                    if (idlePercent >= 0 && idlePercent <= 100) {
                        header.setIdlePercent((byte) idlePercent);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
            } else if (token.startsWith("b=")) {
                try {
                    int busyPercent = Integer.parseInt(token.substring(2));
                    if (busyPercent >= 0 && busyPercent <= 100) {
                        header.setBusyPercent((byte) busyPercent);
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
            } else if (token.startsWith("app=")) {
                final String app = token.substring(4).trim();
                if (!app.isEmpty()) {
                    header.setApp(app);
                }
            }
        }
    }

    // for testcase.
    ProxyTimeUnit getNginxUnit() {
        return nginxUnit;
    }

    // for testcase.
    ProxyTimeUnit getApacheUnit() {
        return apacheUnit;
    }

    // for testcase.
    ProxyTimeUnit getAppUnit() {
        return appUnit;
    }

    interface ProxyTimeUnit {
        long toReceivedTimeMillis(final String value);

        int toDurationTimeMicros(final String value);
    }

    private static class NginxTimeUnit implements ProxyTimeUnit {

        public NginxTimeUnit() {
        }

        @Override
        public long toReceivedTimeMillis(final String value) {
            if (value == null) {
                return 0;
            }

            final int length = value.length();
            // e.g. 1504230492.763
            final int millisPosition = value.lastIndexOf('.');
            if (millisPosition != -1) {
                if (length - millisPosition != 4) {
                    // invalid format.
                    return 0;
                }
                try {
                    return Long.parseLong(value.substring(0, millisPosition) + value.substring(millisPosition + 1));
                } catch (NumberFormatException ignored) {
                }
            }
            return 0;
        }

        @Override
        public int toDurationTimeMicros(final String value) {
            if (value == null) {
                return 0;
            }

            final int length = value.length();
            final int millisPosition = value.lastIndexOf('.');
            if (millisPosition != -1) {
                // e.g. 0.000
                if (length - millisPosition != 4) {
                    // invalid format.
                    return 0;
                }
                try {
                    // to microseconds
                    return Integer.parseInt(value.substring(0, millisPosition) + value.substring(millisPosition + 1)) * 1000;
                } catch (NumberFormatException ignored) {
                }
            }
            return 0;
        }
    }

    private static class ApacheTimeUnit implements ProxyTimeUnit {
        @Override
        public long toReceivedTimeMillis(final String value) {
            if (value == null) {
                return 0;
            }

            final int length = value.length();
            // convert to milliseconds from microseconds.
            if (length > 3) {
                try {
                    return Long.parseLong(value.substring(0, length - 3));
                } catch (NumberFormatException ignored) {
                }
            }
            return 0;
        }

        @Override
        public int toDurationTimeMicros(final String value) {
            if (value == null) {
                return 0;
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
            }
            return 0;
        }
    }

    private static class AppTimeUnit implements ProxyTimeUnit {
        @Override
        public long toReceivedTimeMillis(final String value) {
            if (value == null) {
                return 0;
            }

            // to milliseconds.
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
            }
            return 0;
        }

        @Override
        public int toDurationTimeMicros(String value) {
            return 0;
        }
    }
}