package com.nhn.pinpoint.web.service.map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 나중에 삭제하면 됨.
 * @author emeroad
 */
@Deprecated
public class AcceptApplicationLocalCacheV1 {

    private final Map<String, Set<AcceptApplication>> acceptApplicationLocalCacheV1 = new HashMap<String, Set<AcceptApplication>>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public Set<AcceptApplication> get(String host) {
        final Set<AcceptApplication> hit = acceptApplicationLocalCacheV1.get(host);
        if (CollectionUtils.isNotEmpty(hit)) {
            logger.debug("acceptApplicationLocalCacheV1 hit");
            return hit;
        }

        return Collections.emptySet();
    }

    public void put(String host, Set<AcceptApplication> acceptApplicationSet) {

        if (CollectionUtils.isEmpty(acceptApplicationSet)) {
            // 비어 있는 값에 대해서도 생성해야 함.
            Set<AcceptApplication> emptySet = Collections.emptySet();
            acceptApplicationLocalCacheV1.put(host, emptySet);
            return ;
        }
        // build cache
        for (AcceptApplication acceptApplication : acceptApplicationSet) {
            Set<AcceptApplication> findSet = acceptApplicationLocalCacheV1.get(acceptApplication.getHost());
            if (findSet == null) {
                findSet = new HashSet<AcceptApplication>();
                acceptApplicationLocalCacheV1.put(acceptApplication.getHost(), findSet);
            }
            findSet.add(acceptApplication);
        }
    }

}
