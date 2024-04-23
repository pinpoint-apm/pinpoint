FROM alpine:3.7

COPY Dockerfiles/configure-agent.sh /usr/local/bin/
COPY target/pinpoint-agent-*.tar.gz /pinpoint-agent.tar.gz

RUN apk add --update curl bash \
    && chmod a+x /usr/local/bin/configure-agent.sh \
    && mkdir -p /pinpoint-agent \
    && chmod -R o+x /pinpoint-agent

RUN gunzip pinpoint-agent.tar.gz \
    && tar -xf pinpoint-agent.tar --strip 1 -C /pinpoint-agent \
    && rm pinpoint-agent.tar \
    && apk del curl \
    && rm /var/cache/apk/*

VOLUME ["/pinpoint-agent"]

ENTRYPOINT ["/usr/local/bin/configure-agent.sh"]
CMD ["tail", "-f", "/dev/null"]