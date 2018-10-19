# Dockerfile for universalregistrar/driver-did-sov

FROM universalregistrar/driver
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build dependencies

RUN apt-get install -y --no-install-recommends curl build-essential pkg-config cmake libssl-dev libsqlite3-dev libsodium-dev libzmq3-dev

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y

RUN cd /opt/ && git clone https://github.com/hyperledger/indy-sdk.git && cd indy-sdk && git checkout v1.6.2
RUN cd /opt/indy-sdk/libindy/ && ~/.cargo/bin/cargo clean && ~/.cargo/bin/cargo build
RUN cd /opt/indy-sdk/wrappers/java/ && mvn clean install -DskipTests

# build driver-did-sov

ADD . /opt/driver-did-sov
RUN cd /opt/driver-did-sov && mvn install package -N -DskipTests

RUN cp /opt/indy-sdk/libindy/target/debug/libindy.so /opt/driver-did-sov/sovrin/lib/

# clean up

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# variables

ENV uniregistrar_driver_did_sov_libIndyPath=./sovrin/lib/
ENV uniregistrar_driver_did_sov_poolConfigs=_;./sovrin/live.txn;stn;./sovrin/stn.txn;danube;./sovrin/11347-05.txn
ENV uniregistrar_driver_did_sov_poolVersions=_;1;stn;2;danube;2
ENV uniregistrar_driver_did_sov_walletName=default
ENV uniregistrar_driver_did_sov_trustAnchorSeed=

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-sov/docker/run-driver-did-sov.sh
CMD "/opt/driver-did-sov/docker/run-driver-did-sov.sh"
