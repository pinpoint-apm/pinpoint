CREATE TABLE member (
    id int IDENTITY,
    name NCHAR,
    joined DATETIME
);

CREATE TABLE test (
    id int IDENTITY,
    name VARCHAR(45),
    age int
);


CREATE PROCEDURE concatCharacters
    @a CHAR(1),
    @b CHAR(1),
    @c CHAR(2) OUTPUT
AS
    SET @c = @a + @b;

CREATE PROCEDURE swapAndGetSum
    @a INT OUTPUT,
    @b INT OUTPUT
AS
    DECLARE @temp INT
    SET @temp = @a
    SET @a = @b
    SET @b = @temp
    SELECT @b + @a;