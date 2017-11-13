# Download sttsse/wikispeech_base from hub.docker.com | source repository: https://github.com/stts-se/wikispeech_base.git
FROM sttsse/wikispeech_base


WORKDIR "/"


## INSTALL MARYTTS
RUN git clone https://github.com/HaraldBerthelsen/marytts.git
#RUN git clone https://github.com/HannaLindgren/marytts.git

WORKDIR "/marytts"

RUN ./gradlew installDist

## INSTALL STTS VOICES
RUN cp stts_voices/* build/install/marytts/lib/



## SCRIPT FOR LISTING VOICES
RUN echo "echo 'AVAILABLE VOICES:' && ls build/install/marytts/lib/ | egrep ^voice | sed 's/.jar//' | sed 's/^/* /' " > /bin/voices
RUN chmod +x /bin/voices


## LIST VOICES
RUN voices


## RUNTIME SETTINGS
EXPOSE 59125
CMD ./gradlew run
