'use strict';

pinpointApp.service('WebSql', [ '$window', function WebSql($window) {
    // AngularJS will instantiate a singleton by calling "new" on this function
    var oDb;

    oDb = $window.openDatabase('pinpoint_test', '', 'PinPoint', 1024 * 1024 * 1024); // 1GB

    this.getDb = function () {
        return oDb;
    };

    this.executeSql = function (query, params, cb) {
        oDb.transaction(function (oTx) {
            oTx.executeSql(query, params, function (tx, results) {
                if (angular.isFunction(cb)) {
                    cb(results);
                }
            })
        });
    };

}]);
