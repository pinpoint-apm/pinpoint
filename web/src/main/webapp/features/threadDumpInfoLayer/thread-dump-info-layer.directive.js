(function( $ ) {
	'use strict';
	pinpointApp.constant("ThreadDumpInfoLayerDirectiveConfig", {
		ACTIVE_THREAD_LIGHT_DUMP_URL: "agent/activeThreadLightDump.pinpoint",
		ACTIVE_THREAD_DUMP_URL: "agent/activeThreadDump.pinpoint",
		PADDING_WIDTH: 15, 	// %
		PADDING_HEIGHT: 15  	// %
	});
	pinpointApp.directive( "threadDumpInfoLayerDirective", [ "ThreadDumpInfoLayerDirectiveConfig", "$rootScope", "$timeout", "$http", "$window", "CommonUtilService", "AnalyticsService",
		function ( cfg, $rootScope, $timeout, $http, $window, CommonUtilService, AnalyticsService ) {
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
					var $elHeader = $el.find("table");
					var $elListWrapper = $el.find(".thread-list");
					var $elList = $elListWrapper.find("tbody");
					var $elEmpty = $el.find(".panel-body");
					var $elTextarea = $el.find("textarea");
					var oRefListAjax = {
						"obj": null,
						"ing": false
					};
					var oRefDetailAjax = {
						"obj": null,
						"ing": false
					};
					var applicationName = "";

					$el.draggable({
						handle: ".panel-heading"
					});

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
					scope.$on( "thread-dump-info-layer.open", function( event, appName, agentId ) {
						// 이미 오픈된 상태면 "Loading..."을 어딘가에 표시
						// 기존 요청이 끝나지 않았으면 취소 후 새로운 요청 생성
						initAjax( oRefListAjax, true );
						if ( $el.is(":visible") ) {
							$elSpin.show();
						}
						applicationName = appName;
						oRefListAjax.obj = $http( {
							"url": cfg.ACTIVE_THREAD_LIGHT_DUMP_URL +
								"?applicationName=" + appName +
								"&agentId=" + agentId,
							"method": "GET"
						}).then(function ( oResult ) {
							var aThread = oResult.data.message.threadDumpData;
							if ( aThread.length === 0 ) {
								hideContent();
								$elEmpty.show();
								$elTextarea.val("");
							} else {
								$elList.empty();
								addThreadDumpData( aThread, agentId );
								$elEmpty.hide();
								showContent();
							}
							$elSpin.hide();
							$el.show();
							initAjax( oRefListAjax );
						}, function () {
							$elSpin.hide();
							console.log( arguments );
						});
						oRefListAjax.ing = true;
					});

					scope.$on( "thread-dump-info-layer.close", function() {
						scope.hideThreadDump();
					});

					scope.hideThreadDump = function() {
						initAjax( oRefListAjax, true );
						initAjax( oRefDetailAjax, true );
						$el.hide();
						$elSpin.hide();
						hideContent();
					};

					scope.loadDetailMessage = function( $event ) {
						var $elThread = $($( $event.target ).parents("tr")[0]);
						if ( $elThread.attr("data-detail-message") ) {
							$elTextarea.val($elThread.attr("data-detail-message")).show();
						} else {
							$elSpin.show();
							initAjax( oRefDetailAjax, true );
							oRefDetailAjax.obj = $http( {
								"url": cfg.ACTIVE_THREAD_DUMP_URL +
									"?applicationName=" + applicationName +
									"&agentId=" + $elThread.attr("data-agent") +
									"&threadName=" + $elThread.next().find("td:nth-child(1)").html() +
									"&localTraceId=" + $elThread.next().attr("data-traceId"),
								"method": "GET"
							}).then(function ( oResult ) {
								$elThread.find("button span").removeClass("glyphicon-repeat").addClass("glyphicon-ok-circle");
								var msg = "";
								if ( oResult.data.message.threadDumpData.length > 0 ) {
									msg = oResult.data.message.threadDumpData[0].detailMessage;
								} else {
									msg = "There is no message";
								}
								$elThread.attr("data-detail-message", msg );
								$elTextarea.val( msg ).show();
								initAjax( oRefDetailAjax );
								$elSpin.hide();
							}, function () {
								$elSpin.hide();
								console.log( arguments );
							});
							oRefDetailAjax.ing = true;
						}
					};

					function addThreadDumpData( aThread, agentId ) {
						for( var i = 0 ; i < aThread.length ; i++ ) {
							$elList.append(
								'<tr data-agent="' + agentId + '">' +
								'<td rowspan="2">' + (i + 1 ) + '</td>' +
								'<td>' + aThread[i].threadId + '</td>' +
								'<td>' + aThread[i].threadState + '</td>' +
								'<td>' + CommonUtilService.formatDate(aThread[i].startTime, "MM/DD HH:mm:ss SSS") + '</td>' +
								'<td>' + aThread[i].entryPoint + '</td>' +
								'<td rowspan="2"><button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-repeat"></span></button></td>' +
								'</tr>' +
								'<tr data-traceId="' + aThread[i].localTraceId + '">' +
								'<td>' + aThread[i].threadName + '</td>' +
								'<td>' + aThread[i].sampled + '</td>' +
								'<td>' + aThread[i].execTime + '</td>' +
								'<td>' + aThread[i].transactionId + '</td>' +
								'</tr>'
							);
						}
					}
					function initLayerSizeNPosition() {
						var docWidth = $window.document.body.clientWidth;
						var docHeight = $window.document.body.clientHeight;
						var paddingWidthPixel = parseInt(( cfg.PADDING_WIDTH * docWidth ) / 100);
						var paddingHeightPixel = parseInt(( cfg.PADDING_HEIGHT * docHeight ) / 100);

						var layerWidth = docWidth - ( paddingWidthPixel * 2 );
						var layerHeight = docHeight - ( paddingHeightPixel * 2 );
						var layerHeightHalf = parseInt( layerHeight / 2 );

						// title: 41
						// thead: 50
						$el.css({
							"top": paddingHeightPixel,
							"left": paddingWidthPixel,
							"width": layerWidth,
							"height": layerHeightHalf
						});
						$elListWrapper.css({
							"height": layerHeightHalf - 91
						});
						$elEmpty.css({
							"height": layerHeightHalf - 41
						});
						$elTextarea.css({
							"height": layerHeightHalf
						});
					}
					function hideContent() {
						$elHeader.hide();
						$elListWrapper.hide();
						$elTextarea.hide().val("");
					}
					function showContent() {
						$elHeader.show();
						$elListWrapper.show();
					}
				}
			};
		}
	]);
})( jQuery );