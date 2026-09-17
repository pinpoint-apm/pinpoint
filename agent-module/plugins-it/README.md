## Plugin Integration Tests

The `plugins-it` modules run each agent plugin against the real target library, downloaded from Maven Central at test time in the versions declared by `@Dependency`. They are always part of the Maven reactor, but the tests themselves are skipped by default: the root `pom.xml` sets `skipITs=true`, and the tests only run when `-DskipITs=false` is passed.

### Prerequisites

* `JAVA_HOME` pointing at JDK 17 or higher for Maven itself
* `JAVA_8_HOME`, `JAVA_11_HOME` and `JAVA_17_HOME` set, because the IT modules fork the test JVM from these variables (`<jvm>${env.JAVA_8_HOME}/bin/java</jvm>` and friends)
* Docker for the modules that use Testcontainers (databases, Kafka, Redis, RabbitMQ, Pulsar, Elasticsearch, Cassandra, MongoDB, MQTT)
* The agent distribution built in `agent-module/agent/target/pinpoint-agent-<version>`. The tests attach it through `AgentPath.PATH`, so build the project once before running them. The integration tests are already skipped by default; `-DskipTests` only saves time by skipping the unit tests of the whole project as well:

```bash
./mvnw --batch-mode -DskipTests clean install
```

### Running

```bash
# Every plugin IT (from the repository root)
./mvnw clean install -f agent-module/plugins-it -DskipITs=false

# One IT module
./mvnw clean install -f agent-module/plugins-it -pl rxjava-it -DskipITs=false

# One IT class or method (failsafe uses it.test, not test)
./mvnw verify -f agent-module/plugins-it -pl rxjava-it -DskipITs=false -Dit.test=RxJava_1_1_1_to_1_1_5_IT
./mvnw verify -f agent-module/plugins-it -pl rxjava-it -DskipITs=false -Dit.test=RxJava_1_1_1_to_1_1_5_IT#methodName

# Keep going after failures and collect every report
./mvnw clean install -f agent-module/plugins-it -DskipITs=false -Dmaven.test.failure.ignore=true
```

Reports are written to `<module>/target/failsafe-reports/`. The IT modules are not installed or deployed (`maven.install.skip` and `maven.deploy.skip` are `true`).

### Skipping

| Option | Effect |
|---|---|
| none | IT modules compile, tests are skipped (`skipITs=true` in the root pom) |
| `-DskipITs=false` | Runs the integration tests |
| `-DskipTests` | Skips the unit tests and the integration tests |
| `-Dmaven.test.skip=true` | Skips test compilation too |

### Test styles

* `@PluginTest` runs the test in the Maven JVM with a dedicated plugin test class loader per dependency version. This is the default and the fastest style.
* `@PluginForkedTest` forks one JVM per dependency version with the agent attached through `-javaagent`. Use it when the plugin instruments JDK classes or needs a clean JVM.
* `@SharedTestLifeCycleClass` starts an expensive fixture, such as a Testcontainers database, once and shares it across every version of the test.

The annotations live in `com.navercorp.pinpoint.test.plugin.api` (module `pinpoint-plugins-test-api`). A minimal in-process test looks like this:

```java
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("rxjava/pinpoint-rxjava.config")
@Dependency({"io.reactivex:rxjava:[1.1.1,1.1.5]"})
public class RxJava_1_1_1_to_1_1_5_IT {
}
```

`@Dependency` takes Maven coordinates with a version or a version range. Every version that resolves from the range becomes one test run, so keep ranges tight when a library releases often.

### CI

* `it-schedule.yml` runs the whole suite every day.
* On a pull request, a maintainer comments `/it` to run the whole suite, or `/it <module>` (for example `/it rxjava-it`) to run one module. The results are posted back to the pull request.
