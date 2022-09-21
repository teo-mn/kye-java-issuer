#!/bin/bash
java -Djdk.tls.client.protocols="TLSv1,TLSv1.1,TLSv1.2" -Djava.security.egd=file:/dev/urandom -jar app.jar
