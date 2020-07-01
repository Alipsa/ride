#!/usr/bin/env bash
if [[ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]]; then
   source "$HOME/.sdkman/bin/sdkman-init.sh"
fi
# run Ride in target dir

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "${DIR}" || exit 1

PROPERTY_FILE=version.properties

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=$(cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2)
   echo "$PROP_VALUE" | xargs
}

echo "# Reading property from $PROPERTY_FILE"
VERSION=$(getProperty "version")
JAR_NAME=$(getProperty "jar.name")
RELEASE_TAG=$(getProperty "release.tag")

TARGET=${PWD}/target
ZIPFILE="${TARGET}/ride-${RELEASE_TAG}-dist.zip"

if [[ ! -f "${ZIPFILE}" ]]; then
  echo "${ZIPFILE} does not exist, creating it..."
  mvn clean package
fi

ZIPDIR=${ZIPFILE%.*}
if [[ ! -e "${ZIPDIR}" ]]; then
  echo "Unpacking ${ZIPFILE} to ${ZIPDIR}"
  unzip -d "$ZIPDIR" "$ZIPFILE"
fi

cd "${ZIPDIR}" || exit
./ride.sh
