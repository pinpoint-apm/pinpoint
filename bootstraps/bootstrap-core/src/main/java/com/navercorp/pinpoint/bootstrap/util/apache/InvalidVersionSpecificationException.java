/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.navercorp.pinpoint.bootstrap.util.apache;

// copy 2021.09.08 -> master branch : b06469053450eafee03aae287cc4ae3a1f4d9645
// https://github.com/apache/maven-resolver/commits/master

/**
 * Thrown when a version or version range could not be parsed.
 */
public class InvalidVersionSpecificationException extends RuntimeException {

    private final String version;

    /**
     * Creates a new exception with the specified version and detail message.
     *
     * @param version The invalid version specification, may be {@code null}.
     * @param message The detail message, may be {@code null}.
     */
    public InvalidVersionSpecificationException(String version, String message) {
        super(message);
        this.version = version;
    }

    /**
     * Creates a new exception with the specified version and cause.
     *
     * @param version The invalid version specification, may be {@code null}.
     * @param cause   The exception that caused this one, may be {@code null}.
     */
    public InvalidVersionSpecificationException(String version, Throwable cause) {
        super("Could not parse version specification " + version + ": ," + cause != null ? cause.getMessage() : "", cause);
        this.version = version;
    }

    /**
     * Creates a new exception with the specified version, detail message and cause.
     *
     * @param version The invalid version specification, may be {@code null}.
     * @param message The detail message, may be {@code null}.
     * @param cause   The exception that caused this one, may be {@code null}.
     */
    public InvalidVersionSpecificationException(String version, String message, Throwable cause) {
        super(message, cause);
        this.version = version;
    }

    /**
     * Gets the version or version range that could not be parsed.
     *
     * @return The invalid version specification or {@code null} if unknown.
     */
    public String getVersion() {
        return version;
    }

}
