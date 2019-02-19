#!/usr/bin/env bash

mvn clean install -P online -P createProps
echo "******************************"
echo "*** Create offline package ***"
echo "******************************"
mvn install -P offline

#mvn site