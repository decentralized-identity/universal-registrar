# Dockerfile for universalregistrar/driver

FROM universalregistrar/base
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver

ADD . /opt/uni-registrar-java

RUN cd /opt/uni-registrar-java && mvn clean install -N -DskipTests
RUN cd /opt/uni-registrar-java/uni-registrar-core && mvn install -N -DskipTests
RUN cd /opt/uni-registrar-java/driver && mvn install -N -DskipTests
