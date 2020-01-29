#!/bin/bash

# Downloading the latest dist
#(exec "./download-midpoint")
rm -rf ./midpoint-dist
mkdir -p midpoint-dist/var

# Unpacking the dist and syncing the config files
tar xzf ./midpoint-dist.tar.gz -C midpoint-dist --strip-components=1
rsync -va ./demo/simple/midpoint_server/container_files/mp-home/ midpoint-dist/var

# Replacing the hostnames to localhost
current_dir=`pwd`/
sed -i '' 's/jdbc\:postgresql\:\/\/postgres_resource\:8432\/pgres/jdbc\:postgresql\:\/\/localhost\:8432\/pgres/' midpoint-dist/var/post-initial-objects/300-scriptedsql-webidm-resource.xml
sed -i '' "s/\<icscscriptedsql\:scriptRoots\>\/opt\/midpoint\/var\/scripts\<\/icscscriptedsql\:scriptRoots\>/\<icscscriptedsql\:scriptRoots\>${current_dir//\//\\/}midpoint-dist\/var\/scripts<\/icscscriptedsql\:scriptRoots\>/" midpoint-dist/var/post-initial-objects/300-scriptedsql-webidm-resource.xml
sed -i '' 's/\<gen67\:host\>ldap\<\/gen67\:host\>/\<gen67\:host\>localhost\<\/gen67\:host\>/' midpoint-dist/var/post-initial-objects/400-opendj-resource.xml

(exec "./midpoint-dist/bin/start.sh")
sleep 5
tail -400f ./midpoint-dist/var/log/midpoint.log