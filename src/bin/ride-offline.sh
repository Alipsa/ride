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

java -cp ${LIB_DIR}/${JAR_NAME} se.alipsa.ride.splash.SplashScreen &
java -Djava.library.path=${LIB_DIR} -jar ant-launcher.jar -f ride.xml \
-Dcom.github.fommil.netlib.BLAS=${BLAS} \
-Dcom.github.fommil.netlib.LAPACK=${LAPACK} \
-Dcom.github.fommil.netlib.ARPACK=${ARPACK}
