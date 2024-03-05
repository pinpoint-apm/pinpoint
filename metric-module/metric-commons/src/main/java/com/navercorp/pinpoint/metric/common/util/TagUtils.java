/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.common.util;


import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Hyunjoon Cho
 */
public class TagUtils {

    private static final Pattern MULTI_VALUE_FIELD_PATTERN = Pattern.compile("[\\[\\]\"]");
    private static final Pattern JSON_TAG_STRING_PATTERN = Pattern.compile("[{}\"]");

    private TagUtils() {
    }

    public static List<Tag> parseTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return new ArrayList<>();
        }

        List<Tag> tagList = new ArrayList<>();
        for (String tagString : tags) {
            Tag tag = parseTag(tagString);
            tagList.add(tag);
        }
        return tagList;
    }

    public static List<Tag> parseTags(String tagStrings) {
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

    public static Tag parseTag(String tagString) {
        Objects.requireNonNull(tagString, "tagString");

        String[] tag = StringUtils.split(tagString, ":", 2);

        if (tag.length == 1) {
            return new Tag(tag[0], "");
        } else if (tag.length == 2) {
            return new Tag(tag[0], tag[1]);
        } else {
            throw new IllegalArgumentException("tagString:" + tagString);
        }
    }

    private static String[] parseMultiValueFieldList(String string) {
        String cleanStr = MULTI_VALUE_FIELD_PATTERN.matcher(string).replaceAll("");
        return StringUtils.split(cleanStr, ',');
    }

    public static String toTagString(String jsonTagString) {
        if (jsonTagString.equals("{}")) {
            return "";
        }
        return JSON_TAG_STRING_PATTERN.matcher(jsonTagString).replaceAll("");
    }

    public static String toTagString(List<Tag> tagList) {
        return tagList.stream()
                .map(Tag::toString)
                .collect(Collectors.joining(","));
    }
}
