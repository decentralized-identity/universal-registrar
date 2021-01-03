# Dockerfile for universalregistrar/uni-registrar-web

FROM maven:3-jdk-11
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-registrar-web

ADD ./registrar/java/ /opt/uni-registrar-java
ADD ./config.json /opt/uni-registrar-java/uni-registrar-web/
RUN cd /opt/uni-registrar-java && mvn clean install -N -DskipTests
RUN cd /opt/uni-registrar-java/uni-registrar-core && mvn clean install -N -DskipTests
RUN cd /opt/uni-registrar-java/driver && mvn clean install -N -DskipTests
RUN cd /opt/uni-registrar-java/driver-http && mvn clean install -N -DskipTests
RUN cd /opt/uni-registrar-java/uni-registrar-local && mvn clean install -N -DskipTests
RUN cd /opt/uni-registrar-java/uni-registrar-web && mvn clean install package -N -DskipTests

# build image

FROM openjdk:11
MAINTAINER Markus Sabadello <markus@danubetech.com>

COPY --from=0 /opt/uni-registrar-java/uni-registrar-web/target/*.war /var/lib/jetty/webapps/ROOT.war
ENV jetty.port=9080

# done

EXPOSE 9080
