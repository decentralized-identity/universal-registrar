# Dockerfile for universalregistrar/driver-did-btcr

FROM universalregistrar/driver
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build dependencies

RUN cd /opt/ && git clone https://github.com/WebOfTrustInfo/txref-conversion-java.git
RUN cd /opt/txref-conversion-java/ && mvn clean install -DskipTests

# build driver-did-btcr

ADD . /opt/driver-did-btcr
RUN cd /opt/driver-did-btcr && mvn install package -N -DskipTests

# clean up

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# variables

ENV uniregistrar_driver_did_btcr_peerMainnet=localhost:28333
ENV uniregistrar_driver_did_btcr_peerTestnet=localhost:38333
ENV uniregistrar_driver_did_btcr_didDocContinuation=localfile
ENV uniregistrar_driver_did_btcr_basePath=/tmp/
ENV uniregistrar_driver_did_btcr_baseUri=http://localhost/

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-btcr/docker/run-driver-did-btcr.sh
CMD "/opt/driver-did-btcr/docker/run-driver-did-btcr.sh"
