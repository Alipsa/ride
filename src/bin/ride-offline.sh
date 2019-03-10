#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ${DIR}

PROPERTY_FILE=version.properties

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE | xargs
}

VERSION=$(getProperty "version")
JAR_NAME=$(getProperty "jar.name")
RELEASE_TAG=$(getProperty "release.tag")

LIB_DIR=${DIR}/lib
export PATH=$PATH:${LIB_DIR}

java -cp ${LIB_DIR}/${JAR_NAME} se.alipsa.ride.splash.SplashScreen &
java -Djava.library.path=${LIB_DIR} -jar ant-launcher.jar -f ride.xml
