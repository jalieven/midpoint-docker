# Show the midPoint server logs
logs_midpoint:
	- docker logs -f $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2)

# Stops all the containers (midpoint, ldap and postgres servers)
stop_all:
	- docker stop $$(docker ps -a --filter name=simple | awk '{print$$1}' | tail -n +2)

# Removes all containers and volumes
clean_all:
	- docker rm $$(docker ps -a --filter name=simple | awk '{print$$1}' | tail -n +2)
	- docker volume rm $$(docker volume ls -q --filter name=simple)

prune:
	- docker system prune

# Starts all the containers (midpoint, ldap and postgres servers)
start_all:
	cd demo/simple/; docker-compose up -d --build

# Starts all the containers (midpoint in debug mode, ldap and postgres servers)
start_all_debug:
	cd demo/simple/; docker-compose -f docker-compose-debug.yml up -d --build

# Only starts the resources (ldap and 2 databases)
start_resources:
	cd demo/simple/; docker-compose up -d ldap midpoint_data postgres_resource

# Only starts the local midPoint server (so not using Docker)
start_local_midpoint:
	./start-local-midpoint.sh

# Resume the import task (first time use, it will chain import of account, entitlements and reconciliation)
resume_import_task:
	curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/tasks/ef99c20d-215d-4c76-91ba-24380f342c59/resume

# Run the import accounts task
run_import_accounts_task:
	curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/tasks/ef99c20d-215d-4c76-91ba-24380f342c59/run

# Run the import entitlements task
run_import_entitlements_task:
	curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/tasks/da99c20d-215d-4c76-91ba-24380f342c60/run

# Run the reconciliation task for the accounts
run_reconciliation_accounts_task:
	curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/tasks/37e6eee9-f92a-4e78-a634-7bdaf6eec8ec/run

# Resume the reconciliation task for the accounts
resume_reconciliation_accounts_task:
	curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/tasks/37e6eee9-f92a-4e78-a634-7bdaf6eec8ec/resume

# Run the reconciliation task for the entitlements
run_reconciliation_entitlements_task:
	curl --user administrator:5ecr3t -X POST http://localhost:8080/midpoint/ws/rest/tasks/fda23770-7902-4963-a81f-e2b01293ad6f/run

# Only stops the local midPoint server (so not using Docker)
stop_local_midpoint:
	- ./stop-local-midpoint.sh

# Displays the logs from the local midpoint server
local_logs_midpoint:
	- tail -400f ./midpoint-dist/var/log/midpoint.log

# Bashes into docker more specifically the midPoint home directory of the midPoint server where all the goodness happens
bash_midpoint:
	- docker exec -w /opt/midpoint/var -it $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2) bash

# Syncs the Scripted SQL to docker
sync_sql_groovy_scripts:
	- docker cp ./demo/simple/midpoint_server/container_files/mp-home/scripts/. $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2):/opt/midpoint/var/scripts/
	- docker logs -f $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2)

# Syncs the Scripted SQL to local midPoint
sync_local_sql_groovy_scripts:
	cp -a ./demo/simple/midpoint_server/container_files/mp-home/scripts/. ./midpoint-dist/var/scripts/

# Check if the midPoint config files are detected/imported
check_midpoint:
	- docker exec -it $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2) ls -alt /opt/midpoint/var/post-initial-objects

# Check the LDAP groups
check_ldap_groups:
	- docker exec -it $$(docker ps -a --filter name=simple_ldap | awk '{print$$1}' | tail -n +2) /opt/opendj/bin/ldapsearch --port 1389 --bindDN "cn=admin,dc=didm,dc=be" --bindPassword secret --baseDN dc=didm,dc=be "(&(objectClass=groupOfUniqueNames))"

# Check the LDAP users
check_ldap_users:
	- docker exec -it $$(docker ps -a --filter name=simple_ldap | awk '{print$$1}' | tail -n +2) /opt/opendj/bin/ldapsearch --port 1389 --bindDN "cn=admin,dc=didm,dc=be" --bindPassword secret --baseDN dc=didm,dc=be "(&(objectClass=inetOrgPerson))"

# Inserts an account to PostgreSQL
insert_account:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -c "INSERT INTO public.source_accounts(accountId, username, firstname, lastname, rijksregisternummer, disabled, lastmodification) VALUES ('3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jalie', 'Jan', 'Lievens', '81071032345', false, now() AT TIME ZONE 'UTC');"

# Adds an entitlement to PostgreSQL
insert_entitlement:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -c "INSERT INTO public.source_entitlements(entitlementid, accountid, email, organisatiecode, departement, dienst, functie, personeelsnummer, fax, gsm, telefoonnr, privileges, disabled, lastmodification) VALUES ('20001-Milieumedewerker-01', '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jan.lievens@vlaanderen.be', '20001', 'Omgeving', 'DIDM', 'Developer', NULL, NULL, '0494846697', '053839066', 'Milieumedewerker;Developer;VIP', false, now() AT TIME ZONE 'UTC');"

# Removes entitlement from PostgreSQL
delete_entitlement:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -c "UPDATE public.source_entitlements SET deleted = true, lastmodification = now() AT TIME ZONE 'UTC' WHERE entitlementid = 'e0c6496f-c28a-4747-81d9-e50a8f1a553d'"

# Updates the account in PostgreSQL
update_account:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -c "UPDATE public.source_accounts SET username = 'tvangulck', firstname = 'Tom', lastname = 'Van Gulck', rijksregisternummer = '77071032345', disabled = true, lastmodification = now() AT TIME ZONE 'UTC' WHERE accountId = '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95';"

# Updates the entitlements priviliges in PostgreSQL
update_entitlement_privileges:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -c "UPDATE public.source_entitlements SET privileges = 'TechnicalLead;Developer', lastmodification = now() AT TIME ZONE 'UTC' WHERE entitlementid = '20001-Milieumedewerker-01';"

# Deletes the account from PostgreSQL
delete_account:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -c "UPDATE public.source_accounts SET deleted = true, lastmodification = now() AT TIME ZONE 'UTC' WHERE accountId = '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95'"

insert_accounts:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -a -f /db-scripts/100_accounts.sql

insert_entitlements:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -a -f /db-scripts/200_entitlements.sql

insert_all: insert_accounts insert_entitlements

# Composite command to restart all over again
restart: stop_all clean_all start_all logs_midpoint

# Composite command to restart all in debug mode
restart_debug: stop_all clean_all start_all_debug logs_midpoint

restart_resources: stop_all clean_all start_resources

restart_local_midpoint: stop_local_midpoint restart_resources start_local_midpoint