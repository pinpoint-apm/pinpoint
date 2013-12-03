'use strict';

pinpointApp.service('WebSql', [ '$window', '$timeout', 'WebSqlMigrator', function WebSql($window, $timeout, oWebSqlMigrator) {

    // define private variables
    var oDb;

    /**
     * initiailize
     */
    $timeout(function () {
        if (this.isAvailable()) {
            try {
                oDb = $window.openDatabase('pinpoint', '', 'PinPoint', 1024 * 1024 * 1024); // 1GB

                oWebSqlMigrator.migration(1, function (oTx) {
                    oTx.executeSql('CREATE TABLE IF NOT EXISTS transactionData (ID INTEGER PRIMARY KEY ASC, name TEXT, data TEXT, add_date DATETIME)');
                });

                oWebSqlMigrator.doIt(oDb);
            } catch (e) {
                console.log('Web Sql Database is not supported.');
            }
        }
    }.bind(this));

    /**
     * get db
     * @returns {*}
     */
    this.getDb = function () {
        return oDb;
    };

    /**
     * wxecute sql
     * @param query
     * @param params
     * @param cb
     */
    this.executeSql = function (query, params, cb) {
        oDb.transaction(function (oTx) {
            oTx.executeSql(query, params, function (tx, results) {
                if (angular.isFunction(cb)) {
                    cb(results);
                }
            });
        });
    };

    /**
     * is available
     * @returns {boolean}
     */
    this.isAvailable = function () {
        return Modernizr.websqldatabase;
    };

}]);
