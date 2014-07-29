package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.*;
import com.nhn.pinpoint.web.dao.HostApplicationMapDao;
import com.nhn.pinpoint.web.dao.MapStatisticsCalleeDao;
import com.nhn.pinpoint.web.dao.MapStatisticsCallerDao;
import com.nhn.pinpoint.web.service.map.AcceptApplicationLocalCache;
import com.nhn.pinpoint.web.service.map.AcceptApplication;
import com.nhn.pinpoint.web.service.map.AcceptApplicationLocalCacheV1;
import com.nhn.pinpoint.web.service.map.RpcApplication;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkKey;
import com.nhn.pinpoint.web.vo.Range;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.HbaseSystemException;

import java.util.*;

/**
 * @author emeroad
 */
public class LinkDataSelector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkVisitChecker linkVisitChecker = new LinkVisitChecker();

    private final MapStatisticsCalleeDao mapStatisticsCalleeDao;

    private final MapStatisticsCallerDao mapStatisticsCallerDao;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final AcceptApplicationLocalCache acceptApplicationLocalCache = new AcceptApplicationLocalCache();

    @Deprecated
    private final AcceptApplicationLocalCacheV1 acceptApplicationLocalCacheV1 = new AcceptApplicationLocalCacheV1();

    private final Set<LinkData> emulationLinkMarker = new HashSet<LinkData>();

    public LinkDataSelector(MapStatisticsCalleeDao mapStatisticsCalleeDao, MapStatisticsCallerDao mapStatisticsCallerDao, HostApplicationMapDao hostApplicationMapDao) {
        if (mapStatisticsCalleeDao == null) {
            throw new NullPointerException("mapStatisticsCalleeDao must not be null");
        }
        if (mapStatisticsCallerDao == null) {
            throw new NullPointerException("mapStatisticsCallerDao must not be null");
        }
        if (hostApplicationMapDao == null) {
            throw new NullPointerException("hostApplicationMapDao must not be null");
        }
        this.mapStatisticsCalleeDao = mapStatisticsCalleeDao;
        this.mapStatisticsCallerDao = mapStatisticsCallerDao;
        this.hostApplicationMapDao = hostApplicationMapDao;
    }

    /**
     * callerApplicationName이 호출한 callee를 모두 조회
     *
     * @param callerApplication
     * @param range
     * @return
     */
    private LinkDataDuplexMap selectCaller(Application callerApplication, Range range) {
        // 이미 조회된 구간이면 skip
        if (linkVisitChecker.visitCaller(callerApplication)) {
            return new LinkDataDuplexMap();
        }

        LinkDataMap caller = mapStatisticsCallerDao.selectCaller(callerApplication, range);
        if (logger.isDebugEnabled()) {
            logger.debug("Found Caller. count={}, caller={}", caller.size(), callerApplication);
        }

        final LinkDataMap replaceRpcCaller = new LinkDataMap();
        for (LinkData link : caller.getLinkDataList()) {
            final List<LinkData> checkedLink = checkRpcCallAccepted(link, range);
            for (LinkData linkData : checkedLink) {
                replaceRpcCaller.addLinkData(linkData);
            }
        }


        final LinkDataDuplexMap resultCaller = new LinkDataDuplexMap();
        for (LinkData link : replaceRpcCaller.getLinkDataList()) {
            resultCaller.addSourceLinkData(link);

            final Application toApplication = link.getToApplication();
            // terminal, unknowncloud 인 경우에는 skip
            if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
                continue;
            }

            logger.debug("     Find subCaller of {}", toApplication);
            LinkDataDuplexMap callerSub = selectCaller(toApplication, range);
            logger.debug("     Found subCaller. count={}, caller={}", callerSub.size(), toApplication);

            resultCaller.addLinkDataDuplexMap(callerSub);

            // 찾아진 녀석들에 대한 caller도 찾는다.
            for (LinkData eachCaller : callerSub.getSourceLinkDataList()) {
                logger.debug("     Find callee of {}", eachCaller.getFromApplication());
                LinkDataDuplexMap calleeSub = selectCallee(eachCaller.getFromApplication(), range);
                logger.debug("     Found subCallee. count={}, callee={}", calleeSub.size(), eachCaller.getFromApplication());
                resultCaller.addLinkDataDuplexMap(calleeSub);
            }
        }
        return resultCaller;
    }

    /**
     * callee applicationname을 호출한 caller 조회.
     *
     * @param calleeApplication
     * @param range
     * @return
     */
    private LinkDataDuplexMap selectCallee(Application calleeApplication, Range range) {
        // 이미 조회된 구간이면 skip
        if (linkVisitChecker.visitCallee(calleeApplication)) {
            return new LinkDataDuplexMap();
        }

        final LinkDataMap callee = mapStatisticsCalleeDao.selectCallee(calleeApplication, range);
        logger.debug("Found Callee. count={}, callee={}", callee.size(), calleeApplication);

        final LinkDataDuplexMap calleeSet = new LinkDataDuplexMap();
        for (LinkData stat : callee.getLinkDataList()) {
            calleeSet.addTargetLinkData(stat);

            // 나를 부른 application을 찾아야 하기 떄문에 to를 입력.
            LinkDataDuplexMap calleeSub = selectCallee(stat.getFromApplication(), range);
            calleeSet.addLinkDataDuplexMap(calleeSub);

            // 찾아진 녀석들에 대한 callee도 찾는다.
            for (LinkData eachCallee : calleeSub.getTargetLinkDataList()) {
                // terminal이면 skip
                final Application eachCalleeToApplication = eachCallee.getToApplication();
                if (eachCalleeToApplication.getServiceType().isTerminal() || eachCalleeToApplication.getServiceType().isUnknown()) {
                    continue;
                }
                LinkDataDuplexMap callerSub = selectCaller(eachCalleeToApplication, range);
                calleeSet.addLinkDataDuplexMap(callerSub);
            }
        }

        return calleeSet;
    }


    private List<LinkData> checkRpcCallAccepted(LinkData linkData, Range range) {
        // rpc client의 목적지가 agent가 설치되어 application name이 존재한다면 replace.
        final Application toApplication = linkData.getToApplication();
        if (!toApplication.getServiceType().isRpcClient()) {
            return Arrays.asList(linkData);
        }

        logger.debug("checkRpcCallAccepted(). Find applicationName:{} {}", toApplication, range);

        final Set<AcceptApplication> acceptApplicationList = findAcceptApplication(linkData.getFromApplication(), toApplication.getName(), range);
        logger.debug("find accept application:{}", acceptApplicationList);
        if (CollectionUtils.isNotEmpty(acceptApplicationList)) {
            if (acceptApplicationList.size() == 1) {
                logger.debug("Application info replaced. {} => {}", linkData, acceptApplicationList);

                AcceptApplication first = acceptApplicationList.iterator().next();
                final LinkData acceptedLinkData = new LinkData(linkData.getFromApplication(), first.getApplication(), linkData.getLinkCallDataMap());
                return Arrays.asList(acceptedLinkData);
            } else {
                // specialcase 한개의 url에 2개의 노드가 묶여 있다.
                return createVirtualLinkData(linkData, toApplication, acceptApplicationList);
            }
        } else {
            final Application unknown = new Application(toApplication.getName(), ServiceType.UNKNOWN);
            final LinkData unknownLinkData = new LinkData(linkData.getFromApplication(), unknown, linkData.getLinkCallDataMap());
            return Arrays.asList(unknownLinkData);
        }

    }

    private List<LinkData> createVirtualLinkData(LinkData linkData, Application toApplication, Set<AcceptApplication> acceptApplicationList) {
        logger.warn("ono to N replaced. node:{}->host:{} accept:{}", linkData.getFromApplication(), toApplication.getName(), acceptApplicationList);

        List<LinkData> emulationLink = new ArrayList<LinkData>();
        for (AcceptApplication acceptApplication : acceptApplicationList) {
            // linkCallData를 바꿔야 한다.
            // 일부러 callhistogram을 빼버린다.
            final LinkData acceptedLinkData = new LinkData(linkData.getFromApplication(), acceptApplication.getApplication(), linkData.getLinkCallDataMap());
            emulationLink.add(acceptedLinkData);
            traceEmulationLink(acceptedLinkData);
        }
        return emulationLink;
    }

    private void traceEmulationLink(LinkData acceptApplication) {
        final boolean add = emulationLinkMarker.add(acceptApplication);
        if (!add) {
            logger.warn("emulationLink add error. {}", acceptApplication );
        }
    }


    private Set<AcceptApplication> findAcceptApplication(Application fromApplication, String host, Range range) {
        logger.debug("findAcceptApplication {} {}", fromApplication, host);
        Set<AcceptApplication> acceptApplicationVer2;
        try {
            // 호환성을 위해 일단 2번 뒤진다.
            // 신데이터를 먼저 뒤지고 이후 구데이터를 뒤진다. 6개월 뒤에는 어차피 데이터가 없어지므로 호환성 코드를 지울것. 2014.07월 개발
            acceptApplicationVer2 = findAcceptApplicationVer2(fromApplication, host, range);
            logger.debug("findAcceptApplication2 {}->{} result:{}", fromApplication, host, acceptApplicationVer2);
        } catch (HbaseSystemException ex) {
            acceptApplicationVer2 = Collections.emptySet();
        }

        if (CollectionUtils.isEmpty(acceptApplicationVer2)) {
            Set<AcceptApplication> acceptApplicationVer1 = findAcceptApplicationVer1(host, range);
            if (CollectionUtils.isNotEmpty(acceptApplicationVer1)) {
                logger.debug("acceptApplicationVer1 result:{}", acceptApplicationVer1);
            }
            return acceptApplicationVer1;
        }
        return acceptApplicationVer2;
    }

    private Set<AcceptApplication> findAcceptApplicationVer1(String host, Range range) {

        final Set<AcceptApplication> hit = acceptApplicationLocalCacheV1.get(host);
        if (CollectionUtils.isNotEmpty(hit)) {
            logger.debug("acceptApplicationLocalCacheV1 hit");
            return hit;
        }

        final Set<AcceptApplication> acceptApplicationSet= hostApplicationMapDao.findAcceptApplicationName(host, range);
        this.acceptApplicationLocalCacheV1.put(host, acceptApplicationSet);
        return acceptApplicationLocalCacheV1.get(host);
    }

    private Set<AcceptApplication> findAcceptApplicationVer2(Application fromApplication, String host, Range range) {

        final RpcApplication rpcApplication = new RpcApplication(host, fromApplication);
        final Set<AcceptApplication> hit = this.acceptApplicationLocalCache.get(rpcApplication);
        if (CollectionUtils.isNotEmpty(hit)) {
            logger.debug("acceptApplicationLocalCacheV2 hit");
            return hit;
        }
        final Set<AcceptApplication> acceptApplicationSet= hostApplicationMapDao.findAcceptApplicationName(fromApplication, range);
        this.acceptApplicationLocalCache.put(rpcApplication, acceptApplicationSet);
        return this.acceptApplicationLocalCache.get(rpcApplication);
    }

    private void fillEmulationLink(LinkDataDuplexMap linkDataDuplexMap) {
        // TODO 이쪽 부분은 추후에 ui가 들어오면 다시 구현이 필요하다.
        // http://yobi.navercorp.com/Pinpoint/pinpoint-web/issue/193
        logger.debug("this.emulationLinkMarker{}", this.emulationLinkMarker);
        List<LinkData> emulationLinkDataList = findEmulationLinkData(linkDataDuplexMap);

        for (LinkData emulationLinkData : emulationLinkDataList) {
            LinkCallDataMap beforeImage = emulationLinkData.getLinkCallDataMap();
            logger.debug("beforeImage:{}", beforeImage);
            emulationLinkData.resetLinkData();

            LinkKey findLinkKey = new LinkKey(emulationLinkData.getFromApplication(), emulationLinkData.getToApplication());
            LinkData targetLinkData = linkDataDuplexMap.getTargetLinkData(findLinkKey);

            // 역치환 데이터 생성. target이 accept한 데이터를 반대로 호출 데이터로 바꾼다.
            LinkCallDataMap targetList = targetLinkData.getLinkCallDataMap();
            Collection<LinkCallData> beforeLinkDataList = beforeImage.getLinkDataList();

            LinkCallData beforeLinkCallData = beforeLinkDataList.iterator().next();
            for (LinkCallData agentHistogram : targetList.getLinkDataList()) {
                Application target = new Application(agentHistogram.getTarget(), agentHistogram.getTargetServiceType());
//                LinkCallData beforeLinkCallData = findBeforeAgent(beforeLinkDataList, target);


                Collection<TimeHistogram> timeHistogramList = agentHistogram.getTimeHistogram();
                LinkCallDataMap linkCallDataMap = emulationLinkData.getLinkCallDataMap();

                logger.debug("emulationLink {}", beforeLinkCallData);

//                linkCallDataMap.addCallData(beforeLinkCallData.getSource(), beforeLinkCallData.getSourceServiceType().getCode(),
//                        beforeLinkCallData.getTarget(), beforeLinkCallData.getTargetServiceType().getCode(), timeHistogramList);
                linkCallDataMap.addCallData(beforeLinkCallData.getSource(), beforeLinkCallData.getSourceServiceType().getCode(),
                        beforeLinkCallData.getTarget(), beforeLinkCallData.getTargetServiceType().getCode(), timeHistogramList);
            }

        }


    }

    public LinkCallData findBeforeAgent(Collection<LinkCallData> linkCallDataCollection, Application target) {
        for (LinkCallData linkCallData : linkCallDataCollection) {
            Application source = new Application(linkCallData.getSource(), linkCallData.getSourceServiceType());
            if (source.equals(target)) {
                logger.debug("findBeforeAgent:{}", linkCallData);
                return linkCallData;
            }
        }
        return null;
    }

    private List<LinkData> findEmulationLinkData(LinkDataDuplexMap linkDataDuplexMap) {
        // emulationLinkMarker의 데이터를 직접 수정해도 LinkDataDuplexMap에서는 이미 데이터를 copy하여 사용하기 때문에 수정해도 효과가 없음.
        // LinkDataDuplexMap에서 데이터를 다시 찾아야 한다.
        List<LinkData> searchList = new ArrayList<LinkData>();
        for (LinkData emulationLinkData : this.emulationLinkMarker) {
            LinkKey search = getLinkKey(emulationLinkData);
            for (LinkData linkData : linkDataDuplexMap.getSourceLinkDataList()) {
                LinkKey linkKey = getLinkKey(linkData);
                if (linkKey.equals(search)) {
                    searchList.add(linkData);
                }
            }
        }
        return searchList;
    }

    private LinkKey getLinkKey(LinkData emulationLinkData) {
        Application fromApplication = emulationLinkData.getFromApplication();
        Application toApplication = emulationLinkData.getToApplication();
        return new LinkKey(fromApplication, toApplication);
    }

    public LinkDataDuplexMap select(Application sourceApplication, Range range) {
        LinkDataDuplexMap caller = selectCaller(sourceApplication, range);
        logger.debug("Result of finding caller {}", caller);

        LinkDataDuplexMap callee = selectCallee(sourceApplication, range);
        logger.debug("Result of finding callee {}", callee);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
        linkDataDuplexMap.addLinkDataDuplexMap(caller);
        linkDataDuplexMap.addLinkDataDuplexMap(callee);
        fillEmulationLink(linkDataDuplexMap);

        return linkDataDuplexMap;
    }
}
