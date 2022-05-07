#!/bin/bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`
lib=`cd "$bin/jarpath" >/dev/null; pwd`
APP_MAINCLASS="com.cqx.netty.sdtp.client.SDTPClient"
export JAVA_HOME=/opt/hwclient/JDK/jdk1.8.0_242

CLASSPATH=""
for i in "$lib"/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
$JAVA_HOME/bin/java -classpath $CLASSPATH $APP_MAINCLASS 10.44.26.189 18007 100000
