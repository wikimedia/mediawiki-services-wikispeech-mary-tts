#!/usr/bin/env bash

# This is the test build entrypoint for Mary-TTS.
# It will startup the service in the background,
# wait for 10 seconds and make sure the process didn't die,
# and then send a version-request to the HTTP server
# which need to respond with HTTP 200.
# If all goes well the test will pass.

echo "Starting Mary TTS STTS."
export GRADLE_USER_HOME=/srv/gradle_user_home
cd /srv/mary-tts/build/install/mary-tts/
./bin/marytts-server &

PID=$!
sleep 10
if ! kill -0 ${PID}; then
  echo "ERROR: Service process has prematurely ended!"
  exit 1
fi
wget -O /dev/null -o /dev/null "http://localhost:59125/version"
EXIT_CODE=$?
kill ${PID}
if [ ${EXIT_CODE} -ne 0 ]; then
  echo "ERROR: Test failed!"
else
  echo "Test successful!"
fi
exit ${EXIT_CODE}
