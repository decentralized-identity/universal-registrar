![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-registrar/master/docs/logo-dif.png)

# Universal Registrar

A Universal Registrar is an identifier registrar that works with any decentralized identifier system, including Decentralized Identifiers (DIDs).

See https://uniregistrar.io/ for a publicly hosted instance of a Universal Registrar.

## Quick Start

You can deploy the Universal Registrar on your local machine by cloning this Github repository, and using `docker-compose` to build and run the Universal Registrar as well as its drivers:

	git clone https://github.com/decentralized-identity/universal-registrar
	cd universal-registrar/
	docker-compose -f docker-compose.yml pull
	docker-compose -f docker-compose.yml up

You should then be able to create/update/deactivate identifiers locally using simple `curl` requests as follows:

	curl -d '{"options":{"ledger":"test","keytype": "ed25519"}}' -X POST 'http://localhost:9080/1.0/register?driver-universalregistrar%2Fdriver-did-v1'
	curl -d '{"options":{"network":"builder"}}' -X POST 'http://localhost:9080/1.0/register?driver-universalregistrar%2Fdriver-did-sov'
	curl -d '{"options":{"chain":"TESTNET"}}' -X POST 'http://localhost:9080/1.0/register?driver-universalregistrar%2Fdriver-did-btcr'

If this doesn't work, see [Troubleshooting](/docs/troubleshooting.md).

## Drivers

Are you developing a DID method and Universal Registrar driver? Click [Driver Development](/docs/driver-development.md) for instructions.

| Driver Name | Driver Version | DID Spec Version | DID Method Spec Version | Docker Image |
| ----------- | -------------- | ---------------- | ----------------------- | ------------ |
| [did-btcr](https://github.com/decentralized-identity/uni-registrar-driver-did-btcr/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://w3c-ccg.github.io/didm-btcr) | [universalregistrar/driver-did-btcr](https://hub.docker.com/r/universalregistrar/driver-did-btcr/)
| [did-sov](https://github.com/decentralized-identity/uni-registrar-driver-did-sov/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://sovrin-foundation.github.io/sovrin/spec/did-method-spec-template.html) | [universalregistrar/driver-did-sov](https://hub.docker.com/r/universalregistrar/driver-did-sov/)
| [did-v1](https://github.com/decentralized-identity/uni-registrar-driver-did-v1/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://w3c-ccg.github.io/did-method-v1/) | [universalregistrar/driver-did-v1](https://hub.docker.com/r/universalregistrar/driver-did-v1/)

## More Information

 * [Driver Development](/docs/driver-development.md)
 * [API Documentation](/docs/api-documentation.md)
 * [Universal DID Operations](/docs/Universal-DID-Operations.md)
 * [Troubleshooting](/docs/troubleshooting.md)
 * [Java Components](/registrar/java)

## About

<img align="left" src="https://raw.githubusercontent.com/decentralized-identity/universal-registrar/master/docs/logo-dif.png" width="115">

Decentralized Identity Foundation - http://identity.foundation/

<br clear="left" />

<img align="left" src="https://raw.githubusercontent.com/decentralized-identity/universal-registrar/master/docs/logo-ngi0pet.png" width="115">

Supported by [NLnet](https://nlnet.nl/) and [NGI0 PET](https://nlnet.nl/PET/#NGI), which is made possible with financial support from the European Commission's [Next Generation Internet](https://ngi.eu/) programme.
