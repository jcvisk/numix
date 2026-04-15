#!/usr/bin/env sh
set -eu

CATALINA_HOME="${CATALINA_HOME:-/opt/tomcat}"
TOMCAT_USERS_FILE="${CATALINA_HOME}/conf/tomcat-users.xml"
MANAGER_CONTEXT_FILE="${CATALINA_HOME}/webapps/manager/META-INF/context.xml"

MANAGER_USER="${TOMCAT_MANAGER_USERNAME:-cargo}"
MANAGER_PASS="${TOMCAT_MANAGER_PASSWORD:-cargo123}"

if [ -f "$MANAGER_CONTEXT_FILE" ]; then
  sed -i '/RemoteAddrValve/d' "$MANAGER_CONTEXT_FILE"
fi

if ! grep -q 'rolename="manager-script"' "$TOMCAT_USERS_FILE"; then
  sed -i '/<\/tomcat-users>/i\
  <role rolename="manager-script"/>\
' "$TOMCAT_USERS_FILE"
fi

if ! grep -q "username=\"$MANAGER_USER\"" "$TOMCAT_USERS_FILE"; then
  sed -i '/<\/tomcat-users>/i\
  <user username="'"$MANAGER_USER"'" password="'"$MANAGER_PASS"'" roles="manager-script"/>\
' "$TOMCAT_USERS_FILE"
fi

exec catalina.sh run
