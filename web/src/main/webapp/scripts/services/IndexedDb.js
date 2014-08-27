'use strict';

pinpointApp.service('IndexedDb', [ '$window', '$timeout', function IndexedDb($window, $timeout) {

    /**
     * define private variables
     */
    var oDb, nVersion, bReady;

    /**
     * define private variables of methods
     */
    var indexedDB, IDBTransaction, IDBTransactionType, IDBKeyRange, openDb, upgradeDb;

    /**
     * set private variables
     */
    indexedDB = $window.indexedDB || $window.mozIndexedDB || $window.webkitIndexedDB || $window.msIndexedDB;
    IDBTransaction = $window.IDBTransaction || $window.webkitIDBTransaction || $window.msIDBTransaction;
    IDBTransactionType = { READ_ONLY: "readonly", READ_WRITE: "readwrite" }
    IDBKeyRange = $window.IDBKeyRange || $window.webkitIDBKeyRange || $window.msIDBKeyRange;
    nVersion = 1;
    bReady = false;

    /**
     * initialize
     */
    $timeout(function () {
        if (this.isAvailable()) {
            try {
                openDb();
            } catch (e) {
                console.log('IndexedDB is not supported.');
            }
        }
    }.bind(this));

    /**
     * open db
     * @returns {*}
     */
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

    /**
     * upgrade db
     * @param db
     */
    upgradeDb = function (db) {
        var objectStore = db.createObjectStore('transactionData', { keyPath: 'id', autoIncrement: true });
        objectStore.createIndex('name', 'name', {unique: true});
        objectStore.createIndex('add_date', 'add_date', {unique: false});
    };

    /**
     * add data
     * @param objectStoreName
     * @param data
     * @param cb
     */
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

    /**
     * get data
     * @param objectStoreName
     * @param indexName
     * @param keyName
     * @param cb
     */
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

    /**
     * delete old data
     * @param objectStoreName
     * @param indexName
     * @param timestamp
     */
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

    /**
     * is available
     * @returns {boolean}
     */
    this.isAvailable = function () {
        return Modernizr.indexeddb;
    };
}]);
