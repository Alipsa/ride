#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ${DIR}

PROPERTY_FILE=version.properties

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE | xargs
}

JAR_NAME=$(getProperty "jar.name")
RELEASE_TAG=$(getProperty "release.tag")

TARGET=${DIR}/${JAR_NAME}

#LIB_DIR=${DIR}/lib
#mkdir -p ${LIB_DIR}
#export PATH=$PATH:${LIB_DIR}

mvn initialize -Dride.jar=${TARGET} -Drelease.tag=${RELEASE_TAG}
#mvn exec:java -Djava.library.path=${LIB_DIR} -Dride.jar=${TARGET} -Drelease.tag=${RELEASE_TAG}
mvn exec:java -Duser.home=$HOME -Dride.jar=${TARGET} -Drelease.tag=${RELEASE_TAG} --no-snapshot-updates