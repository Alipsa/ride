#!/usr/bin/env bash

mvn clean install

cd src/bin
mvn exec:java