# Dockerfile for universalregistrar/uni-registrar-web

FROM universalregistrar/base
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-registrar-web

ADD . /opt/uni-registrar-java

RUN cd /opt/uni-registrar-java && mvn clean
RUN cd /opt/uni-registrar-java && mvn install -N -DskipTests
RUN cd /opt/uni-registrar-java/uni-registrar-core && mvn install -N -DskipTests
RUN cd /opt/uni-registrar-java/driver && mvn install -N -DskipTests
RUN cd /opt/uni-registrar-java/driver-http && mvn install -N -DskipTests
RUN cd /opt/uni-registrar-java/uni-registrar-local && mvn install -N -DskipTests
RUN cd /opt/uni-registrar-java/uni-registrar-web && mvn install -N -DskipTests

# done

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

EXPOSE 8080

RUN chmod a+rx /opt/uni-registrar-java/uni-registrar-web/docker/run-uni-registrar-web.sh
CMD "/opt/uni-registrar-java/uni-registrar-web/docker/run-uni-registrar-web.sh"
