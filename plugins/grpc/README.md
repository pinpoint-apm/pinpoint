## gRPC
* Since: Pinpoint 1.8.0
* See: https://grpc.io/
* Range: io.grpc/grpc-core [1.2, 1.50]

### Pinpoint Configuration
pinpoint.config

#### Gson options.
~~~
profiler.grpc.client.enable=false
profiler.grpc.server.enable=false
# In case of streaming, does not track all of each stream request.
# After the streaming open in client, all of each stream request are tracked in one remote trace.
# Therefore, tracking stream request  that you want can be difficult.
# please be sure that this description.
profiler.grpc.server.streaming.enable=false
profiler.grpc.server.streaming.onmessage.enable=false
~~~
