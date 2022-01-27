/*
 * www.msxf.com Inc.
 * Copyright (c) 2017 All Rights Reserved
 */

/*
 * 修订记录:
 * shuijing 2021-12-10 15:52 创建
 */
package com.navercorp.pinpoint.plugin.elasticsearch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shuijing
 */
public class ElasticPluginUtil {
    public static final Pattern INDEX_NAME_PATTERN = Pattern.compile("^/([^/]+)/.*");
    public static final Pattern INDEX_NAME_PATTERN2 = Pattern.compile("([^/]+)/.*");

    public static CharSequence toString(Object part) {
        return (part instanceof CharSequence) ? (CharSequence) part : part.toString();
    }


    public static String getIndex(String url) {
        final Matcher matcher = INDEX_NAME_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            final Matcher matcher2 = INDEX_NAME_PATTERN2.matcher(url);
            if (matcher2.find()) {
                return matcher2.group(1);
            }
        }
        return url;
    }


}
