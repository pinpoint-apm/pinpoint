# log4j2-core-2.12.1 pinpoint patch for CVE-2021-44228

# Patch 
1. Modify the default value of `FORMAT_MESSAGES_PATTERN_DISABLE_LOOKUPS` field in `Constants` to true
```java
org.apache.logging.log4j.core.util.Constants.FORMAT_MESSAGES_PATTERN_DISABLE_LOOKUPS=true
```
2. Remove `JndiLookup.class` from log4j2-core