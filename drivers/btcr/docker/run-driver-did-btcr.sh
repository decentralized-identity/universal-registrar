#!/bin/sh

cd /opt/driver-did-btcr/
mvn jetty:run -P war -Dorg.eclipse.jetty.annotations.maxWait=240
