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

# This is just to avoid warnings about missing native BLAS libs when running the REPL
# i.e. we will fall back to pure java
BLAS=com.github.fommil.netlib.F2jBLAS
LAPACK=com.github.fommil.netlib.F2jLAPACK
ARPACK=com.github.fommil.netlib.F2jARPACK

# Allow for any kind of customization of variables or paths etc. without having to change this script
# Which would be overwritten on a subsequent install.
if [[ -f $DIR/env.sh ]]; then
  source "$DIR/env.sh"
fi

java -cp ${JAR_NAME} se.alipsa.ride.splash.SplashScreen &

# it is possible to force the initial packageloader by adding:
# -DConsoleComponent.PackageLoader=ClasspathPackageLoader
# to the command below

java -Djava.library.path=${LIB_DIR} -cp "${JAR_NAME}:${LIB_DIR}/*" \
-Dcom.github.fommil.netlib.BLAS=${BLAS} \
-Dcom.github.fommil.netlib.LAPACK=${LAPACK} \
-Dcom.github.fommil.netlib.ARPACK=${ARPACK} \
se.alipsa.ride.Ride &
