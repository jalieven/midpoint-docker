# midPoint: the Identity Governance and Administration tool
## Info
MidPoint is open identity & organization management and governance platform which uses Identity Connector Framework (ConnId) and leverages Spring framework. It is a Java application deployed as a stand-alone server process. This image is based on official Ubuntu 18.04 image and deploys latest MidPoint version.

## Tags:
- `latest`[(midpoint/Dockerfile)](https://github.com/Evolveum/midpoint-docker)
- `4.0.1`[(midpoint/Dockerfile)](https://github.com/Evolveum/midpoint-docker/tree/4.0.1)
- `4.0`[(midpoint/Dockerfile)](https://github.com/Evolveum/midpoint-docker/tree/4.0)
- `3.9`[(midpoint/Dockerfile)](https://github.com/Evolveum/midpoint-docker/tree/3.9)
- `3.8`[(midpoint/Dockerfile)](https://github.com/Evolveum/midpoint-docker/tree/3.8)
- `3.7.1`[(midpoint/Dockerfile)](https://github.com/Evolveum/midpoint-docker/tree/3.7.1)

## Download image:
- download image without building:
```
$ docker pull evolveum/midpoint
```

## Build from git repository  
- clone git repository:
```
$ git clone https://github.com/Evolveum/midpoint-docker.git
$ cd midpoint-docker
```
- build:
```
$ docker build -t evolveum/midpoint ./
```
- or
```
$ ./build.sh
```

## Starting midPoint

Or if you are in a lazy mood: go to root of this project and just go

```
$ make restart
```

Every time you run this command if will clear all things (in the resources) and reset the whole midPoint server.
See the Makefile in the root of this project for more easy docker manipulating commands.

## Checks after startup

To see which object xml files that were successfully imported at startup perform these commands
and verify that these files have a .done extension:

```
make check_midpoint
```

## Start the import tasks

Since all tasks are suspended (import from PostgreSQL is not immediately triggered) you must start the account import task with:

```
make resume_import_task
```

This will first import the accounts and then the entitlements after which a reconciliation of the accounts is triggered.

## Access MidPoint:
- URL: http://127.0.0.1:8080/midpoint
- username: Administrator
- password: 5ecr3t

## To Remote Debug MidPoint

Pre-requisities:

    1. Java (JDK) 11
    2. Apache Maven 3

```
git checkout git@github.com:Evolveum/midpoint.git
git checkout tags/v4.0.1 -b my-4.0.1
cd midpoint
```

Then to "fast" build:
```
mvn install -DskipTests=true
```

In IntelliJ IDEA open the git@github.com:Evolveum/midpoint.git project
And create a Remote debug configuration that points to localhost:8000
Happy de-🐞ging!

## If you want to "fix" something in the midPoint source and run the configuration with that code

Build the midpoint project (latest) with your fix applied and place the midpoint-dist.tar.gz in the root of this project
(which can be found here: {midpoint-root}/dist/target/midpoint-4.1-SNAPSHOT-dist.tar.gz which you must rename to midpoint-dist.tar.gz).

For example:

```
cp ../midpoint/dist/target/midpoint-4.1-SNAPSHOT-dist.tar.gz ./midpoint-dist.tar.gz
```

When that is done build the docker image with following command (the n-switch is added to prevent downloading of the midpoint dist in favour of using our fixed version):

```
./build.sh -n
```

And simply start as usual:

```
make restart
```

# I don't want to run the midpoint server in docker

When you see the time going backwards 😅 for midPoint it is time to run midPoint on your host itself.
Because of docker time drift: https://www.docker.com/blog/addressing-time-drift-in-docker-desktop-for-mac/ and the fact
that midPoint checks against time inconsistencies (LightweightIdentifierGeneratorImpl.java:45) for identifier generation
purposes, you might see these time errors from time to time.

The following command will only start the ldap and the postgresql resources in docker and then run the midPoint server locally.
See the start-local-midpoint.sh script on how we set up midPoint for local use. Just make sure there is a midpoint-dist.tar.gz file in the root.

```
make restart_local_midpoint
```

Every time you run this command if will clear all things (in the resources) and reset the whole midPoint server.

## Documentation
Please see [Dockerized midPoint](https://wiki.evolveum.com/display/midPoint/Dockerized+midPoint) wiki page.
