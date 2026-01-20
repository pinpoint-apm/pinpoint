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
import com.navercorp.pinpoint.common.util.KeyValueTokenizer;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * @author Hyunjoon Cho
 */
public class TagUtils {

    private static final Pattern MULTI_VALUE_FIELD_PATTERN = Pattern.compile("[\\[\\]\"]");
    private static final String JSON_TAG_STRING = "{}\"";

    public static final KeyValueTokenizer.TokenFactory<Tag> TAG_FACTORY = new KeyValueTokenizer.TokenFactory<>() {
        @Override
        public Tag accept(String key, String value) {
            return new Tag(key, value);
        }
    };

    private TagUtils() {
    }

    public static List<Tag> parseTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return List.of();
        }

        List<Tag> tagList = new ArrayList<>(tags.size());
        for (String tagString : tags) {
            Tag tag = parseTag(tagString);
            tagList.add(tag);
        }
        return tagList;
    }

    public static List<Tag> parseTags(String tagStrings) {
        if (tagStrings == null || tagStrings.contains("null")) {
            return List.of();
        }

        String[] tagStrArray = parseMultiValueFieldList(tagStrings);

        List<Tag> tagList = new ArrayList<>(tagStrArray.length);
        for (String tagString : tagStrArray) {
            Tag tag = parseTag(tagString);
            tagList.add(tag);
        }

        return tagList;
    }

    public static Tag parseTag(String tagString) {
        Objects.requireNonNull(tagString, "tagString");
        return KeyValueTokenizer.tokenize(tagString, ":", TAG_FACTORY);
    }

    private static String[] parseMultiValueFieldList(String string) {
        String cleanStr = MULTI_VALUE_FIELD_PATTERN.matcher(string).replaceAll("");
        return StringUtils.split(cleanStr, ',');
    }

    public static String toTagString(String jsonTagString) {
        if ("{}".equals(jsonTagString)) {
            return "";
        }

        return org.springframework.util.StringUtils.deleteAny(jsonTagString, JSON_TAG_STRING);
    }

    public static String toTagString(List<Tag> tagList) {
        StringJoiner joiner = new StringJoiner(",");
        for (Tag tag : tagList) {
            joiner.add(tag.toString());
        }
        return joiner.toString();
    }

    public static List<Tag> defaultTags(List<Tag> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags;
    }
}
