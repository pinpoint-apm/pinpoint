/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real-world {@code exception.stacktrace} fixtures per OTel SDK language, exercising both the
 * language selection (attribute + sniffing) and the frame extraction rules.
 */
class StackTraceParsersTest {

    private final StackTraceParserRegistry registry = new StackTraceParserRegistry();

    private List<StackFrame> parse(String sdkLanguage, String stackTrace) {
        StackTraceParser parser = registry.select(sdkLanguage, stackTrace);
        StackFrameSink sink = new StackFrameSink(256);
        parser.parse(stackTrace, sink);
        return sink.frames();
    }

    // =======================================================================
    // Java
    // =======================================================================

    private static final String JAVA_STACK = """
            java.lang.IllegalStateException: boom
            \tat com.example.Service.handle(Service.java:42)
            \tat java.base/java.lang.Thread.run(Thread.java:833)
            \tat com.example.Native.call(Native Method)
            Caused by: java.io.IOException: io down
            \tat com.example.Io.read(Io.java:7)
            \t... 3 more
            """;

    @Test
    void java_framesAndSpecialLines() {
        List<StackFrame> frames = parse("java", JAVA_STACK);

        // current semantics: "Caused by:" frames merge into one flat list (chain split = follow-up)
        assertThat(frames).hasSize(4);
        assertThat(frames.get(0)).isEqualTo(new StackFrame("com.example.Service", "Service.java", 42, "handle"));
        assertThat(frames.get(2).lineNumber()).isEqualTo(-2); // Native Method
        assertThat(frames.get(3)).isEqualTo(new StackFrame("com.example.Io", "Io.java", 7, "read"));
    }

    @Test
    void java_malformedLineToken_isUnknownLine() {
        List<StackFrame> frames = parse("java", "\tat com.example.Foo.bar(Foo.java:??)\n");

        assertThat(frames).containsExactly(new StackFrame("com.example.Foo", "Foo.java", -1, "bar"));
    }

    @Test
    void java_selectedBySniffingWithoutLanguageAttribute() {
        assertThat(registry.select(null, JAVA_STACK).name()).isEqualTo("java");
    }

    // =======================================================================
    // Node.js (V8)
    // =======================================================================

    private static final String NODE_STACK = """
            Error: boom
                at handler (/app/routes/user.js:10:15)
                at async UserService.load (/app/services/user.js:22:9)
                at Layer.handle [as handle_request] (/app/node_modules/express/lib/router/layer.js:95:5)
                at /app/index.js:3:1
                at process.processTicksAndRejections (node:internal/process/task_queues:95:5)
            """;

    @Test
    void node_framesLineNotColumn() {
        List<StackFrame> frames = parse("nodejs", NODE_STACK);

        assertThat(frames).hasSize(5);
        // the location tail is file:line:column — line must be the middle token, not the column
        assertThat(frames.get(0)).isEqualTo(new StackFrame("<anonymous>", "/app/routes/user.js", 10, "handler"));
        assertThat(frames.get(1)).isEqualTo(new StackFrame("UserService", "/app/services/user.js", 22, "load"));
        // "[as alias]" decoration dropped
        assertThat(frames.get(2)).isEqualTo(new StackFrame("Layer", "/app/node_modules/express/lib/router/layer.js", 95, "handle"));
        // anonymous frame (no function, no parens)
        assertThat(frames.get(3)).isEqualTo(new StackFrame("<anonymous>", "/app/index.js", 3, "<anonymous>"));
        assertThat(frames.get(4).fileName()).isEqualTo("node:internal/process/task_queues");
    }

    @Test
    void node_selectedBySniffing_firstFrameWithoutLocation() {
        // Built-in frames carry no file:line:col tail; sniffing must keep scanning to the next frame.
        String stack = "TypeError: boom\n"
                + "    at Array.map (<anonymous>)\n"
                + "    at handler (/app/routes/user.js:10:15)\n";

        assertThat(registry.select(null, stack).name()).isEqualTo("node");
        assertThat(parse(null, stack)).containsExactly(
                new StackFrame("Array", "<anonymous>", -1, "map"),
                new StackFrame("<anonymous>", "/app/routes/user.js", 10, "handler"));
    }

    @Test
    void node_selectedBySniffing_notMistakenForJava() {
        // V8 frames also start with "at ", but the :line:col tail must route to the node parser.
        assertThat(registry.select(null, NODE_STACK).name()).isEqualTo("node");
    }

    // =======================================================================
    // Python
    // =======================================================================

    private static final String PYTHON_STACK = """
            Traceback (most recent call last):
              File "/app/main.py", line 10, in handler
                do_work()
              File "/app/services/work.py", line 3, in do_work
                raise ValueError("boom")
            ValueError: boom
            """;

