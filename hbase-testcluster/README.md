# pinpoint-hbase-testcluster

A test-only module that boots an HBase MiniCluster (HBase + HDFS + ZooKeeper) for the lifetime of an integration test, so that components depending on HBase can be tested in-process without a real cluster.

Internally it wraps `TestingHBaseCluster` from `org.apache.hbase:hbase-shaded-testing-util` and exposes it as Spring beans.

## Dependency

Add it to the consumer module's `pom.xml` with `test` scope. The version is managed by the parent BOM.

```xml
<dependency>
    <groupId>com.navercorp.pinpoint</groupId>
    <artifactId>pinpoint-hbase-testcluster</artifactId>
    <scope>test</scope>
</dependency>
```

## Requirements

- **JDK 17+** (the module itself is built with `jdk.version=17`).
- The test JVM needs the following `--add-opens` for Hadoop 3 reflective access:
  ```
  --add-opens java.base/java.lang=ALL-UNNAMED
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED
  ```
  Add them to the consumer module's `maven-surefire-plugin` `argLine`:

  ```xml
  <build>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-surefire-plugin</artifactId>
              <version>${plugin.surefire.version}</version>
              <configuration>
                  <argLine>
                      @{argLine}
                      --add-opens java.base/java.lang=ALL-UNNAMED
                      --add-opens java.base/java.lang.reflect=ALL-UNNAMED
                  </argLine>
              </configuration>
          </plugin>
      </plugins>
  </build>
  ```

## Quick start — Spring

Importing `HbaseTestClusterConfiguration` into a Spring test context registers the following beans automatically.

| Bean name | Type | Notes |
|---|---|---|
| `hbaseTestCluster` | `HbaseTestCluster` | Started in `@PostConstruct`, stopped via `destroyMethod="close"` |
| `hbaseConfiguration` | `org.apache.hadoop.conf.Configuration` | Conf wired to the running cluster |
| `hbaseConnection` | `org.apache.hadoop.hbase.client.Connection` | Synchronous client |
| `hbaseAsyncConnection` | `org.apache.hadoop.hbase.client.AsyncConnection` | Asynchronous client |

```java
@SpringJUnitConfig(HbaseTestClusterConfiguration.class)
class MyHbaseIT {

    private static final TableName TABLE = TableName.valueOf("my_table");
    private static final byte[] CF = Bytes.toBytes("cf");

    @Autowired
    private HbaseTestCluster cluster;

    @Autowired
    @Qualifier("hbaseConnection")
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        cluster.createTable(TABLE, CF);
    }

    @Test
    void putAndGet() throws Exception {
        try (Table table = connection.getTable(TABLE)) {
            Put put = new Put(Bytes.toBytes("row1"));
            put.addColumn(CF, Bytes.toBytes("name"), Bytes.toBytes("alice"));
            table.put(put);

            Result result = table.get(new Get(Bytes.toBytes("row1")));
            assertEquals("alice", Bytes.toString(result.getValue(CF, Bytes.toBytes("name"))));
        }
    }
}
```

See `src/test/java/.../HbaseClusterIT.java` for a complete working example.

## Standalone usage (without Spring)

```java
try (HbaseTestCluster cluster = new HbaseTestCluster()) {
    cluster.start();

    Configuration conf = cluster.getConfiguration();
    try (Connection connection = ConnectionFactory.createConnection(conf)) {
        cluster.createTable(TableName.valueOf("t"), Bytes.toBytes("cf"));
        // ... use connection
    }
} // close() automatically calls cluster.stop()
```

`new HbaseTestCluster()` uses `defaultOption()` internally. To customize the topology, build the option yourself:

```java
TestingHBaseClusterOption option = TestingHBaseClusterOption.builder()
        .numMasters(1)
        .numRegionServers(2)
        .numDataNodes(2)
        .build();
HbaseTestCluster cluster = new HbaseTestCluster(option);
```

## Logging

The default logging configuration lives in `src/test/resources/log4j2-test.xml`. Hadoop and HBase are intentionally muted at `WARN` so the test output stays readable. Raise specific loggers to `DEBUG` only when diagnosing a problem.

### Default log levels

Drop the snippet below into your test's `log4j2-test.xml` to silence Hadoop/HBase noise while keeping the rest of the output readable.

```xml
<Loggers>
    <!-- Hadoop core (HDFS, IPC) — extremely chatty at INFO -->
    <Logger name="org.apache.hadoop" level="WARN"/>
    <!-- HBase server-side -->
    <Logger name="org.apache.hadoop.hbase" level="WARN"/>
    <!-- Shaded third-party deps inside HBase (Netty, Guava, ...) -->
    <Logger name="org.apache.hadoop.hbase.shaded" level="WARN"/>
    <!-- Newer-package HBase classes -->
    <Logger name="org.apache.hbase" level="WARN"/>
    <!-- Shaded protobuf / netty under hbase.thirdparty -->
    <Logger name="org.apache.hbase.thirdparty" level="WARN"/>
    <!-- HDFS NameNode block-state log; one line per block transition -->
    <Logger name="BlockStateChange" level="WARN"/>

    <Root level="INFO">
        <AppenderRef ref="console"/>
    </Root>
</Loggers>
```

## Notes

- This is a **test-only** module. Do not import it from production code.
- Cluster startup is expensive (seconds to tens of seconds). Prefer sharing a single cluster across tests within a class — Spring's test context caching does this by default.