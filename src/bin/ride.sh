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

TARGET=${DIR}/${JAR_NAME}

LIB_DIR=${DIR}/lib
mkdir -p ${LIB_DIR}
export PATH=$PATH:${LIB_DIR}

# This is just to avoid warnings about missing native BLAS libs when running the REPL
# i.e. we will fall back to pure java
BLAS=com.github.fommil.netlib.F2jBLAS
LAPACK=com.github.fommil.netlib.F2jLAPACK
ARPACK=com.github.fommil.netlib.F2jARPACK

java -cp ${JAR_NAME} se.alipsa.ride.splash.SplashScreen &

mvn initialize -Dride.jar=${TARGET} -Drelease.tag=${RELEASE_TAG} --no-snapshot-updates

mvn exec:java \
-Duser.home=$HOME \
-Djava.library.path=${LIB_DIR} \
-Dride.jar=${TARGET} \
-Drelease.tag=${RELEASE_TAG} \
-Dcom.github.fommil.netlib.BLAS=${BLAS} \
-Dcom.github.fommil.netlib.LAPACK=${LAPACK} \
-Dcom.github.fommil.netlib.ARPACK=${ARPACK}