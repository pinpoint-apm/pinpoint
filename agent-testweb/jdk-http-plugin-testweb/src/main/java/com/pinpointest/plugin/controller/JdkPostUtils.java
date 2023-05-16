/*
 * Copyright 2021 NAVER Corp.
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

package com.pinpointest.plugin.controller;


import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JdkPostUtils {
    private static final String CHAR_SET = StandardCharsets.UTF_8.name();

    public static HttpURLConnection getHttpURLPostConnection(String postUrl) throws Exception {
        URL url = new URL(postUrl);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setDoInput(true);
        http.setDoOutput(true);
        http.setAllowUserInteraction(true);
        http.setRequestMethod("POST");
        http.setRequestProperty("Content-type", "application/json");

        return http;
    }

    public static String checkLsmList(String url) throws Exception {
        HttpURLConnection http = null;
        OutputStream writer = null;
        InputStream reader = null;
        String jsonResult;
        try {
            http = getHttpURLPostConnection(url);
            //Set Parameter
            StringBuilder postSb = new StringBuilder();
            postSb.append("{foo:bar}");
            writer = http.getOutputStream();
            IOUtils.write(postSb, writer, CHAR_SET);
            IOUtils.closeQuietly(writer);
            http.connect();
            reader = http.getInputStream();
            jsonResult = IOUtils.toString(reader, CHAR_SET);
        } finally {
            if (http != null) {
                http.disconnect();
            }
            IOUtils.close(reader);
            IOUtils.close(writer);
        }

        return jsonResult;
    }
}