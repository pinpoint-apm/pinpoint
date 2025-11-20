/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpMethod implements Comparable<HttpMethod> {

    /**
     * GET method
     */
    public static final HttpMethod GET = new HttpMethod("GET");

    /**
     * POST method
     */
    public static final HttpMethod POST = new HttpMethod("POST");

    /**
     * PUT method
     */
    public static final HttpMethod PUT = new HttpMethod("PUT");

    /**
     * DELETE method
     */
    public static final HttpMethod DELETE = new HttpMethod("DELETE");

    /**
     * The HEAD method
     */
    public static final HttpMethod HEAD = new HttpMethod("HEAD");

    /**
     * OPTIONS method
     */
    public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS");

    /**
     * PATCH method
     */
    public static final HttpMethod PATCH = new HttpMethod("PATCH");

    /**
     * TRACE method
     */
    public static final HttpMethod TRACE = new HttpMethod("TRACE");

    /**
     * CONNECT method
     */
    public static final HttpMethod CONNECT = new HttpMethod("CONNECT");


    /**
     * The RFC 2518/4918 {@code PROPPATCH} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod PROPPATCH = new HttpMethod("PROPPATCH");


    /**
     * The RFC 2518/4918 {@code COPY} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod COPY = new HttpMethod("COPY");

    /**
     * The RFC 2518/4918 {@code MOVE} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod MOVE = new HttpMethod("MOVE");

    /**
     * The RFC 2518/4918 {@code LOCK} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod LOCK = new HttpMethod("LOCK");

    /**
     * The RFC 2518/4918 {@code UNLOCK} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod UNLOCK = new HttpMethod("UNLOCK");
    /**
     * The RFC 3253 {@code VERSION_CONTROL} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod VERSION_CONTROL = new HttpMethod("VERSION_CONTROL");

    /**
     * The RFC 3253 {@code REPORT} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod REPORT = new HttpMethod("REPORT");

    /**
     * The RFC 3253 {@code CHECKOUT} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod CHECKOUT = new HttpMethod("CHECKOUT");

    /**
     * The RFC 3253 {@code CHECKIN} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod CHECKIN = new HttpMethod("CHECKIN");

    /**
     * The RFC 3253 {@code UNCHECKOUT} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod UNCHECKOUT = new HttpMethod("UNCHECKOUT");


    /**
     * The RFC 3253 {@code UPDATE} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod UPDATE = new HttpMethod("UPDATE");

    /**
     * The RFC 3253 {@code LABEL} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod LABEL = new HttpMethod("LABEL");

    /**
     * The RFC 3253 {@code MERGE} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod MERGE = new HttpMethod("MERGE");

    /**
     * The RFC 3253 {@code BASELINE_CONTROL} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod BASELINE_CONTROL = new HttpMethod("BASELINE_CONTROL");

    /**
     * The RFC 3648 {@code ORDERPATCH} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod ORDERPATCH = new HttpMethod("ORDERPATCH");

    /**
     * The RFC 3744 {@code ACL} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod ACL = new HttpMethod("ACL");

    /**
     * The RFC 5323 {@code SEARCH} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod SEARCH = new HttpMethod("SEARCH");

    /**
     * The RFC 2518/4918 {@code MKCOL} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod MKCOL = new HttpMethod("MKCOL");

    /**
     * The RFC 3253 {@code MKACTIVITY} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod MKACTIVITY = new HttpMethod("MKACTIVITY");

    /**
     * The RFC 3253 {@code MKWORKSPACE} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod MKWORKSPACE = new HttpMethod("MKWORKSPACE");

    /**
     * The RFC 4791 {@code MKCALENDAR} method, this instance is interned and uniquely used.
     */
    public static final HttpMethod MKCALENDAR = new HttpMethod("MKCALENDAR");




    private static final HttpMethod[] BASIC_METHODS = {
            GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE, CONNECT,
    };

    private static final HttpMethod[] EXTENDED_METHODS = {
            PROPPATCH, COPY, MOVE, LOCK, UNLOCK,
            VERSION_CONTROL, REPORT, CHECKOUT, CHECKIN,
            UNCHECKOUT, LABEL, MERGE, BASELINE_CONTROL,
            ORDERPATCH, ACL, SEARCH,
            MKCOL, MKACTIVITY, MKWORKSPACE, MKCALENDAR,
    };

    private static final HttpMethod[] ALL_METHODS = copy(BASIC_METHODS, EXTENDED_METHODS);

    private static final Map<String, HttpMethod> EXTENDED_MAP = nameMap(EXTENDED_METHODS);

    private static Map<String, HttpMethod> nameMap(HttpMethod[] methods) {
        HashMap<String, HttpMethod> map = new HashMap<>(methods.length);
        for (HttpMethod method : methods) {
            HttpMethod exist = map.put(method.name, method);
            if (exist != null) {
                throw new IllegalStateException("duplicate method name:" + method.name);
            }
        }
        return map;
    }

    private static HttpMethod[] copy(HttpMethod[] methods1, HttpMethod[] methods2) {
        HttpMethod[] sum = new HttpMethod[methods1.length + methods2.length];
        System.arraycopy(methods1, 0, sum, 0, methods1.length);
        System.arraycopy(methods2, 0, sum, methods1.length, methods2.length);
        return sum;
    }

    private final String name;

    public static HttpMethod of(String name) {
        Objects.requireNonNull(name, "name");
        return new HttpMethod(name);
    }

    HttpMethod(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        HttpMethod that = (HttpMethod) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(HttpMethod o) {
        if (o == this) {
            return 0;
        }
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Resolves a given HTTP method name to its corresponding {@code HttpMethod} instance.
     *
     * @param method the name of the HTTP method to resolve; it must match one of the standard HTTP methods
     *               (e.g., "GET", "POST", "PUT", "DELETE", etc.), case-sensitive. If it does not match any
     *               predefined methods, a custom {@code HttpMethod} instance will be created.
     * @return the corresponding {@code HttpMethod} instance for the given method name. If the provided
     *         method does not match any predefined methods, a custom {@code HttpMethod} instance
     *         will be generated.
     */
    public static HttpMethod valueOf(String method) {
        Objects.requireNonNull(method, "method");

        switch (method) {
            case "GET":
                return GET;
            case "POST":
                return POST;
            case "PUT":
                return PUT;
            case "DELETE":
                return DELETE;
            case "HEAD":
                return HEAD;
            case "OPTIONS":
                return OPTIONS;
            case "PATCH":
                return PATCH;
            case "TRACE":
                return TRACE;
            case "CONNECT":
                return CONNECT;
        }
        final HttpMethod httpMethod = EXTENDED_MAP.get(method);
        if (httpMethod != null) {
            return httpMethod;
        }
        return null;
    }

    /**
     * Resolves a given HTTP method name to its corresponding {@code HttpMethod} instance.
     *
     * @param method the name of the HTTP method to resolve, case-insensitive
     * @return the corresponding {@code HttpMethod} instance for the given method name
     * @throws NullPointerException if the method name is null
     */
    public static HttpMethod valueOfIgnoreCase(String method) {
        Objects.requireNonNull(method, "method");
        return valueOf(method.toUpperCase());
    }
}
