'use strict';

pinpointApp.service('WebSql', [ '$window', function WebSql($window) {
    // AngularJS will instantiate a singleton by calling "new" on this function
    var oDb;

    oDb = $window.openDatabase('pinpoint', '1.1', 'PinPoint', 1024 * 1024 * 1024); // 1GB

    oDb.transaction(function (oTx) {
        oTx.executeSql('CREATE TABLE IF NOT EXISTS transactionList (ID INTEGER PRIMARY KEY ASC, name TEXT, data TEXT, add_date DATETIME)');
        oTx.executeSql('DELETE TABLE FROM transactoinList WHERE add_date < datetime("now", "-12 hours")')
    });


    this.query = function (query, params, cb) {
        oDb.transaction(function (oTx) {
            oTx.executeSql(query, params, function (oTx, results) {
                if (angular.isFunction(cb)) {
                    cb(results);
                }
            })
        });
    };

    this.select = function (query, params, cb) {
        oDb.transaction(function (oTx) {
            oTx.executeSql(query, params, function (oTx, results) {
                if (angular.isFunction(cb)) {
                    cb(results);
                }
            })
        });
    }

}]);
