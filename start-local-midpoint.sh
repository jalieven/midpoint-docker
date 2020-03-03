#!/bin/bash

# Downloading the latest dist
#(exec "./download-midpoint")
echo "############## Deleting midPoint dist directory ##############"
rm -rf ./midpoint-dist
mkdir -p midpoint-dist/var

echo "############## Unpacking midPoint dist ##############"
# Unpacking the dist and syncing the config files
tar xzf ./midpoint-dist.tar.gz -C midpoint-dist --strip-components=1

echo "############## Syncing midPoint var directory ##############"
rsync -va ./demo/simple/midpoint_server/container_files/mp-home/ midpoint-dist/var

echo "############## Replacing midPoint var file snippets ##############"
# Replacing the hostnames and paths to localhost
current_dir=`pwd`/
sed -i 's/jdbc\:postgresql\:\/\/postgres_resource\:8432\/pgres/jdbc\:postgresql\:\/\/localhost\:8432\/pgres/' midpoint-dist/var/post-initial-objects/400-postgresql-webidm-resource.xml
sed -i "s/\<icscscriptedsql\:scriptRoots\>\/opt\/midpoint\/var\/scripts\<\/icscscriptedsql\:scriptRoots\>/\<icscscriptedsql\:scriptRoots\>${current_dir//\//\\/}midpoint-dist\/var\/scripts<\/icscscriptedsql\:scriptRoots\>/" midpoint-dist/var/post-initial-objects/400-postgresql-webidm-resource.xml
sed -i 's/\<gen67\:host\>ldap\<\/gen67\:host\>/\<gen67\:host\>localhost\<\/gen67\:host\>/' midpoint-dist/var/post-initial-objects/300-opendj-resource.xml

sed -i 's/Starting midPoint.../Starting midPoint... ($JAVA_OPTS)/'  midpoint-dist/bin/midpoint.sh
# If you do not understand what is going on in the startup script uncomment following line
# gsed -i '2s;^;set -x\n;' midpoint-dist/bin/midpoint.sh

echo "############## Copying the password files to the midPoint var directory ##############"
# Copy the keystore and password files
# TODO: generate a jceks file: keytool -genseckey -alias default -keystore keystore.jceks -storetype jceks -keyalg AES -keysize 128
# TODO: edit the keystore_password.txt with the used password in above command and chmod them for security reasons
#cp ./keystore.jceks ./midpoint-dist/var/keystore.jceks
cp ./demo/simple/configs-and-secrets/midpoint/keystore_password.txt ./midpoint-dist/var
cp ./demo/simple/configs-and-secrets/source-db/database_password.txt ./midpoint-dist/var

echo "############## Setting the JAVA_OPTS correct ##############"
# Update the JAVA_OPTS
cp ./setenv.sh ./midpoint-dist/bin/setenv.sh
chmod +x ./midpoint-dist/bin/setenv.sh

echo "############## Starting midPoint server ##############"
(exec "./midpoint-dist/bin/start.sh")
sleep 5
tail -400f ./midpoint-dist/var/log/midpoint.log
