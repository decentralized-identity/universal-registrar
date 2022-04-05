![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-registrar/master/docs/logo-dif.png)

# Universal Registrar

The Universal Registrar creates/updates/deactivates Decentralized Identifiers (DIDs) across many different DID methods, based on the [W3C DID Core 1.0](https://www.w3.org/TR/did-core/) and [DID Registration](https://identity.foundation/did-registration/) specifications.

See https://uniregistrar.io/ for a publicly hosted instance of a Universal Registrar. See [Docker Hub](https://hub.docker.com/u/universalregistrar) for images.

## Quick Start

You can deploy the Universal Registrar on your local machine by cloning this Github repository, and using `docker-compose` to build and run the Universal Registrar as well as its drivers:

	git clone https://github.com/decentralized-identity/universal-registrar
	cd universal-registrar/
	docker-compose -f docker-compose.yml pull
	docker-compose -f docker-compose.yml up

You should then be able to create/update/deactivate identifiers locally using simple `curl` requests as follows:

	curl -X POST 'http://localhost:9080/1.0/create?method=v1' -d '{"options":{"ledger":"test","keytype": "ed25519"}}'
	curl -X POST 'http://localhost:9080/1.0/create?method=sov' -d '{"options":{"network":"danube"}}'
	curl -X POST 'http://localhost:9080/1.0/create?method=btcr' -d '{"options":{"chain":"TESTNET"}}'
	curl -X POST 'http://localhost:9080/1.0/create?method=key' -d '{"options":{"keyType": "Ed25519VerificationKey2018"}}'
	curl -X POST 'http://localhost:9080/1.0/create?method=ion' -d '{}'
	curl -X POST 'http://localhost:9080/1.0/create?method=web' -d '{}'
	curl -X POST 'http://localhost:9080/1.0/create?method=ebsi' -d '{"secret":{"token":"ey......"}}'
	curl -X POST 'http://localhost:9080/1.0/create?method=oyd' -d '{"didDocument":{"@context":"https://www.w3.org/ns/did/v1","authentication":[]}}'

If this doesn't work, see [Troubleshooting](/docs/troubleshooting.md).

Note that there is also a [Universal Registrar frontend](https://github.com/decentralized-identity/universal-registrar-frontend/) that can optionally be installed separately.

## Drivers

Are you developing a DID method and Universal Registrar driver? Click [Driver Development](/docs/driver-development.md) for instructions.

| Driver Name | Driver Version | DID Method Spec Version | Docker Image or URL |
| ----------- | -------------- | ----------------------- | ------------------- |
| [did-btcr](https://github.com/decentralized-identity/uni-registrar-driver-did-btcr/) | 0.1-SNAPSHOT | [0.1](https://w3c-ccg.github.io/didm-btcr) | [universalregistrar/driver-did-btcr](https://hub.docker.com/r/universalregistrar/driver-did-btcr/)
| [did-sov](https://github.com/decentralized-identity/uni-registrar-driver-did-sov/) | 0.1-SNAPSHOT | [0.1](https://sovrin-foundation.github.io/sovrin/spec/did-method-spec-template.html) | [universalregistrar/driver-did-sov](https://hub.docker.com/r/universalregistrar/driver-did-sov/)
| [did-v1](https://github.com/decentralized-identity/uni-registrar-driver-did-v1/) | 0.1-SNAPSHOT | [0.1](https://w3c-ccg.github.io/did-method-v1/) | [universalregistrar/driver-did-v1](https://hub.docker.com/r/universalregistrar/driver-did-v1/)
| [did-key](https://github.com/decentralized-identity/uni-registrar-driver-did-key/) | 1.0.0 | [0.7](https://w3c-ccg.github.io/did-method-key/) | [universalregistrar/driver-did-key](https://hub.docker.com/r/universalregistrar/driver-did-key/)
| [did-ion](https://github.com/decentralized-identity/uni-registrar-driver-did-ion/) | 1.0.0 | [0.0](https://github.com/decentralized-identity/ion-did-method) | [universalregistrar/driver-did-ion](https://hub.docker.com/r/universalregistrar/driver-did-ion/)
| [did-web](https://github.com/decentralized-identity/uni-registrar-driver-did-web/) | 1.0.0 | [0.0](https://w3c-ccg.github.io/did-method-web/) | [universalregistrar/driver-did-web](https://hub.docker.com/r/universalregistrar/driver-did-web/)
| [did-ebsi](https://github.com/danubetech/uni-registrar-driver-did-ebsi/) | 1.0.0 | (missing) | [universalregistrar/driver-did-ebsi](https://hub.docker.com/r/universalregistrar/driver-did-ebsi/)
| [did-oyd](https://github.com/OwnYourData/oydid/tree/main/uni-registrar-driver-did-oyd) | 0.4.7 | [0.4](https://ownyourdata.github.io/oydid/) | [oydeu/oydid-registrar](https://hub.docker.com/r/oydeu/oydid-registrar/)

## More Information

 * [Driver Development](/docs/driver-development.md)
 * [Troubleshooting](/docs/troubleshooting.md)
* [Java Components](/docs/java-components.md)

## About

<img align="left" src="https://raw.githubusercontent.com/decentralized-identity/universal-registrar/master/docs/logo-dif.png" width="115">

Decentralized Identity Foundation - https://identity.foundation/

<br clear="left" />

<img align="left" src="https://raw.githubusercontent.com/decentralized-identity/universal-registrar/master/docs/logo-ngi0pet.png" width="115">

Supported by [NLnet](https://nlnet.nl/) and [NGI0 PET](https://nlnet.nl/PET/#NGI), which is made possible with financial support from the European Commission's [Next Generation Internet](https://ngi.eu/) programme.
