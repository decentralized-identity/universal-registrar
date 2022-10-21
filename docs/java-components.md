# Universal Registrar — Java Components

This is a Java implementation of a Universal Registrar. See [universal-registrar](https://github.com/decentralized-identity/universal-registrar/) for a general introduction to Universal Registrars and drivers.

## Build (native Java)

Maven build:

	mvn clean install

## Local Registrar

You can use a [Local Registrar](https://github.com/decentralized-identity/universal-registrar/tree/master/uni-registrar-client) in your Java project that invokes drivers locally (either directly via their JAVA API or via a Docker REST API).

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-registrar-local</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

[Example Use](https://github.com/decentralized-identity/universal-registrar/blob/master/examples/src/main/java/uniregistrar/examples/TestLocalUniRegistrar.java):

## Web Registrar

You can deploy a [Web Registrar](https://github.com/decentralized-identity/universal-registrar/tree/master/uni-registrar-web) that can be called by clients and invokes drivers locally (either directly via their JAVA API or via a Docker REST API).

See the [Example Configuration](https://github.com/decentralized-identity/universal-registrar/blob/master/uni-registrar-web/src/main/resources/application.yml).

How to run:

	mvn jetty:run

## Client Registrar

You can use a [Client Registrar](https://github.com/decentralized-identity/universal-registrar/tree/master/uni-registrar-client) in your Java project that calls a remote Web Registrar.

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-registrar-client</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

[Example Use](https://github.com/decentralized-identity/universal-registrar/blob/master/examples/src/main/java/uniregistrar/examples/TestClientUniRegistrar.java):

## About

Decentralized Identity Foundation - http://identity.foundation/
