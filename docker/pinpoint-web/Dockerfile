# DESCRIPTION       Pinpoint APM Web (User Interface)
# TO_BUILD          docker build -t pinpoint-web .
# TO_RUN            docker run -d -p 8080:8080 -e HBASE_HOST=192.168.59.103 -e HBASE_PORT=2181 --name=pinpoint-web pinpoint-web

FROM tomcat:8-jre8
MAINTAINER John Crygier <john.crygier@ventivtech.com>

ENV PINPOINT_VERSION 1.0.4

RUN curl -SL "https://github.com/naver/pinpoint/releases/download/$PINPOINT_VERSION/pinpoint-web-$PINPOINT_VERSION.war" -o pinpoint-web.war && \
    rm -rf /usr/local/tomcat/webapps && \
    mkdir -p /usr/local/tomcat/webapps && \
    unzip pinpoint-web.war -d /usr/local/tomcat/webapps/ROOT && \
    rm -rf pinpoint-web.war

ADD start.sh /usr/local/tomcat/start.sh
RUN chmod a+x /usr/local/tomcat/start.sh

ADD hbase.properties /assets/hbase.properties
ADD pinpoint-web.properties /assets/pinpoint-web.properties

CMD ["/usr/local/tomcat/start.sh"]