    @Test
    void python_framesReversedToInnermostFirst() {
        List<StackFrame> frames = parse("python", PYTHON_STACK);

        assertThat(frames).hasSize(2);
        // python prints outermost-first; frames are normalized to innermost-first (throw site on top)
        assertThat(frames.get(0)).isEqualTo(new StackFrame("work", "/app/services/work.py", 3, "do_work"));
        assertThat(frames.get(1)).isEqualTo(new StackFrame("main", "/app/main.py", 10, "handler"));
    }

    @Test
    void python_selectedBySniffing() {
        assertThat(registry.select(null, PYTHON_STACK).name()).isEqualTo("python");
    }

    // =======================================================================
    // .NET
    // =======================================================================

    private static final String DOTNET_STACK = """
            System.InvalidOperationException: boom
               at Cart.Services.CartService.GetCart(GetCartRequest request) in /app/services/CartService.cs:line 42
               at Grpc.AspNetCore.Server.Internal.CallHandlers.UnaryServerCallHandler`3.HandleCallAsyncCore(HttpContext httpContext, HttpContextServerCallContext serverCallContext)
               --- End of stack trace from previous location ---
               at lambda_method1(Closure, Object)
            """;

    @Test
    void dotnet_argListNotMistakenForFileInfo() {
        List<StackFrame> frames = parse("dotnet", DOTNET_STACK);

        assertThat(frames).hasSize(3);
        // parens hold the parameter list; the source location comes from the " in file:line N" suffix
        assertThat(frames.get(0)).isEqualTo(new StackFrame(
                "Cart.Services.CartService", "/app/services/CartService.cs", 42, "GetCart"));
        // frame without source info: file empty, line unknown
        assertThat(frames.get(1).fileName()).isEmpty();
        assertThat(frames.get(1).lineNumber()).isEqualTo(-1);
        // signature without a dot keeps the whole name as the method
        assertThat(frames.get(2)).isEqualTo(new StackFrame("<unknown>", "", -1, "lambda_method1"));
    }

    @Test
    void dotnet_selectedBySniffingViaInLineMarker() {
        assertThat(registry.select(null, DOTNET_STACK).name()).isEqualTo("dotnet");
    }

    // =======================================================================
    // Go
    // =======================================================================

    private static final String GO_STACK = """
            goroutine 19 [running]:
            github.com/acme/shop/checkout.(*Service).PlaceOrder(0xc000010000, {0x8b1e20, 0xc0000a4000})
            \t/app/checkout/service.go:42 +0x5e
            main.main()
            \t/app/main.go:10 +0x20
            created by net/http.(*Server).Serve
            \t/usr/local/go/src/net/http/server.go:3086 +0x5cb
            """;

    @Test
    void go_pairsParsed_offsetDropped() {
        List<StackFrame> frames = parse("go", GO_STACK);

        assertThat(frames).hasSize(3);
        assertThat(frames.get(0)).isEqualTo(new StackFrame(
                "github.com/acme/shop/checkout.(*Service)", "/app/checkout/service.go", 42, "PlaceOrder"));
        assertThat(frames.get(1)).isEqualTo(new StackFrame("main", "/app/main.go", 10, "main"));
        // "created by" prefix stripped; the +0x program-counter offset never reaches the frame
        assertThat(frames.get(2)).isEqualTo(new StackFrame(
                "net/http.(*Server)", "/usr/local/go/src/net/http/server.go", 3086, "Serve"));
    }

    @Test
    void go_selectedBySniffingViaGoroutineHeader() {
        assertThat(registry.select(null, GO_STACK).name()).isEqualTo("go");
    }

    // =======================================================================
    // Raw fallback (unrecognized formats)
    // =======================================================================

    private static final String RUBY_STACK = """
            /app/services/order.rb:12:in `place'
            /app/controllers/orders_controller.rb:5:in `create'
            """;

    @Test
    void unknownFormat_fallsBackToRawLines() {
        assertThat(registry.select(null, RUBY_STACK).name()).isEqualTo("raw-fallback");

        List<StackFrame> frames = parse(null, RUBY_STACK);
        assertThat(frames).hasSize(2);
        assertThat(frames.get(0).className()).isEqualTo("/app/services/order.rb:12:in `place'");
        assertThat(frames.get(0).methodName()).isEqualTo("?");
    }

    @Test
    void rawFallback_scrubsHexAddressesForStableGrouping() {
        StackFrameSink sink = new StackFrameSink(16);
        registry.rawFallback().parse("panic at 0xc000010af8 in worker", sink);

        assertThat(sink.frames().get(0).className()).isEqualTo("panic at 0x? in worker");
    }

