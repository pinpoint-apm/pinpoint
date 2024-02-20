/*
 * Copyright 2024 NAVER Corp.
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
package org.springframework.http.client.reactive;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatusCode;

import java.io.Closeable;

/**
 * @author intr3p1d
 */
public interface ClientHttpResponse extends HttpInputMessage, Closeable {
    HttpStatusCode getStatusCode();

    int getRawStatusCode();
}
