# MaryTTS

This is a copy of the source code repository for [MaryTTS](https://github.com/marytts/marytts), adapted by STTS for use with the <a href="http://stts-se.github.io/wikispeech">Wikispeech engine</a>.

MaryTTS is a multilingual open-source MARY text-to-speech platform (MaryTTS).
MaryTTS is a client-server system written in pure Java, so it runs on many platforms.

[![Build Status](https://travis-ci.org/stts-se/marytts.svg)](https://travis-ci.org/stts-se/marytts)

**For a downloadable package ready for use, see [the releases page](https://github.com/stts-se/marytts/releases).**

**For documentation on using MaryTTS from various angles, see [the wiki](https://github.com/marytts/marytts/wiki).**

Older documentation can also be found at http://mary.dfki.de and https://mary.opendfki.de.

This README is part of the the MaryTTS source code repository.
It contains information about compiling and developing the MaryTTS sources.

The code comes under the Lesser General Public License LGPL version 3 -- see LICENSE.md for details.

## Setting up MaryTTS for Wikispeech

### Basic setup

    ./gradlew installDist    

## Install voices

    cp stts_voices/voice-ar-nah-hsmm-5.2.jar build/install/marytts/lib/
    cp stts_voices/voice-dfki-spike-hsmm-5.1.jar build/install/marytts/lib/
    cp stts_voices/voice-stts_no_nst-hsmm-5.2.jar build/install/marytts/lib/
    cp stts_voices/voice-stts_sv_nst-hsmm-5.2-SNAPSHOT.jar build/install/marytts/lib/


## Running MaryTTS

Run `./gradlew run`  (or `gradlew.bat run` on Windows) to start a MaryTTS server.
Then access it at http://localhost:59125 using your web browser.


## Further information

* Further information on MaryTTS can be found in the <a href="README_marytts.md">original README file</a>.
* [MaryTTS](https://github.com/marytts/marytts) master branch on Github
