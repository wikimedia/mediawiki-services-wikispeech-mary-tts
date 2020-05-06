#!/usr/bin/env bash

m_error() {
  echo $1
  exit 2
}

if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
  _java="$JAVA_HOME/bin/java"
else
  m_error "JAVA_HOME is not pointing at a valid java home."
fi

if [[ "$_java" ]]; then
  version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
  if ! [[ "$version" =~ ^11\.0\..* ]]; then
    m_error "JAVA_HOME must point at 11.0.x but was $version. Unable to create compatible Gradle cache. Try update-java-alternatives -l and then manually export JAVA_HOME=..."
  fi
fi

if [ ! -d blubber ]; then
  cp -r . /tmp/mary-tts_tmp
  mkdir blubber
  cd blubber
  mv /tmp/mary-tts_tmp mary-tts
else
  cd blubber
fi

GRADLE_USER_HOME_BAK=${GRADLE_USER_HOME}
export GRADLE_USER_HOME=`pwd`/gradle_user_home

cd mary-tts

if ! ./gradlew clean build -x test -x integrationTest; then
  m_error "Unable to build Mary TTS STTS!"
fi

if ! ./gradlew installDist -x test -x integrationTest; then
  m_error "Unable to build distribution of Mary TTS STTS!"
fi
if ! cp stts_voices/voice-ar-nah-hsmm-5.2.jar stts_voices/voice-dfki-spike-hsmm-5.1.jar stts_voices/voice-stts_no_nst-hsmm-5.2.jar stts_voices/voice-stts_sv_nst-hsmm-5.2-SNAPSHOT.jar build/install/mary-tts/lib/; then
  m_error "Unable to install voices to Mary TTS STTS!"
fi

export GRADLE_USER_HOME=${GRADLE_USER_HOME_BAK}

echo "Successfully prepared Mary-TTS! Now run ./blubber-build.sh"
