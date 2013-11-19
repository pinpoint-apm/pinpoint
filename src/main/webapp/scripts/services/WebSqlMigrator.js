'use strict';

pinpointApp.service('WebSqlMigrator', function WebSqlMigrator() {
    var migrations = [],
        oDb;
    this.migration = function (number, func) {
        migrations[number] = func;
    };
    var doMigration = function (number) {
        if (migrations[number]) {
            oDb.changeVersion(oDb.version, String(number), function (t) {
                migrations[number](t);
            }, function (err) {
                if (console.error) console.error("Error!: %o", err);
            }, function () {
                doMigration(number + 1);
            });
        }
    };
    this.doIt = function (db) {
        oDb = db;
        var initialVersion = parseInt(db.version) || 0;
        try {
            doMigration(initialVersion + 1);
        } catch (e) {
            if (console.error) console.error(e);
        }
    }
});
