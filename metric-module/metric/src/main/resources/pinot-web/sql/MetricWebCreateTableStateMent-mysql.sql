-- DROP TABLE system_metric_host_Group_exclusion;
-- DROP TABLE system_metric_host_exclusion;

CREATE TABLE system_metric_host_Group_exclusion (
    hostGroupName VARCHAR(50),
    PRIMARY KEY(hostGroupName)
);

CREATE TABLE system_metric_host_exclusion (
      hostGroupName VARCHAR(50),
      hostName VARCHAR(50),
      PRIMARY KEY(hostGroupName, hostName)
);