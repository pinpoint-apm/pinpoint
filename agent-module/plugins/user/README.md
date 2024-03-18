## User defined classes
* Since: Pinpoint 1.6.0

### Pinpoint Configuration
pinpoint.config

#### Compatibility setting
~~~
# Specify classes and methods you want to profile here.

# Needs to be a comma separated list of fully qualified class names, or fully qualified package names with wild card class.
profiler.include=
# Ex: foo.bar.MyClass, foo.baz.*

# Needs to be a comma separated list of fully qualified method names. Wild card not supported.
profiler.entrypoint=
# Ex: foo.bar.MyClass.myMethod, foo.bar.MyClass.anotherMethod

# Message queue listener invoker methods.
# This is usually for when a separate implementation or a framework provides a separate handler for invoking callbacks
# when consuming messages.
# Comma-separated list of fully qualified method names with a Message argument.
profiler.message.queue.client.handler.methods=org.springframework.jms.listener.AbstractMessageListenerContainer.invokeListener,org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListener
~~~
