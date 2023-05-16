## Pinpoint Paho MQTT Client Plugin

#### Supported libraries
The plugin has been tested on the following libraries.
```
org.eclipse.paho:org.eclipse.paho.mqttv5.client - 1.2.5
org.eclipse.paho:org.eclipse.paho.client.mqttv3 - 1.2.5, 1.1.x, 1.0.x
```

### Configuration 
To enable Paho MQTT V3 Publisher, Subscriber set the following option in *pinpoint.config*:
```
profiler.paho.mqtt.client.v3.enable=true
profiler.paho.mqtt.client.publisher.enable=true
profiler.paho.mqtt.client.subscriber.enable=true
```

To enable Paho MQTT V5 Publisher, Subscriber set the following option in *pinpoint.config*:
```
profiler.paho.mqtt.client.v5.enable=true
profiler.paho.mqtt.client.publisher.enable=true
profiler.paho.mqtt.client.subscriber.enable=true
```

### MQTT Versions

#### MQTT Version 3.1, 3.1.1

MQTT Version 3 does not support custom properties, *so  is not possible to map publication and subscription into a single transaction.*  
In this case, even if the same topic is published or subscribed, it is indicated by two transactions.

#### MQTT Version 5
  
MQTT Version 5 supports custom properties, *so publication and subscription can be mapped into a single transaction.*  
In this case, the publication, subscription of the same topic is represented by one transaction.