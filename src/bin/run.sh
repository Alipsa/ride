#!/usr/bin/env bash

PROPERTY_FILE=version.properties

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE | xargs
}

JAR_NAME=$(getProperty "jar.name")

target=${PWD}/${JAR_NAME}
mvn initialize -Dride.jar=${target}
mvn exec:java