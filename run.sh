#!/usr/bin/env sh
set -eu
mkdir -p out
javac -encoding UTF-8 -d out src/*.java
exec java -cp out Main
