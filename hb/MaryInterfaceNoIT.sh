#!/bin/bash
##########################################################################
# MARY TTS server
##########################################################################

# Set the Mary base installation directory in an environment variable:
BINDIR="`dirname "$0"`"
export MARY_BASE="$BINDIR/../target/marytts-builder-5.2-SNAPSHOT"

java -showversion -ea -Xms40m -Xmx4g -cp "$MARY_BASE/lib/*" -Dmary.base="$MARY_BASE" marytts.language.no.MaryInterfaceNoIT $*

