#!/bin/bash

# getting all the users:
curl --user administrator:5ecr3t -H "Content-Type: application/xml" -X GET  http://localhost:8080/midpoint/ws/rest/users

# searching all the accounts of CSV resource:
curl --user administrator:5ecr3t -H "Content-Type: application/xml" -X POST http://localhost:8080/midpoint/ws/rest/shadows/search -d @all-accounts-csv.xml
