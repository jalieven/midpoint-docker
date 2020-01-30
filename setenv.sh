#!/bin/sh

export JAVA_OPTS="-Dfile.encoding=UTF8 -Xms2048M -Xmx4096M -Dmidpoint.home=$MIDPOINT_HOME -Dpython.cachedir=$MIDPOINT_HOME/tmp -Djavax.net.ssl.trustStore=$MIDPOINT_HOME/keystore.jceks -Djavax.net.ssl.trustStoreType=jceks -Dmidpoint.keystore.keyStorePassword_FILE=${PWD}/var/keystore_password.txt  -Dmidpoint.repository.database=postgresql -Dmidpoint.repository.jdbcUsername=midpoint -Dmidpoint.repository.jdbcPasswordFile=${PWD}/var/database_password.txt -Dmidpoint.repository.jdbcUrl=jdbc:postgresql://localhost:5432/midpoint -Dmidpoint.repository.hibernateHbm2ddl=none -Dmidpoint.repository.missingSchemaAction=create -Dmidpoint.repository.upgradeableSchemaAction=stop -Dmidpoint.repository.initializationFailTimeout=60000 -Dmidpoint.logging.alt.enabled=true"
