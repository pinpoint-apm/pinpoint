CREATE TABLE `service` (
    `uid` int NOT NULL,
    `name` varchar(255) NOT NULL,
    `configuration` text,
    PRIMARY KEY (`uid`),
    UNIQUE KEY `name_idx` (`name`)
);