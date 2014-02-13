#!/bin/sh

SCRIPT_DIR=$(dirname $(readlink -f $0))

java -classpath "$SCRIPT_DIR/../conf:$SCRIPT_DIR/../lib/*" \
   dk.statsbiblioteket.medieplatform.newspaper.statistics.StatisticsComponent -c $SCRIPT_DIR/../conf/config.properties