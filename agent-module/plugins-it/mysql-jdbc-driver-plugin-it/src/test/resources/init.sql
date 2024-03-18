USE test;

CREATE TABLE member
(
    id     varchar(11) not null primary key,
    name   varchar(20) not null,
    joined date        null
);

CREATE TABLE test
(
    id   int auto_increment primary key,
    name varchar(20) not null,
    age  int         not null
);


CREATE PROCEDURE concatCharacters(IN a char, IN b char, OUT c char(2))
BEGIN
    SET c = CONCAT(a, b);
END;

CREATE PROCEDURE swapAndGetSum(INOUT a int, INOUT b int)
BEGIN
    DECLARE temp INT;
    SET temp = a;
    SET a = b;
    SET b = temp;
    SELECT temp + a;
END;
