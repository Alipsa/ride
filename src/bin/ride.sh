#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

function notify() {
  echo "$1"
  if command -v zenity > /dev/null 2>&1; then
    zenity --info --text="$1"
  elif command -v notify-send > /dev/null 2>&1; then
    notify-send "$1"
  elif [[ "${OSTYPE}" == "msys" ]]; then
    msg ${USERNAME} "$1" /time:30
  fi
}

# on gitbash (msys) we need to convert to windows style path for java
function winpath {
  echo "${1}" | sed -e 's/^\///' -e 's/\//\\/g' -e 's/^./\0:/'
}

cd "${DIR}" || { notify "Failed to cd to $DIR"; exit 1; }

PROPERTY_FILE=version.properties

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=$(cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2)
   echo "$PROP_VALUE" | xargs
}

VERSION=$(getProperty "version")
JAR_NAME=$(getProperty "jar.name")
RELEASE_TAG=$(getProperty "release.tag")

LIB_DIR=${DIR}/lib
export PATH=$PATH:${LIB_DIR}

# This is just to avoid warnings about missing native BLAS libs when running the REPL
# i.e. we will fall back to pure java
BLAS=com.github.fommil.netlib.F2jBLAS
LAPACK=com.github.fommil.netlib.F2jLAPACK
ARPACK=com.github.fommil.netlib.F2jARPACK

# Allow for any kind of customization of variables or paths etc. without having to change this script
# which would otherwise be overwritten on a subsequent install.
if [[ -f $DIR/env.sh ]]; then
  source "$DIR/env.sh"
fi

if [[ "${OSTYPE}" == "msys" ]]; then
  CLASSPATH="${JAR_NAME};$(winpath ${LIB_DIR})/*"
  LD_PATH="$(winpath ${LIB_DIR})"

  # Fixes bug  Unable to get Charset 'cp65001' for property 'sun.stdout.encoding'
  JAVA_OPTS="${JAVA_OPTS} -Dsun.stdout.encoding=UTF-8 -Dsun.err.encoding=UTF-8"
else
  CLASSPATH="${JAR_NAME}:${LIB_DIR}/*"
  LD_PATH="${LIB_DIR}"
fi

java -cp "${JAR_NAME}" $JAVA_OPTS se.alipsa.ride.splash.SplashScreen &

# Note: It is possible to force the initial packageloader by adding:
# -DConsoleComponent.PackageLoader=ClasspathPackageLoader
# to the command below, but even better to add it to JAVA_OPTS variable in env.sh

java -Djava.library.path="${LD_PATH}" -cp "${CLASSPATH}" \
-Dcom.github.fommil.netlib.BLAS=${BLAS} \
-Dcom.github.fommil.netlib.LAPACK=${LAPACK} \
-Dcom.github.fommil.netlib.ARPACK=${ARPACK} \
$JAVA_OPTS se.alipsa.ride.Ride &
