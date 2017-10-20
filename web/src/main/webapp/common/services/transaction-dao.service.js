(function() {
	'use strict';
	/**
	 * (en)TransactionDaoService 
	 * @ko TransactionDaoService
	 * @group Service
	 * @name TransactionDaoService
	 * @class
	 */
	pinpointApp.constant("TransactionDaoServiceConfig", {
	    transactionInfoUrl: "transactionInfo.pinpoint"
	});
	
	pinpointApp.service('TransactionDaoService', ['TransactionDaoServiceConfig', '$timeout', '$window',
	    function TransactionDaoService(cfg, $timeout, $window) {
	
	
	        /**
	         * initialize, especially remove old transaction data
	         */
	        $timeout(function () {
	            $window.transactionData = {};
	        });
	
	        /**
	         * add data
	         * @param name
	         * @param data
	         * @param cb
	         */
	        this.addData = function (name, data, cb) {
	            $window.transactionData[name] = data;
	        };
	
	        /**
	         * get data by name
	         * @param name
	         * @param cb
	         */
	        this.getDataByName = function (name, cb) {
	            if (angular.isFunction(cb)) {
	                cb(opener.transactionData[name] || {});
	            }
	        };
	
	        /**
	         * get transaction detail
	         * @param traceId
	         * @param focusTimestamp
	         * @param cb
	         */
	        this.getTransactionDetail = function (agentId, spanId, traceId, focusTimestamp, cb) {
	            jQuery.ajax({
	                type: 'GET',
	                url: cfg.transactionInfoUrl,
	                cache: false,
	                dataType: 'json',
	                data: {
						agentId: agentId,
						spanId: spanId,
	                    traceId: traceId,
	                    focusTimestamp: focusTimestamp
	                },
	                success: function (result) {
	                    if (angular.isFunction(cb)) {
	                        cb(null, result);
	                    }
	                },
	                error: function (xhr, status, error) {
	                    if (angular.isFunction(cb)) {
	                        cb('ERROR', {});
	                    }
	                }
	            });
	        };
	    }
	]);
})();