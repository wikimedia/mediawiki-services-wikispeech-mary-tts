#!/bin/bash
##########################################################################
# MARY TTS server
##########################################################################

# Set the Mary base installation directory in an environment variable:
BINDIR="`dirname "$0"`"
export MARY_BASE="$BINDIR/.."

#Funkar men jobbig att kompilera
#java -showversion -ea -Xms40m -Xmx4g -cp "$MARY_BASE/target/marytts-5.2-SNAPSHOT/lib/*" -Dmary.base="$MARY_BASE" marytts.modules.WikispeechJsonExtractor $*

#mvn compile i marytts-runtime
java -showversion -ea -Xms40m -Xmx4g -cp "$MARY_BASE/marytts-runtime/target/classes:$MARY_BASE/target/marytts-5.2-SNAPSHOT/lib/*" -Dmary.base="$MARY_BASE" marytts.modules.WikispeechJsonExtractor $*

#mvn package i marytts-runtime
#java -showversion -ea -Xms40m -Xmx4g -cp "$MARY_BASE/target/marytts-5.2-SNAPSHOT/lib/*:$MARY_BASE/marytts-runtime/target/marytts-runtime-5.2-SNAPSHOT-jar-with-dependencies.jar" -Dmary.base="$MARY_BASE" marytts.modules.WikispeechJsonExtractor $*

