CREATE TABLE `service` (
  `uid` int NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `name_idx` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
