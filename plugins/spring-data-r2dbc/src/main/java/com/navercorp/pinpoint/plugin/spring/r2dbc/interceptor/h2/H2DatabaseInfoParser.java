/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.h2;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants.UNKNOWN_DATABASE;

public class H2DatabaseInfoParser {
    static final String START_URL = "jdbc:h2:";

    final ConnectInfo connectInfo;

    public H2DatabaseInfoParser() {
        this.connectInfo = new ConnectInfo();
    }

    public H2DatabaseInfoParser(String url) {
        this.connectInfo = new ConnectInfo(url);
    }

    public List<String> getHostList() {
        final String name = connectInfo.getName();
        final boolean remote = connectInfo.isRemote();
        return getHostList(name, remote);
    }

    public List<String> getHostList(String name, boolean remote) {
        final List<String> hostList = new ArrayList<>();
        if (remote) {
            String info = name;
            if (info.startsWith("//")) {
                info = info.substring("//".length());
            }
            int idx = info.indexOf('/');
            if (idx < 0) {
                return hostList;
            }
            final String server = info.substring(0, idx);
            if (server.indexOf(',') >= 0) {
                for (String host : server.split(",")) {
                    hostList.add(host);
                }
            } else {
                hostList.add(server);
            }
        } else {
            hostList.add("local");
        }

        return hostList;
    }

    public String getDatabase() {
        final String name = connectInfo.getName();
        final boolean remote = connectInfo.isRemote();
        return getDatabase(name, remote);
    }

    public String getDatabase(String name, boolean remote) {
        if (remote) {
            String info = name;
            if (info.startsWith("//")) {
                info = info.substring("//".length());
            }
            int idx = info.indexOf('/');
            if (idx < 0) {
                return UNKNOWN_DATABASE;
            }
            return info.substring(idx + 1);
        }
        return name;
    }

    static class ConnectInfo {
        private String name;
        private boolean remote;

        public ConnectInfo() {
            this.name = "unknown";
            this.remote = false;
        }

        public ConnectInfo(String url) {
            final String parsedUrl = parseUrl(url);
            this.name = parseName(parsedUrl);
        }

        String parseUrl(String url) {
            if (url == null) {
                return "";
            }

            String parsed = url;
            if (parsed.startsWith(START_URL)) {
                parsed = parsed.substring(START_URL.length());
            }
            int idx = parsed.indexOf(';');
            if (idx >= 0) {
                return parsed.substring(0, idx);
            }
            return parsed;
        }

        String parseName(String url) {
            boolean persistent = false;
            boolean unnamed = false;

            String name = url;
            if (".".equals(name)) {
                name = "mem:";
            }
            if (name.startsWith("tcp:")) {
                remote = true;
                name = name.substring("tcp:".length());
            } else if (name.startsWith("ssl:")) {
                remote = true;
                name = name.substring("ssl:".length());
            } else if (name.startsWith("mem:")) {
                persistent = false;
                if ("mem:".equals(name)) {
                    unnamed = true;
                }
            } else if (name.startsWith("file:")) {
                name = name.substring("file:".length());
                persistent = true;
            } else {
                persistent = true;
            }
            if (persistent && !remote) {
                name = nameSeparatorsToNative(name);
            }

            if (unnamed) {
                return "unnamed-in-memory";
            }
            return name;
        }

        String nameSeparatorsToNative(String path) {
            return path.replace('\\', '/');
        }

        public String getName() {
            return name;
        }

        public boolean isRemote() {
            return remote;
        }
    }
}
