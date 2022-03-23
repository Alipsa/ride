#!/usr/bin/env bash

# build and run Ride

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "${DIR}" || exit 1

mvn -DskipTests clean package
status=$?
if [ $status -ne 0 ]; then
   echo "Build failed"
   exit 1
fi

PROPERTY_FILE=version.properties

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=$(cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2)
   echo $PROP_VALUE | xargs
}

echo "# Reading property from $PROPERTY_FILE"
VERSION=$(getProperty "version")
JAR_NAME=$(getProperty "jar.name")
RELEASE_TAG=$(getProperty "release.tag")

TARGET=${DIR}/target/${JAR_NAME}

# Allow for any kind of customization of variables or paths etc. without having to change this script
# which would otherwise be overwritten on a subsequent install.
if [[ -f $DIR/env.sh ]]; then
  source "$DIR/env.sh"
fi


java -cp ${TARGET} $JAVA_OPTS se.alipsa.ride.splash.SplashScreen &
#mvn initialize -Dride.jar=${TARGET} -Drelease.tag=${RELEASE_TAG}
mvn $JAVA_OPTS exec:java