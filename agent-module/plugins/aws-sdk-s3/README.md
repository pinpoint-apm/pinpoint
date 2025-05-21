## AWS SDK S3
* Since: Pinpoint 3.1.0
* See: https://github.com/aws/aws-sdk-java-v2/

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
# Amazon SDK v2 S3
profiler.aws.sdk.s3.enable=true
# Allow profiling status code value.
profiler.aws.sdk.s3.statuscode=true
# Mark error
profiler.aws.sdk.s3.mark.error=true
# Cannot be used with the Apache HttpClient 4.x library.
# Use the -Dprofiler.apache.httpclient4.enable=false setting.
~~~

### Trace

