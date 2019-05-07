# Dockerfile for universalregistrar/driver-did-btcr

FROM universalregistrar/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build dependencies

RUN cd /opt/ && git clone https://github.com/WebOfTrustInfo/txref-conversion-java.git && \
    cd /opt/txref-conversion-java/ && mvn clean install -DskipTests && mvn clean

# build driver-did-btcr

ADD . /opt/driver-did-btcr
RUN cd /opt/driver-did-btcr && mvn clean install package -N -DskipTests

# variables

ENV uniregistrar_driver_did_btcr_peerMainnet=localhost:28333
ENV uniregistrar_driver_did_btcr_peerTestnet=localhost:38333
ENV uniregistrar_driver_did_btcr_didDocContinuation=localfile
ENV uniregistrar_driver_did_btcr_basePath=/tmp/
ENV uniregistrar_driver_did_btcr_baseUri=http://localhost/
ENV uniregistrar_driver_did_btcr_bitcoinConnection=blockcypherapi
ENV uniregistrar_driver_did_btcr_rpcUrlMainnet=http://user:pass@localhost:8332/
ENV uniregistrar_driver_did_btcr_rpcUrlTestnet=http://user:pass@localhost:18332/

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-btcr/docker/run-driver-did-btcr.sh
CMD "/opt/driver-did-btcr/docker/run-driver-did-btcr.sh"
