package com.navercorp.pinpoint.plugin.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * @author Jiaqi Feng
 */
public class SayHelloCommand extends HystrixCommand<String> {
    private final String _name;

    public SayHelloCommand(String name)
    {
        //builder for HystrixCommand, a simple way and a complex way
        //super(HystrixCommandGroupKey.Factory.asKey("HelloService"));

        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloServiceGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().
                        withExecutionIsolationThreadTimeoutInMilliseconds(500)));

        _name = new String(name);

    }

    @Override
    protected String run() {
        return String.format("Hello %s!", _name);
    }
}
