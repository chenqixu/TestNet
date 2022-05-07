ps -ef|grep SDTPClient|grep -v grep|awk '{print "kill -9 "$2}'|sh
