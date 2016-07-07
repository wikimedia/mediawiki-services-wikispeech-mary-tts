/**
 * Copyright 2000-2006 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * This file is part of MARY TTS.
 *
 * MARY TTS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package marytts.modules;

// DOM classes
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.util.dom.NameNodeFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import marytts.util.data.audio.MaryAudioUtils;
import javax.sound.sampled.AudioInputStream;
import marytts.util.MaryUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import java.util.ArrayList;

/**
 * Transforms a full MaryXML document into json for wikispeech, with link to audio file and token timing information
 * 
 * @author Harald Berthelsen
 */

public class WikispeechJsonExtractor extends InternalModule {
	public WikispeechJsonExtractor() {
		super("Wikispeech json extractor", MaryDataType.AUDIO, MaryDataType.WIKISPEECH_JSON, null);
	}

	private static final Logger logger = MaryUtils.getLogger("Wikispeech Extractor");


	public MaryData process(MaryData d) throws Exception {
	    String outputFileName = "/tmp/marytts_tmpfile.wav";
	    //AudioInputStream ais= d.getAudio();

	    //problem with audio, length is -1.. 
	    //OutputStream os = new FileOutputStream(outputFileName);
	    //d.writeTo(os);

	    //just stops, doesn't seem to do anything, the file is there but empty
	    //MaryAudioUtils.writeWavFile(MaryAudioUtils.getSamplesAsDoubleArray(d.getAudio()), outputFileName, d.getAudio().getFormat());

	    //Thread.sleep(5000);
	    /*
	    logger.debug("MaryData type: "+d.getType());
	    AudioInputStream audio = d.getAudio();
	    logger.debug("audio: " + audio);
	    logger.debug("audio format: " + audio.getFormat());
	    logger.debug("audio frame length: " + audio.getFrameLength());
	    */
	    //stops here and nothing happens..
	    //need to wait for the vocoder thread to finish..
	    //how to do that?
	    /*
	    double[] samples = MaryAudioUtils.getSamplesAsDoubleArray(audio);
	    logger.debug("Samples array length: " + samples.length);

	    try {
		MaryAudioUtils.writeWavFile(samples, outputFileName, audio.getFormat());
		logger.debug("Output written to " + outputFileName);
	    } catch (IOException e) {
		System.err.println("Could not write to file: " + outputFileName + "\n" + e.getMessage());
		System.exit(1);
	    }
	    */

	    //I can't get any of the above to work..
	    //Now writing to tmpfile in HTSEngine.java

	    Document doc = d.getDocument();

	    MaryData result = new MaryData(outputType(), d.getLocale());
	    StringBuilder buf = new StringBuilder();
	    //buf.append("#\n");
	    NodeIterator ni = ((DocumentTraversal) doc).createNodeIterator(doc, NodeFilter.SHOW_ELEMENT, new NameNodeFilter(											     new String[] { MaryXML.SENTENCE, MaryXML.BOUNDARY, MaryXML.TOKEN }), false);

	    Element element = null;
	    float end = 0.f;
	    float sentenceEnd = 0;
	    //float tokenEnd = 0;
	    Element phone = null;



	    JSONObject obj = new JSONObject();
	    obj.put("audio", "/tmp/marytts_tmpfile_HTSEngine.wav");
	    ArrayList<JSONObject> tokens = new ArrayList<JSONObject>();
	    obj.put("tokens", tokens);




	    while ((element = (Element) ni.nextNode()) != null) {
		String durString = null;
		String endString = null;

		String word = null;
		boolean printme = false;

		//if (element.getTagName().equals(MaryXML.PHONE)) {
		//    sampa = element.getAttribute("p");
		    // durString = element.getAttribute("d"); // less accurate than end
		//    endString = element.getAttribute("end");
		//} else 

		if (element.getTagName().equals(MaryXML.SENTENCE)) {
		    sentenceEnd += end;
		} else if (element.getTagName().equals(MaryXML.TOKEN)) {


		    word = element.getTextContent();
		    //always print the token, even if it has no phonemes (punctuation etc)
		    printme = true;

		    NodeList nList = element.getElementsByTagName("ph");

		    for (int i = 0; i < nList.getLength(); i++) {
			phone = (Element) nList.item(i);
			if ( phone.hasAttribute("end") ) {
			    String symbol = phone.getAttribute("p");
			    endString = phone.getAttribute("end");
			    //System.err.println("Reading symbol: "+symbol+" end: "+endString);
			    end = Float.parseFloat(endString);



			}
		    }


		} else {
		    assert element.getTagName().equals(MaryXML.BOUNDARY);
		    durString = element.getAttribute("duration");
		    float dur = Float.parseFloat(durString) * 0.001f;
		    end += dur;
		    word = "";
		    printme = true;
		}
		//boolean printme = false;
		//if (endString != null && !endString.equals("")) {
		//    end = Float.parseFloat(endString);
		    //printme = true;
		//} else if (durString != null && !durString.equals("")) {
		//    float dur = Float.parseFloat(durString) * 0.001f;
		//    end += dur;
		    //printme = true;
		//}
		if (printme) {
		    //buf.append((end + sentenceEnd) + " 125 " + sampa + "\n");
		    buf.append(word + " " + (end + sentenceEnd) + "\n");

		    JSONObject token = new JSONObject();
		    token.put("string", word);
		    token.put("end", end);
		    tokens.add(token);

		}
	    }

	    //result.setPlainText(buf.toString());
	    result.setPlainText(obj.toString());
	    return result;
	}


    public static void main(String[] args) {
	WikispeechJsonExtractor wje = new WikispeechJsonExtractor();
        System.out.println("Hello hello: "+wje.getOutputType()); 
    }


}
