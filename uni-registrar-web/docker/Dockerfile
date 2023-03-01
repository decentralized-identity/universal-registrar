# Dockerfile for universalregistrar/uni-registrar-web

FROM maven:3-openjdk-17 AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-registrar-web

ADD . /opt/universal-registrar
RUN cd /opt/universal-registrar && mvn clean install -N
RUN cd /opt/universal-registrar/uni-registrar-core && mvn clean install -N
RUN cd /opt/universal-registrar/driver && mvn clean install -N
RUN cd /opt/universal-registrar/driver-http && mvn clean install -N
RUN cd /opt/universal-registrar/uni-registrar-local && mvn clean install -N
RUN cd /opt/universal-registrar/uni-registrar-web && mvn clean install package -N

# build image

FROM openjdk:17-jdk-slim
MAINTAINER Markus Sabadello <markus@danubetech.com>

WORKDIR /opt/universal-registrar/uni-registrar-web/

COPY --from=build /opt/universal-registrar/uni-registrar-web/target/*-exec.jar ./

ENV uniregistrar_web_spring_profiles_active=default

# done

EXPOSE 9080
CMD java -jar *.jar
