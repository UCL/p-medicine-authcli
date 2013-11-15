P-Medicine authcli
==================

This is a simple CLI authentication utility for the P-medicine
security framework. It can be used to obtain SAML authentication
tokens for different services.

Building authcli
----------------

To build authcli you will need Custodix version of STSClient
library. If you have an account on https://pauli.chem.ucl.ac.uk/nexus/
it will be fetched automatically by maven during build. Otherwise
please modify pom.xml to add your local repository containing
STSClient library, or build manually. To build using maven run:

    mvn package

Using authcli
-------------

Usage:

    java -jar authcli-1.0-SNAPSHOT.jar [options]

Options:

* `-h,--help`             Show help text
* `-o,--output <arg>`     output file name (default: print to stdout)
* `-p,--password <arg>`   password
* `-s,--service <arg>`    service URL (default: http://localhost:8080/carafe/)
* `-S,--server <arg>`     authentication server to use (default: https://dev-pmed-idp-vm.custodix.com/sts2/services/STS)
* `-u,--user <arg>`       user name

