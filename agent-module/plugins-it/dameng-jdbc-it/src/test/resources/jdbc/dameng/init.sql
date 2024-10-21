CREATE TABLE citest (
 ci_id INT IDENTITY(1,1) PRIMARY KEY,
 ci_key VARCHAR(64) NOT NULL,
 ci_val VARCHAR(64) NOT NULL,
 UNIQUE(ci_id)
);
INSERT INTO citest (ci_key, ci_val) VALUES('plugin', 'dameng-jdbc');
INSERT INTO citest (ci_key, ci_val) VALUES('init', 'updateKey');
INSERT INTO citest (ci_key, ci_val) VALUES('ciUpdateValueCall', 'init');
INSERT INTO citest (ci_key, ci_val) VALUES('4del', '4del');

SELECT * from citest;
/

CREATE OR REPLACE PROCEDURE ciUpdateKey (prc_id IN INT, prc_key IN VARCHAR(64), cnt OUT INT)
AS
BEGIN
  UPDATE citest SET ci_key = prc_key WHERE ci_id = prc_id;
  SELECT count(1) INTO cnt FROM citest WHERE ci_key = prc_key;
END;
/

CREATE OR REPLACE PROCEDURE ciUpdateValue (prc_id IN INT, prc_value IN VARCHAR(64), cnt OUT INT)
AS
BEGIN
  UPDATE citest SET ci_val = prc_value WHERE ci_id = prc_id;
  SELECT count(1) INTO cnt FROM citest WHERE ci_val = prc_value;
END;
/
