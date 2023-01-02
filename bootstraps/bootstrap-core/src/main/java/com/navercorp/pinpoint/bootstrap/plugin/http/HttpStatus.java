/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.navercorp.pinpoint.bootstrap.plugin.http;


/**
 * copy from : Apache httpcomponents-core https://github.com/apache/httpcomponents-core
 * https://github.com/apache/httpcomponents-core/blob/master/httpcore5/src/main/java/org/apache/hc/core5/http/HttpStatus.java
 * Constants enumerating the HTTP status codes.
 * All status codes defined in RFC 7231 (HTTP/1.1), RFC 2518 (WebDAV), RFC 7540 (HTTP/2),
 * RFC 6585 (Additional HTTP Status Codes), RFC 8297 (Early Hints), RFC 7538 (Permanent Redirect),
 * RFC 7725 (An HTTP Status Code to Report Legal Obstacles) and RFC 2295 (Transparent Content
 * Negotiation) are listed.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231 (HTTP/1.1)</a>
 * @see <a href="https://tools.ietf.org/html/rfc2518">RFC 2518 (WebDAV)</a>
 * @see <a href="https://tools.ietf.org/html/rfc7540">RFC 7540 (HTTP/2)</a>
 * @see <a href="https://tools.ietf.org/html/rfc6585">RFC 6585 (Additional HTTP Status Codes)</a>
 * @see <a href="https://tools.ietf.org/html/rfc8297">RFC 8297 (Early Hints)</a>
 * @see <a href="https://tools.ietf.org/html/rfc7538">RFC 7538 (Permanent Redirect)</a>
 * @see <a href="https://tools.ietf.org/html/rfc7725">RFC 7725 (An HTTP Status Code to Report Legal Obstacles)</a>
 * @see <a href="https://tools.ietf.org/html/rfc2295">RFC 2295 (Transparent Content Negotiation)</a>
 * @see <a href="https://tools.ietf.org/html/rfc2817">RFC 2817 (Upgrading to TLS Within HTTP/1.1)</a>
 * @see <a href="https://tools.ietf.org/html/rfc8470">RFC 8470 (Using Early Data in HTTP)</a>
 * @since 4.0
 */
public final class HttpStatus {

    private HttpStatus() {
        // no instances.
    }

    // --- 1xx Informational ---
    /** {@code 100 1xx Informational} (HTTP/1.1 - RFC 7231) */
    public static final int SC_INFORMATIONAL = 100;

    /** {@code 100 Continue} (HTTP/1.1 - RFC 7231) */
    public static final int SC_CONTINUE = 100;
    /** {@code 101 Switching Protocols} (HTTP/1.1 - RFC 7231)*/
    public static final int SC_SWITCHING_PROTOCOLS = 101;
    /** {@code 102 Processing} (WebDAV - RFC 2518) */
    public static final int SC_PROCESSING = 102;
    /** {@code 103 Early Hints (Early Hints - RFC 8297)}*/
    public static final int SC_EARLY_HINTS = 103;

    // --- 2xx Success ---
    /** {@code 2xx Success} (HTTP/1.0 - RFC 7231) */
    public static final int SC_SUCCESS = 200;

    /** {@code 200 OK} (HTTP/1.0 - RFC 7231) */
    public static final int SC_OK = 200;
    /** {@code 201 Created} (HTTP/1.0 - RFC 7231) */
    public static final int SC_CREATED = 201;
    /** {@code 202 Accepted} (HTTP/1.0 - RFC 7231) */
    public static final int SC_ACCEPTED = 202;
    /** {@code 203 Non Authoritative Information} (HTTP/1.1 - RFC 7231) */
    public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
    /** {@code 204 No Content} (HTTP/1.0 - RFC 7231) */
    public static final int SC_NO_CONTENT = 204;
    /** {@code 205 Reset Content} (HTTP/1.1 - RFC 7231) */
    public static final int SC_RESET_CONTENT = 205;
    /** {@code 206 Partial Content} (HTTP/1.1 - RFC 7231) */
    public static final int SC_PARTIAL_CONTENT = 206;
    /**
     * {@code 207 Multi-Status} (WebDAV - RFC 2518)
     * or
     * {@code 207 Partial Update OK} (HTTP/1.1 - draft-ietf-http-v11-spec-rev-01?)
     */
    public static final int SC_MULTI_STATUS = 207;
    /**
     * {@code 208 Already Reported} (WebDAV - RFC 5842, p.30, section 7.1)
     */
    public static final int SC_ALREADY_REPORTED = 208;
    /**
     * {@code 226 IM Used} (Delta encoding in HTTP - RFC 3229, p. 30, section 10.4.1)
     */
    public static final int SC_IM_USED = 226;

