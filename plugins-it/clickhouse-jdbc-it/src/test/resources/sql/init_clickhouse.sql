
CREATE TABLE member
(
    id     Int64,
    name   String,
    joined DateTime('Asia/Seoul') DEFAULT now()
) ENGINE = MergeTree()
ORDER BY id;

CREATE TABLE test
(
    id   Int64,
    name String,
    age  Int32
) ENGINE = MergeTree()
ORDER BY id;
