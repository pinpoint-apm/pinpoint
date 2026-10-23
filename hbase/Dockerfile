# HBase 2.2.6 runs on Java 8 only (hbase-env.sh also sets Java 8 GC options), so the base stays a Java 8 JRE.
# The official openjdk images are deprecated and openjdk:8u342-slim has not been updated since 2022;
# Eclipse Temurin is maintained (Java 8 until at least 2030) and is what Jib uses for the other images.
# The tag is pinned to a patch release on Ubuntu 24.04 LTS (noble); move it on purpose when updating.
FROM eclipse-temurin:8u504-b01-jre-noble AS builder

ARG HBASE_VERSION=2.2.6
ENV HBASE_REPOSITORY=https://archive.apache.org/dist/hbase
ENV BASE_DIR=/opt/hbase

WORKDIR ${BASE_DIR}
# Fetch the tarball with ADD: no package needs to be installed to build the image.
ADD ${HBASE_REPOSITORY}/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz hbase.tar.gz
RUN tar xfz hbase.tar.gz -C ${BASE_DIR} \
    && rm hbase.tar.gz

FROM eclipse-temurin:8u504-b01-jre-noble

ARG HBASE_VERSION=2.2.6
ENV BASE_DIR=/opt/hbase
ENV HBASE_HOME=${BASE_DIR}/hbase-${HBASE_VERSION}

WORKDIR ${BASE_DIR}
COPY --from=builder ${BASE_DIR}/hbase-${HBASE_VERSION} ${BASE_DIR}/hbase-${HBASE_VERSION}
COPY scripts/hbase-create.hbase ${BASE_DIR}/hbase-create.hbase
COPY hbase-docker/configure-hbase.sh hbase-docker/check-table.sh hbase-docker/initialize-hbase.sh /usr/local/bin/
COPY hbase-docker/hbase-site.xml ${HBASE_HOME}/conf/hbase-site.xml
COPY hbase-docker/hbase-env.sh ${HBASE_HOME}/conf/hbase-env.sh
RUN chmod a+x /usr/local/bin/*.sh

VOLUME ["/home/pinpoint/hbase", "/home/pinpoint/zookeeper"]

CMD /usr/local/bin/initialize-hbase.sh && tail -f /dev/null
