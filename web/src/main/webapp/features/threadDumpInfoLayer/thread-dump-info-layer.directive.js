(function( $ ) {
	'use strict';
	pinpointApp.constant("ThreadDumpInfoLayerDirectiveConfig", {
		ACTIVE_THREAD_LIGHT_DUMP_URL: "agent/activeThreadLightDump.pinpoint",
		ACTIVE_THREAD_DUMP_URL: "agent/activeThreadDump.pinpoint",
		PADDING_WIDTH: 5, 	// %
		PADDING_HEIGHT: 15  	// %
	});
	pinpointApp.directive( "threadDumpInfoLayerDirective", [ "ThreadDumpInfoLayerDirectiveConfig", "$rootScope", "$timeout", "$http", "$window", "CommonUtilService",
		function ( cfg, $rootScope, $timeout, $http, $window, CommonUtilService ) {
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

					$el.draggable({
						handle: ".panel-heading"
					});
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
						if ( $elThread.attr("data-detail-message") ) {
							$elTextarea.val($elThread.attr("data-detail-message"));
						} else {
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
									msg = "There is no message";
								}
								$elThread.attr("data-detail-message", msg );
								$elTextarea.val( msg );
								initAjax( oRefDetailAjax );
								$elSpin.hide();
							}, function () {
								$elSpin.hide();
							});
							oRefDetailAjax.ing = true;
						}
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
							scope.threadList = oResult.data.message.threadDumpData;
							$elTextarea.val("");
							$elSpin.hide();
							$el.show();
							initAjax( oRefListAjax );
						}, function () {
							$elSpin.hide();
							console.log( arguments );
						});
						oRefListAjax.ing = true;
					}
					function initLayerSizeNPosition( openType ) {
						var docWidth = $window.document.body.clientWidth;
						var docHeight = $window.document.body.clientHeight;
						var paddingWidthPixel = parseInt(( cfg.PADDING_WIDTH * docWidth ) / 100);
						var paddingHeightPixel = parseInt(( cfg.PADDING_HEIGHT * docHeight ) / 100);
						var titleHeight = 78;

						var layerWidth = docWidth - ( paddingWidthPixel * 2 );
						var layerHeight = docHeight - ( paddingHeightPixel * 2 );
						var layerHeightHalf = parseInt( (layerHeight - titleHeight) / 2 );

						// thead: 67
						$el.css({
							"top": paddingHeightPixel,
							"left": paddingWidthPixel,
							"width": layerWidth,
							"height": layerHeight
						});
						$elListWrapper.css({
							"height": layerHeightHalf - 67 - 30
						});
						$elEmpty.css({
							"height": layerHeightHalf
						});
						$elDetailMessage.css({
							"height": layerHeightHalf
						});
					}
					function setForWindow() {
						$el.draggable("destroy");
						$el.find(".panel-heading").css("cursor", "default").find("button").hide();
						resetSize();
					}
					function resetSize () {
						var docHeight = $window.document.body.clientHeight;
						var navHeight = 40;
						var titleHeight = 78;
						var layerHeightHalf = parseInt( (docHeight - titleHeight - navHeight) / 2 );

						// thead: 67
						$el.css({
							"top": navHeight + "px",
							"left": "0px",
							"width": "100%",
							"height": "100%"
						});
						$elListWrapper.css({
							"height": layerHeightHalf - 67 - 30
						});
						$elEmpty.css({
							"height": layerHeightHalf
						});
						$elDetailMessage.css({
							"height": layerHeightHalf
						});
					}
				}
			};
		}
	]);
})( jQuery );