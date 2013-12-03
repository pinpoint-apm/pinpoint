'use strict';

pinpointApp.service('TransactionDao', [ '$timeout', 'WebSql', 'IndexedDb', function Transactiondao ($timeout, oWebSql, oIndexedDb) {
    // AngularJS will instantiate a singleton by calling "new" on this function

    /**
     * initialize
     */
    $timeout(function () {
        if (oIndexedDb.isAvailable()) {
            oIndexedDb.deleteOldData('transactionData', 'add_date', Date.now() - 60 * 60 * 12);  // 12시간 전
        } else if (oWebSql.isAvailable()) {
            oWebSql.getDb().transaction(function (oTx) {
//            tx.executeSql('DROP TABLE IF EXISTS transactionData', [], function () {
//                console.log('DROP ', arguments);
//            });
//            oTx.executeSql('CREATE TABLE IF NOT EXISTS transactionData (ID INTEGER PRIMARY KEY ASC, name TEXT, data TEXT, add_date DATETIME)', [], function () {
//            });
                oTx.executeSql('DELETE TABLE FROM transaction WHERE add_date < datetime("now", "-12 hours")', [], function () {
                    console.log('The data of transaction before 12 hours has been deleted.');
                });
            });
        }
    });

    this.addData = function (name, data, cb) {
        if (oIndexedDb.isAvailable()) {
            oIndexedDb.addData('transactionData', {
                name : name,
                data : data,
                add_date : Date.now()
            });
        } else if (oWebSql.isAvailable()) {
            oWebSql.executeSql('INSERT INTO transactionData (name, data, add_date) VALUES (?, ?, datetime("now", "localtime"))', [name, JSON.stringify(data)], cb);
        }

    };

    this.getDataByName = function (name, cb) {
        if (oIndexedDb.isAvailable()) {
            oIndexedDb.getData('transactionData', 'name', name, function (err, results) {
                if (!err) {
                    if(angular.isFunction(cb)) {
                        if (angular.isDefined(results.data)) {
                            cb(results.data);
                        } else {
                            cb({});
                        }
                    }
                }
            });
        } else if (oWebSql.isAvailable()) {
            oWebSql.executeSql('SELECT data FROM transactionData WHERE name = ?', [name], function (results) {
                if (angular.isFunction(cb)) {
                    if (results.rows) {
                        cb(JSON.parse(results.rows.item(0).data));
                    } else {
                        cb();
                    }
                }
            });
        }
    };
}]);
