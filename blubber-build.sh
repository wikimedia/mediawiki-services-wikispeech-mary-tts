#!/usr/bin/env bash

# clean up previous builds
docker rm wikispeech-mary-tts-test
docker rmi --force wikispeech-mary-tts-test

docker rm wikispeech-mary-tts
docker rmi --force wikispeech-mary-tts

# build docker
docker build --tag wikispeech-mary-tts-test --file .pipeline/blubber.yaml --target test .
docker build --tag wikispeech-mary-tts --file .pipeline/blubber.yaml --target production .
