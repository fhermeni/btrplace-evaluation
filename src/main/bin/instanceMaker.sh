#!/bin/sh

JAVA_OPTS="-server -Xmx2G -Dlogback.configurationFile=logback.xml"
#Define the classpath
JARS=`ls lib/*.jar`

for JAR in $JARS; do
 CLASSPATH=$JAR:$CLASSPATH
done

java $JAVA_OPTS -cp $CLASSPATH evaluation.demo.InstanceMaker $*