FROM flink:1.18-java17

RUN apt-get update \
 && apt-get install --no-install-recommends -y zip \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/* \
 && mkdir -p /build

COPY target/pinpoint-flink-job*.jar /build/pinpoint-flink-job.jar
COPY dockerfiles/build/ /build/
COPY dockerfiles/docker-bin/ /docker-bin/
COPY dockerfiles/flink-conf.yml /opt/flink/conf/flink-conf.yaml

RUN chmod a+x /docker-bin/docker-entrypoint.sh

ENTRYPOINT ["/docker-bin/docker-entrypoint.sh"]