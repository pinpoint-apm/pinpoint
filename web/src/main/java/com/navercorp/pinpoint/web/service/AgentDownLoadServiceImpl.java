package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.web.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class AgentDownLoadServiceImpl implements AgentDownLoadService {
    private static final Comparator<AgentDownloadInfo> REVERSE = Comparator.comparing(AgentDownloadInfo::getVersion).reversed();

    private final AgentDownloadInfoDao agentDownloadInfoDao;
    private volatile AgentDownloadInfo cachedAgentDownloadInfo;

    public AgentDownLoadServiceImpl(AgentDownloadInfoDao agentDownloadInfoDao) {
        this.agentDownloadInfoDao = Objects.requireNonNull(agentDownloadInfoDao, "agentDownloadInfoDao");
    }

    @Override
    public AgentDownloadInfo getLatestStableAgentDownloadInfo() {
        if (cachedAgentDownloadInfo != null) {
            return cachedAgentDownloadInfo;
        }

        List<AgentDownloadInfo> downloadInfoList = agentDownloadInfoDao.getDownloadInfoList();
        if (CollectionUtils.isEmpty(downloadInfoList)) {
            return null;
        }

        downloadInfoList.sort(REVERSE);

        // 1st. find same
        for (AgentDownloadInfo downloadInfo : downloadInfoList) {
            if (Version.VERSION.equals(downloadInfo.getVersion())) {
                cachedAgentDownloadInfo = downloadInfo;
                return downloadInfo;
            }
        }

        // 2nd. find lower
        for (AgentDownloadInfo downloadInfo : downloadInfoList) {
            if (Version.VERSION.compareTo(downloadInfo.getVersion()) > 0) {
                cachedAgentDownloadInfo = downloadInfo;
                return downloadInfo;
            }
        }

        // 3rd find greater
        AgentDownloadInfo downloadInfo = CollectionUtils.lastElement(downloadInfoList);
        cachedAgentDownloadInfo = downloadInfo;
        return downloadInfo;
    }
}
