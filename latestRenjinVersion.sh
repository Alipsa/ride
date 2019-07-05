#!/usr/bin/env bash
echo "Checking latest version at nexus.bedatadriven.com"

function checkVersion {
    curl -s $1 | grep "artifactId\|latest\|release\|lastUpdated"
}
checkVersion "https://nexus.bedatadriven.com/content/groups/public/org/renjin/renjin-core/maven-metadata.xml"
checkVersion "https://nexus.bedatadriven.com/content/groups/public/org/renjin/renjin-script-engine/maven-metadata.xml"