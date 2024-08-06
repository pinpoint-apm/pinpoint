package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.web.view.TagApplications;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;

public class TagApplicationsUtils {

    public static TagApplications wrapApplicationList(List<Application> applicationList) {
        String tag = newTag(applicationList);
        return new TagApplications(tag, applicationList);
    }

    public static String newTag(List<Application> applicationList) {
        // Precondition : If the application list of hbase is the same,
        // ETag value of multiple web servers is also the same.
        // need MD5 hash (128 bit)
        return String.valueOf(applicationList.hashCode());
    }


}
