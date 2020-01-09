
logs:
	- docker logs -f $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2)

stop_simple:
	- docker stop $$(docker ps -a --filter name=simple | awk '{print$$1}' | tail -n +2)

clean_simple:
	- docker rm $$(docker ps -a --filter name=simple | awk '{print$$1}' | tail -n +2)
	- docker volume rm $$(docker volume ls -q --filter name=simple)

start_simple:
	cd demo/simple/; docker-compose up -d --build

bash_midpoint:
	- docker exec -w /opt/midpoint/var -it $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2) bash

sync_sql_groovy_scripts:
	- docker cp ./demo/simple/midpoint_server/container_files/mp-home/scripts/. $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2):/opt/midpoint/var/scripts/
	- docker logs -f $$(docker ps -a --filter name=simple_midpoint_server | awk '{print$$1}' | tail -n +2)

restart: stop_simple clean_simple start_simple logs
