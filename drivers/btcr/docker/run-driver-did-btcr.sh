#!/bin/sh

mkdir ${uniregistrar_driver_did_btcr_basePath}

cd /opt/driver-did-btcr/
mvn jetty:run -P war -Dorg.eclipse.jetty.annotations.maxWait=240
