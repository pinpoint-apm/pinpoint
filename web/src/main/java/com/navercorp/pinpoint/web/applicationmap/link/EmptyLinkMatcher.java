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

package com.navercorp.pinpoint.web.applicationmap.link;

import com.navercorp.pinpoint.web.applicationmap.link.LinkInfo.LinkType;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 *
 */
public class EmptyLinkMatcher implements ServerMatcher {

    @Override
    public boolean isMatched(String value) {
        return true;
    }

    @Override
    public LinkInfo getLinkInfo(String value) {
        return new LinkInfo("", "", LinkType.ATAG);
    }

}