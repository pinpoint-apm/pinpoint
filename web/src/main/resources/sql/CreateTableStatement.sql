DROP TABLE user_group;
DROP TABLE user_group_member;
DROP TABLE user;
DROP TABLE alarm_rule;

CREATE TABLE `user_group` (
  `groupId` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`groupId`)
);

CREATE TABLE `user_group_member` (
  `group_id` VARCHAR(30) NOT NULL,
  `member_id` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`groupId`, memberId)
);

CREATE TABLE `user` (
  `user_id` VARCHAR(30) NOT NULL,
  `name` VARCHAR(30) NOT NULL,
  `department` VARCHAR(100) NOT NULL,
  `phonenumber` VARCHAR(30) NOT NULL,
  `email` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `alarm_rule` (
  `rule_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `application_id` varchar(30) NOT NULL,
  `checker_name` varchar(30) NOT NULL,
  `threshold` int(10) DEFAULT NULL,
  `user_group_id` varchar(30) NOT NULL,
  `sms_send` char(1) DEFAULT NULL,
  `email_send` char(1) DEFAULT NULL,
  `notes` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`rule_id`),
  KEY `idx_application_id_agent_id` (`application_id`)
) 
