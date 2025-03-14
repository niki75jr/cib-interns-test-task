#!/bin/bash
#default entrypoint
/bin/bash /usr/local/bin/docker-entrypoint.sh postgres
# for healthcheck
while [ $(pg_isready -U postgres -q; echo $?) -ne 0 ]; do
	#statements
	sleep 1
	echo -e '\e[1;33awaiting launch'
done
# after while
createdb -U postgres socks && psql -d socks -f ./src/init.sql &> /dev/null
echo -e "\e[1;95mBuildVersion: \e[1;92m""3.0.0-rc0"