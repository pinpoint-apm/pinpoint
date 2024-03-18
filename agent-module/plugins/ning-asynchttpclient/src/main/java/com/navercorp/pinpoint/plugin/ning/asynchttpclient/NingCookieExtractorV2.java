/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

import java.util.Iterator;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NingCookieExtractorV2 implements CookieExtractor<Request> {

    public static final CookieExtractor<Request> INSTANCE = new NingCookieExtractorV2();

    @Override
    public String getCookie(Request httpRequest) {
        List<Cookie> cookies = httpRequest.getCookies();
        if (cookies.isEmpty()) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        Iterator<Cookie> iterator = cookies.iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();
            sb.append(cookie.getName());
            sb.append('=');
            sb.append(cookie.getValue());
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }
}
