# Universal Registrar — API Documentation

Similar to the [Universal Resolver](https://github.com/decentralized-identity/universal-resolver), the Universal Registrar also makes use of "drivers" to allow registration of various types of identifiers (e.g. different DID methods, or naming systems such as BNS or ENS).

## API

Each driver exposes two API calls. They may be invoked in various ways, e.g. through a locally linked API, a command line interface, or remotely via a web-based REST interface.

Both API calls return a *registration state* (see below).

### register()

`register(identifier, credentials, ddo, options)`

This API is called exactly once to initiate a *registration job*.

 * `identifier` (may be null): The identifier to be registered. For some drivers, this may be required to be NULL, since the identifier that will be registered may only be determined later in the registration process. For example, in the `btcr` DID method, the identifier only becomes known at a point when a transaction on the Bitcoin blockchain has been placed and confirmed.
 * `credentials` (may be null): The credentials used to "own" / "control" the identifier (TODO: align with correct language of the DID/DDO specifications). Some drivers may allow or require credentials to be created by the user outside this API, while some drivers may allow or require credentials to be created by the driver itself.
 * `ddo` (may be null): DDO elements such as service endpoints and public keys to be registered with the identifier.
 * `options` (may be null): Various driver-specific options.

### checkRegistration()

`checkRegistration(registrationJobId)`

This API may be called several times to check the current status of a registration job.

 * `registrationJobId` (required): The ID of the registration job to check.

## Registration state

Both API calls return a registration state object that describes the current state of the registration job. Four basic states are possible:

 * `action`: Additional action external to the Universal Registrar's functionality is required. E.g. this could mean that funds need to be transferred to a wallet, or a trust anchor must be contacted, in order to continue the registration job.
 * `wait`: No action is currently required, but the registration requires more time to pass.
 * `finished` The registration job is complete, and the identifier has been registered.
 * `failure`: The registration job failed.

Depending on the driver, various simple or more complex combinations of `action` and `wait` states are possible. An extremely simple driver may return a `finished` state immediately after calling `register()`, therefore not requiring any additional action or time to pass. A complex driver may require a longer sequence of different `action` and `wait` states.

Drivers MUST document in detail which states their respective registration logic require.

All registration state objects contain the current state as well as the ID of the registration job:

	{
	  "state": "action",
	  "jobid": "aa116280-5401-4678-931a-499cc8714b6e"
	}

### action

An `action` state object SHOULD indicate the type of action that is required. It MAY indicate details on the action that is required.

An `action` state object for the `sov` DID method may look like this:

	{
	  "state": "action",
	  "jobid": "aa116280-5401-4678-931a-499cc8714b6e",
	  "action": "trustanchorrequired",
	  "did": "BrYDA5NubejDVHkCYBbpY5",
	  "verkey": "~4qKbF4VjuiHFiSFcgfnpHE",
	  "trustanchorurl":"https://trustanchor.danubetech.com/?did=3QJmV3qfvL9SuYo34YihAf3sRCW3qSinyC&verkey=~4qKbF4VjuiHFiSFcgfnpHE"
	}

This indicates that a trust anchor's website must be contacted in order to continue the registration job.

An `action` state object for the `btcr` DID method may look like this:

	{
	  "state": "action",
	  "jobid": "aa116280-5401-4678-931a-499cc8714b6e",
	  "action": "fundingrequired",
	  "bitcoinaddress": "3QJmV3qfvL9SuYo34YihAf3sRCW3qSinyC",
	  "satoshis": "13250"
	}

This indicates that a a certain amount of Bitcoin funding must be sent to a certain Bitcoin wallet address in order to register the identifier.

### wait

A `wait` state object SHOULD indicate the reason why more time is required to pass. It MAY indicate details on how long one is expected to wait, e.g. a certain amount of time, or for a certain event to occur.

	{
	  "state": "wait",
	  "jobid": "aa116280-5401-4678-931a-499cc8714b6e",
	  "wait": "confirmingtransaction",
	  "waittime": "3600000"
	}

### finished

A `finished` state object MAY indicate the identifier that has been registered. It MUST indicate this identifier if it was determined/chosen by the driver itself, rather than provided in the initial `register()` API call.

A `finished` state object MAY indicate the credentials used to "own" / "control" the identifier (TODO: align with correct language of the DID/DDO specifications). It MUST indicate these credentials if they were determined/chosen by the driver itself, rather than provided in the initial `register()` API call.

	{
	  "state": "finished",
	  "jobid": "aa116280-5401-4678-931a-499cc8714b6e",
	  "identifier": "did:sov:WRfXPg8dantKVubE3HX8pw"
	}

### failure

A `failure` state object SHOULD indicate the reason for failing to register the identifier (e.g. the identifier is invalid or unavailable).

	{ “state”: “failed”,
	  “jobid”: “aa116280”,
	  “reason”: “identifier_unavailable”
	}

## Architecture

![System Architecture](/docs/figures/architecture.png)
