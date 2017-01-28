(function() {
	'use strict';

	pinpointApp.constant("LocalStorageManagerServiceConfig", {
		"THREAD_DUMP_LAYER_OPEN_TYPE": "thread-dump-layer-open-type",
		"REAL_TIME_LAYER_HEIGHT": "real-time-layer-height"
	});

	pinpointApp.service("LocalStorageManagerService", [ "LocalStorageManagerServiceConfig", "webStorage", function (cfg, webStorage) {
		this.getThreadDumpLayerOpenType = function() {
			return webStorage.get(cfg.THREAD_DUMP_LAYER_OPEN_TYPE);
		};
		this.setRealtimeLayerHeight = function( h ) {
			webStorage.add(cfg.REAL_TIME_LAYER_HEIGHT, h);
		};
		this.getRealtimeLayerHeight = function() {
			return webStorage.get(cfg.REAL_TIME_LAYER_HEIGHT);
		};
	}]);
})();