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

import static com.navercorp.pinpoint.web.servlet.VersionPrefixRewriter.DEFAULT_MAIN_PATH;

class VersionPrefixRewriterTest {

    private final String version = "/v3";
    private final String main = DEFAULT_MAIN_PATH;
    private final VersionPrefixRewriter rewriter = new VersionPrefixRewriter();

    @Test
    public void api() {
        String rewrite = rewriter.rewrite("/api/servermap");
        Assertions.assertNull(rewrite);
    }

    @Test
    public void apiPublic() {
        String rewrite = rewriter.rewrite("/api-public/serverTime");
        Assertions.assertNull(rewrite);
    }

    @Test
    public void apiTest() {
        String rewrite = rewriter.rewrite("/api-test/test");
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
        Assertions.assertEquals(main, rewrite);
    }

    @Test
    public void dispatch_resource() {
        String rewrite = rewriter.rewrite("/assets");
        Assertions.assertNull(rewrite);
    }

    @Test
    public void dispatch_resource_main() {
        String rewrite = rewriter.rewrite("/main");
        Assertions.assertEquals(main, rewrite);
    }

    @Test
    public void dispatch_resource_jpg() {
        String rewrite = rewriter.rewrite("/test.jpg");
        Assertions.assertNull(rewrite);
    }


    @Test
    public void version_main() {
        String rewrite = rewriter.rewrite(version);
        Assertions.assertEquals(version + main, rewrite);
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
        Assertions.assertEquals(version + main, rewrite);
    }

}