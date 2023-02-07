package com.pinpoint.test.common.view;

import j2html.tags.specialized.ATag;
import j2html.tags.specialized.HtmlTag;

import java.util.ArrayList;
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
    private List<ATag> aTags = new ArrayList<>();

    public ApiLinkPage(String title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public ApiLinkPage addHrefTag(List<HrefTag> hrefTags) {
        List<ATag> collect = hrefTags.stream()
                .map(this::aTag)
                .collect(Collectors.toList());
        this.aTags.addAll(collect);
        return this;
    }

    private ATag aTag(HrefTag hrefTag) {
        return a()
                .withText(hrefTag.getText())
                .withHref(hrefTag.getPath())
                .withTarget("_blank");
    }

    public String build() {

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
