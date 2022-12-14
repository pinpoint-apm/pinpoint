package com.pinpoint.test.plugin;

import io.vertx.ext.web.Route;
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

    public String buildRoute(List<Route> routes) {
        List<ATag> collect = routes.stream()
                .map(this::aTag)
                .collect(Collectors.toList());
        return buildHtml(collect);
    }

    private ATag aTag(Route route) {
        return a()
                .withText(route.getPath())
                .withHref(route.getPath())
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
