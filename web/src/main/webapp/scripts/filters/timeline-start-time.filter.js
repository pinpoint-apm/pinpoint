(function() {
	'use strict';
	
	pinpointApp.filter('timelineStartTime', function() {
		return function(stack, key, callStackStart) {
			return (stack[key.begin] - callStackStart);
		}
	});
})();