/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.util;


import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
public class TagParser {
    public TagParser() {
    }

    public List<Tag> parseTags(List<String> tags) {
        if (tags == null || tags.size() == 0) {
            return new ArrayList<>();
        }

        List<Tag> tagList = new ArrayList<>();
        for (String tagString : tags) {
            Tag tag = parseTag(tagString);
            tagList.add(tag);
        }
        return tagList;
    }

    public List<Tag> parseTags(String tagStrings) {
        if (tagStrings == null || tagStrings.contains("null")) {
            return new ArrayList<>();
        }

        List<Tag> tagList = new ArrayList<>();

        String[] tagStrArray = parseMultiValueFieldList(tagStrings);
        for (String tagString : tagStrArray) {
            Tag tag = parseTag(tagString);
            tagList.add(tag);
        }

        return tagList;
    }

    public Tag parseTag(String tagString) {
        Objects.requireNonNull(tagString, "tagString");

        String[] tag = tagString.split(":");
        return new Tag(tag[0], tag[1]);
    }

    private static String[] parseMultiValueFieldList(String string) {
        return string.replaceAll("[\\[\\]\"]", "").split(",");
    }
}
