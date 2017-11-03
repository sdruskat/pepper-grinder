#!/bin/bash

# Set version numbers
if [ $# -eq 0 ]
  then
    echo "Need to give the version number as argument."
    exit 1
fi
cd .. &&
echo 'Using Tycho Versions plugin to update versions automatically'
mvn tycho-versions:set-version -DnewVersion=$1
echo 'Update version in config POM'
mvn -f ./releng/net.sdruskat.peppergrinder.configuration/pom.xml versions:set -DnewVersion=$1
echo 'Using version in doc POM'
mvn -f ./releng/net.sdruskat.peppergrinder.doc/pom.xml versions:set -DnewVersion=$1
echo 'Replace config version in root POM with sed'
sed -i -e '/<artifactId>net.sdruskat.peppergrinder.configuration<\/artifactId>/!b;n;c        <version>$1</version>' pom.xml 