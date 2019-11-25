# build script for Wikispeech

set -e

if [ $# -ne 0 ]; then
    echo "For developers: If you are developing for Wikispeech, and need to make changes to this repository, make sure you run a test build using build_and_test.sh before you make a pull request. Don't run more than one instance of this script at once, and make sure no pronlex server is already running on the default port."
    exit 0
fi


./gradlew check
./gradlew assembleDist
./gradlew test

./gradlew run &
export pid=$!  
echo "marytts running on pid $pid"
sleep 30
sh .travis/exit_server_and_fail_if_not_running.sh marytts $pid

docker build --no-cache . -t sttsse/marytts:buildtest

