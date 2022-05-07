#!/bin/bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`
lib=`cd "$bin/jarpath" >/dev/null; pwd`
APP_MAINCLASS="com.cqx.netty.sdtp.service.SDTPServer"
export JAVA_HOME=/opt/hwclient/JDK/jdk1.8.0_242

CLASSPATH=""
for i in "$lib"/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
JAVA_OPT="-server -Xmx80g -Xms80g"
rm -f /bi/app/sdtpApp/data/*
#arg1 filePath
#arg2 port
#arg3 work_num
#arg4 isWrite
#arg5 isParser
#arg6 parallel_num
#arg7 isWriteSingle
#arg8 queue_limit
PART1="/bi/app/sdtpApp/data/xdr.txt 18007 1 false false 1 false 50000"
PART1="/bi/app/sdtpApp/data/xdr.txt 18007 5 false false 1 false 50000"
PART2="/bi/app/sdtpApp/data/xdr.txt 18007 1 false true 1 false 50000"
PART2="/bi/app/sdtpApp/data/xdr.txt 18007 3 false true 6 false 50000"
PART2="/bi/app/sdtpApp/data/xdr.txt 18007 3 false true 1 false 50000"
PART3="/bi/app/sdtpApp/data/xdr.txt 18007 6 true true 8 true 50000"
PART3="/bi/app/sdtpApp/data/xdr.txt 18007 6 false true 10 true 50000"
PART3="/bi/app/sdtpApp/data/xdr.txt 18007 6 true true 6 false 50000"
$JAVA_HOME/bin/java $JAVA_OPT -classpath $CLASSPATH $APP_MAINCLASS $PART2
