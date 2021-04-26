CREATE TABLE `system_metric_data_type` (
  `number` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `metric_name` varchar(30) NOT NULL,
  `field_name` varchar(30) NOT NULL,
  `data_type` tinyint(1) NOT NULL,
  PRIMARY KEY (`number`)
);
ALTER TABLE system_metric_data_type ADD UNIQUE KEY metric_name_field_name_idx (`metric_name`,`field_name`);

CREATE TABLE `system_metric_tag` (
  `number` int(20) unsigned NOT NULL AUTO_INCREMENT,
  `application_id` varchar(50) NOT NULL,
  `host_name` varchar(50) NOT NULL,
  `metric_name` varchar(50) NOT NULL,
  `field_name` varchar(50) NOT NULL,
  `tags` varchar(5000),
  PRIMARY KEY (`number`)
);
ALTER TABLE system_metric_tag ADD INDEX application_id_host_name_metric_name_field_name_idx (application_id, host_name, metric_name, field_name);