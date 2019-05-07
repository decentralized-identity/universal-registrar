# Dockerfile for universalregistrar/driver-did-v1

FROM universalregistrar/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build dependencies

RUN apt-get install -y --no-install-recommends curl software-properties-common gpg make build-essential python

RUN curl -sL https://deb.nodesource.com/setup_8.x | bash -
RUN apt-get install -y --no-install-recommends nodejs
#RUN cd /opt/ && git clone https://github.com/digitalbazaar/did-cli.git && cd did-cli && git checkout v0.3.1 && sed -i 's/veres-one\/did-veres-one#implementation/veres-one\/did-veres-one#master/g' package.json && npm install .

# build driver-did-v1

ADD . /opt/driver-did-v1
RUN chmod a+x /opt/driver-did-v1/did-client/did
RUN cd /opt/driver-did-v1 && mvn install package -N -DskipTests

# variables

ENV uniregistrar_driver_did_v1_trustAnchorSeed=

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-v1/docker/run-driver-did-v1.sh
CMD "/opt/driver-did-v1/docker/run-driver-did-v1.sh"
