# Universal Registrar — Java Components

This is a Java implementation of a Universal Registrar. See [universal-resolver](https://github.com/decentralized-identity/universal-registrar/) for a general introduction to Universal Registrars and drivers.

... TODO ...

## About

Decentralized Identity Foundation - http://identity.foundation/

## Build

Add the following instructions and a valid Personal Access Token to your Maven settings.xml file


    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                        http://maven.apache.org/xsd/settings-1.0.0.xsd">

    <activeProfiles>
        <activeProfile>github</activeProfile>
    </activeProfiles>

    <profiles>
        <profile>
        <id>github</id>
        <repositories>
            <repository>
            <id>github</id>
            <name>GitHub did-common-java</name>
            <url>https://github.com/decentralized-identity/did-common-java</url>
            </repository>
        </repositories>
        </profile>
    </profiles>

    <servers>
        <server>
        <id>github</id>
        <!-- Personal access token with permission: read:packages -->
        <username>OWNER</username>
        <password>TOKEN</password>
        </server>
    </servers>
    </settings>