    @Test
    void unmappedLanguage_fallsBackToSniffing() {
        // e.g. "ruby": no dedicated parser → sniffing → raw fallback
        assertThat(registry.select("ruby", RUBY_STACK).name()).isEqualTo("raw-fallback");
        // an unmapped language with a recognizable format still routes by content
        assertThat(registry.select("kotlin-native", JAVA_STACK).name()).isEqualTo("java");
    }

    @Test
    void sink_capsFramesAndMarksTruncated() {
        StackFrameSink sink = new StackFrameSink(2);
        registry.rawFallback().parse("a\nb\nc\nd", sink);

        assertThat(sink.frames()).hasSize(2);
        assertThat(sink.isTruncated()).isTrue();
    }

    // =======================================================================
    // Real captured fixtures (src/test/resources/stacktrace/*.txt)
    //
    // Captured verbatim from each runtime on Windows, exactly as the OTel SDK
    // records exception.stacktrace: Java = Throwable.printStackTrace, Python =
    // traceback.format_exception (CPython 3.14), Node = error.stack (Node 24),
    // Go = runtime/debug.Stack() (Go 1.26). They carry the real-world artifacts
    // hand-written fixtures miss: JDK module prefixes, "... N more" elisions,
    // Python 3.11+ caret marker lines, Windows drive-letter paths (bare V8
    // frames included), V8 "(<anonymous>)" locations, and Go paths with spaces.
    // =======================================================================

