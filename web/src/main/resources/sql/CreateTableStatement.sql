DROP TABLE user_group;
DROP TABLE user_group_member;
DROP TABLE user;

CREATE TABLE `user_group` (
  `groupId` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`groupId`)
);

CREATE TABLE `user_group_member` (
  `groupId` VARCHAR(30) NOT NULL,
  `memberId` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`groupId`, memberId)
);

CREATE TABLE `user` (
  `userId` VARCHAR(30) NOT NULL,
  `name` VARCHAR(30) NOT NULL,
  `department` VARCHAR(100) NOT NULL,
  `phonenumber` VARCHAR(30) NOT NULL,
  `email` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`)
);