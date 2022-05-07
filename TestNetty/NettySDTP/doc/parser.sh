#!/bin/bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`
lib=`cd "$bin/jarpath" >/dev/null; pwd`
APP_MAINCLASS="com.cqx.netty.sdtp.client.ParserClient"
export JAVA_HOME=/opt/hwclient/JDK/jdk1.8.0_242

CLASSPATH=""
for i in "$lib"/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
#JAVA_OPT="-server -Xmx5g -Xms5g -Xloggc:gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC"
JAVA_OPT="-server -Xmx80g -Xms80g"
#args0 执行次数/队列大小限制
#args1 并发个数
#args2 解析并发个数
#args3 true:单线程解析 / false:并发解析
PART1="50000 5 7 false"
PART2="5000000 6 1 true"
$JAVA_HOME/bin/java $JAVA_OPT -classpath $CLASSPATH $APP_MAINCLASS $PART1
