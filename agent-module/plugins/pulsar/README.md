## Apache Pulsar
* Since: Pinpoint 3.1.0
* See: https://pulsar.apache.org/
* Range: org.apache.pulsar/pulsar-client [4.0.0, 5.0.1]

### Verified versions
Verified by the integration tests in `agent-module/plugins-it/pulsar-it`.

| Artifact | Versions | Verified |
| --- | --- | --- |
| `pulsar-client` (shaded) | 4.0.0 ~ 4.0.14, 4.1.0 ~ 4.2.5 | producer |
| `pulsar-client` (shaded) | 5.0.0, 5.0.1 | producer, send callback, trace propagation, consumer (`receive()`, `MessageListener`) |
| `pulsar-client-original` (unshaded) | 5.0.0, 5.0.1 | producer, send callback, trace propagation, consumer (`receive()`, `MessageListener`) |

Later versions are not verified.

### Pulsar 5.x
* The shaded artifacts of 5.x (`pulsar-client`, `pulsar-client-all`) relocate the client implementation to
  `org.apache.pulsar.shade.org.apache.pulsar.*`. The unshaded `pulsar-client-original` keeps `org.apache.pulsar.*`.
  Both layouts are traced. `pulsar-client-all` 5.x is not covered by an integration test.
* Only the classic API (`org.apache.pulsar.client.api`) is verified. The new API of `pulsar-client-api-v5` is not.
* `pulsar-client` 5.0.0 / 5.0.1 fails on its own with
  `NoClassDefFoundError: org/apache/pulsar/common/naming/TopicDomain` when a producer or a consumer is created:
  the class is in `org.apache.pulsar:pulsar-client-admin-api`, which `pulsar-client` 5.0.x does not declare as a dependency
  (`pulsar-client-all` does). This is not related to Pinpoint. Add `pulsar-client-admin-api` of the same version.

### Pulsar Configuration
To enable Pulsar Producer, set the following option in *pinpoint.config*:
```
profiler.pulsar.producer.enable=true
```

To enable Pulsar Consumer, set the following option in *pinpoint.config*:
```
profiler.pulsar.consumer.enable=true
```
