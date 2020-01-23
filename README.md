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
You can then continue with image or one of demo composition, e.g. postgresql or clustering one.

## Launch:
- run image on port 8080:
```
$ docker run -p 8080:8080 --name midpoint evolveum/midpoint
```
- run image on port 8080 with increased heap size:
```
$ docker run -p 8080:8080 -e MP_MEM_MAX='4096M' -e MP_MEM_INIT='4096M' --name midpoint evolveum/midpoint
```
- run one of demo composition, e.g. postgresql:
```
$ cd demo/postgresql/
$ docker-compose up --build
```

## Access MidPoint:
- URL: http://127.0.0.1:8080/midpoint
- username: Administrator
- password: 5ecr3t

## Starting overnew

Since this is a playground the need to start all over again with a clean PostgreSQL and LDAP perform these 2 steps:

```
$ docker system prune --all
$ docker volume rm $(docker volume ls -q)
```

The first command is kind of intrusive in that it deletes everything from your docker :(
To be more precise in your cleanup:

```
$ docker rm $(docker ps -a --filter name=simple | awk '{print$1}'| tail -n +2)
$ docker volume rm $(docker volume ls -q --filter name=simple)
```

Or if you are in a lazy mood: go to root of this project and just go

```
$ make restart
```

See the Makefile in the root of this project for more easy docker manipulating commands.

## Checks after startup

To see which object xml files that were successfully imported at startup perform these commands
and verify that these files have a .done extension:

```
docker ps
docker exec -it <container_id_of_simple_midpoint_server> bash
ls -alt /opt/midpoint/var/post-initial-objects
```

## To Remote Debug MidPoint

Pre-requisities:

    1. Java (JDK) 11
    2. Apache Maven 3

Because there is some hacking going on in midPoint (maven plugin versions that work with java11 or something)
```
wget https://nexus.evolveum.com/nexus/repository/releases/org/apache/maven/plugins/maven-dependency-plugin/3.1.1e1/maven-dependency-plugin-3.1.1e1.pom
wget https://nexus.evolveum.com/nexus/repository/releases/org/apache/maven/plugins/maven-dependency-plugin/3.1.1e1/maven-dependency-plugin-3.1.1e1.jar
mvn install:install-file -Dfile=maven-dependency-plugin-3.1.1e1.jar -DpomFile=maven-dependency-plugin-3.1.1e1.pom
```

```
git checkout git@github.com:Evolveum/midpoint.git
git checkout tags/v4.0.1 -b my-4.0.1
cd midpoint
```

The remove the "import org.bouncycastle.util.Arrays" from the following files:
    repo/repo-test-util/src/main/java/com/evolveum/midpoint/test/asserter/LinkFinder.java
    repo/repo-test-util/src/main/java/com/evolveum/midpoint/test/asserter/ShadowAssociationAsserter.java
    repo/repo-test-util/src/main/java/com/evolveum/midpoint/test/asserter/TriggerFinder.java

Add the exclusion of itext in the jasperreports dependency (net.sf.jasperreports:jasperreports) build-system/pom.xml (since it references an obscure version):
    <exclusion>
        <groupId>com.lowagie</groupId>
        <artifactId>itext</artifactId>
    </exclusion>


Then finally:
```
mvn install -DskipTests=true
```

In IntelliJ IDEA open the git@github.com:Evolveum/midpoint.git project
And create a Remote debug configuration that points to localhost:8000
Happy de-🐞ging!

## Documentation
Please see [Dockerized midPoint](https://wiki.evolveum.com/display/midPoint/Dockerized+midPoint) wiki page.
