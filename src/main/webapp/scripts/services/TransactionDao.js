'use strict';

pinpointApp.service('TransactionDao', [ '$timeout', 'WebSql', function Transactiondao ($timeout, oWebSql) {
    // AngularJS will instantiate a singleton by calling "new" on this function

    /**
     * initialize
     */
    $timeout(function () {
        oWebSql.getDb().transaction(function (oTx) {
//            tx.executeSql('DROP TABLE IF EXISTS transactionData', [], function () {
//                console.log('DROP ', arguments);
//            });
            oTx.executeSql('CREATE TABLE IF NOT EXISTS transactionData (ID INTEGER PRIMARY KEY ASC, name TEXT, data TEXT, add_date DATETIME)', [], function () {
            });
            oTx.executeSql('DELETE TABLE FROM transaction WHERE add_date < datetime("now", "-12 hours")', [], function () {
                console.log('The data of transaction before 12 hours has been deleted.');
            })
        });
    });

    this.addData = function (name, data, cb) {
        oWebSql.executeSql('INSERT INTO transactionData (name, data, add_date) VALUES (?, ?, datetime("now", "localtime"))', [name, JSON.stringify(data)], cb)
    };

    this.getDataByName = function (name, cb) {
        oWebSql.executeSql('SELECT data FROM transactionData WHERE name = ?', [name], function (results) {
            if (angular.isFunction(cb)) {
                if (results.rows) {
                    cb(JSON.parse(results.rows.item(0).data));
                } else {
                    cb();
                }
            };
        })
    }

}]);
