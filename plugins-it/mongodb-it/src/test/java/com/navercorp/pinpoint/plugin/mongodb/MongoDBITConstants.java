/*
 * Copyright 2021 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance,the License.
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

package com.navercorp.pinpoint.plugin.mongodb;

public final class MongoDBITConstants {

    public static final String BIND_ADDRESS = "localhost";

    public static final int PORT = 27018;

    public static final String MONGODB_ADDRESS = BIND_ADDRESS + ":" + PORT;

    public static final String EMBED_MONGODB_VERSION = "de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.4.6";

}