    // --- 3xx Redirection ---
    /** {@code 3xx Redirection} (HTTP/1.1 - RFC 7231) */
    public static final int SC_REDIRECTION = 300;

    /** {@code 300 Multiple Choices} (HTTP/1.1 - RFC 7231) */
    public static final int SC_MULTIPLE_CHOICES = 300;
    /** {@code 301 Moved Permanently} (HTTP/1.0 - RFC 7231) */
    public static final int SC_MOVED_PERMANENTLY = 301;
    /** {@code 302 Moved Temporarily} (Sometimes {@code Found}) (HTTP/1.0 - RFC 7231) */
    public static final int SC_MOVED_TEMPORARILY = 302;
    /** {@code 303 See Other} (HTTP/1.1 - RFC 7231) */
    public static final int SC_SEE_OTHER = 303;
    /** {@code 304 Not Modified} (HTTP/1.0 - RFC 7231) */
    public static final int SC_NOT_MODIFIED = 304;
    /** {@code 305 Use Proxy} (HTTP/1.1 - RFC 7231) */
    public static final int SC_USE_PROXY = 305;
    /** {@code 307 Temporary Redirect} (HTTP/1.1 - RFC 7231) */
    public static final int SC_TEMPORARY_REDIRECT = 307;

    /** {@code 308 Permanent Redirect} (HTTP/1.1 - RFC 7538) */
    public static final int SC_PERMANENT_REDIRECT = 308;

    // --- 4xx Client Error ---
    /** {@code 4xx Client Error} (HTTP/1.1 - RFC 7231) */
    public static final int SC_CLIENT_ERROR = 400;

    /** {@code 400 Bad Request} (HTTP/1.1 - RFC 7231) */
    public static final int SC_BAD_REQUEST = 400;
    /** {@code 401 Unauthorized} (HTTP/1.0 - RFC 7231) */
    public static final int SC_UNAUTHORIZED = 401;
    /** {@code 402 Payment Required} (HTTP/1.1 - RFC 7231) */
    public static final int SC_PAYMENT_REQUIRED = 402;
    /** {@code 403 Forbidden} (HTTP/1.0 - RFC 7231) */
    public static final int SC_FORBIDDEN = 403;
    /** {@code 404 Not Found} (HTTP/1.0 - RFC 7231) */
    public static final int SC_NOT_FOUND = 404;
    /** {@code 405 Method Not Allowed} (HTTP/1.1 - RFC 7231) */
    public static final int SC_METHOD_NOT_ALLOWED = 405;
    /** {@code 406 Not Acceptable} (HTTP/1.1 - RFC 7231) */
    public static final int SC_NOT_ACCEPTABLE = 406;
    /** {@code 407 Proxy Authentication Required} (HTTP/1.1 - RFC 7231)*/
    public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
    /** {@code 408 Request Timeout} (HTTP/1.1 - RFC 7231) */
    public static final int SC_REQUEST_TIMEOUT = 408;
    /** {@code 409 Conflict} (HTTP/1.1 - RFC 7231) */
    public static final int SC_CONFLICT = 409;
    /** {@code 410 Gone} (HTTP/1.1 - RFC 7231) */
    public static final int SC_GONE = 410;
    /** {@code 411 Length Required} (HTTP/1.1 - RFC 7231) */
    public static final int SC_LENGTH_REQUIRED = 411;
    /** {@code 412 Precondition Failed} (HTTP/1.1 - RFC 7231) */
    public static final int SC_PRECONDITION_FAILED = 412;
    /** {@code 413 Request Entity Too Large} (HTTP/1.1 - RFC 7231) */
    public static final int SC_REQUEST_TOO_LONG = 413;
    /** {@code 414 Request-URI Too Long} (HTTP/1.1 - RFC 7231) */
    public static final int SC_REQUEST_URI_TOO_LONG = 414;
    /** {@code 415 Unsupported Media Type} (HTTP/1.1 - RFC 7231) */
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
    /** {@code 416 Requested Range Not Satisfiable} (HTTP/1.1 - RFC 7231) */
    public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    /** {@code 417 Expectation Failed} (HTTP/1.1 - RFC 7231) */
    public static final int SC_EXPECTATION_FAILED = 417;
    /** {@code 421 Misdirected Request} (HTTP/2 - RFC 7540) */
    public static final int SC_MISDIRECTED_REQUEST = 421;

