# Dockerfile for universalregistrar/uni-registrar-web

FROM maven:3-jdk-11 AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-registrar-web

ADD . /opt/universal-registrar
ADD config.json /opt/universal-registrar/uni-registrar-web/
RUN cd /opt/universal-registrar && mvn clean install -N
RUN cd /opt/universal-registrar/uni-registrar-core && mvn clean install -N
RUN cd /opt/universal-registrar/driver && mvn clean install -N
RUN cd /opt/universal-registrar/driver-http && mvn clean install -N
RUN cd /opt/universal-registrar/uni-registrar-local && mvn clean install -N
RUN cd /opt/universal-registrar/uni-registrar-web && mvn clean install package -N

# build image

FROM jetty:9.4-jre11
MAINTAINER Markus Sabadello <markus@danubetech.com>

COPY --from=build /opt/universal-registrar/uni-registrar-web/target/*.war /var/lib/jetty/webapps/ROOT.war
COPY --from=build /opt/universal-registrar/uni-registrar-web/config.json /var/lib/jetty/

# done

EXPOSE 9080
CMD java -Djetty.http.port=9080 -jar /usr/local/jetty/start.jar
