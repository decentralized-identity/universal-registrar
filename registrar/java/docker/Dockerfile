# Dockerfile for universalregistrar/uni-registrar-web

FROM universalregistrar/universal-wallet-client as universal-wallet-client
FROM universalregistrar/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# copy from dependent images

COPY --from=universal-wallet-client /root/.m2/repository/com/danubetech/universal-wallet-client /root/.m2/repository/com/danubetech/universal-wallet-client
COPY --from=universal-wallet-client /root/.m2/repository/com/danubetech/universal-wallet-client-swagger /root/.m2/repository/com/danubetech/universal-wallet-client-swagger

# build uni-registrar-web

ADD . /opt/uni-registrar-java

RUN cd /opt/uni-registrar-java && mvn clean && \
    cd /opt/uni-registrar-java && mvn install -N -DskipTests && \
    cd /opt/uni-registrar-java/uni-registrar-core && mvn install -N -DskipTests && \
    cd /opt/uni-registrar-java/driver && mvn install -N -DskipTests && \
    cd /opt/uni-registrar-java/driver-http && mvn install -N -DskipTests && \
    cd /opt/uni-registrar-java/uni-registrar-local && mvn install -N -DskipTests && \
    cd /opt/uni-registrar-java/uni-registrar-web && mvn install -N -DskipTests

# variables

ENV uniregistrar_universalWalletBase=http://universal-wallet:12080/universal-registrar/universal-wallet/1.0.0

# done

EXPOSE 9080

RUN chmod a+rx /opt/uni-registrar-java/docker/run-uni-registrar-web.sh
CMD "/opt/uni-registrar-java/docker/run-uni-registrar-web.sh"
