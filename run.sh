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
JAR_NAME=$(getProperty "jar.name")

target=${PWD}/target/${JAR_NAME}

cd src/bin
mvn initialize -Dride.jar=${target}
mvn exec:java -Dride.jar=${target}