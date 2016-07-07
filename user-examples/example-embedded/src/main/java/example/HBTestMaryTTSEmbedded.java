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

/*
harald@harald-Vox:/media/bigdisk/git_repos/marytts/user-examples$ java -Dmary.base="../target/marytts-5.2-SNAPSHOT" -cp "../target/marytts-5.2-SNAPSHOT/lib/*:example-embedded/target/example-embedded-5.2-SNAPSHOT.jar" example.HBTestMaryTTSEmbedded
*/


import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.exceptions.SynthesisException;


public class HBTestMaryTTSEmbedded {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		MaryInterface marytts = new LocalMaryInterface();
		Set<String> voices = marytts.getAvailableVoices();

		System.out.println(voices);

		//marytts.setVoice(voices.iterator().next());

		//av någon anledning heter den svenska rösten "no" på min dator, inte på morf.. FIXA DET
		marytts.setVoice("stts_no_nst-hsmm");
		//marytts.setOutputType(MaryDataType.REALISED_DURATIONS.name());
		marytts.setOutputType(MaryDataType.WIKISPEECH_JSON.name());

		String text = "Godmorgon, godmiddag.";
		String res = marytts.generateText(text);
		System.out.println(res);

		/*
		Locale locale = new Locale("sv");
		MaryData in = new MaryData(MaryDataType.TEXT, locale);
		try {
			in.setData(text);
		} catch (Exception ioe) {
			throw new SynthesisException(ioe);
		}
		MaryData out = marytts.process(in);
		*/

		/*
		AudioInputStream audio = marytts.generateAudio("Godmorgon, godmiddag.");
		AudioPlayer player = new AudioPlayer(audio);
		player.start();
		player.join();
		*/

	}

}
