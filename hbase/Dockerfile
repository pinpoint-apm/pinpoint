FROM openjdk:8u342-slim

ARG HBASE_VERSION=${HBASE_VERSION:-2.2.6}

ENV HBASE_REPOSITORY=http://apache.mirrors.pair.com/hbase
ENV HBASE_SUB_REPOSITORY=http://archive.apache.org/dist/hbase

ENV BASE_DIR=/opt/hbase
ENV HBASE_HOME=${BASE_DIR}/hbase-${HBASE_VERSION}

COPY scripts/hbase-create.hbase ${BASE_DIR}/hbase-create.hbase
COPY hbase-docker/Dockerfiles/configure-hbase.sh /usr/local/bin/configure-hbase.sh
COPY hbase-docker/Dockerfiles/check-table.sh /usr/local/bin/check-table.sh
COPY hbase-docker/Dockerfiles/initialize-hbase.sh /usr/local/bin/initialize-hbase.sh

RUN apt-get update && apt-get install --no-install-recommends -y curl && apt-get clean \
    && chmod a+x /usr/local/bin/initialize-hbase.sh \
    && chmod a+x /usr/local/bin/check-table.sh \
    && chmod a+x /usr/local/bin/configure-hbase.sh

RUN mkdir -p ${BASE_DIR} \
    && curl -fSL "${HBASE_REPOSITORY}/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz" -o hbase.tar.gz || curl -fSL "${HBASE_SUB_REPOSITORY}/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz" -o ${BASE_DIR}/hbase.tar.gz \
    && tar xfvz ${BASE_DIR}/hbase.tar.gz -C ${BASE_DIR} \
    && rm ${BASE_DIR}/hbase.tar.gz

COPY hbase-docker/Dockerfiles/hbase-site.xml ${HBASE_HOME}/conf/hbase-site.xml
COPY hbase-docker/Dockerfiles/hbase-env.sh ${HBASE_HOME}/conf/hbase-env.sh

VOLUME ["/home/pinpoint/hbase", "/home/pinpoint/zookeeper"]

CMD /usr/local/bin/initialize-hbase.sh && tail -f /dev/null
