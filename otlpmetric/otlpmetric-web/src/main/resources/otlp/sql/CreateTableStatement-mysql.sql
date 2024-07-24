CREATE TABLE `application_metric_definition` (
     `application_name` VARCHAR(60) NOT NULL,
     `metric_definition` text NOT NULL,
     `schema_version` int(10) NOT NULL,
     PRIMARY KEY (`application_name`)
);