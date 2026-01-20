FROM alpine:3.19 AS builder

COPY target/pinpoint-agent-*.tar.gz /pinpoint-agent.tar.gz

RUN mkdir -p /pinpoint-agent && \
    tar -xzf /pinpoint-agent.tar.gz --strip 1 -C /pinpoint-agent && \
    rm /pinpoint-agent.tar.gz

FROM alpine:3.19

COPY Dockerfiles/configure-agent.sh /usr/local/bin/
COPY --from=builder /pinpoint-agent /pinpoint-agent

RUN chmod a+x /usr/local/bin/configure-agent.sh && \
    apk add --no-cache bash

VOLUME ["/pinpoint-agent"]

ENTRYPOINT ["/usr/local/bin/configure-agent.sh"]
CMD ["tail", "-f", "/dev/null"]
