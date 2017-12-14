--DROP TABLE user_group;
--DROP TABLE user_group_member;
--DROP TABLE puser;
--DROP TABLE alarm_rule;

CREATE TABLE `user_group` (
    `number` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
	`id` VARCHAR(30) NOT NULL,
	PRIMARY KEY (`number`)
);
ALTER TABLE user_group ADD UNIQUE KEY id_idx (id);

CREATE TABLE `user_group_member` (
  `number` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, 
  `user_group_id` VARCHAR(30) NOT NULL,
  `member_id` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`number`)
);
ALTER TABLE user_group_member ADD UNIQUE KEY user_group_id_member_id_idx (`user_group_id`,`member_id`);

CREATE TABLE `puser` (
  `number` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, 
  `user_id` VARCHAR(30) NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `department` VARCHAR(150) NOT NULL,
  `phonenumber` VARCHAR(100),
  `email` VARCHAR(100),
  PRIMARY KEY (`number`)
);
ALTER TABLE puser ADD UNIQUE KEY user_id_idx (`user_id`);

CREATE TABLE `alarm_rule` (
  `rule_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `application_id` VARCHAR(30) NOT NULL,
  `service_type` VARCHAR(30) NOT NULL,
  `checker_name` VARCHAR(50) NOT NULL,
  `threshold` INT(10) DEFAULT NULL,
  `user_group_id` VARCHAR(30) NOT NULL,
  `sms_send` CHAR(1) DEFAULT NULL,
  `email_send` CHAR(1) DEFAULT NULL,
  `notes` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`rule_id`)
);
ALTER TABLE alarm_rule ADD UNIQUE KEY application_id_checker_name_user_group_id_idx (application_id, user_group_id, checker_name);

CREATE TABLE `alarm_history` (
  `history_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `application_id` VARCHAR(30) NOT NULL,
  `checker_name` VARCHAR(50) NOT NULL,
  `detected` CHAR(1) DEFAULT NULL,
  `sequence_count` INT(10),
  `timing_count` INT(10),
  PRIMARY KEY (`history_id`)
);
ALTER TABLE alarm_history ADD UNIQUE KEY application_id_checker_name_idx (application_id, checker_name);

CREATE TABLE `agent_statistics` (
  `agent_count` INT(10) UNSIGNED NOT NULL,
  `date_val`DATETIME NOT NULL,
  PRIMARY KEY (`date_val`)
);
