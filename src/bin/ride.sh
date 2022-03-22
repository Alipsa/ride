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
  echo "/${1}" | sed -e 's/\\/\//g' -e 's/://' -e '/\/$/! s|$|/|'
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
  if [[ -f ${JAVA_HOME}/bin/${JAVA_CMD} ]]; then
  	JAVA_CMD=${JAVA_HOME}/bin/${JAVA_CMD}
  elif [[ ! -z {JAVA_HOME+x} ]] && [[ -d ${JAVA_HOME} ]]; then
    JAVA_CMD=$(posixpath ${JAVA_HOME})bin/${JAVA_CMD}
  fi
  echo ${JAVA_CMD}
}

# freebsd and similar not supported with this construct, there are no javafx platform jars for those anyway
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  OS=linux
elif [[ "$OSTYPE" == "darwin"* ]]; then
  OS=mac
else
  # msys, cygwin, win32
  OS=win
fi

# Note: It is possible to force the initial package loader by adding:
# -DConsoleComponent.PackageLoader=ClasspathPackageLoader
# to the command below, but even better to add it to JAVA_OPTS variable in env.sh
MODULES=javafx.controls,javafx.media,javafx.web,javafx.swing

if [[ "${OSTYPE}" == "msys" ]]; then
	JAVA_CMD=$(fullJavaPath "javaw")
	CLASSPATH="${JAR_NAME};$(winpath ${LIB_DIR})/*"
	LD_PATH="$(winpath ${LIB_DIR})"

	# Fixes bug  Unable to get Charset 'cp65001' for property 'sun.stdout.encoding'
	JAVA_OPTS="${JAVA_OPTS} -Dsun.stdout.encoding=UTF-8 -Dsun.err.encoding=UTF-8"
	start ${JAVA_CMD} --module-path ${LIB_DIR}/${OS} --add-modules ${MODULES} -cp "${JAR_NAME}" $JAVA_OPTS se.alipsa.ride.splash.SplashScreen
	# shellcheck disable=SC2068
	start ${JAVA_CMD} --module-path ${LIB_DIR}/${OS} --add-modules ${MODULES}  -Djava.library.path="${LD_PATH}" -cp "${CLASSPATH}" ${BLAS_PROPS[@]} $JAVA_OPTS se.alipsa.ride.Ride

else
	JAVA_CMD=$(fullJavaPath "java")
	CLASSPATH="${JAR_NAME}:${LIB_DIR}/*"
	LD_PATH="${LIB_DIR}"
	${JAVA_CMD} --module-path ${LIB_DIR}/${OS} --add-modules ${MODULES}  -cp "${JAR_NAME}" $JAVA_OPTS se.alipsa.ride.splash.SplashScreen &
	# shellcheck disable=SC2068
	${JAVA_CMD} --module-path ${LIB_DIR}/${OS} --add-modules ${MODULES}  -Djava.library.path="${LD_PATH}" -cp "${CLASSPATH}" ${BLAS_PROPS[@]} $JAVA_OPTS se.alipsa.ride.Ride &
fi