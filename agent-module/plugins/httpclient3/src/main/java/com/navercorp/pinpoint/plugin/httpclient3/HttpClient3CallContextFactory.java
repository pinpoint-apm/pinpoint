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

package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.AttachmentFactory;

/**
 * @author emeroad
 */
public class HttpClient3CallContextFactory implements AttachmentFactory {

    public static final AttachmentFactory HTTPCLIENT3_CONTEXT_FACTORY = new HttpClient3CallContextFactory();

    @Override
    public Object createAttachment() {
        return new HttpClient3CallContext();
    }
}
