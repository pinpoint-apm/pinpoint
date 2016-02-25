USE test;
CREATE TABLE IF NOT EXISTS playground (id int(5) NOT NULL, name varchar(50) DEFAULT NULL);
INSERT INTO playground VALUES (1, 'ONE');
INSERT INTO playground VALUES (2, 'TWO');
INSERT INTO playground VALUES (3, 'THREE');

DELIMITER //
CREATE PROCEDURE getPlaygroundByName (IN inputParamName VARCHAR(50), OUT outputParamCount INT)
    BEGIN
        SELECT count(*) INTO outputParamCount FROM playground;
        SELECT id, name FROM playground WHERE name = inputParamName ORDER BY id ASC;
    END;
//
DELIMITER ;