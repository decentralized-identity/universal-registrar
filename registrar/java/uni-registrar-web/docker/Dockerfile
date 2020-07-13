# Dockerfile for universalregistrar/uni-registrar-web

FROM maven:3-jdk-8
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

# install dependencies

RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -DrepoUrl=https://repo.maven.apache.org/maven2 -Dartifact=org.eclipse.jetty:jetty-maven-plugin:9.4.18.v20190429
RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -DrepoUrl=https://repo.maven.apache.org/maven2 -Dartifact=org.apache.maven.plugins:maven-war-plugin:3.2.2

# build dependencies

RUN cd /opt/ && git clone https://github.com/decentralized-identity/did-common-java.git && \
    cd /opt/did-common-java/ && mvn clean install -DskipTests

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
