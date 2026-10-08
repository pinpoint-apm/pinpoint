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

CREATE TABLE IF NOT EXISTS `user_group_member` (
    `number`        INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_group_id` VARCHAR(30)      NOT NULL,
    `member_id`     VARCHAR(30)      NOT NULL,
    PRIMARY KEY (`number`),
    UNIQUE KEY user_group_id_member_id_idx (`user_group_id`, `member_id`)
);

CREATE TABLE IF NOT EXISTS `puser` (
    `number`      INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`     VARCHAR(30)      NOT NULL,
    `name`        VARCHAR(150)     NOT NULL,
    `department`  VARCHAR(150)     NOT NULL,
    `phone_country_code` INT(10)   NOT NULL DEFAULT '0',
    `phonenumber` VARCHAR(100),
    `email`       VARCHAR(100),
    PRIMARY KEY (`number`),
    UNIQUE KEY user_id_idx (`user_id`)
);
