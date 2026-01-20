FROM openjdk:8u342-slim AS builder

ARG HBASE_VERSION=2.2.6
ENV HBASE_REPOSITORY=http://apache.mirrors.pair.com/hbase
ENV HBASE_SUB_REPOSITORY=http://archive.apache.org/dist/hbase
ENV BASE_DIR=/opt/hbase

WORKDIR ${BASE_DIR}
RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && (curl -fSL "${HBASE_REPOSITORY}/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz" -o hbase.tar.gz \
        || curl -fSL "${HBASE_SUB_REPOSITORY}/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz" -o hbase.tar.gz) \
    && tar xfvz hbase.tar.gz -C ${BASE_DIR} \
    && rm hbase.tar.gz

FROM openjdk:8u342-slim

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
