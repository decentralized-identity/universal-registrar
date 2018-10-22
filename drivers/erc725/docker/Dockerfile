# Dockerfile for universalregistrar/driver-did-erc725

FROM universalregistrar/driver
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build dependencies

RUN apt-get install -y --no-install-recommends curl software-properties-common gpg make build-essential python

RUN curl -sL https://deb.nodesource.com/setup_8.x | bash -
RUN apt-get install -y --no-install-recommends nodejs
#RUN cd /opt/ && git clone https://github.com/digitalbazaar/did-cli.git && cd did-cli && git checkout v0.3.1 && sed -i 's/veres-one\/did-veres-one#implementation/veres-one\/did-veres-one#master/g' package.json && npm install .

# build driver-did-erc725

ADD . /opt/driver-did-erc725
RUN chmod a+x /opt/driver-did-erc725/did-client/did
RUN cd /opt/driver-did-erc725 && mvn install package -N -DskipTests

# clean up

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# variables

ENV uniregistrar_driver_did_erc725_trustAnchorSeed=

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-erc725/docker/run-driver-did-erc725.sh
CMD "/opt/driver-did-erc725/docker/run-driver-did-erc725.sh"
