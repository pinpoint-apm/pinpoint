-- DROP TABLE system_metric_host_group_exclusion;
-- DROP TABLE system_metric_host_exclusion;

CREATE TABLE system_metric_host_group_exclusion (
    `number` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `host_group_name` VARCHAR(50),
    PRIMARY KEY(`number`),
    UNIQUE KEY `host_group_name_idx` (`host_group_name`)
);

CREATE TABLE system_metric_host_exclusion (
    `number` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `host_group_name` VARCHAR(50),
    `host_name` VARCHAR(50),
    PRIMARY KEY(`number`),
    UNIQUE KEY `host_group_name_host_name_idx` (`host_group_name`, `host_name`)
);