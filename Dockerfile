FROM openjdk

RUN apt-get update -y && apt-get upgrade -y && apt-get install apt-utils -y
RUN apt-get install -y wget python git


## INSTALL MARYTTS
#RUN git clone https://github.com/HaraldBerthelsen/marytts.git
RUN git clone https://github.com/HannaLindgren/marytts.git
RUN cd marytts && ./gradlew installDist

## INSTALL STTS VOICES
RUN cp marytts/stts_voices/* marytts/build/install/marytts/lib/


## INSTALL ARABIC VOCALIZER/MISHKAL
#RUN cd marytts/build/install/marytts/lib && wget http://repo1.maven.org/maven2/org/glassfish/javax.json/1.0.4/javax.json-1.0.4.jar

#RUN git clone https://github.com/linuxscout/mishkal.git
#RUN mv mishkal/mishkal/tashkeel/tashkeel.py mishkal/mishkal/tashkeel/tashkeel.py.BAK && sed 's/vocalized_text = u" ".join([vocalized_text, self.display(word, format_display)])/vocalized_text = u" ".join([vocalized_text, self.display(voc_word, format_display)])/' mishkal/mishkal/tashkeel/tashkeel.py.BAK > mishkal/mishkal/tashkeel/tashkeel.py


## SCRIPT FOR LISTING VOICES
RUN echo "echo 'AVAILABLE VOICES:' && ls marytts/build/install/marytts/lib/ | egrep ^voice | sed 's/.jar//' | sed 's/^/* /' " > /bin/voices
RUN chmod +x /bin/voices


## LIST VOICES
RUN voices


## CMD SETTINGS
EXPOSE 59125

CMD (cd marytts && ./gradlew run)
