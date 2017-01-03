#!/usr/bin/env bash

mvn install -Dmaven.test.skip=true

echo '   >>>>> Create hbase image'
mvn -f hbase package -Dmaven.test.skip=true
mvn -f hbase docker:build

echo '   >>>>> Create collector image'
mvn -f collector package -Dmaven.test.skip=true
mvn -f collector docker:build

echo '   >>>>> Create web image'
mvn -f web package -Dmaven.test.skip=true
mvn -f web docker:build