    /**
     * Static constant for a 419 error.
     * {@code 419 Insufficient Space on Resource}
     * (WebDAV - draft-ietf-webdav-protocol-05?)
     * or {@code 419 Proxy Reauthentication Required}
     * (HTTP/1.1 drafts?)
     */
    public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    /**
     * Static constant for a 420 error.
     * {@code 420 Method Failure}
     * (WebDAV - draft-ietf-webdav-protocol-05?)
     */
    public static final int SC_METHOD_FAILURE = 420;
    /** {@code 422 Unprocessable Entity} (WebDAV - RFC 2518) */
    public static final int SC_UNPROCESSABLE_ENTITY = 422;
    /** {@code 423 Locked} (WebDAV - RFC 2518) */
    public static final int SC_LOCKED = 423;
    /** {@code 424 Failed Dependency} (WebDAV - RFC 2518) */
    public static final int SC_FAILED_DEPENDENCY = 424;
    /** {@code 425 Too Early} (Using Early Data in HTTP - RFC 8470) */
    public static final int SC_TOO_EARLY = 425;
    /** {@code 426 Upgrade Dependency} (HTTP/1.1 - RFC 2817) */
    public static final int SC_UPGRADE_REQUIRED = 426;
    /** {@code 428 Precondition Required} (Additional HTTP Status Codes - RFC 6585) */
    public static final int SC_PRECONDITION_REQUIRED = 428;
    /** {@code 429 Too Many Requests} (Additional HTTP Status Codes - RFC 6585) */
    public static final int SC_TOO_MANY_REQUESTS = 429;
    /** {@code 431 Request Header Fields Too Large} (Additional HTTP Status Codes - RFC 6585) */
    public static final int SC_REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
    /** {@code 451 Unavailable For Legal Reasons} (Legal Obstacles - RFC 7725) */
    public static final int SC_UNAVAILABLE_FOR_LEGAL_REASONS = 451;

    // --- 5xx Server Error ---
    /** {@code 500 Server Error} (HTTP/1.0 - RFC 7231) */
    public static final int SC_SERVER_ERROR = 500;

    /** {@code 500 Internal Server Error} (HTTP/1.0 - RFC 7231) */
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    /** {@code 501 Not Implemented} (HTTP/1.0 - RFC 7231) */
    public static final int SC_NOT_IMPLEMENTED = 501;
    /** {@code 502 Bad Gateway} (HTTP/1.0 - RFC 7231) */
    public static final int SC_BAD_GATEWAY = 502;
    /** {@code 503 Service Unavailable} (HTTP/1.0 - RFC 7231) */
    public static final int SC_SERVICE_UNAVAILABLE = 503;
    /** {@code 504 Gateway Timeout} (HTTP/1.1 - RFC 7231) */
    public static final int SC_GATEWAY_TIMEOUT = 504;
    /** {@code 505 HTTP Version Not Supported} (HTTP/1.1 - RFC 7231) */
    public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
    /** {@code 506 Variant Also Negotiates} ( Transparent Content Negotiation - RFC 2295) */
    public static final int SC_VARIANT_ALSO_NEGOTIATES = 506;
    /** {@code 507 Insufficient Storage} (WebDAV - RFC 2518) */
    public static final int SC_INSUFFICIENT_STORAGE = 507;

    /**
     * {@code 508 Loop Detected} (WebDAV - RFC 5842, p.33, section 7.2)
     */
    public static final int SC_LOOP_DETECTED = 508;

    /**
     * {@code 510 Not Extended} (An HTTP Extension Framework - RFC 2774, p. 10, section 7)
     */
    public static final int SC_NOT_EXTENDED = 510;

    /** {@code  511 Network Authentication Required} (Additional HTTP Status Codes - RFC 6585) */
    public static final int SC_NETWORK_AUTHENTICATION_REQUIRED = 511;

}

