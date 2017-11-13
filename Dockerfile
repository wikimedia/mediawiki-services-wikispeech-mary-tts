# Download sttsse/wikispeech_base from hub.docker.com | source repository: https://github.com/stts-se/wikispeech_base.git
FROM sttsse/wikispeech_base


WORKDIR "/"


## INSTALL MARYTTS
RUN git clone https://github.com/HaraldBerthelsen/marytts.git
#RUN git clone https://github.com/HannaLindgren/marytts.git

WORKDIR "/marytts"

RUN ./gradlew installDist
#RUN ./gradlew build

## INSTALL STTS VOICES
#RUN cp stts_voices/* build/install/marytts/lib/

RUN cp stts_voices/voice-ar-nah-hsmm-5.2.jar build/install/marytts/lib/
RUN cp stts_voices/voice-dfki-spike-hsmm-5.1.jar build/install/marytts/lib/
RUN cp stts_voices/voice-stts_no_nst-hsmm-5.2.jar build/install/marytts/lib/
RUN cp stts_voices/voice-stts_sv_nst-hsmm-5.2-SNAPSHOT.jar build/install/marytts/lib/

#RUN cp stts_voices/voice-stts-sv-hb-hsmm-5.1-SNAPSHOT.jar build/install/marytts/lib/

#RUN cp stts_voices/voice-nst_da_hsmm-hsmm-5.0-SNAPSHOT.jar build/install/marytts/lib/
#RUN cp stts_voices/voice-nst_no_hsmm-hsmm-5.0-SNAPSHOT.jar build/install/marytts/lib/
#RUN cp stts_voices/voice-nst-sv-1000-hsmm-5.0-SNAPSHOT.jar build/install/marytts/lib/

## SCRIPT FOR LISTING VOICES
RUN echo "echo 'AVAILABLE VOICES:' && ls build/install/marytts/lib/ | egrep ^voice | sed 's/.jar//' | sed 's/^/* /' " > /bin/voices
RUN chmod +x /bin/voices


## LIST VOICES
RUN voices


## RUNTIME SETTINGS
#EXPOSE 59125
#CMD ./gradlew run
CMD echo "marytts (core) was built without errors"
