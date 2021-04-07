CREATE TABLE `system_metric_data_type` (
  `number` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `metric_name` varchar(30) NOT NULL,
  `field_name` varchar(30) NOT NULL,
  `data_type` tinyint(1) NOT NULL,
  PRIMARY KEY (`number`)
);
ALTER TABLE system_metric_data_type ADD UNIQUE KEY metric_name_field_name_idx (`metric_name`,`field_name`);