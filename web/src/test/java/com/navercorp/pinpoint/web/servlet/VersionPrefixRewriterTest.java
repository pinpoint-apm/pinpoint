/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.servlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.navercorp.pinpoint.web.servlet.VersionPrefixRewriter.MAIN;

class VersionPrefixRewriterTest {

    private static final String VERSION = "/v3";

    VersionPrefixRewriter rewriter = new VersionPrefixRewriter();

    @Test
    public void api() {
        String rewrite = rewriter.rewrite("/api/servermap");
        Assertions.assertNull(rewrite);
    }

    @Test
    public void normalize() {
        String rewrite = rewriter.rewrite("///api///servermap");
        Assertions.assertNull(rewrite);
    }

    @Test
    void dispatch() {
        String rewrite = rewriter.rewrite("/");
        Assertions.assertEquals(MAIN, rewrite);
    }

    @Test
    public void dispatch_resource() {
        String rewrite = rewriter.rewrite("/assets");
        Assertions.assertNull(rewrite);
    }

    @Test
    public void dispatch_resource_main() {
        String rewrite = rewriter.rewrite("/main");
        Assertions.assertEquals(MAIN, rewrite);
    }

    @Test
    public void dispatch_resource_jpg() {
        String rewrite = rewriter.rewrite("/test.jpg");
        Assertions.assertNull(rewrite);
    }


    @Test
    public void version_main() {
        String rewrite = rewriter.rewrite(VERSION);
        Assertions.assertEquals(VERSION + MAIN, rewrite);
    }

    @Test
    public void version_api() {
        String rewrite = rewriter.rewrite("/v3/api");
        Assertions.assertNotNull(rewrite);
    }

    @Test
    public void version_resource() {
        String rewrite = rewriter.rewrite("/v3/assets");
        Assertions.assertNull(rewrite);
    }

    @Test
    public void version_resource_main() {
        String rewrite = rewriter.rewrite("/v3/main");
        Assertions.assertEquals(VERSION + MAIN, rewrite);
    }

}