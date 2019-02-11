#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ${DIR}

LIB_DIR=${DIR}/lib
export PATH=$PATH:${LIB_DIR}

java -Djava.library.path=${LIB_DIR} -jar ant-launcher.jar -f ride.xml
