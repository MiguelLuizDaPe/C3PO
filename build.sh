#!/usr/bin/env sh

set -xe
javac c3po/*.java
jar cmf Manifest.txt c3po.jar c3po LICENSE
java -jar c3po.jar compile source.c3po

