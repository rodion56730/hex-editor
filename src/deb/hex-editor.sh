#!/bin/sh
exec java -jar "/usr/share/hex-editor/$(basename /usr/share/hex-editor/*jar-with-dependencies.jar)" "$@"
