# DESCRIPTION       Pinpoint APM Collector
# TO_BUILD          docker build -t pinpoint-collector .
# TO_RUN            docker run -d --net=host -e HBASE_HOST=<HOST_IP> -e HBASE_PORT=2181 -e COLLECTOR_TCP_PORT=9994 -e COLLECTOR_UDP_STAT_LISTEN_PORT=9995 -e COLLECTOR_UDP_SPAN_LISTEN_PORT=9996 -p 9994:9994 -p 9995:9995/udp -p 9996:9996/udp --name=pinpoint-collector pinpoint-collector

FROM tomcat:8-jre8
MAINTAINER John Crygier <john.crygier@ventivtech.com>

ENV PINPOINT_VERSION 1.0.4

RUN curl -SL "https://github.com/naver/pinpoint/releases/download/$PINPOINT_VERSION/pinpoint-collector-$PINPOINT_VERSION.war" -o pinpoint-collector.war && \
    rm -rf /usr/local/tomcat/webapps && \
    mkdir -p /usr/local/tomcat/webapps && \
    unzip pinpoint-collector.war -d /usr/local/tomcat/webapps/ROOT && \
    rm -rf pinpoint-collector.war

ADD start.sh /usr/local/tomcat/start.sh
RUN chmod a+x /usr/local/tomcat/start.sh

ADD hbase.properties /assets/hbase.properties
ADD pinpoint-collector.properties /assets/pinpoint-collector.properties

CMD ["/usr/local/tomcat/start.sh"]
