package com.navercorp.pinpoint.web.hyperlink;

import com.navercorp.pinpoint.common.util.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HyperLinkFactory {
    private final MatcherGroup[] matcherGroups;

    public HyperLinkFactory(@Nullable List<MatcherGroup> matcherGroups) {
        if (matcherGroups == null) {
            this.matcherGroups = new MatcherGroup[0];
        } else {
            this.matcherGroups = matcherGroups.toArray(new MatcherGroup[0]);
        }
    }

    public List<HyperLink> build(LinkSource source) {
        if (ArrayUtils.isEmpty(matcherGroups)) {
            return Collections.emptyList();
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
}
