# Universal Registrar — Driver Development

## Introduction

The Universal Registrar wraps an API around a number of co-located Docker containers running DID-method-specific drivers. Additional DID methods can be supported as they are developed by the community. The contribution for a new DID method driver consists of a Docker image which exposes an HTTP interface for creating/updating/deactivating DIDs. New contributions are submitted as Pull Requests to the Universal Registrar (this) repository.

An example driver is available [here](https://github.com/peacekeeper/uni-registrar-driver-did-example).

An example PR for contributing a driver is available [here](https://github.com/decentralized-identity/universal-registrar/pull/7).

## Driver Interface

Your driver will be invoked via an HTTP POST call to:

`http://<your-image>:8080/1.0/register`

Your driver will receive a DID request object like the following in the HTTP body:

```
{
	"jobId": null,
	"options": {
		"ledger": "test",
		"keytype": "ed25519"
	},
	"secret": {},
	"didDocument": {}
}
```

It should return a DID state object like the following in the HTTP body:

```
{
	"jobId": null,
	"didState": {
		"did": "did:example:0000000000123456",
		"state": "finished",
		"secret": {
			"keys": [{
				"id": "did:example:0000000000123456#key-1",
				"type": "Ed25519VerificationKey2018",
				"privateKeyJwk": {...}
			}]
		}
	}
}
```

A Swagger API definition is available [here](https://github.com/decentralized-identity/universal-registrar/blob/master/swagger/api-driver.yml).

For more information about this interface, see the [DID Registration](https://identity.foundation/did-registration/) specification.

## Driver Rules

- Driver must be fully open-source under a permissive license (Apache 2.0 preferred).
- Driver source code may be published at DIF or anywhere else.
- Driver image must be published on [DockerHub](https://hub.docker.com/) with version tags.
- Driver image should be tested as standalone Docker container.
- Driver image should be tested as part of the Universal Registrar with `docker-compose`.

## How to

### How to contribute a driver

Contributing a driver to the Universal Registrar expands the functionality of the service as new DID methods are added and used by the community.

In order to contribute a driver to the Universal Registrar, the driver's source code must be published. You may choose to publish it at the [Decentralized Identity Foundation](https://github.com/decentralized-identity/universal-registrar/tree/master/drivers) or on another publicly available site. In addition, your Docker image must be published on [DockerHub](https://hub.docker.com/) with version tags to allow configuration of your driver. Ideally, the same DockerHub image is kept up-to-date to preserve versioning history (see below in the "How to Update a Driver" section).

In your PR, edit the following files in the Universal Registrar root directory:

- `.env`
  * list environment variables (if any) with default values
- `config.json` (add your driver)
  * Docker image name
- `docker-compose.yml` (add your driver)
  * docker image name and increment port number
  * environment variables
- `README.md` (insert a line to the driver table)
  * driver name (e.g. `did-example`), with link to driver source code
  * driver version (e.g. `0.1`)
  * DID spec version that the driver conforms to, with link to DID spec
  * DID method spec version (e.g. `0.1`), with link to DID method spec (or mark "missing")
  * Docker image name (e.g. `exampleorg/uni-registrar-driver-did-example`) with link to Docker image at DockerHub

Your driver is expected to be well-documented, tested, and working before you submit a PR. The documentation for your driver should be clear enough to explain how to run your driver, how to troubleshoot it, and a process for updating the driver over time.

### How to update a driver

As DID methods are developed and matured, the Universal Registrar should maintain its DID drivers with new changes. Contributors should keep their drivers up-to-date as changes happen to the DID Core spec and the DID method spec. Contributors may only wish to direct users to the latest driver, or they may have a `stable` version, a `developer` version, etc. The driver version specified in the `README.md` file should be reflected in a DockerHub image with a tag that matches the driver version.

In order to update a driver, simply submit a new PR that increments the Docker image version and updates the relevant files (see above in the "How to Contribute a Driver" section).

### How to test a driver locally

Once your driver is implemented and published as a docker container on DockerHub, you may want to test that it is running properly within the Universal Registrar.

To do so, follow these steps:

- Clone the Universal Registrar (this) repository:

  ```bash
  git clone https://github.com/decentralized-identity/universal-registrar
  cd universal-registrar/
  ```

- Make the required changes mentioned above ("How to contribute a driver") to the `.env`, `config.json` and `docker-compose.yml` files.

- Build uni-registrar-web locally:

  ```bash
  docker build -f ./registrar/java/uni-registrar-web/docker/Dockerfile . -t universalregistrar/uni-registrar-web
  ```

- Run the uni-registrar-web locally:

  ```bash
  docker-compose -f docker-compose.yml pull
  docker-compose -f docker-compose.yml up
  ```

You can now create/update/deactivate DID Documents via `curl` commands as documented in the [Quick Start](https://github.com/decentralized-identity/universal-registrar#quick-start) notes.

## Additional Notes

- Depending on the DID method, oftentimes DID drivers will need to read some decentralized ledger or distributed filesystem (the "target system") in order to create/update/deactivate a DID. Each driver may decide how it will communicate with its respective target system. For those drivers performing operations on DLT's, the driver may do so via web API, communicating with a remote node, running a full node, or another experimental configuration.
