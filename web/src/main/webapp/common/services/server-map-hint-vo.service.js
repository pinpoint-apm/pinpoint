(function() {
	'use strict';
	/**
	 * (en)ServerMapHintVoService 
	 * @ko ServerMapHintVoService
	 * @group Service
	 * @name ServerMapHintVoService
	 * @class
	 */
	pinpointApp.factory('ServerMapHintVoService', [function () {
	    return function ServerMapHintVo(dataSet) {
	        var self = this;
	
	        this._sApplicationName = false;
	        this._aHint = false;
	
	        this.setHint = function (applicationName, hint) {
	            this._sApplicationName = applicationName;
	            this._aHint = hint;
	        };
	
	        this.getHint = function () {
	            var hint = {};
	            if (self._sApplicationName) {
	                hint[self._sApplicationName] = self._aHint;
	            } else {
	                hint = false;
	            }
	            return hint;
	        };
	    };
	  }
	]);
})();