/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.Charsets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author emeroad
 */
public final class HttpUtils {

    private static final String UTF8 = Charsets.UTF_8_NAME;

    private static final String CHARSET = "charset=";

    private HttpUtils() {
    }

    public static String parseContentTypeCharset(String contentType) {
        return parseContentTypeCharset(contentType, UTF8);
    }

    public static String parseContentTypeCharset(String contentType, String defaultCharset) {
        if (contentType == null) {
            // default spec specifies iso-8859-1, but most WASes set it to UTF-8.
            // might be better to make it configurable
            return defaultCharset;
        }
        int charsetStart = contentType.indexOf(CHARSET);
        if (charsetStart == -1) {
            // none
            return defaultCharset;
        }
        charsetStart = charsetStart + CHARSET.length();
        int charsetEnd = contentType.indexOf(';', charsetStart);
        if (charsetEnd == -1) {
            charsetEnd = contentType.length();
        }
        contentType = contentType.substring(charsetStart, charsetEnd);

        return contentType.trim();
    }

    public static String doGet(String httpUrl, int connectTimeout, int readTimeout) throws IOException {
        //链接
        HttpURLConnection connection=null;
        InputStream is=null;
        BufferedReader br = null;
        StringBuffer result=new StringBuffer();
        try {
            //创建连接
            URL url=new URL(httpUrl);
            connection= (HttpURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //设置连接超时时间
            connection.setConnectTimeout(connectTimeout);
            //设置读取超时时间
            connection.setReadTimeout(readTimeout);
            //开始连接
            connection.connect();
            //获取响应数据
            if(connection.getResponseCode()==200){
                //获取返回的数据
                is=connection.getInputStream();
                if(is!=null){
                    br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
                    String temp = null;
                    while ((temp=br.readLine())!=null){
                        result.append(temp+"\r\n");
                    }
                }
            }
        } catch (MalformedURLException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }finally {
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connection.disconnect();// 关闭远程连接
        }
        return result.toString();
    }
    public static String doGet(String httpUrl) throws IOException {
        return doGet(httpUrl, 3000, 2000);
    }
}
