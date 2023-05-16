# Adding Agent-SDK to your build
Maven
```xml
<dependency>
  <groupId>com.navercorp.pinpoint</groupId>
  <artifactId>pinpoint-agent-sdk</artifactId>
  <version>#{PINPOINT_VERSION}</version>
</dependency>
```

# AsyncContext Propagation

## Runnable instrumentation
Wrap the method you want to trace with `TraceRunnable.asyncEntry()`.

```java
public class AsyncEntryExample {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @GetMapping(value = "/sdk-async-plugin/asyncEntry-propagation")
    public String asyncEntryAndExecute() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();

        Runnable command = TraceRunnable.asyncEntry(() -> future.complete("asyncEntry-execute"));
        executor.execute(command);

        Thread.sleep(1000);

        return future.get();
    }
}
```

## Executor instrumentation
Wrap the executor you want to trace to `TraceExecutors.wrapExecutorService(executr, true)`.

```java
public class AutoExample {

    private final ExecutorService contextPropagationExecutor 
            = TraceExecutors.wrapExecutorService(Executors.newSingleThreadExecutor(), true);

    @GetMapping(value = "/sdk-async-plugin/auto-context-propagation")
    public String autoWrapAndExecute() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();

        contextPropagationExecutor.execute(() -> future.complete("auto-execute"));

        Thread.sleep(1000);

        return future.get();
    }
}
```

## Executor and Runnable instrumentation
This method has high tracing precision.
```java
public class ManualExample {
    private final ExecutorService traceExecutor 
            = TraceExecutors.wrapExecutorService(Executors.newSingleThreadExecutor());

    @GetMapping(value = "/sdk-async-plugin/manual-context-propagation")
    public String manualWrapAndExecute() throws Exception {

        CompletableFuture<String> future = new CompletableFuture<>();

        traceExecutor.execute(TraceRunnable.wrap(() -> future.complete("manual-execute")));
        
        traceExecutor.execute(() -> "Not captured");

        Thread.sleep(1000);

        return future.get();
    }
}
```
