#!/bin/bash

# getting all the users:
curl --user administrator:5ecr3t -H "Content-Type: application/xml" -X GET  http://localhost:8080/midpoint/ws/rest/users

# searching all the accounts of CSV resource:
curl --user administrator:5ecr3t -H "Content-Type: application/xml" -X POST http://localhost:8080/midpoint/ws/rest/shadows/search -d @all-accounts-csv.xml

# test the LDAP resource connection:
curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/resources/d0811790-6420-11e4-86b2-3c9755567874/test

# test the CSV resource connection:
curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/resources/ef2bc95b-76e0-59e2-86d6-9999cccccccc/test

# resume the task with oid:
curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/tasks/ef99c20d-215d-4c76-91ba-24380f342c59/resume