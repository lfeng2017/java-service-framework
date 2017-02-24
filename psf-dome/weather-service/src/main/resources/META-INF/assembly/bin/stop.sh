#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`


PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
echo "Find PID: $PIDS"
if [ -z "$PIDS" ]; then
    echo "ERROR: The $DEPLOY_DIR does not started!"
    exit 1
fi

if [ "$1" != "skip" ]; then
    echo "EXEC DUMP $BIN_DIR/dump.sh  start"
fi

echo -e "Stopping the $DEPLOY_DIR ...\c"
for PID in $PIDS ; do
    kill $PID > /dev/null 2>&1
done

sleep 1
PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
echo "OK!"
echo "PID: $PIDS"