Examples for using MARY TTS 5 with the new MaryInterface.

Build with

    mvn package
    
    
then run using


    java -jar example-embedded/target/example-embedded-<VERSION>.jar
    
and (assuming you have a marytts server running at localhost on port 59125):

    java -jar example-remote/target/example-remote-<VERSION>.jar
    



#HB:

this command works:

java -Dmary.base="target/marytts-5.1-SNAPSHOT" -cp "target/marytts-5.1-SNAPSHOT/lib/*:user-examples/example-embedded/target/example-embedded-5.1-SNAPSHOT.jar" example.MaryTTSEmbedded
