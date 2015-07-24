(function() {
	'use strict';
	/**
	 * (en)agentInfoDirective 
	 * @ko agentInfoDirective
	 * @group Directive
	 * @name agentInfoDirective
	 * @class
	 */	
	pinpointApp.constant('agentInfoConfig', {
	    agentStatUrl: '/getAgentStat.pinpoint'
	});
	
	pinpointApp.directive('agentInfoDirective', [ 'agentInfoConfig', '$timeout', 'AlertsService', 'ProgressBarService', 'AgentDaoService', 'helpContentTemplate', 'helpContentService',
	    function (cfg, $timeout, AlertsService, ProgressBarService, AgentDaoService, helpContentTemplate, helpContentService) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/agentInfo/agentInfo.html',
	            link: function postLink(scope, element, attrs) {
	
	                // define private variables
	                var oNavbarVoService, oAlertService, oProgressBarService, bInitTooltip = false;
	
	                // define private variables of methods
	                var getAgentStat, getLink, initServiceInfo, showCharts, parseMemoryChartDataForAmcharts, parseCpuLoadChartDataForAmcharts,
	                broadcastToCpuLoadChart, resetServerMetaDataDiv, initTooltip;
	
	                // initialize
	                scope.agentInfoTemplate = 'features/agentInfo/agentInfoReady.html';
	                oAlertService = new AlertsService();
	                oProgressBarService = new ProgressBarService();
	                

	                initTooltip = function() {
	                	if ( bInitTooltip === false ) {
	                		jQuery('.heapTooltip').tooltipster({
			                	content: function() {
			                		return helpContentTemplate(helpContentService.inspector.heap);
			                	},
			                	position: "top",
			                	trigger: "click"
			                });
			                jQuery('.permGenTooltip').tooltipster({
			                	content: function() {
			                		return helpContentTemplate(helpContentService.inspector.permGen);
			                	},
			                	position: "top",
			                	trigger: "click"
			                });
			                jQuery('.cpuUsageTooltip').tooltipster({
			                	content: function() {
			                		return helpContentTemplate(helpContentService.inspector.cpuUsage);
			                	},
			                	position: "top",
			                	trigger: "click"
			                });
	                	}
	                };
	
	                /**
	                 * scope event of agentInfo.initialize
	                 */
	                scope.$on('agentInfoDirective.initialize', function (event, navbarVoService, agent) {
	                    scope.agentInfoTemplate = 'features/agentInfo/agentInfoMain.html';
	                    scope.agent = agent;
	                    oNavbarVoService = navbarVoService;
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
	                        'serverMetaData': agent.serverMetaData,
	                        'linkName' : agent.linkName,
	                        'linkURL' : agent.linkURL
	                    };
	                    scope.currentServiceInfo = initServiceInfo(agent);
	                    
	                    $timeout(function () {
	                        getAgentStat(agent.agentId, oNavbarVoService.getQueryStartTime(), oNavbarVoService.getQueryEndTime(), oNavbarVoService.getPeriod());
	                        scope.$apply();
	                    });
	                });
	                
	                scope.selectServiceInfo = function(serviceInfo) {
	                    if (serviceInfo.serviceLibs.length > 0) {
	                        scope.currentServiceInfo = serviceInfo;
	                    }
	                };
	                
	                initServiceInfo = function (agent) {
	                    if (agent.serverMetaData && agent.serverMetaData.serviceInfos) {
	                        var serviceInfos = agent.serverMetaData.serviceInfos;
	                        for (var i = 0; i < serviceInfos.length; ++i) {
	                            if (serviceInfos[i].serviceLibs.length > 0) {
	                                return serviceInfos[i];
	                            }
	                        } 
	                    }
	                    return;
	                }
	
	                /**
	                 * show charts
	                 * @param agentStat
	                 */
	                showCharts = function (agentStat) {
	
	                    var heap = { id: 'heap', title: 'Heap', span: 'span12', line: [
	                        { id: 'JVM_MEMORY_HEAP_USED', key: 'Used', values: [], isFgc: false },
	                        { id: 'JVM_MEMORY_HEAP_MAX', key: 'Max', values: [], isFgc: false },
	                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
	                    ]};
	
	                    var nonheap = { id: 'nonheap', title: 'PermGen', span: 'span12', line: [
	                        { id: 'JVM_MEMORY_NON_HEAP_USED', key: 'Used', values: [], isFgc: false },
	                        { id: 'JVM_MEMORY_NON_HEAP_MAX', key: 'Max', values: [], isFgc: false },
	                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
	                    ]};
	                    
	                    var cpuLoad = { id: 'cpuLoad', title: 'JVM/System Cpu Usage', span: 'span12', isAvailable: false};
	
	                    scope.memoryGroup = [ heap, nonheap ];
	                    scope.cpuLoadChart = cpuLoad;
	
	                    scope.$broadcast('jvmMemoryChartDirective.initAndRenderWithData.forHeap', AgentDaoService.parseMemoryChartDataForAmcharts(heap, agentStat), '100%', '270px');
	                    scope.$broadcast('jvmMemoryChartDirective.initAndRenderWithData.forNonHeap', AgentDaoService.parseMemoryChartDataForAmcharts(nonheap, agentStat), '100%', '270px');
	                    scope.$broadcast('cpuLoadChartDirective.initAndRenderWithData.forCpuLoad', AgentDaoService.parseCpuLoadChartDataForAmcharts(cpuLoad, agentStat), '100%', '270px');
	                };
	                
	                /**
	                 * get agent stat
	                 * @param agentId
	                 * @param from
	                 * @param to
	                 */
	                getAgentStat = function (agentId, from, to, period) {
	                    oProgressBarService.startLoading();
	                    var query = {
	                        agentId: agentId,
	                        from: from,
	                        to: to,
	                        sampleRate: AgentDaoService.getSampleRate(period)
	                    };
	                    oProgressBarService.setLoading(40);
	                    AgentDaoService.getAgentStat(query, function (err, result) {
	                        if (err || result.exception ) {
                                oProgressBarService.stopLoading();
                                if ( err ) {
                                	oAlertService.showError('There is some error.');
                                } else {
                                	oAlertService.showError(result.exception);
                                }
                                return;
                            }
	                        
	                        scope.agentStat = result;
	                        if (angular.isDefined(result.type) && result.type) {
	                            scope.info['jvmGcType'] =  result.type;
	                        }
	                        oProgressBarService.setLoading(80);
	                        showCharts(result);
	                        $timeout(function () {
	                            oProgressBarService.setLoading(100);
	                            oProgressBarService.stopLoading();
	                        }, 700);
	                        scope.$digest();
	                        
	                        initTooltip();
	                    });
	                };
	                
	                broadcastToCpuLoadChart = function(e, event) {
	                	if (scope.cpuLoadChart.isAvailable) {
	                        scope.$broadcast('cpuLoadChartDirective.showCursorAt.forCpuLoad', event.index);
	                	}
	                }
	
	                /**
	                 * scope event on jvmMemoryChartDirective.cursorChanged.forHeap
	                 */
	                scope.$on('jvmMemoryChartDirective.cursorChanged.forHeap', function (e, event) {
	                    scope.$broadcast('jvmMemoryChart.showCursorAt.forNonHeap', event.index);
	                    broadcastToCpuLoadChart(e, event);
	                });
	
	                /**
	                 * scope event on jvmMemoryChartDirective.cursorChanged.forNonHeap
	                 */
	                scope.$on('jvmMemoryChartDirective.cursorChanged.forNonHeap', function (e, event) {
	                    scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
	                    broadcastToCpuLoadChart(e, event);
	                });
	
	                /**
	                 * scope event on cpuLoadChart.cursorChanged.forCpuLoad
	                 */
	                scope.$on('cpuLoadChartDirective.cursorChanged.forCpuLoad', function (e, event) {
	                    scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
	                    scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forNonHeap', event.index);
	                });
	            }
	        };
	    }
	]);
})();