package example;
import java.util.Set;
import java.util.Locale;

import javax.sound.sampled.AudioInputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat.Type;
import java.io.File;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.AudioPlayer;


public class MaryTTSEmbedded {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

	    MaryInterface marytts = new LocalMaryInterface();

	    Set<String> voices = marytts.getAvailableVoices();
	    System.out.println("Available voices: "+voices);
	    //marytts.setVoice(voices.iterator().next());
	    //AudioInputStream audio = marytts.generateAudio("Hello world.");
	    
	    //marytts.setLocale(new Locale("sv"));
	    //AudioInputStream audio = marytts.generateAudio("Det här är ett test av svensk talsyntes.");

	    marytts.setLocale(new Locale("ga"));
	    marytts.setVoice("nnc-hsmm");
	    AudioInputStream audio = marytts.generateAudio("Dia dhíbh go léir a chairde.");
	    
	    /*
	      AudioPlayer player = new AudioPlayer(audio);
	      player.start();
	      player.join();
	      System.out.println("Finished playing, exiting..");
	    */
	    
	    Type filetype = Type.WAVE;
	    File file = new File("/tmp/apa.wav");
	    AudioSystem.write(audio, filetype, file);
	    System.out.println("Finished writing to file, exiting..");
	    
	    
	    
	    System.exit(0);
	}

}
