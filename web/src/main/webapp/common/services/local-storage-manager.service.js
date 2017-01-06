(function() {
	'use strict';
	pinpointApp.service("LocalStorageManagerService", [ "webStorage", function (webStorage) {
		this.getThreadDumpLayerOpenType = function() {
			return webStorage.get("thread-dump-layer-open-type");
		};
	}]);
})();