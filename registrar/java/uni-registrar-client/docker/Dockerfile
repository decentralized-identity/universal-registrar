# Dockerfile for universalregistrar/uni-registrar-client

FROM maven:3-jdk-8
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

# build dependencies

RUN cd /opt/ && git clone https://github.com/decentralized-identity/did-common-java.git
RUN cd /opt/did-common-java/ && mvn clean install -DskipTests

# build uni-registrar-client

ADD ./registrar/java/ /opt/uni-registrar-java

RUN cd /opt/uni-registrar-java && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-registrar-java/uni-registrar-core && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-registrar-java/uni-registrar-client && mvn clean install -N -DskipTests && mvn clean
