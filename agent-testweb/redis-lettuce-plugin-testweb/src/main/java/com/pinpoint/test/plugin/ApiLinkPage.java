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
 */

package com.pinpoint.test.plugin;

import j2html.tags.specialized.ATag;
import j2html.tags.specialized.HtmlTag;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.title;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApiLinkPage {
    private final String title;

    public ApiLinkPage(String title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public String build(List<String> routes) {
        List<ATag> collect = routes.stream()
                .map(this::aTag)
                .collect(Collectors.toList());
        return buildHtml(collect);
    }

    private ATag aTag(String path) {
        return a()
                .withText(path)
                .withHref(path)
                .withTarget("_blank");
    }

    public String buildHtml(List<ATag> aTags) {

        HtmlTag html = html(
                head(title(title)),
                body(
                    h1(title),
                    each(aTags, aTag -> div(aTag))
                )
        );
        return html.render();
    }
}
