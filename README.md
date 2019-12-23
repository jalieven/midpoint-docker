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


## Checks after startup

To see which object xml files that were successfully imported at startup perform these commands
and verify that these files have a .done extension:

```
docker ps
docker exec -it <container_id_of_simple_midpoint_server> bash
ls -alt /opt/midpoint/var/post-initial-objects
```

## Documentation
Please see [Dockerized midPoint](https://wiki.evolveum.com/display/midPoint/Dockerized+midPoint) wiki page.




<resource xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xmlns:icfs="http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/resource-schema-3" xmlns:org="http://midpoint.evolveum.com/xml/ns/public/common/org-3" xmlns:q="http://prism.evolveum.com/xml/ns/public/query-3" xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3" xmlns:t="http://prism.evolveum.com/xml/ns/public/types-3" oid="d0811790-6420-11e4-86b2-3c9755567874" version="13">
    <name>OpenDJ</name>
    <metadata>
        <createTimestamp>2019-12-20T15:25:44.777Z</createTimestamp>
        <createChannel>http://midpoint.evolveum.com/xml/ns/public/model/channels-3#objectImport</createChannel>
    </metadata>
    <operationalState>
        <lastAvailabilityStatus>up</lastAvailabilityStatus>
    </operationalState>
    <connectorRef oid="4a30dfb6-942e-45b2-9b23-761c0ad801d8" relation="org:default" type="c:ConnectorType">
        <!-- ConnId com.evolveum.polygon.connector.ldap.LdapConnector v2.4 -->
        <filter>
            <q:equal>
                <q:path xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3">c:connectorType</q:path>
                <q:value>com.evolveum.polygon.connector.ldap.LdapConnector</q:value>
            </q:equal>
        </filter>
    </connectorRef>
    <connectorConfiguration xmlns:icfc="http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/connector-schema-3">
        <icfc:configurationProperties xmlns:gen243="http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/bundle/com.evolveum.polygon.connector-ldap/com.evolveum.polygon.connector.ldap.LdapConnector">
            <gen243:host>ldap</gen243:host>
            <gen243:port>1389</gen243:port>
            <gen243:bindDn>cn=admin,dc=didm,dc=be</gen243:bindDn>
            <gen243:bindPassword>
                <t:encryptedData>
                    <t:encryptionMethod>
                        <t:algorithm>http://www.w3.org/2001/04/xmlenc#aes256-cbc</t:algorithm>
                    </t:encryptionMethod>
                    <t:keyInfo>
                        <t:keyName>Gyk/GpKM1NCgdiEkcpfgT9zHQQM=</t:keyName>
                    </t:keyInfo>
                    <t:cipherData>
                        <t:cipherValue>p+JppldR2Ezl0pUTXj/HklJdd8n8Uy3Xjj6aOOVl69Y=</t:cipherValue>
                    </t:cipherData>
                </t:encryptedData>
            </gen243:bindPassword>
            <gen243:baseContext>dc=didm,dc=be</gen243:baseContext>
            <gen243:pagingStrategy>auto</gen243:pagingStrategy>
            <gen243:vlvSortAttribute>entryUUID</gen243:vlvSortAttribute>
            <gen243:operationalAttributes>ds-pwp-account-disabled</gen243:operationalAttributes>
            <gen243:operationalAttributes>isMemberOf</gen243:operationalAttributes>
        </icfc:configurationProperties>
    </connectorConfiguration>
    <schema>
        <cachingMetadata>
            <retrievalTimestamp>2019-12-20T15:25:46.382Z</retrievalTimestamp>
            <serialNumber>d210c97f2b79407d-d3c40c48ee1e7575</serialNumber>
        </cachingMetadata>
        <generationConstraints>
            <generateObjectClass>ri:inetOrgPerson</generateObjectClass>
        </generationConstraints>
        <definition>
            <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:a="http://prism.evolveum.com/xml/ns/public/annotation-3" xmlns:ra="http://midpoint.evolveum.com/xml/ns/public/resource/annotation-3" xmlns:tns="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3" elementFormDefault="qualified" targetNamespace="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">
                <xsd:import namespace="http://prism.evolveum.com/xml/ns/public/annotation-3"/>
                <xsd:import namespace="http://midpoint.evolveum.com/xml/ns/public/resource/annotation-3"/>
                <xsd:complexType name="inetOrgPerson">
                    <xsd:annotation>
                        <xsd:appinfo>
                            <ra:resourceObject/>
                            <ra:identifier xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:entryUUID</ra:identifier>
                            <ra:secondaryIdentifier xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:dn</ra:secondaryIdentifier>
                            <ra:displayNameAttribute xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:dn</ra:displayNameAttribute>
                            <ra:namingAttribute xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:dn</ra:namingAttribute>
                            <ra:nativeObjectClass>inetOrgPerson</ra:nativeObjectClass>
                        </xsd:appinfo>
                    </xsd:annotation>
                    <xsd:sequence>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="initials" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>120</a:displayOrder>
                                    <a:matchingRule xmlns:qn111="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn111:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>initials</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>initials</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="isMemberOf" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>130</a:displayOrder>
                                    <a:access>read</a:access>
                                    <a:matchingRule xmlns:qn194="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn194:distinguishedName</a:matchingRule>
                                    <ra:nativeAttributeName>isMemberOf</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>isMemberOf</ra:frameworkAttributeName>
                                    <ra:returnedByDefault>false</ra:returnedByDefault>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="homePhone" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>140</a:displayOrder>
                                    <ra:nativeAttributeName>homePhone</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>homePhone</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="audio" type="xsd:base64Binary">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>150</a:displayOrder>
                                    <ra:nativeAttributeName>audio</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>audio</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="mail" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>160</a:displayOrder>
                                    <a:matchingRule xmlns:qn125="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn125:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>mail</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>mail</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="carLicense" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>170</a:displayOrder>
                                    <a:matchingRule xmlns:qn327="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn327:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>carLicense</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>carLicense</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="departmentNumber" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>180</a:displayOrder>
                                    <a:matchingRule xmlns:qn342="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn342:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>departmentNumber</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>departmentNumber</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="manager" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>190</a:displayOrder>
                                    <a:matchingRule xmlns:qn87="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn87:distinguishedName</a:matchingRule>
                                    <ra:nativeAttributeName>manager</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>manager</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="businessCategory" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>200</a:displayOrder>
                                    <a:matchingRule xmlns:qn118="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn118:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>businessCategory</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>businessCategory</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="homePostalAddress" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>210</a:displayOrder>
                                    <ra:nativeAttributeName>homePostalAddress</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>homePostalAddress</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="secretary" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>220</a:displayOrder>
                                    <a:matchingRule xmlns:qn311="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn311:distinguishedName</a:matchingRule>
                                    <ra:nativeAttributeName>secretary</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>secretary</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="photo" type="xsd:base64Binary">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>230</a:displayOrder>
                                    <ra:nativeAttributeName>photo</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>photo</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="labeledURI" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>240</a:displayOrder>
                                    <ra:nativeAttributeName>labeledURI</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>labeledURI</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element minOccurs="0" name="displayName" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>250</a:displayOrder>
                                    <a:matchingRule xmlns:qn974="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn974:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>displayName</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>displayName</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="pager" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>260</a:displayOrder>
                                    <ra:nativeAttributeName>pager</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>pager</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="roomNumber" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>270</a:displayOrder>
                                    <a:matchingRule xmlns:qn492="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn492:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>roomNumber</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>roomNumber</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="physicalDeliveryOfficeName" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>280</a:displayOrder>
                                    <a:matchingRule xmlns:qn646="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn646:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>physicalDeliveryOfficeName</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>physicalDeliveryOfficeName</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="uid" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>290</a:displayOrder>
                                    <a:matchingRule xmlns:qn316="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn316:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>uid</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>uid</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="seeAlso" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>300</a:displayOrder>
                                    <a:matchingRule xmlns:qn652="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn652:distinguishedName</a:matchingRule>
                                    <ra:nativeAttributeName>seeAlso</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>seeAlso</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="destinationIndicator" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>310</a:displayOrder>
                                    <a:matchingRule xmlns:qn55="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn55:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>destinationIndicator</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>destinationIndicator</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="postalAddress" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>320</a:displayOrder>
                                    <ra:nativeAttributeName>postalAddress</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>postalAddress</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element minOccurs="0" name="preferredLanguage" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>330</a:displayOrder>
                                    <a:matchingRule xmlns:qn391="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn391:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>preferredLanguage</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>preferredLanguage</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element minOccurs="0" name="ds-pwp-account-disabled" type="xsd:boolean">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>340</a:displayOrder>
                                    <ra:nativeAttributeName>ds-pwp-account-disabled</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>ds-pwp-account-disabled</ra:frameworkAttributeName>
                                    <ra:returnedByDefault>false</ra:returnedByDefault>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element minOccurs="0" name="preferredDeliveryMethod" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>350</a:displayOrder>
                                    <ra:nativeAttributeName>preferredDeliveryMethod</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>preferredDeliveryMethod</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="facsimileTelephoneNumber" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>360</a:displayOrder>
                                    <ra:nativeAttributeName>facsimileTelephoneNumber</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>facsimileTelephoneNumber</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="employeeType" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>370</a:displayOrder>
                                    <a:matchingRule xmlns:qn706="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn706:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>employeeType</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>employeeType</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="internationaliSDNNumber" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>380</a:displayOrder>
                                    <ra:nativeAttributeName>internationaliSDNNumber</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>internationaliSDNNumber</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="postOfficeBox" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>390</a:displayOrder>
                                    <a:matchingRule xmlns:qn986="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn986:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>postOfficeBox</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>postOfficeBox</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="telephoneNumber" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>400</a:displayOrder>
                                    <ra:nativeAttributeName>telephoneNumber</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>telephoneNumber</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="l" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>410</a:displayOrder>
                                    <a:matchingRule xmlns:qn880="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn880:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>l</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>l</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element minOccurs="0" name="employeeNumber" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>420</a:displayOrder>
                                    <a:matchingRule xmlns:qn763="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn763:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>employeeNumber</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>employeeNumber</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="jpegPhoto" type="xsd:base64Binary">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>430</a:displayOrder>
                                    <ra:nativeAttributeName>jpegPhoto</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>jpegPhoto</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="o" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>440</a:displayOrder>
                                    <a:matchingRule xmlns:qn16="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn16:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>o</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>o</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="userPKCS12" type="xsd:base64Binary">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>450</a:displayOrder>
                                    <ra:nativeAttributeName>userPKCS12</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>userPKCS12</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="description" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>460</a:displayOrder>
                                    <a:matchingRule xmlns:qn130="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn130:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>description</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>description</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element name="dn" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>110</a:displayOrder>
                                    <a:matchingRule xmlns:qn636="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn636:distinguishedName</a:matchingRule>
                                    <ra:nativeAttributeName>dn</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>__NAME__</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" name="sn" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>470</a:displayOrder>
                                    <a:matchingRule xmlns:qn362="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn362:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>sn</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>sn</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="givenName" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>480</a:displayOrder>
                                    <a:matchingRule xmlns:qn468="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn468:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>givenName</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>givenName</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="telexNumber" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>490</a:displayOrder>
                                    <ra:nativeAttributeName>telexNumber</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>telexNumber</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="postalCode" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>500</a:displayOrder>
                                    <a:matchingRule xmlns:qn131="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn131:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>postalCode</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>postalCode</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="userSMIMECertificate" type="xsd:base64Binary">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>510</a:displayOrder>
                                    <ra:nativeAttributeName>userSMIMECertificate</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>userSMIMECertificate</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="userCertificate" type="xsd:base64Binary">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>520</a:displayOrder>
                                    <ra:nativeAttributeName>userCertificate</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>userCertificate</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="st" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>530</a:displayOrder>
                                    <a:matchingRule xmlns:qn736="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn736:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>st</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>st</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="teletexTerminalIdentifier" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>540</a:displayOrder>
                                    <ra:nativeAttributeName>teletexTerminalIdentifier</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>teletexTerminalIdentifier</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="ou" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>550</a:displayOrder>
                                    <a:matchingRule xmlns:qn194="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn194:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>ou</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>ou</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="street" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>560</a:displayOrder>
                                    <a:matchingRule xmlns:qn809="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn809:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>street</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>street</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" name="cn" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>570</a:displayOrder>
                                    <a:matchingRule xmlns:qn598="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn598:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>cn</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>cn</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="registeredAddress" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>580</a:displayOrder>
                                    <ra:nativeAttributeName>registeredAddress</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>registeredAddress</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="x121Address" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>590</a:displayOrder>
                                    <ra:nativeAttributeName>x121Address</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>x121Address</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="title" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>600</a:displayOrder>
                                    <a:matchingRule xmlns:qn486="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn486:stringIgnoreCase</a:matchingRule>
                                    <ra:nativeAttributeName>title</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>title</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="x500UniqueIdentifier" type="xsd:base64Binary">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>610</a:displayOrder>
                                    <ra:nativeAttributeName>x500UniqueIdentifier</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>x500UniqueIdentifier</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element maxOccurs="unbounded" minOccurs="0" name="mobile" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>620</a:displayOrder>
                                    <ra:nativeAttributeName>mobile</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>mobile</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                        <xsd:element minOccurs="0" name="entryUUID" type="xsd:string">
                            <xsd:annotation>
                                <xsd:appinfo>
                                    <a:displayOrder>100</a:displayOrder>
                                    <a:access>read</a:access>
                                    <a:matchingRule xmlns:qn0="http://prism.evolveum.com/xml/ns/public/matching-rule-3">qn0:uuid</a:matchingRule>
                                    <ra:nativeAttributeName>entryUUID</ra:nativeAttributeName>
                                    <ra:frameworkAttributeName>__UID__</ra:frameworkAttributeName>
                                </xsd:appinfo>
                            </xsd:annotation>
                        </xsd:element>
                    </xsd:sequence>
                </xsd:complexType>
            </xsd:schema>
        </definition>
    </schema>
    <schemaHandling>
        <objectType id="1">
            <kind>account</kind>
            <displayName>Normal Account</displayName>
            <default>true</default>
            <objectClass>ri:inetOrgPerson</objectClass>
            <attribute id="2">
                <c:ref xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:dn</c:ref>
                <displayName>Distinguished Name</displayName>
                <limitations>
                    <minOccurs>0</minOccurs>
                    <access>
                        <read>true</read>
                        <add>true</add>
                        <modify>true</modify>
                    </access>
                </limitations>
                <matchingRule xmlns:mr="http://prism.evolveum.com/xml/ns/public/matching-rule-3">mr:stringIgnoreCase</matchingRule>
                <outbound>
                    <source>
                        <c:path>$user/name</c:path>
                    </source>
                    <expression>
                        <script xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xsi:type="c:ScriptExpressionEvaluatorType">
                            <code>
								'uid=' + name + iterationToken + ',ou=people,dc=didm,dc=be'
							</code>
                        </script>
                    </expression>
                </outbound>
            </attribute>
            <attribute id="3">
                <c:ref xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:entryUUID</c:ref>
                <displayName>Entry UUID</displayName>
                <limitations>
                    <access>
                        <read>true</read>
                        <add>false</add>
                        <modify>true</modify>
                    </access>
                </limitations>
                <matchingRule xmlns:mr="http://prism.evolveum.com/xml/ns/public/matching-rule-3">mr:stringIgnoreCase</matchingRule>
            </attribute>
            <attribute id="4">
                <c:ref xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:cn</c:ref>
                <displayName>Common Name</displayName>
                <limitations>
                    <minOccurs>0</minOccurs>
                    <access>
                        <read>true</read>
                        <add>true</add>
                        <modify>true</modify>
                    </access>
                </limitations>
                <outbound>
                    <source>
                        <c:path>$user/fullName</c:path>
                    </source>
                </outbound>
            </attribute>
            <attribute id="5">
                <c:ref xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:sn</c:ref>
                <displayName>Surname</displayName>
                <limitations>
                    <minOccurs>0</minOccurs>
                </limitations>
                <outbound>
                    <source>
                        <c:path>familyName</c:path>
                    </source>
                </outbound>
            </attribute>
            <attribute id="6">
                <c:ref xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:givenName</c:ref>
                <displayName>Given Name</displayName>
                <outbound>
                    <source>
                        <c:path xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3">$c:user/c:givenName</c:path>
                    </source>
                </outbound>
            </attribute>
            <attribute id="8">
                <c:ref xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">ri:uid</c:ref>
                <displayName>Login Name</displayName>
                <matchingRule xmlns:mr="http://prism.evolveum.com/xml/ns/public/matching-rule-3">mr:stringIgnoreCase</matchingRule>
                <outbound>
                    <strength>weak</strength>
                    <source>
                        <c:path>$user/name</c:path>
                    </source>
                    <expression>
                        <script xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xsi:type="c:ScriptExpressionEvaluatorType">
                            <code>name</code>
                        </script>
                    </expression>
                </outbound>
            </attribute>
            <protected>
                <filter>
                    <q:equal>
                        <q:matching>http://prism.evolveum.com/xml/ns/public/matching-rule-3#stringIgnoreCase</q:matching>
                        <q:path xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">attributes/ri:dn</q:path>
                        <q:value>cn=admin,dc=didm,dc=be</q:value>
                    </q:equal>
                </filter>
            </protected>
            <activation>
                <administrativeStatus>
                    <outbound id="8"/>
                </administrativeStatus>
            </activation>
            <credentials>
                <password xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xsi:type="c:ResourcePasswordDefinitionType">
                    <outbound>
                        <expression>
                            <asIs xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="c:AsIsExpressionEvaluatorType"/>
                        </expression>
                    </outbound>
                </password>
            </credentials>
        </objectType>
    </schemaHandling>
    <capabilities>
        <cachingMetadata>
            <retrievalTimestamp>2019-03-07T21:46:39.904Z</retrievalTimestamp>
            <serialNumber>d5d2b0113448065b-fd82f7ab9a00b7ae</serialNumber>
        </cachingMetadata>
        <native xmlns:cap="http://midpoint.evolveum.com/xml/ns/public/resource/capabilities-3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xsi:type="c:CapabilityCollectionType">
            <cap:schema/>
            <cap:liveSync/>
            <cap:testConnection/>
            <cap:create/>
            <cap:update>
                <cap:delta>true</cap:delta>
            </cap:update>
            <cap:delete/>
            <cap:script>
                <cap:host>
                    <cap:type>connector</cap:type>
                </cap:host>
            </cap:script>
            <cap:addRemoveAttributeValues/>
            <cap:credentials>
                <cap:password>
                    <cap:returnedByDefault>false</cap:returnedByDefault>
                </cap:password>
            </cap:credentials>
            <cap:auxiliaryObjectClasses/>
            <cap:pagedSearch/>
            <cap:read>
                <cap:returnDefaultAttributesOption>true</cap:returnDefaultAttributesOption>
            </cap:read>
        </native>
    </capabilities>
    <synchronization>
        <objectSynchronization>
            <focusType>c:UserType</focusType>
            <enabled>true</enabled>
            <correlation>
                <q:equal>
                    <q:path>name</q:path>
                    <expression>
                        <path xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3">
								declare namespace ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3";
								$account/attributes/ri:uid
							</path>
                    </expression>
                </q:equal>
            </correlation>
            <reaction>
                <situation>linked</situation>
                <synchronize>true</synchronize>
            </reaction>
            <reaction>
                <situation>deleted</situation>
                <synchronize>true</synchronize>
                <action>
                    <handlerUri>http://midpoint.evolveum.com/xml/ns/public/model/action-3#unlink</handlerUri>
                </action>
            </reaction>
            <reaction>
                <situation>unlinked</situation>
                <synchronize>true</synchronize>
                <action>
                    <handlerUri>http://midpoint.evolveum.com/xml/ns/public/model/action-3#link</handlerUri>
                </action>
            </reaction>
            <reaction>
                <situation>unmatched</situation>
                <synchronize>true</synchronize>
                <action/>
            </reaction>
        </objectSynchronization>
    </synchronization>
</resource>
