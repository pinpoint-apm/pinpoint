-- Tables the alarm schema reads but does not own, copied from the definition the web module
-- ships (web/src/main/resources/sql/CreateTableStatement-mysql.sql) so the channel queries
-- that join user_group run against the shape they will meet in a deployment. The alarm tables
-- themselves come from the deployed DDL (sql/alarm/CreateTableStatement-mysql.sql) so that a
-- schema change cannot pass CI while the shipped script and the test schema disagree.
CREATE TABLE IF NOT EXISTS `user_group` (
    `number` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `id`     VARCHAR(30)      NOT NULL,
    PRIMARY KEY (`number`),
    UNIQUE KEY id_idx (id)
);
