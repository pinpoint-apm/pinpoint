const PROXY_CONFIG = [
    {
        context: [
            "/configuration.pinpoint",
            "/serverTime.pinpoint",
            "/userConfiguration.pinpoint",
            "/applications.pinpoint",
            "/getServerMapData.pinpoint",
            "/getServerMapDataV2.pinpoint",
            "/getResponseTimeHistogramDataV2.pinpoint",
            "/getLinkTimeHistogramData.pinpoint",
            "/getFilteredServerMapDataMadeOfDotGroup.pinpoint",
            "/getAgentList.pinpoint",
            "/getAgentStat/jvmGc/chart.pinpoint",
            "/getAgentStat/cpuLoad/chart.pinpoint",
            "/getAgentStat/transaction/chart.pinpoint",
            "/getAgentStat/activeTrace/chart.pinpoint",
            "/getAgentStat/responseTime/chart.pinpoint",
            "/getAgentStat/dataSource/chartList.pinpoint",
            "/getApplicationStat/memory/chart.pinpoint",
            "/getApplicationStat/cpuLoad/chart.pinpoint",
            "/getApplicationStat/transaction/chart.pinpoint",
            "/getApplicationStat/activeTrace/chart.pinpoint",
            "/getApplicationStat/responseTime/chart.pinpoint",
            "/getApplicationStat/dataSource/chart.pinpoint",
            "/getAgentStatusTimeline.pinpoint",
            "/getAgentEvents.pinpoint",
            "/getAgentInfo.pinpoint",
            "/transactionmetadata.pinpoint",
            "/transactionInfo.pinpoint",
            "/sqlBind.pinpoint",
            "/jsonBind.pinpoint",
            "/getScatterData.pinpoint",
            "/agent/activeThreadLightDump.pinpoint",
            "/agent/activeThreadDump.pinpoint",
            "/getAgentInstallationInfo.pinpoint",
            "/isAvailableApplicationName.pinpoint",
            "/isAvailableAgentId.pinpoint",
            "/userGroup.pinpoint",
            "/user.pinpoint",
            "/userGroup/member.pinpoint",
            "/application/alarmRule/checker.pinpoint",
            "/application/alarmRule.pinpoint",
            "/getAgentStat/fileDescriptor/chart.pinpoint",
            "/getApplicationStat/fileDescriptor/chart.pinpoint",
            "/getAgentStat/directBuffer/chart.pinpoint",
            "/getApplicationStat/directBuffer/chart.pinpoint",
            "/admin/removeAgentId.pinpoint",
            "/admin/removeInactiveAgents.pinpoint",
            "/bind.pinpoint"
        ],
        target: 'http://localhost:8080',
        secure: false
    },
    {
        context: [
            "/agent/activeThread.pinpointws"
        ],
        target: "http://localhost:8080",
        secure: false,
        ws: true
    }
]

module.exports = PROXY_CONFIG;