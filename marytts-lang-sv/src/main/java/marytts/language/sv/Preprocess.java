/**
 * Copyright 2002 DFKI GmbH.
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

package marytts.language.sv;

import marytts.language.sv.LSEQ_Finder.*;

import java.util.regex.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import marytts.language.sv.tn.TNParser;
import marytts.language.sv.tn.TNNormalize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.modules.InternalModule;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;
import marytts.server.MaryProperties;

import org.apache.log4j.Level;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.io.*;
import java.util.*;

/**
 * Class that handles textnormalization. Please see the file 'tn_rules.pdf' in the subfolder 'tn'
 * for instructions on how to write new rules or how to make changes in the existing ones.
 * 
 * @author Erik Sterneberg
 *
 */
public class Preprocess extends InternalModule {
	private static HashSet<String> readAsLSEQWords = generateLSEQList();
	// Initializing a LSEQ_Finder object
	private static LSEQ_Finder lseq_finder = new LSEQ_Finder(); 

	public Preprocess(){
		super("Preprocess",
				MaryDataType.TOKENS,
				MaryDataType.WORDS,
				new Locale("sv", "SE"));
		//        System.out.println("Swedish Preprocess Milestone 1.");
	}

	public MaryData process(MaryData d)	throws Exception {
		Document doc = d.getDocument();

		// Loading TN-rules from tn_rules.xml
		String path = MaryProperties.maryBase() + "/java/marytts/language/sv/tn/tn_rules.xml";
		HashMap<String, String[]> rules = TNParser.query(path);

		// Loading regular expressions from tn_rules_regex.txt
		path = MaryProperties.maryBase() + "/java/marytts/language/sv/tn/tn_rules_regex.txt";
		HashMap<String, String> regex = TNParser.loadRegexes(path);	

		// Original inputstring
		NodeList tokenElements = doc.getElementsByTagName(MaryXML.TOKEN);
		
		String input = "";

		for (int i=0; i < tokenElements.getLength(); i++){
			Element tokenElement = (Element) tokenElements.item(i);			
			input = input + " " + MaryDomUtils.tokenText(tokenElement);
		}
		input = input.trim();

		// Normalize string using method TNNormalize
		TNNormalize normalizer = new TNNormalize(regex, rules);
		String string = input;
		assert (string.length() > 0); 	
		String returned_string = null;

		// If the returned string is unchanged the loop is exited. 
		do {
			returned_string = normalizer.normalize(doc);
			//System.out.println("Swedish Preprocess Milestone 2.1.");
			if (returned_string != null)
				string = returned_string;
		}
		while(returned_string != null);
		//System.out.println("Swedish Preprocess Milestone 3.");

		/*
	if (string.equals(input) != true){
	    System.out.println("The inputstring was processed by TNNormalize.");
	}
	else {
	    System.out.println("The inputstring was unchanged by TNNormalize.");
	}
		 */

		tokenElements = doc.getElementsByTagName(MaryXML.TOKEN);
		for (int i=0; i < tokenElements.getLength(); i++){
			Element tokenElement = (Element) tokenElements.item(i);
			// Tag the token with LSEQ or ASWD if it is a word, i.e. if it contains letters
			if (tokenElement.getTextContent().matches(".*\\w.*")){
				identifyClass(tokenElement);
			}
		}	

		MaryData result = new MaryData(outputType(), d.getLocale());
		result.setDocument(doc);
		return result;
	}

	/**
	 * Determines if a token should be read out as a word  (ASWD) or as a sequence of letters (LSEQ),
	 * then tags the token with the 'say-as' attribute. 
	 * 
	 * @param t
	 */
	private void identifyClass(Element t){    	
		String text = MaryDomUtils.tokenText(t);

		// If the string contains no letters nothing is to be done
		if (! text.matches("^.*[A-ZÅÄÖa-zåäö]+.*$")){
			return;
		}    	

		// If a string contains no vowels, like "SSH", it is labeled "LSEQ" 
		else if (! containsVowel(text) ||
				inLSEQList(text)){
			//System.out.println("1. Setting say-as attribute to \"LSEQ\".");
			t.setAttribute("say-as", "LSEQ");
		}    	
		// Words with the structure "LSEQ-ASWD", such as "HTS-röst", is to be labeled "LSEQ-ASWD"    	
		else if(text.matches("^[A-ZÅÄÖ]+[-][a-zåäö]+$")){
			Pattern pattern = Pattern.compile("^([A-ZÅÄÖ]+)[-][a-zåäö]+$");
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()){
				System.out.println("matcher.group(1): " + matcher.group(1));
				if (! containsVowel(matcher.group(1)) || inLSEQList(matcher.group(1))){
					//System.out.println("2. Setting say-as attribute to \"LSEQ+ASWD\".");
					t.setAttribute("say-as", "LSEQ+ASWD");
				}
				else if (isAllUpperCase(matcher.group(1))){    			
					if (!lseq_finder.phonotactically_possible(matcher.group(1))){
						//System.out.println("2. Setting say-as attribute to \"LSEQ+ASWD\".");
						t.setAttribute("say-as", "LSEQ+ASWD");    				
					}
					else{
						//System.out.println("3. Setting say-as attribute to \"ASWD\".");
						t.setAttribute("say-as", "ASWD");
					}
				}
			}    		
			//else
				//System.out.println("Preprocess: Couldn't find the first part in the \"LSEQ+ASWD\" token.");
		}    	
		else if (isAllUpperCase(text)){
			if (! lseq_finder.phonotactically_possible(text)){
				//System.out.println("4. Setting say-as attribute to \"LSEQ\".");
				t.setAttribute("say-as", "LSEQ");
			}
			else{
				//System.out.println("5. Setting say-as attribute to \"ASWD\".");
				t.setAttribute("say-as", "ASWD");
			}
		}
		else{
			//System.out.println("6. Setting say-as attribute to \"ASWD\".");
			t.setAttribute("say-as", "ASWD");
		}
	}        

	private boolean isAllUpperCase(String s){
		for (int i=0; i< s.length()-1; i++){
			if (Character.isLowerCase(s.charAt(i)))
				return false;
		}    	
		return true;
	}

	private boolean containsVowel(String text){
		Pattern pattern = Pattern.compile("[EYUIOÅÄÖAeyuioåäöa]");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find())
			return true;    	
		else
			return false;
	}

	private static boolean inLSEQList(String word){
		if (readAsLSEQWords.contains(word)){
			//System.err.println(word + " is in the list of words that are to be read out as a letter sequence.");
			return true;
		}
		else{
			//System.err.println(word + " is not in the list of words that are to be read out as a letter sequence.");
			return false;
		}
	}

	private static HashSet<String> generateLSEQList(){
		HashSet<String> LSEQWords = new HashSet<String>();
		try{
		    //BufferedReader infile = new BufferedReader(new FileReader(MaryProperties.needFilename("sv.LSEQ")));
		    BufferedReader infile = new BufferedReader(new InputStreamReader(MaryProperties.needStream("sv.LSEQ")));
			String s;
			while ((s = infile.readLine()) != null){
				s = s.trim();
				if (s.length() > 0){
					if (Character.isLetter(s.charAt(0))){
						LSEQWords.add(s);
						//System.out.println("Adding '" + s + "' to list of LSEQ words.");
					}
				}
			}
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
		return LSEQWords;
	}
}
