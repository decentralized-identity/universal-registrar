# Dockerfile for universalregistrar/uni-registrar-web

FROM maven:3-jdk-11
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

# build uni-registrar-web

ADD ./registrar/java/ /opt/uni-registrar-java
ADD ./config.json /opt/uni-registrar-java/uni-registrar-web/

RUN cd /opt/uni-registrar-java && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-registrar-java/uni-registrar-core && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-registrar-java/driver && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-registrar-java/driver-http && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-registrar-java/uni-registrar-local && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-registrar-java/uni-registrar-web && mvn clean install package -N -DskipTests

# done

EXPOSE 9080

RUN chmod a+rx /opt/uni-registrar-java/uni-registrar-web/docker/run-uni-registrar-web.sh
CMD "/opt/uni-registrar-java/uni-registrar-web/docker/run-uni-registrar-web.sh"
