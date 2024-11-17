#!/usr/bin/env sh

set -e

BuildCompiler(){
	javac c3po/*.java
	jar cmf c3po/Manifest.txt c3po.jar c3po LICENSE
	echo "Compiler built: c3po.jar"
}

BuildIDE(){
	javac *.java
	jar cmf Manifest.txt c3po-ide.jar *.class c3po LICENSE
	echo "IDE built: c3po-ide.jar"
}

BuildCompiler
BuildIDE
