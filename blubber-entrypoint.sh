#!/usr/bin/env bash

echo "Starting HAProxy"
/usr/sbin/haproxy -f /srv/haproxy.cfg

echo "Starting Mary TTS STTS."
export GRADLE_USER_HOME=/srv/gradle_user_home
#export MARY_TTS_MISHKAL_URL=http://localhost:8080/
#cd src/marytts
#./gradlew run --no-rebuild -Dmodules.poweronselftest=false
cd /srv/mary-tts/build/install/mary-tts/
./bin/marytts-server
#/bin/bash
