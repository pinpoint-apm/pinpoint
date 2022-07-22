package com.navercorp.pinpoint.web.hyperlink;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class HyperLinkFactory {
    private static HyperLinkFactory EMPTY = new HyperLinkFactory(List.of());

    private final MatcherGroup[] matcherGroups;

    public HyperLinkFactory(List<MatcherGroup> matcherGroups) {
        if (CollectionUtils.isEmpty(matcherGroups)) {
            this.matcherGroups = new MatcherGroup[0];
        } else {
            this.matcherGroups = matcherGroups.toArray(new MatcherGroup[0]);
        }
    }

    public List<HyperLink> build(LinkSource source) {
        if (ArrayUtils.isEmpty(matcherGroups)) {
            return List.of();
        }
        List<HyperLink> list = new ArrayList<>();
        for (MatcherGroup matcherGroup : matcherGroups) {
            if (matcherGroup.isMatchingType(source)) {
                HyperLink linkInfo = matcherGroup.makeLinkInfo(source);
                list.add(linkInfo);
            }
        }
        return list;
    }

    public static HyperLinkFactory empty() {
        return EMPTY;
    }
}
