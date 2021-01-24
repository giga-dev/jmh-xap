#!/bin/sh -x
. ./start-env.sh

sh ${GS_HOME}/bin/gs.sh --server ${GS_MANAGER_SERVERS} container create \
	--memory=1g --count=1 \
	--vm-option=-Duse_map_server=true \
	--vm-option=-Dcom.gs.ops-ui.enabled=false \
	--vm-option=-XX:+UseSerialGC \
	${GS_MANAGER_SERVERS}