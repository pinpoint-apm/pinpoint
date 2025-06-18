CREATE TABLE `service` (
    `uid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `configuration` text,
    PRIMARY KEY (`uid`),
    UNIQUE KEY `name_idx` (`name`)
) AUTO_INCREMENT=100;