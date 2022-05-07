#!/bin/bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`
lib=`cd "$bin/jarpath" >/dev/null; pwd`
APP_MAINCLASS="com.cqx.netty.sdtp.client.WriteClient"
export JAVA_HOME=/opt/hwclient/JDK/jdk1.8.0_242

CLASSPATH=""
for i in "$lib"/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
#JAVA_OPT="-server -Xmx5g -Xms5g -Xloggc:gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC"
JAVA_OPT="-server -Xmx10g -Xms10g"
$JAVA_HOME/bin/java $JAVA_OPT -classpath $CLASSPATH $APP_MAINCLASS /bi/app/sdtpApp/data1/2.txt 6 2000000 20000
