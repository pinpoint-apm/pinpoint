(function( $ ) {
	'use strict';
	pinpointApp.constant("ThreadDumpInfoLayerDirectiveConfig", {
		ACTIVE_THREAD_LIGHT_DUMP_URL: "agent/activeThreadLightDump.pinpoint",
		ACTIVE_THREAD_DUMP_URL: "agent/activeThreadDump.pinpoint",
		PADDING_WIDTH: 5, 	// %
		PADDING_HEIGHT: 15  	// %
	});
	pinpointApp.directive( "threadDumpInfoLayerDirective", [ "ThreadDumpInfoLayerDirectiveConfig", "$routeParams", "$timeout", "$http", "$window", "CommonUtilService", "CommonAjaxService",
		function ( cfg, $routeParams, $timeout, $http, $window, CommonUtilService, CommonAjaxService ) {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/threadDumpInfoLayer/threadDumpInfoLayer.html?v=" + G_BUILD_TIME,
				scope: {
					namespace: "@"
				},
				link: function(scope, element) {
					var $el = $(element);
					var $elSpin = $el.find("i");
					var $elListWrapper = $el.find(".thread-list");
					var $elEmpty = $el.find(".panel-body");
					var $elDetailMessage = $el.find(".detail-message");
					var $elTextarea = $el.find("textarea");
					var oRefListAjax = {
						"obj": null,
						"ing": false
					};
					var oRefDetailAjax = {
						"obj": null,
						"ing": false
					};
					var currentAgentId = "";
					var currentApplicationName = "";

					// $el.draggable({
					// 	handle: ".panel-heading"
					// });
					scope.threadList = [];
					scope.sortType = "-execTime";

					initLayerSizeNPosition();
					function initAjax( oRef, bForceAbort ) {
						if ( bForceAbort === true ) {
							if ( oRef.ing === true && oRef.obj && oRef.obj.abort ) {
								oRef.obj.abort();
							}
						}
						oRef.obj = null;
						oRef.ing = false;
					}
					scope.$on( "thread-dump-info-window.open", function( event, appName, agentId) {
						setForWindow();
						currentApplicationName = appName;
						currentAgentId = agentId;
						openLayer();
					});
					scope.$on( "thread-dump-info-layer.open", function( event, appName, agentId) {
						initAjax( oRefListAjax, true );
						if ( $el.is(":visible") ) {
							$elSpin.show();
						}
						currentApplicationName = appName;
						currentAgentId = agentId;
						openLayer();
					});

					scope.$on( "thread-dump-info-layer.close", function() {
						scope.hideThreadDump();
					});

					scope.changeSortType = function( type ) {
						scope.sortType = ( scope.sortType.indexOf( type ) <= 0 ? "-" : "" ) + type;
					};
					scope.hideThreadDump = function() {
						initAjax( oRefListAjax, true );
						initAjax( oRefDetailAjax, true );
						$el.hide();
						$elSpin.hide();
						$elTextarea.val("");
					};

					scope.loadDetailMessage = function( $event ) {
						var $elThread = $($( $event.target ).parents("tr")[0]);
						$elThread.parent().find(".selected").removeClass("selected").end().end().addClass("selected");
						$elSpin.show();
						initAjax( oRefDetailAjax, true );
						oRefDetailAjax.obj = $http( {
							"url": cfg.ACTIVE_THREAD_DUMP_URL +
								"?applicationName=" + currentApplicationName +
								"&agentId=" + currentAgentId +
								"&threadName=" + $elThread.find("td:nth-child(3)").html() +
								"&localTraceId=" + $elThread.attr("data-traceId"),
							"method": "GET"
						}).then(function ( oResult ) {
							var msg = "";
							if ( oResult.data.message.threadDumpData.length > 0 ) {
								msg = oResult.data.message.threadDumpData[0].detailMessage;
							} else {
								msg = "There is no message( may be completed )";
								// CommonAjaxService.getServerTime( function( serverTime ) {
								// 	var aUrlParam = [
								// 		"transactionList",
								// 		$routeParams.application,
								// 		"5m",
								// 		CommonUtilService.formatDate( serverTime ),
								// 		$elThread.attr("data-transactionId") + "-0-0"
								// 	];
								//
								// 	$window.parent.open( "#/" + aUrlParam.join("/") );
								// });
							}
							$elTextarea.val( msg );
							initAjax( oRefDetailAjax );
							$elSpin.hide();
						}, function () {
							$elSpin.hide();
						});
						oRefDetailAjax.ing = true;
					};
					scope.formatDate = function( startTime ) {
						return CommonUtilService.formatDate(startTime, "MM/DD HH:mm:ss SSS");
					};
					function openLayer() {
						oRefListAjax.obj = $http( {
							"url": cfg.ACTIVE_THREAD_LIGHT_DUMP_URL +
							"?applicationName=" + currentApplicationName +
							"&agentId=" + currentAgentId,
							"method": "GET"
						}).then(function ( oResult ) {
							if( oResult.data.code && oResult.data.code === -1 ) {
								$elEmpty.find("> span").hide().end().find(".when-has-message").show().find("span").html( oResult.data.message );
							} else {
								scope.threadList = oResult.data.message.threadDumpData;
								if ( scope.threadList.length === 0 ) {
									$elEmpty.find(".when-has-message").hide().end().find("> span").show();
								}
							}
							$elTextarea.val("");
							$elSpin.hide();
							$el.show();
							initAjax(oRefListAjax);
						}, function () {
							$elSpin.hide();
						});
						oRefListAjax.ing = true;
					}
					function initLayerSizeNPosition() {
						var oBaseSize = getBaseSizeInfo();
						var paddingWidthPixel = parseInt(( cfg.PADDING_WIDTH * oBaseSize.docWidth ) / 100);
						var paddingHeightPixel = parseInt(( cfg.PADDING_HEIGHT * oBaseSize.docHeight ) / 100);

						var layerWidth = oBaseSize.docWidth - ( paddingWidthPixel * 2 );
						var layerHeight = oBaseSize.docHeight - ( paddingHeightPixel * 2 );
						var layerHeightHalf = parseInt( (layerHeight - oBaseSize.titleHeight) / 2 );

						$el.css({
							"top": paddingHeightPixel,
							"left": paddingWidthPixel,
							"width": layerWidth,
							"height": layerHeight
						});
						$elListWrapper.css({
							"height": layerHeightHalf - oBaseSize.tableHeadHeight - oBaseSize.margin
						});
						$elEmpty.css({
							"height": layerHeightHalf
						});
						$elDetailMessage.css({
							"height": layerHeightHalf
						});
					}
					function setForWindow() {
						// $el.draggable("destroy");
						$el.find(".panel-heading").css("cursor", "default").find("button").hide();
						resetSize();
					}
					function resetSize () {
						var oBaseSize = getBaseSizeInfo();
						var layerHeightHalf = parseInt( (oBaseSize.docHeight - oBaseSize.titleHeight - oBaseSize.navHeight) / 2 );

						$el.css({
							"top": oBaseSize.navHeight + "px",
							"left": "0px",
							"width": "100%",
							"height": "100%"
						});
						$elListWrapper.css({
							"height": layerHeightHalf - oBaseSize.tableHeadHeight - oBaseSize.margin
						});
						$elEmpty.css({
							"height": layerHeightHalf
						});
						$elDetailMessage.css({
							"height": layerHeightHalf
						});
						$(window).on("resize", function() {
							var oNewBase = getBaseSizeInfo();
							var height = oNewBase.docHeight - oNewBase.titleHeight - oNewBase.navHeight - layerHeightHalf - oNewBase.tableHeadHeight - oNewBase.margin;
							if ( scope.threadList.length === 0 ) {
								$elEmpty.css({
									"height": height + 96
								});
							} else {
								$elListWrapper.css({
									"height": height
								});
							}
						});
					}
					function getBaseSizeInfo() {
						return {
							docWidth: $window.document.body.clientWidth,
							docHeight: $window.document.body.clientHeight,
							navHeight: 40,
							titleHeight: 78,
							tableHeadHeight: 67,
							margin: 30
						};
					}
				}
			};
		}
	]);
})( jQuery );