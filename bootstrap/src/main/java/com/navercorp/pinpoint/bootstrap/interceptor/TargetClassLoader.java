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

package com.navercorp.pinpoint.bootstrap.interceptor;

/**
 * 마커 newInterceptor를 통해 new할 경우 마크로 붙여야 한다. new Interceptor()를 하지 않아도 되는 곳에 강제 로딩을 하였을 경우 에러를 발생시키기 위해서 만듬.
 * @author emeroad
 */
public interface TargetClassLoader {
}
