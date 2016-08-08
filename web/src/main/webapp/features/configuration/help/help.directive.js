(function( $ ) {
	"use strict";

	pinpointApp.directive( "helpDirective", [
		function () {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/configuration/help/help.html?v=" + G_BUILD_TIME,
				scope: {
					namespace: "@"
				},
				link: function( scope, element, attr ) {
					var $element = element;
					var myName = attr["name"];
					$element[ attr["initState"] ]();

					scope.enHelpList = [
						{ "title": "Quick start guide", "link": "https://github.com/naver/pinpoint/blob/master/quickstart/README.md" },
						{ "title": "Technical Overview of Pinpoint", "link": "https://github.com/naver/pinpoint/wiki/Technical-Overview-Of-Pinpoint" },
						{ "title": "Using Pinpont with Docker", "link": "http://yous.be/2015/05/05/using-pinpoint-with-docker/" },
						{ "title": "Notes on Jetty Plugin for Pinpoint ", "link": "https://github.com/cijung/Docs/blob/master/JettyPluginNotes.md" },
						{ "title": "About Alarm", "link": "https://github.com/naver/pinpoint/blob/master/doc/alarm.md#alarm" }
					];
					scope.koHelpList = [
						{ "title": "Pinpoint 개발자가 작성한 Pinpoint 기술문서", "link": "http://helloworld.naver.com/helloworld/1194202" },
						{ "title": "소개 및 설치 가이드", "link": "http://dev2.prompt.co.kr/33" },
						{ "title": "Pinpoint 사용 경험", "link": "http://www.barney.pe.kr/blog/category/development/page/2/" },
						{ "title": "설치 가이드 동영상 강좌 1", "link": "https://www.youtube.com/watch?v=hrvKaEaDEGs" },
						{ "title": "설치 가이드 동영상 강좌 2", "link": "https://www.youtube.com/watch?v=fliKPGHGXK4" },
						{ "title": "AWS Ubuntu 14.04 설치 가이드 ", "link": "http://lky1001.tistory.com/132" },
						{ "title": "Alarm 가이드", "link": "https://github.com/naver/pinpoint/blob/master/doc/alarm.md#alarm-1" }
					];

					scope.$on( "configuration.selectMenu", function( event, selectedName ) {
						if ( myName === selectedName ) {
							$element.show();
						} else {
							$element.hide();
						}
					});
				}
			};
		}
	]);
})( jQuery );