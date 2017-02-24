#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`


PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $DEPLOY_DIR already started!"
    echo "PID: $PIDS"
    exit 1
fi

echo "The $DEPLOY_DIR Found PIDS : $PIDS"

LOGS_DIR=$DEPLOY_DIR/logs
if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi

STDOUT_FILE=$LOGS_DIR/stdout.log

LIB_DIR=$DEPLOY_DIR/lib
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`

_VELCP=.
for i in $LIB_DIR/*.jar
do
    _VELCP=$_VELCP:"$i"
done

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "

## JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn256m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "
JAVA_MEM_OPTS=""
BITS=`java -version 2>&1 | grep -i 64-bit`
if [ -n "$BITS" ]; then
    ##JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn256m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "
    JAVA_MEM_OPTS=" -server -Xmx1g -Xms1g -XX:+UseG1GC "
else
    JAVA_MEM_OPTS=" -server -Xms256m -Xmx256m -XX:PermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC "
fi

echo -e "Starting the $DEPLOY_DIR ...\c"

nohup java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS -classpath $CONF_DIR:$LIB_JARS  -Dcatalina.home=$DEPLOY_DIR com.yonche.test.weather.JunitRunner $1 > $STDOUT_FILE 2>&1 &


echo "OK!"
sleep 1
PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'`
echo "PID: $PIDS"
echo "STDOUT: $STDOUT_FILE"