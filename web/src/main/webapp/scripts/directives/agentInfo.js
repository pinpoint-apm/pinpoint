'use strict';

pinpointApp.constant('agentInfoConfig', {
    agentStatUrl: '/getAgentStat.pinpoint'
});

pinpointApp.directive('agentInfo', [ 'agentInfoConfig', '$timeout', 'Alerts', 'ProgressBar', 'AgentDao',
    function (cfg, $timeout, Alerts, ProgressBar, AgentDao) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/agentInfo.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var oNavbarVo, oAlert, oProgressBar;

                // define private variables of methods
                var getAgentStat, showCharts, parseMemoryChartDataForAmcharts, parseCpuLoadChartDataForAmcharts,
                broadcastToCpuLoadChart, resetServerMetaDataDiv, openServerMetaDataDiv;

                // initialize
                scope.agentInfoTemplate = 'views/agentInfoReady.html';
                oAlert = new Alerts();
                oProgressBar = new ProgressBar();

                /**
                 * scope event of agentInfo.initialize
                 */
                scope.$on('agentInfo.initialize', function (event, navbarVo, agent) {
                    scope.agentInfoTemplate = 'views/agentInfoMain.html';
                    scope.agent = agent;
                    oNavbarVo = navbarVo;
                    scope.chartGroup = null;
                    scope.info = {
                        'agentId': agent.agentId,
                        'applicationName': agent.applicationName,
                        'hostName': agent.hostName,
                        'ip': agent.ip,
                        'serviceType': agent.serviceType,
                        'pid': agent.pid,
                        'agentVersion': agent.version,
                        'jvmGcType': '',
                        'serverMetaData': agent.serverMetaData
                    };

                    $timeout(function () {
                        getAgentStat(agent.agentId, oNavbarVo.getQueryStartTime(), oNavbarVo.getQueryEndTime(), oNavbarVo.getPeriod());
                        scope.$apply();
                    });
                });
                
                resetServerMetaDataDiv = function() {
                	$('#serverMetaDataDiv').modal('hide');
                }
                
                openServerMetaDataDiv = function() {
                	resetServerMetaDataDiv();
                	$('#serverMetaDataDiv').modal('show');
                }
                
                /**
                 * open server meta data div
                 */
                scope.openServerMetaDataDiv = function() {
                	openServerMetaDataDiv();
                }

                /**
                 * show charts
                 * @param agentStat
                 */
                showCharts = function (agentStat) {
                    var total = { id: 'total', title: 'Total (Heap + PermGen)', span: 'span12', line: [
                        { id: 'jvmMemoryTotalUsed', key: 'Used', values: [], isFgc: false },
                        { id: 'jvmMemoryTotalMax', key: 'Max', values: [], isFgc: false },
                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
                    ]};

                    var heap = { id: 'heap', title: 'Heap', span: 'span12', line: [
                        { id: 'jvmMemoryHeapUsed', key: 'Used', values: [], isFgc: false },
                        { id: 'jvmMemoryHeapMax', key: 'Max', values: [], isFgc: false },
                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
                    ]};

                    var nonheap = { id: 'nonheap', title: 'PermGen', span: 'span12', line: [
                        { id: 'jvmMemoryNonHeapUsed', key: 'Used', values: [], isFgc: false },
                        { id: 'jvmMemoryNonHeapMax', key: 'Max', values: [], isFgc: false },
                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
                    ]};
                    
                    var cpuLoad = { id: 'cpuLoad', title: 'CpuLoad', span: 'span12', isAvailable: false};

                    scope.memoryGroup = [ heap, nonheap ];
                    scope.cpuLoadChart = cpuLoad;

                    scope.$broadcast('jvmMemoryChart.initAndRenderWithData.forHeap', AgentDao.parseMemoryChartDataForAmcharts(heap, agentStat), '100%', '270px');
                    scope.$broadcast('jvmMemoryChart.initAndRenderWithData.forNonHeap', AgentDao.parseMemoryChartDataForAmcharts(nonheap, agentStat), '100%', '270px');
                    scope.$broadcast('cpuLoadChart.initAndRenderWithData.forCpuLoad', AgentDao.parseCpuLoadChartDataForAmcharts(cpuLoad, agentStat), '100%', '270px');
                };
                
                /**
                 * get agent stat
                 * @param agentId
                 * @param from
                 * @param to
                 */
                getAgentStat = function (agentId, from, to, period) {
                    oProgressBar.startLoading();
                    var query = {
                        agentId: agentId,
                        from: from,
                        to: to,
                        sampleRate: AgentDao.getSampleRate(period)
                    };
                    oProgressBar.setLoading(40);
                    AgentDao.getAgentStat(query, function (err, result) {
                        if (err) {
                            oProgressBar.stopLoading();
                            oAlert.showError('There is some error.');
                            return;
                        }
                        scope.agentStat = result;
                        if (angular.isDefined(result.type) && result.type) {
                            scope.info['jvmGcType'] =  result.type;
                            oProgressBar.setLoading(80);
                            showCharts(result);
                            $timeout(function () {
                                oProgressBar.setLoading(100);
                                oProgressBar.stopLoading();
                            }, 700);
                        } else {
                            oProgressBar.stopLoading();
                        }

                        scope.$digest();
                    });
                };
                
                broadcastToCpuLoadChart = function(e, event) {
                	if (scope.cpuLoadChart.isAvailable) {
                        scope.$broadcast('cpuLoadChart.showCursorAt.forCpuLoad', event.index);
                	}
                }

                /**
                 * scope event on jvmMemoryChart.cursorChanged.forHeap
                 */
                scope.$on('jvmMemoryChart.cursorChanged.forHeap', function (e, event) {
                    scope.$broadcast('jvmMemoryChart.showCursorAt.forNonHeap', event.index);
                    broadcastToCpuLoadChart(e, event);
                });

                /**
                 * scope event on jvmMemoryChart.cursorChanged.forNonHeap
                 */
                scope.$on('jvmMemoryChart.cursorChanged.forNonHeap', function (e, event) {
                    scope.$broadcast('jvmMemoryChart.showCursorAt.forHeap', event.index);
                    broadcastToCpuLoadChart(e, event);
                });

                /**
                 * scope event on cpuLoadChart.cursorChanged.forCpuLoad
                 */
                scope.$on('cpuLoadChart.cursorChanged.forCpuLoad', function (e, event) {
                    scope.$broadcast('jvmMemoryChart.showCursorAt.forHeap', event.index);
                    scope.$broadcast('jvmMemoryChart.showCursorAt.forNonHeap', event.index);
                });
                
                scope.$on('agentInfo.openServerMetaDataDiv', function (event) {
                	openServerMetaDataDiv();
                });
                
            }
        };
    }]);
