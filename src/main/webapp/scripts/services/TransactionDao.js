'use strict';

pinpointApp.constant('TransactionDaoConfig', {
    transactionInfoUrl: '/transactionInfo.pinpoint'
});

pinpointApp.service('TransactionDao', [ 'TransactionDaoConfig','$timeout', 'WebSql', 'IndexedDb', '$window',
    function Transactiondao(cfg, $timeout, oWebSql, oIndexedDb, $window) {

        // define private variables
        var sDaoType;

        /**
         * initialize, especially remove old transaction data
         */
        $timeout(function () {
            if (oIndexedDb.isAvailable()) {
                sDaoType = 'IndexedDb';
                oIndexedDb.deleteOldData('transactionData', 'add_date', Date.now() - 60 * 60 * 12);  // 12시간 전
            } else if (oWebSql.isAvailable()) {
                sDaoType = 'WebSql';
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
            } else {
                sDaoType = 'window';
                $window.transactionData = {};
            }
        });

        /**
         * add data
         * @param name
         * @param data
         * @param cb
         */
        this.addData = function (name, data, cb) {
            if (sDaoType === 'IndexedDb') {
                oIndexedDb.addData('transactionData', {
                    name: name,
                    data: data,
                    add_date: Date.now()
                });
            } else if (sDaoType === 'WebSql') {
                oWebSql.executeSql('INSERT INTO transactionData (name, data, add_date) VALUES (?, ?, datetime("now", "localtime"))', [name, JSON.stringify(data)], cb);
            } else if (sDaoType === 'window') {
                $window.transactionData[name] = data;
            }
        };

        /**
         * get data by name
         * @param name
         * @param cb
         */
        this.getDataByName = function (name, cb) {
            if (sDaoType === 'IndexedDb') {
                oIndexedDb.getData('transactionData', 'name', name, function (err, results) {
                    if (!err) {
                        if (angular.isFunction(cb)) {
                            if (angular.isDefined(results.data)) {
                                cb(results.data);
                            } else {
                                cb({});
                            }
                        }
                    }
                });
            } else if (sDaoType === 'WebSql') {
                oWebSql.executeSql('SELECT data FROM transactionData WHERE name = ?', [name], function (results) {
                    if (angular.isFunction(cb)) {
                        if (results.rows) {
                            cb(JSON.parse(results.rows.item(0).data));
                        } else {
                            cb();
                        }
                    }
                });
            } else if (sDaoType === 'window') {
                if (angular.isFunction(cb)) {
                    cb(opener.transactionData[name] || {});
                }
            }
        };

        /**
         * get transaction detail
         * @param traceId
         * @param focusTimestamp
         * @param cb
         */
        this.getTransactionDetail = function (traceId, focusTimestamp, cb) {
            jQuery.ajax({
                type: 'GET',
                url: cfg.transactionInfoUrl,
                cache: false,
                dataType: 'json',
                data: {
                    jsonResult: true,
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