    private static String readFixture(String name) {
        try (InputStream in = StackTraceParsersTest.class.getResourceAsStream("/stacktrace/" + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void realJava_printStackTrace_chained() {
        String stackTrace = readFixture("java-chained.txt");
        assertThat(registry.select(null, stackTrace).name()).isEqualTo("java");

        List<StackFrame> frames = parse("java", stackTrace);

        // 8 outer frames + 1 per "Caused by:" section; "... N more" elisions contribute nothing
        assertThat(frames).hasSize(10);
        assertThat(frames.get(0)).isEqualTo(new StackFrame("ChainTest", "ChainTest.java", 9, "main"));
        // JDK module prefix stays in the class name; (Native Method) → line -2
        assertThat(frames.get(1)).isEqualTo(new StackFrame(
                "java.base/jdk.internal.reflect.NativeMethodAccessorImpl", "Native Method", -2, "invoke0"));
        // merged "Caused by:" frames (flat semantics), innermost last
        assertThat(frames.get(8)).isEqualTo(new StackFrame("ChainTest", "ChainTest.java", 5, "middle"));
        assertThat(frames.get(9)).isEqualTo(new StackFrame("ChainTest", "ChainTest.java", 3, "inner"));
    }

    @Test
    void realPython_formatException_chained() {
        String stackTrace = readFixture("python-chained.txt");
        assertThat(registry.select(null, stackTrace).name()).isEqualTo("python");

        List<StackFrame> frames = parse("python", stackTrace);

        // 2 frames per segment; caret marker lines (~~~^^^) and source echoes contribute nothing.
        // Normalized to innermost-first: the final raise site leads, the root-cause segment trails.
        assertThat(frames).hasSize(4);
        assertThat(frames.get(0)).isEqualTo(new StackFrame(
                "handler", "C:\\Temp\\otelapp\\handler.py", 8, "handle"));
        assertThat(frames.get(1)).isEqualTo(new StackFrame(
                "handler", "C:\\Temp\\otelapp\\handler.py", 12, "<module>"));
        assertThat(frames.get(2)).isEqualTo(new StackFrame(
                "work", "C:\\Temp\\otelapp\\services\\work.py", 2, "do_work"));
        assertThat(frames.get(3)).isEqualTo(new StackFrame(
                "handler", "C:\\Temp\\otelapp\\handler.py", 6, "handle"));
    }

    @Test
    void realNode_errorStack() {
        String stackTrace = readFixture("node-stack.txt");
        assertThat(registry.select(null, stackTrace).name()).isEqualTo("node");

        List<StackFrame> frames = parse("nodejs", stackTrace);

        assertThat(frames).hasSize(10);
        // bare frame with a Windows drive-letter path: the drive colon must not confuse file:line:col
        assertThat(frames.get(0)).isEqualTo(new StackFrame(
                "<anonymous>", "C:\\Temp\\otelapp\\server.js", 8, "<anonymous>"));
        // "(<anonymous>)" location: no line info
        assertThat(frames.get(1)).isEqualTo(new StackFrame("Array", "<anonymous>", -1, "map"));
        // line is the middle token (8), never the column (16)
        assertThat(frames.get(2)).isEqualTo(new StackFrame(
                "<anonymous>", "C:\\Temp\\otelapp\\server.js", 8, "fetchUser"));
        assertThat(frames.get(3)).isEqualTo(new StackFrame(
                "UserService", "C:\\Temp\\otelapp\\server.js", 4, "load"));
        // real V8 quirk kept as-is: "Object..js" splits on the last dot
        assertThat(frames.get(7).className()).isEqualTo("Object.");
        assertThat(frames.get(7).methodName()).isEqualTo("js");
    }

    @Test
    void realGo_debugStack() {
        String stackTrace = readFixture("go-stack.txt");
        assertThat(registry.select(null, stackTrace).name()).isEqualTo("go");

        List<StackFrame> frames = parse("go", stackTrace);

        assertThat(frames).hasSize(2);
        // a path containing a space ("Program Files") must parse; the +0x offset never survives
        assertThat(frames.get(0)).isEqualTo(new StackFrame(
                "runtime/debug", "C:/Program Files/Go/src/runtime/debug/stack.go", 26, "Stack"));
        assertThat(frames.get(1)).isEqualTo(new StackFrame(
                "main", "C:/Temp/otelapp/gosvc/main.go", 17, "main"));
    }

    // =======================================================================
    // Real captured fixtures — Linux paths (same generators run in containers:
    // CPython 3.12, Node 22, Go 1.23, .NET SDK 8.0). Java is path-independent
    // (frames carry only the file basename), so it has no Linux variant.
    // =======================================================================

    @Test
    void realPythonLinux_formatException_chained() {
        String stackTrace = readFixture("python-chained-linux.txt");
        assertThat(registry.select(null, stackTrace).name()).isEqualTo("python");

        List<StackFrame> frames = parse("python", stackTrace);

        assertThat(frames).hasSize(4);
        assertThat(frames.get(0)).isEqualTo(new StackFrame("handler", "/app/handler.py", 8, "handle"));
        assertThat(frames.get(2)).isEqualTo(new StackFrame("work", "/app/services/work.py", 2, "do_work"));
    }

    @Test
    void realNodeLinux_errorStack() {
        String stackTrace = readFixture("node-stack-linux.txt");
        assertThat(registry.select(null, stackTrace).name()).isEqualTo("node");

        List<StackFrame> frames = parse("nodejs", stackTrace);

        assertThat(frames).hasSize(10);
        // bare frame with an absolute Unix path
        assertThat(frames.get(0)).isEqualTo(new StackFrame("<anonymous>", "/app/server.js", 8, "<anonymous>"));
        assertThat(frames.get(2)).isEqualTo(new StackFrame("<anonymous>", "/app/server.js", 8, "fetchUser"));
        assertThat(frames.get(3)).isEqualTo(new StackFrame("UserService", "/app/server.js", 4, "load"));
    }

    @Test
    void realGoLinux_debugStack() {
        String stackTrace = readFixture("go-stack-linux.txt");
        assertThat(registry.select(null, stackTrace).name()).isEqualTo("go");

        List<StackFrame> frames = parse("go", stackTrace);

        assertThat(frames).hasSize(2);
        assertThat(frames.get(0)).isEqualTo(new StackFrame(
                "runtime/debug", "/usr/local/go/src/runtime/debug/stack.go", 26, "Stack"));
        assertThat(frames.get(1)).isEqualTo(new StackFrame("main", "/app/gosvc/main.go", 17, "main"));
    }

    @Test
    void realDotNetLinux_toStringChained() {
        // First real .NET capture (Exception.ToString() from the .NET 8 SDK container): the
        // " ---> Inner: msg" chain header and "--- End of inner exception stack trace ---"
        // separator are skipped (flat semantics); inner frames come first in text order.
        String stackTrace = readFixture("dotnet-chained-linux.txt");
        assertThat(registry.select(null, stackTrace).name()).isEqualTo("dotnet");

        List<StackFrame> frames = parse("dotnet", stackTrace);

        assertThat(frames).hasSize(4);
        assertThat(frames.get(0)).isEqualTo(new StackFrame("OrderService", "/app/Program.cs", 5, "Validate"));
        assertThat(frames.get(1)).isEqualTo(new StackFrame("OrderService", "/app/Program.cs", 7, "Place"));
        assertThat(frames.get(2)).isEqualTo(new StackFrame("OrderService", "/app/Program.cs", 8, "Place"));
        assertThat(frames.get(3)).isEqualTo(new StackFrame("Program", "/app/Program.cs", 13, "Main"));
    }
}
