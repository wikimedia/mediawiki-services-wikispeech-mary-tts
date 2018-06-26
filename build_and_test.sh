# build script for Wikispeech

set -e

./gradlew check
./gradlew assembleDist
./gradlew test

./gradlew run &
export pid=$!  
echo "marytts running on pid $pid"
sleep 30
sh .travis/exit_server_and_fail_if_not_running.sh marytts $pid

docker build --no-cache . -t sttsse/marytts:buildtest

