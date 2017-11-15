# Download sttsse/wikispeech_base from hub.docker.com | source repository: https://github.com/stts-se/wikispeech_base.git
FROM sttsse/wikispeech_base


##################### INSTALL MARYTTS #####################
WORKDIR "/"
RUN git clone https://github.com/stts-se/marytts.git

WORKDIR "/marytts"

RUN ./gradlew installDist

## INSTALL STTS VOICES
RUN cp stts_voices/voice-ar-nah-hsmm-5.2.jar build/install/marytts/lib/
RUN cp stts_voices/voice-dfki-spike-hsmm-5.1.jar build/install/marytts/lib/
RUN cp stts_voices/voice-stts_no_nst-hsmm-5.2.jar build/install/marytts/lib/
RUN cp stts_voices/voice-stts_sv_nst-hsmm-5.2-SNAPSHOT.jar build/install/marytts/lib/

## SCRIPT FOR LISTING VOICES
RUN echo "echo 'AVAILABLE VOICES:' && ls /marytts/build/install/marytts/lib/ | egrep ^voice | sed 's/.jar//' | sed 's/^/* /' " > /bin/voices
RUN chmod +x /bin/voices


##################### INSTALL MISHKAL #####################
WORKDIR "/"

# RUN git clone https://github.com/linuxscout/mishkal.git
RUN git clone https://github.com/HaraldBerthelsen/mishkal.git

WORKDIR "mishkal"

RUN sed 's/self.display(word, format_display)/self.display(voc_word, format_display)/' mishkal/tashkeel/tashkeel.py > mishkal/tashkeel/tashkeel.py_UPDATE
RUN mv mishkal/tashkeel/tashkeel.py  mishkal/tashkeel/tashkeel.py_OLD
RUN cp mishkal/tashkeel/tashkeel.py_UPDATE mishkal/tashkeel/tashkeel.py

RUN echo "python /mishkal/interfaces/web/mishkal-webserver.py &" > /bin/marytts-mishkal-start
RUN echo "sleep 2" >> /bin/marytts-mishkal-start
RUN echo "cd /marytts && ./gradlew run" >> /bin/marytts-mishkal-start

RUN chmod +x /bin/marytts-mishkal-start


##################### AFTER INSTALL #####################

WORKDIR "/"


# BUILD INFO
RUN echo -n "Build timestamp: " > /var/.marytts_build_info.txt
RUN date --utc "+%Y-%m-%d %H:%M:%S %Z" >> /var/.marytts_build_info.txt
RUN echo "Built by: docker" >> /var/.marytts_build_info.txt
RUN echo "Application name: marytts"  >> /var/.marytts_build_info.txt


## LIST MARYTTS VOICES
RUN /bin/voices


## RUNTIME SETTINGS
EXPOSE 59125
CMD /bin/marytts-mishkal-start

