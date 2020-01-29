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

# Bashes into docker more specifically the midPoint home directory of the midPoint server where all the goodness happens
bash_midpoint:
	- docker exec -w /opt/midpoint/var -it $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2) bash

# Syncs the Scripted SQL to docker
sync_sql_groovy_scripts:
	- docker cp ./demo/simple/midpoint_server/container_files/mp-home/scripts/. $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2):/opt/midpoint/var/scripts/
	- docker logs -f $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2)

# Check if the midPoint config files are detected/imported
check_midpoint:
	- docker exec -it $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2) ls -alt /opt/midpoint/var/post-initial-objects

# Check the LDAP groups
check_ldap_groups:
	- docker exec -it $$(docker ps -a --filter name=simple_ldap | awk '{print$$1}' | tail -n +2) /opt/opendj/bin/ldapsearch --port 1389 --bindDN "cn=admin,dc=didm,dc=be" --bindPassword secret --baseDN dc=didm,dc=be "(&(objectClass=groupOfUniqueNames))"

# Check the LDAP users
check_ldap_users:
	- docker exec -it $$(docker ps -a --filter name=simple_ldap | awk '{print$$1}' | tail -n +2) /opt/opendj/bin/ldapsearch --port 1389 --bindDN "cn=admin,dc=didm,dc=be" --bindPassword secret --baseDN dc=didm,dc=be "(&(objectClass=inetOrgPerson))"

# Adds an entitlement to Postgres DB
insert_entitlement:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -c "INSERT INTO public.source_entitlements(entitlementid, accountid, email, organisatiecode, departement, dienst, functie, personeelsnummer, fax, gsm, telefoonnr, privileges, disabled) VALUES ('20001-Milieumedewerker-01', '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jan.lievens@vlaanderen.be', '20001', 'Omgeving', 'DIDM', 'Developer', NULL, NULL, '0494846697', '053839066', 'Milieumedewerker;Developer;VIP', false);"

# Removes entitlement from Postgres DB
delete_entitlement:
	- docker exec -it $$(docker ps -a --filter name=simple_postgres_resource_1 | awk '{print$$1}' | tail -n +2) /usr/bin/psql -h localhost -p 8432 -U pgres -d pgres -c "DELETE FROM source_entitlements WHERE entitlementid = '20001-Milieumedewerker-01'"

# Composite command to restart all over again
restart: stop_all clean_all start_all logs_midpoint

# Composite command to restart all in debug mode
restart_debug: stop_all clean_all start_all_debug logs_midpoint