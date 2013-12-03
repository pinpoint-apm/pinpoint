'use strict';

pinpointApp.service('IndexedDb', [ '$window', '$timeout', function IndexedDb($window, $timeout) {

    var indexedDB, IDBTransaction, IDBTransactionType, IDBKeyRange, oDb, nVersion, bReady;

    var openDb, upgradeDb;

    indexedDB = $window.indexedDB || $window.mozIndexedDB || $window.webkitIndexedDB || $window.msIndexedDB;
    IDBTransaction = $window.IDBTransaction || $window.webkitIDBTransaction || $window.msIDBTransaction;
    IDBTransactionType = { READ_ONLY: "readonly", READ_WRITE: "readwrite" }
    IDBKeyRange = $window.IDBKeyRange || $window.webkitIDBKeyRange || $window.msIDBKeyRange;
    nVersion = 1;
    bReady = false;

    $timeout(function () {
        if (this.isAvailable()) {
            try {
                openDb();
            } catch (e) {
                console.log('IndexedDB is not supported.');
            }
        }
    }.bind(this));

    openDb = function () {
        var openRequest = indexedDB.open('pinpoint', nVersion);

        openRequest.onblocked = function (e) {
            console.log('onblocked', e);
        };
        openRequest.onerror = function (e) {
            console.log('IndexedDB error : ', e.target.errorCode);
        };

        openRequest.onupgradeneeded = function (e) {
            console.log('e.oldVersion', e.oldVersion);
            console.log('e.newVersion', e.newVersion);
            if (e.oldVersion < e.newVersion) {
                upgradeDb(e.currentTarget.result);
            }
        };

        openRequest.onsuccess = function (e) {
            oDb = openRequest.result;
            bReady = true;
        };
        return openRequest;
    };

    upgradeDb = function (db) {
        var objectStore = db.createObjectStore('transactionData', { keyPath: 'id', autoIncrement: true });
        objectStore.createIndex('name', 'name', {unique: true});
        objectStore.createIndex('add_date', 'add_date', {unique: false});
    };

    this.addData = function (objectStoreName, data, cb) {
        if (bReady === false) {
            $timeout(function () {
                this.addData(objectStoreName, data, cb);
            }.bind(this));
            return;
        }
        var transaction = oDb.transaction(objectStoreName, IDBTransactionType.READ_WRITE),
            objectStore = transaction.objectStore(objectStoreName),
            request = objectStore.add(data);
        request.onsuccess = function (e) {
            if (angular.isFunction(cb)) {
                cb(null, e);
            }
        };
        request.onerror = function (e) {
            if (angular.isFunction(cb)) {
                cb(e);
            }
        };
    };

    this.getData = function (objectStoreName, indexName, keyName, cb) {
        if (bReady === false) {
            $timeout(function () {
                this.getData(objectStoreName, indexName, keyName, cb);
            }.bind(this));
            return;
        }
        var transaction = oDb.transaction(objectStoreName, IDBTransactionType.READ_ONLY),
            objectStore = transaction.objectStore(objectStoreName),
            index = objectStore.index(indexName),
            found = index.get(keyName);

        found.onsuccess = function (e) {
            if (angular.isFunction(cb)) {
                cb(null, e.target.result);
            }
        };
    };

    this.deleteOldData = function (objectStoreName, indexName, timestamp) {
        if (bReady === false) {
            $timeout(function () {
                this.deleteOldData(objectStoreName, indexName, timestamp);
            }.bind(this));
            return;
        }
        var lowerBoundKeyRange = IDBKeyRange.lowerBound(timestamp, true);

        var transaction = oDb.transaction(objectStoreName, IDBTransactionType.READ_WRITE),
            objectStore = transaction.objectStore(objectStoreName),
            index = objectStore.index(indexName),
            found = index.openCursor(lowerBoundKeyRange);

        found.onsuccess = function (e) {
            var cursor = e.target.result;
            if (cursor) {
                if (cursor.key < timestamp) {
                    cursor.delete();
                }
                cursor.continue();
            }
        };
    };

    this.isAvailable = function () {
        return Modernizr.indexeddb;
    };
}]);
