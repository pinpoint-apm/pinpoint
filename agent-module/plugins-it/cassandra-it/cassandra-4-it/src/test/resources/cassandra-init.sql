CREATE KEYSPACE IF NOT EXISTS mykeyspace WITH replication = {'class':'SimpleStrategy','replication_factor':'1'};
CREATE TABLE mykeyspace.mytable (id text, value text, PRIMARY KEY(id));