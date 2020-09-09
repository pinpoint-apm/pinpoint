##Prerequisites
Update your jdk 8 to [8u272](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot)

## Install
```
$ mvnw -P pinpoint-profiler-container-metric-testweb install -Dmaven.test.skip=true
```

## Run
```
$ docker build -t container-profiler-testweb .
$ docker run -v $HOME_DIRECTORY:/home -p 18080:18080 container-profiler-testweb
```
Mounting home directory to reduce size of image  
Use --network=host option in linux machine instead of -p option and collector ip option in Dockerfile  
Use --memory option to set memory limit  
You can then access here: http://localhost:18080/

## Stop
```
$ docker container ls
$ docker container stop $CONTAINER_NAME
```