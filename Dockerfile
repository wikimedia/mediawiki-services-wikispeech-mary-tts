FROM openjdk

RUN apt-get update -y && apt-get upgrade -y && apt-get install apt-utils -y
RUN apt-get install wget


## INSTALL MARYTTS
#RUN git clone https://github.com/HaraldBerthelsen/marytts.git
RUN git clone https://github.com/HannaLindgren/marytts.git
RUN cd marytts && ./gradlew installDist


## INSTALL STTS VOICES
RUN cp marytts/stts_voices/* marytts/build/install/marytts/lib/

RUN echo "echo 'AVAILABLE VOICES:' && ls marytts/build/install/marytts/lib/ | egrep ^voice | sed 's/.jar//' | sed 's/^/* /' " > /bin/voices
RUN chmod +x /bin/voices

## LIST VOICES
RUN voices


## CMD SETTINGS
EXPOSE 59125

CMD (cd marytts && ./gradlew run)
