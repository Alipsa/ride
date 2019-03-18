#!/usr/bin/env bash

# just run Ride based on latest compilation

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ${DIR}

PROPERTY_FILE=version.properties

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE | xargs
}

echo "# Reading property from $PROPERTY_FILE"
VERSION=$(getProperty "version")
JAR_NAME=$(getProperty "jar.name")
RELEASE_TAG=$(getProperty "release.tag")

TARGET=${PWD}/target/${JAR_NAME}

cd src/bin

java -cp ${TARGET} se.alipsa.ride.splash.SplashScreen &
mvn exec:java -Dride.jar=${TARGET} -Drelease.tag=${VERSION}