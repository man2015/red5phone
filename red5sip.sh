#!/bin/sh
### BEGIN INIT INFO
# Provides:          red5sip
# Required-Start:    networking
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
### END INIT INFO

# Shell script to show how to start/stop EC2 backend application using jsvc

JAVA_HOME=/usr/lib/jvm/java-6-sun
LIB_DIR=/opt/red5sip/lib
LOGS_DIR=/opt/red5sip/logs
JSVC_ERR=$LOGS_DIR/jsvc_red5sip.err
# for multi instances adapt those lines.
TMP_DIR=/var/tmp
PID_FILE=/var/run/red5sip.pid
SETTINGS_FILE=/opt/red5sip/settings.properties

CLASSPATH=$(echo $LIB_DIR/*.jar | sed 's/ /:/g')

cd $EC2_BACKEND_INSTALL_DIR

case "$1" in
  start)
    #
    # Start backend
    #
    echo "Starting red5sip service"
    jsvc -pidfile $PID_FILE \
        -home $JAVA_HOME \
        -errfile $JSVC_ERR \
        -cp $CLASSPATH org.red5.sip.app.Application $SETTINGS_FILE
    ;;


  stop)
    #
    # Stop red5sip service
    #
    jsvc -stop -pidfile $PID_FILE org.red5.sip.app.Application
    exit $?
    ;;
  
  run)
    #
    # Run red5sip
    #
    $JAVA_HOME/bin/java -cp $CLASSPATH org.red5.sip.app.Main $SETTINGS_FILE
    ;;

  *)
    echo "Usage red5sip.sh start/stop"
    exit 1;;
esac


