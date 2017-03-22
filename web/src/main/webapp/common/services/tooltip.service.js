(function($) {
	'use strict';

	/**
	 * (en) initialize tooltip.
	 * @ko Tooltip 초기화.
	 * @group Service
	 * @name TooltipService
	 * @class
	 */
	pinpointApp.constant('TooltipServiceConfig', {
		"scatter": {
			"position": "bottom",
			"trigger": "click"
		},
		"navbar": {
			"position": "bottom",
			"trigger": "click"
		},
		"agentList": {
			"position": "bottom",
			"trigger": "click"
		},
		"heap": {
			"position": "top",
			"trigger": "click"
		},
		"permGen": {
			"position": "top",
			"trigger": "click"
		},
		"cpuUsage": {
			"position": "top",
			"trigger": "click"
		},
		"tps": {
			"position": "top",
			"trigger": "click"
		},
		"activeThread": {
			"position": "top",
			"trigger": "click"
		},
		"dataSource": {
			"position": "top",
			"trigger": "click"
		},
		"responseSummaryChart": {
			"position": "top",
			"trigger": "click"
		},
		"loadChart": {
			"position": "top",
			"trigger": "click"
		},
		"serverList": {
			"position": "bottom",
			"trigger": "click"
		},
		"callTree": {
			"position": "bottom",
			"trigger": "click"
		},
		"realtime": {
			"position": "top",
			"trigger": "click"
		},
		"alarmRules": {
			"position": "top",
			"trigger": "click"
		}
	});

	pinpointApp.service('TooltipService', [ 'TooltipServiceConfig', 'helpContentTemplate', 'helpContentService', function ( $config, helpContentTemplate, helpContentService ) {

		this.init = function( type ) {
			$("." + type + "Tooltip").tooltipster({
				content: getTooltipStr( type ),
				position: $config[type].position,
				trigger: $config[type].trigger
			});
		};

		function getTooltipStr( type ) {
			switch( type ) {
				case "scatter":
					return function() { return helpContentTemplate(helpContentService.scatter["default"]); };
				case "navbar":
					return function() { return helpContentTemplate(helpContentService.navbar.applicationSelector) + helpContentTemplate(helpContentService.navbar.depth) + helpContentTemplate(helpContentService.navbar.periodSelector); };
				case "agentList":
					return function() { return helpContentTemplate(helpContentService.inspector.list); };
				case "heap":
					return function() { return helpContentTemplate(helpContentService.inspector.heap); };
				case "permGen":
					return function() { return helpContentTemplate(helpContentService.inspector.permGen); };
				case "cpuUsage":
					return function() { return helpContentTemplate(helpContentService.inspector.cpuUsage); };
				case "tps":
					return function() { return helpContentTemplate(helpContentService.inspector.tps); };
				case "activeThread":
					return function() { return helpContentTemplate(helpContentService.inspector.activeThread); };
				case "dataSource":
					return function() { return helpContentTemplate(helpContentService.inspector.dataSource); };
				case "responseSummaryChart":
					return function() { return helpContentTemplate(helpContentService.nodeInfoDetails.responseSummary); };
				case "loadChart":
					return function() { return helpContentTemplate(helpContentService.nodeInfoDetails.load); };
				case "serverList":
					return function() { return helpContentTemplate(helpContentService.nodeInfoDetails.nodeServers); };
				case "callTree":
					return function() { return helpContentTemplate(helpContentService.callTree.column); };
				case "realtime":
					return function() { return helpContentTemplate(helpContentService.realtime["default"]); };
				case "alarmRules":
					return function() { return helpContentTemplate(helpContentService.configuration.alarmRules); };
			}
		}
	}]);
})(jQuery);