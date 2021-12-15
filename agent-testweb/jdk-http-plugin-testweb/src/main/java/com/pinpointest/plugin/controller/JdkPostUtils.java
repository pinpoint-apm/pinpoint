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

public class JdkPostUtils {
    public static HttpURLConnection getHttpURLPostConnection(String postUrl) throws Exception {
        HttpURLConnection http = null;
        URL url = new URL(postUrl);
        http = (HttpURLConnection) url.openConnection();
        http.setDoInput(true);
        http.setDoOutput(true);
        http.setAllowUserInteraction(true);
        http.setRequestMethod("POST");    //POST
        http.setRequestProperty("Content-type", "application/json");    //헤더셋팅

        return http;
    }

    public static String checkLsmList(String url) throws Exception {
        HttpURLConnection http = null;
        OutputStream writer = null;
        InputStream reader = null;
        String jsonResult = "";
        try {
            String encodeType = "UTF-8";
            String defaultErrorMsg = "connection error";
            http = getHttpURLPostConnection(url);

            //Set Parameter
            StringBuffer postSb = new StringBuffer();
            postSb.append("{foo:bar}");
            writer = http.getOutputStream();
            IOUtils.write(postSb, writer, encodeType);
            IOUtils.closeQuietly(writer);
            http.connect();
            reader = http.getInputStream();
            jsonResult = IOUtils.toString(reader, encodeType);
        } finally {
            if (http != null) {
                http.disconnect();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        }

        return jsonResult;
    }
}