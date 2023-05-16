package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LinkList implements Iterable<Link> {
    private final List<Link> linkList;

    public LinkList() {
        this.linkList = new ArrayList<>();
    }

    public LinkList(List<Link> linkList) {
        this.linkList = Objects.requireNonNull(linkList, "linkList");
    }

    public boolean add(Link link) {
        return this.linkList.add(link);
    }

    @Override
    public Iterator<Link> iterator() {
        return linkList.iterator();
    }

    @Override
    public void forEach(Consumer<? super Link> action) {
        Objects.requireNonNull(action, "action");

        this.linkList.forEach(action);
    }

    public boolean remove(Link link) {
        return this.linkList.remove(link);
    }

    public boolean removeAll(Collection<Link> link) {
        return this.linkList.removeAll(link);
    }

    public boolean removeAll(LinkList target) {
        Objects.requireNonNull(target, "target");

        return this.linkList.removeAll(target.linkList);
    }

    public boolean isEmpty() {
        return this.linkList.isEmpty();
    }

    public LinkList filter(final Predicate<Link> filter) {
        if (CollectionUtils.isEmpty(linkList)) {
            return new LinkList();
        }
        Objects.requireNonNull(linkList, "linkList");

        List<Link> newLinkList = linkList.stream()
                .filter(filter)
                .collect(Collectors.toList());
        return new LinkList(newLinkList);
    }

    public static Predicate<Link> spanFilter(SpanBo span) {
        return new Predicate<Link>() {
            @Override
            public boolean test(Link link) {
                if (span.getParentSpanId() == link.getParentSpanId() && span.getSpanId() == link.getSpanId()) {
                    // skip self's link
                    return false;
                }
                if (link.getNextSpanId() == span.getParentSpanId()) {
                    return true;
                }
                return false;
            }
        };
    }

    public Link matchSpan(final SpanBo span) {
        if (CollectionUtils.isEmpty(linkList)) {
            return null;
        }

        linkList.sort(Comparator.comparingLong(Link::getStartTimeMillis));

        Optional<Link> first = linkList.stream()
                .filter(LinkList.startTimeFilter(span))
                .findFirst();

        return first.orElse(null);
    }

    public static Predicate<Link> startTimeFilter(final SpanBo span) {
        return new Predicate<Link>() {
            @Override
            public boolean test(Link link) {
                return link.getStartTimeMillis() <= span.getStartTime();
            }
        };

    }


}
