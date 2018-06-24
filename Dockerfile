FROM buildpack-deps

############# INITIAL SETUP/INSTALLATION #############
# setup apt
RUN apt-get update -y && apt-get upgrade -y && apt-get install apt-utils -y

# debugging tools
# RUN apt-get install -y libnet-ifconfig-wrapper-perl/stable curl wget emacs

# RELEASE variable (to be set by build args)
ARG RELEASE="undefined"

LABEL "se.stts.vendor"="STTS - Speech technology services - http://stts.se"
LABEL "se.stts.release"=$RELEASE



############# COMPONENT SPECIFIC DEPENDENCIES #############
RUN apt-get install -y python software-properties-common && add-apt-repository ppa:openjdk && apt-get install -y openjdk-8-jdk
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8


############# MARYTTS #############
ENV BASEDIR /wikispeech/marytts
WORKDIR $BASEDIR

# local copy of https://github.com/stts-se/marytts.git 
COPY . $BASEDIR

RUN mkdir -p $BASEDIR/bin

WORKDIR $BASEDIR

RUN ./gradlew installDist

## INSTALL STTS VOICES
RUN cp stts_voices/voice-ar-nah-hsmm-5.2.jar build/install/marytts/lib/
RUN cp stts_voices/voice-dfki-spike-hsmm-5.1.jar build/install/marytts/lib/
RUN cp stts_voices/voice-stts_no_nst-hsmm-5.2.jar build/install/marytts/lib/
RUN cp stts_voices/voice-stts_sv_nst-hsmm-5.2-SNAPSHOT.jar build/install/marytts/lib/

## SCRIPT FOR LISTING VOICES
RUN echo "echo 'AVAILABLE VOICES:' && ls $BASEDIR/build/install/marytts/lib/ | egrep ^voice | sed 's/.jar//' | sed 's/^/* /' " > $BASEDIR/bin/marytts_voices
RUN chmod +x $BASEDIR/bin/marytts_voices


############# MISHKAL #############
WORKDIR "/wikispeech"

# RUN git clone https://github.com/linuxscout/mishkal.git
# NO LONGER NEEDED (FIXED IN HB'S VERSION):
# RUN sed -i.BAK 's/self.display(word, format_display)/self.display(voc_word, format_display)/' mishkal/tashkeel/tashkeel.py

# RELEASE TAG/COMMIT ID/BRANCH NAME FOR MISHKAL | update if needed
ARG MISHKAL_RELEASE=9624fbd
RUN git clone https://github.com/HaraldBerthelsen/mishkal.git
WORKDIR "/wikispeech/mishkal"
RUN git checkout $MISHKAL_RELEASE

############# START SCRIPT #############
RUN echo "python /wikispeech/mishkal/interfaces/web/mishkal-webserver.py &" > $BASEDIR/bin/marytts-mishkal-start
RUN echo "sleep 2" >> $BASEDIR/bin/marytts-mishkal-start
RUN echo "cd $BASEDIR && ./gradlew run" >> $BASEDIR/bin/marytts-mishkal-start

RUN chmod +x $BASEDIR/bin/marytts-mishkal-start


############# POST INSTALL #############
WORKDIR "/wikispeech"

# BUILD INFO
ENV BUILD_INFO_FILE $BASEDIR/build_info.txt
RUN echo "Application name: marytts"  >> $BUILD_INFO_FILE
RUN echo -n "Build timestamp: " >> $BUILD_INFO_FILE
RUN date --utc "+%Y-%m-%d %H:%M:%S %Z" >> $BUILD_INFO_FILE
RUN echo "Built by: docker" >> $BUILD_INFO_FILE
RUN echo "Release: $RELEASE" >> $BUILD_INFO_FILE
RUN cat $BUILD_INFO_FILE

## LIST MARYTTS VOICES
RUN $BASEDIR/bin/marytts_voices


############# RUNTIME SETTINGS #############
WORKDIR $BASEDIR
EXPOSE 59125
CMD $BASEDIR/bin/marytts-mishkal-start

