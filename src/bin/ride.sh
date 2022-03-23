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

function posixpath {
  if [[ "${OSTYPE}" == "msys" ]]; then
    echo "${1}" | sed -e 's/\\/\//g' -e 's/://' -e '/\/$/! s|$|/|'
  else
    echo "/${1}" | sed -e 's/\\/\//g' -e 's/://' -e '/\/$/! s|$|/|'
  fi
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

BLAS_PROPS=( "-Dcom.github.fommil.netlib.BLAS=${BLAS}" "-Dcom.github.fommil.netlib.LAPACK=${LAPACK}" "-Dcom.github.fommil.netlib.ARPACK=${ARPACK}" )

# Allow for any kind of customization of variables or paths etc. without having to change this script
# which would otherwise be overwritten on a subsequent install.
if [[ -f $DIR/env.sh ]]; then
  source "$DIR/env.sh"
fi

function fullJavaPath {
  JAVA_CMD=$1
  if [[ -n "${JAVA_HOME}" ]] && [[ -d ${JAVA_HOME} ]]; then
    JAVA_CMD=$(posixpath "${JAVA_HOME}")bin/${JAVA_CMD}
  elif [[ $(command -v "${JAVA_CMD}") ]]; then
    JAVA_CMD=$(command -v "${JAVA_CMD}")
  fi
  if [[ ! -f ${JAVA_CMD} ]]; then
    echo "Failed to find $1 as $JAVA_CMD, set JAVA_HOME and/or PATH to $1 in env.sh and try again"
    exit 1
  fi
  echo "${JAVA_CMD}"
}

# Note: It is possible to force the initial package loader by adding:
# -DConsoleComponent.PackageLoader=ClasspathPackageLoader
# to the command below, but even better to add it to JAVA_OPTS variable in env.sh

if [[ "${OSTYPE}" == "msys" ]]; then
	JAVA_CMD=$(fullJavaPath "javaw")
	CLASSPATH="${JAR_NAME};$(winpath "${LIB_DIR}")/*"
	LD_PATH=$(winpath "${LIB_DIR}")

	# Fixes bug  Unable to get Charset 'cp65001' for property 'sun.stdout.encoding'
	JAVA_OPTS="${JAVA_OPTS} -Dsun.stdout.encoding=UTF-8 -Dsun.err.encoding=UTF-8"
	start "${JAVA_CMD}" -cp "${JAR_NAME}" $JAVA_OPTS se.alipsa.ride.splash.SplashScreen
	# shellcheck disable=SC2068
	start "${JAVA_CMD}" -Djava.library.path="${LD_PATH}" -cp "${CLASSPATH}" ${BLAS_PROPS[@]} $JAVA_OPTS se.alipsa.ride.Ride

else
	JAVA_CMD=$(fullJavaPath "java")
	CLASSPATH="${JAR_NAME}:${LIB_DIR}/*"
	LD_PATH="${LIB_DIR}"
	${JAVA_CMD} -cp "${JAR_NAME}" $JAVA_OPTS se.alipsa.ride.splash.SplashScreen &
	# shellcheck disable=SC2068
	${JAVA_CMD} -Djava.library.path="${LD_PATH}" -cp "${CLASSPATH}" ${BLAS_PROPS[@]} $JAVA_OPTS se.alipsa.ride.Ride &
fi