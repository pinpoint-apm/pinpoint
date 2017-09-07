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

import java.util.List;

/**
 * @author jaehong.kim
 */
public class ProxyHttpHeaderParser {
    public ProxyHttpHeader parse(final int type, final String value) {
        final ProxyHttpHeader header = new ProxyHttpHeader(type);
        if (value == null) {
            header.setValid(false);
            header.setCause("value is must not be null");
            return header;
        }

        if (type == ProxyHttpHeader.TYPE_APP) {
            parseAppFormat(header, value);
        } else if (type == ProxyHttpHeader.TYPE_NGINX) {
            parseNginxFormat(header, value);
        } else if (type == ProxyHttpHeader.TYPE_APACHE) {
            parseApacheHttpdFormat(header, value);
        } else {
            header.setValid(false);
            header.setCause("unknown type");
        }

        return header;
    }

    void parseApacheHttpdFormat(final ProxyHttpHeader header, final String value) {
        for (String token : StringUtils.tokenizeToStringList(value, " ")) {
            if (token.startsWith("t=")) {
                // convert to milliseconds from microseconds.
                final int length = token.length() - 3;
                if (length > 2) {
                    final long receivedTimeMillis = ApacheUnit.toReceivedTimeMillis(token.substring(2));
                    if (receivedTimeMillis > 0) {
                        header.setReceivedTimeMillis(receivedTimeMillis);
                        header.setValid(true);
                        continue;
                    }
                }
                header.setValid(false);
                header.setCause("invalid received time");
                return;
            } else if (token.startsWith("D=")) {
                final long durationTimeMicroseconds = ApacheUnit.toDurationTimeMicros(token.substring(2));
                if (durationTimeMicroseconds > 0) {
                    header.setDurationTimeMicroseconds((int) durationTimeMicroseconds);
                    continue;
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
            }
        }
    }

    void parseNginxFormat(final ProxyHttpHeader header, final String value) {
        final List<String> tokens = StringUtils.tokenizeToStringList(value, " ");
        if (tokens.size() >= 1) {
            // first token is receivedTimeMillis.
            final long receivedTimeMillis = NginxUnit.toReceivedTimeMillis(tokens.get(0));
            if (receivedTimeMillis > 0) {
                header.setReceivedTimeMillis(receivedTimeMillis);
                header.setValid(true);
            } else {
                header.setValid(false);
                header.setCause("invalid received time");
                return;
            }
        }

        if (tokens.size() >= 2) {
            final long durationTimeMicroseconds = NginxUnit.toDurationTimeMicros(tokens.get(1));
            if (durationTimeMicroseconds > 0) {
                header.setDurationTimeMicroseconds((int) durationTimeMicroseconds);
            }
        }
    }

    void parseAppFormat(final ProxyHttpHeader header, final String value) {
        final List<String> tokens = StringUtils.tokenizeToStringList(value, " ");
        if (tokens.size() >= 1) {
            final long receivedTimeMillis = AppUnit.toReceivedTimeMillis(tokens.get(0));
            if (receivedTimeMillis > 0) {
                header.setReceivedTimeMillis(receivedTimeMillis);
                header.setValid(true);
            } else {
                header.setValid(false);
                header.setCause("invalid received time");
                return;
            }
        }

        if (tokens.size() >= 2) {
            header.setApp(tokens.get(1));
        }
    }

    static class NginxUnit {
        static long toReceivedTimeMillis(final String value) {
            if (value == null) {
                return 0;
            }

            final int length = value.length();
            // e.g. 1504230492.763
            final int millisPosition = value.lastIndexOf('.');
            if (millisPosition != -1) {
                if ((length - millisPosition) != 4) {
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

        static long toDurationTimeMicros(final String value) {
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
            }
            return 0;
        }
    }

    static class ApacheUnit {
        static long toReceivedTimeMillis(final String value) {
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

        static long toDurationTimeMicros(final String value) {
            if (value == null) {
                return 0;
            }

            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
            }
            return 0;
        }
    }

    static class AppUnit {
        static long toReceivedTimeMillis(final String value) {
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
    }
}