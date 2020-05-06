#!/usr/bin/env bash

# clean up previous builds
docker rm wikispeech-mary-tts-stts-test
docker rmi --force wikispeech-mary-tts-stts-test

docker rm wikispeech-mary-tts-stts
docker rmi --force wikispeech-mary-tts-stts

# build docker
blubber .pipeline/blubber.yaml test | docker build --tag wikispeech-mary-tts-stts-test --file - .
blubber .pipeline/blubber.yaml production | docker build --tag wikispeech-mary-tts-stts --file - .
