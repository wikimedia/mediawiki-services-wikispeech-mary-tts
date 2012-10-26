package marytts.language.sv.tn;

import java.util.*;
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

import org.apache.log4j.Level;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;


/**
 *@author Erik Sterneberg
 *
 * This class will make use of text normalization rules and regular expressions parsed from an xml file and a text file respectively.
 * Its method normalize will process an input string once; substituting the string matching first regular expression with a string
 * where numbers etc. have been expanded or 'normalized'. The method normalize must be run until it returns a null value.
 *
 */
public class TNNormalize {
	private HashMap<String, String> regex;
	private HashMap<String, String[]> rules;	

	/**
	 * Constructor for the class TNNormalize. Only initializes variables used internally by the class.
	 * 
	 * @param rules Language specific rules parsed from an xml file
	 * @param regex Regular expressions pertaining to text normalization rules	 
	 */
	public TNNormalize (HashMap<String, String> regex, HashMap<String, String[]> rules){		
		this.regex = regex;
		this.rules = rules;		
	}
	
	/**
	 * This method will try to match the input string with the categories from the xml file in the order they are written.
	 * When a category is found, that rule is queued and a while-loop will start to process the string. The while-loop will:
	 * 1) Add to queue the first matching referenced rule (if the rules are uppercase),
	 * 2) or add all referenced rules to queue (if the rules are lowercase),
	 * 3) or take input and give words as output (if the queued rule is 'INPUT_OUTPUT_TYPE').
	 * Whenever a rule has been processed it will be removed from the queue. If the inputstring was unchanged after processing,
	 * this method will return a null value instead of the processed string.
	 * 
	 * @param inputstring
	 * @return string or null
	 */
    public String normalize(Document doc){		
	// System.out.println("Method 'normalize' called. TEST");

	NodeList tokenElements = doc.getElementsByTagName(MaryXML.TOKEN);

	String inputstring = "";

	for (int i=0; i < tokenElements.getLength(); i++){
	    Element tokenElement = (Element) tokenElements.item(i);
	    // System.out.println(MaryDomUtils.tokenText(tokenElement));
	    inputstring = inputstring + " " + MaryDomUtils.tokenText(tokenElement);
	}
	inputstring = inputstring.trim();

	int nbr_matched_tokens = 0;
	int first_matched_token_index = 0;
	
		String new_string = inputstring;		
		LinkedList<String> action_queue = new LinkedList<String>();
		LinkedList<String> new_actions = new LinkedList<String>();		
		String[] categories = rules.get("start");		
		String matched_string = null;		
		int matched_string_start = 0; 
		int matched_string_end = 0 ;
		int len_before;
		int len_after;
				
		// First try to queue a category and set matched_string
		// System.out.println("There are " + categories.length + " categories.");
		
		for (int i=0; i < categories.length; i++){
			// System.out.println("Processing category: " + categories[i]);
			if (categories[i] != null){
				Pattern pattern = Pattern.compile(regex.get(categories[i]));			
				Matcher matcher = pattern.matcher(inputstring);
						
				if(matcher.find()){					
					action_queue.add(categories[i]);
					// System.out.println("String matched category: " + categories[i]);
					matched_string = matcher.group();
					matched_string_start = matcher.start();
					matched_string_end = matcher.end();

					// Also count the tokens matched
					nbr_matched_tokens = inputstring.substring(matched_string_start, matched_string_end).split(" ").length;
					// and identify the index in the nodelist of the first token, so these tokens can be replaced by new nodes
					// this can be done by counting the words in the substring BEFORE the substring matched
					if (matched_string_start == 0){
					    first_matched_token_index = 0;
					}
					else{
					    first_matched_token_index = inputstring.substring(0, matched_string_start).split(" ").length;
					}

					// System.out.println("String matched category: " + categories[i] + ". first_matched_token_index is: " + first_matched_token_index);
					String[] test = inputstring.substring(0, matched_string_start).split(" ");
					for (int j=0; j < test.length; j++){
					    // System.out.println("The string '" + test[j] + "' has length: " + test[j].length());
					}
					
					break;
				}
				else{
					// System.out.println("String did not match the category category " + categories[i] + ".");
				}
			}
		}
		
		if (action_queue.size() == 0){
			// No category matching the input string was found
			return null;
		}
		
		// Process queue in a loop until any of the conditions have been reached
		new_string = matched_string;
		String new_words = "";		
		
		// The following while-loop runs and processes the input string until when there are 
		// no more queued actions or when the entire original inputstring has been processed.		
		while (action_queue.size() > 0 & new_string.length() > 0){
		    // System.out.println("Swedish Preprocess milestone 11.");
		    Boolean isInputOutputRule = false;
		    try {
		    if (action_queue.get(0).equals("NEEDS_SEGMENTATION")){
		    	new_words = segment(new_string);	
			new_string = new_words;
			break;
		    }
		    else if (rules.get(action_queue.get(0))[0] == "INPUT_OUTPUT_RULE")
			    isInputOutputRule = true;

			for (int i=0; i < action_queue.size(); i++){
			    // System.out.println("Action nbr " + i + " in queue is: " + action_queue.get(i));
			}
		    }
		    catch (Exception e){
			// System.err.println("Error 1: " + e.getMessage());
		    }

			// Rule takes input and gives output
		    //			if (rules.get(action_queue.get(0))[0] == "INPUT_OUTPUT_RULE"){				
			if (isInputOutputRule){				
			    // System.out.println("Swedish Preprocess milestone 12.");
				if (new_string.startsWith(rules.get(action_queue.get(0))[1])){					
				    // System.out.println("Swedish Preprocess milestone 13.");
					// Shorten 'new_string' with length of input
					int length_of_input = rules.get(action_queue.get(0))[1].length();
					new_string = new_string.substring(length_of_input, new_string.length());  

					// Unless the rule output is just blank space, add the output from the rule to 'new_words',
					// representing the progress so far parsing the input string into words
					if (rules.get(action_queue.get(0))[2].equals(" ") == false)
						new_words = new_words + rules.get(action_queue.get(0))[2];					
				}
				
				// If the input of the rule is blanke space, the rule takes no input and 'new_words' should get the output added
				else if(rules.get(action_queue.get(0))[1].startsWith(" "))
					new_words = new_words + rules.get(action_queue.get(0))[2];				
				
				// The rule has been processed and should be removed from the action queue
				len_before = action_queue.size(); 
				action_queue.remove(0);				
				len_after = action_queue.size();
				assert (len_before - 1 == len_after): "Removing the first item in the action queue failed.";
			}
			else{
				String [] tempArray = rules.get(action_queue.get(0));
				
				for (int i=0; i < tempArray.length; i++){
					new_actions.add(tempArray[i]);	
				}
				
				// TEMPORARY BUG FIX				
				String temp = action_queue.get(0);				
				if (temp.equals(new_actions.get(0))){
					//print("Bug encountered: " + temp + " and " + new_actions.get(0) + " are the same");
					
					len_before = new_actions.size();
					new_actions.remove(0);
					len_after = new_actions.size();
					assert (len_before - 1 == len_after): "The removal of the first item in the new action queue failed.";
				}
				
				// The rule has been processed and should be removed from the action queue
				len_before = action_queue.size(); 
				action_queue.remove(0);				
				len_after = action_queue.size();
				assert (len_before - 1 == len_after): "Removing the first item in the action queue failed.";				

				// System.out.println("Swedish Preprocess milestone 10.");

				// If the rules queued in new_actions are upper case, add the first matching rule to the action queue
				if (new_actions.size() > 0){
					if (Character.isUpperCase(new_actions.get(0).charAt(0))){
						while (new_actions.size() > 0){
							Pattern pattern = Pattern.compile(regex.get(new_actions.get(0)));			
							Matcher matcher = pattern.matcher(new_string);
							boolean found = matcher.find();

							if (found){
								if (matcher.start() == 0){
									if (action_queue.size() == 0){
										action_queue.add(new String(new_actions.get(0)));										
									}
									else{ 
										action_queue.add(0, new String(new_actions.get(0)));									
									}
									// System.out.println("The string " + new_string + " was matched by the regex " + regex.get(new_actions.get(0)) + " and the rule " + new_actions.get(0));
									// System.out.println("Swedish Preprocess milestone 1.");
									new_actions = new LinkedList<String>();
									// System.out.println("Swedish Preprocess milestone 2.");
								}
								else{
									len_before = new_actions.size();
									new_actions.remove(0);
									len_after = new_actions.size();
									assert (len_before - 1 == len_after): "The removal of the first item in the new action queue failed.";
								}
							}
							else {
								len_before = new_actions.size();
								new_actions.remove(0);
								len_after = new_actions.size();
								assert (len_before - 1 == len_after): "The removal of the first item in the new action queue failed.";	      
							}
							// System.out.println("Swedish Preprocess milestone 3.");						
						}
					}
				
					// Otherwise, delete action_queue and insert all of the new actions in the proper order
					else {					
						for (int i=0; i < new_actions.size(); i++){
							if (new_actions.get(i) == null) 
								break;
							else{
								action_queue.add(i, new String(new_actions.get(i)));
								// System.out.println("Adding " + new_actions.get(i) + " to action_queue.");
							}
								
						}										
						new_actions = new LinkedList<String>();
					}
				}
			}


			
			// System.out.println("Swedish Preprocess milestone 9.");
			// If new_string now begins with a comma, point or space, that character can safely be removed
			try{ 
			    // System.out.println("Swedish Preprocess milestone 4.");
				if (new_string.substring(0, 1).equals(".")
						|| new_string.substring(0, 1).equals(",")
						|| new_string.substring(0, 1).equals(" ")){					
					new_string = new_string.substring(1, new_string.length());
				}
			    // System.out.println("Swedish Preprocess milestone 5.");
			}
			catch (StringIndexOutOfBoundsException e){
				break;
			}
		}
		
		if (new_string == null){
		    // System.out.println("new_string is null");
		}
		else if (new_string.length() == 0){
		    // System.out.println("The length of new_string is 0.");
		}
		// If after processing the new string has been unaltered, return null value to signify that no more processing needs to be done. 
		if (new_string.equals(inputstring)){
		    // System.out.println("Swedish Preprocess milestone 6.");
		    return null;		
		}
		else{
		    // Before returning the new string, make changes to the nodes 
		    // System.out.println("Swedish Preprocess milestone 7.");
		    replaceNodes(new_words, tokenElements, nbr_matched_tokens, first_matched_token_index, doc);
		    // System.out.println("Swedish Preprocess milestone 8.");
		    
		    // Use regexes to substitute the substring 'matched_string' in 'inputstring' with the string 'new_words', then return the result.
		    return inputstring.substring(0, matched_string_start) + new_words + inputstring.substring(matched_string_end, inputstring.length()); 			
		}
	}


 private static void replaceNodes(String new_words, NodeList oldSentenceTokens, int nbr_matched_tokens, int first_matched_token_index, Document doc){
     // System.out.println("Input to the method 'replaceNodes':");
     // System.out.println(new_words);
     // System.out.println("nbr_matched_tokens: " + nbr_matched_tokens);
     // System.out.println("first_matched_token_index: " + first_matched_token_index);

	// Split 'sentence' into tokens and put into a string vector
    // First remove redundant whitespace
    new_words = new_words.replaceAll("[ ]+", " ");
	String[] words = new_words.split(" ");


	// Iterate over the string vector, create new token nodes before the first token
	Element firstOldToken = (Element) oldSentenceTokens.item(first_matched_token_index);
	
	for (int i=0; i < words.length; i++){
	    Element newToken = MaryXML.createElement(doc, MaryXML.TOKEN);
	    MaryDomUtils.setTokenText(newToken, words[i]);
	    // System.out.println("Trying to create new token element with the text: " + words[i]);
	    firstOldToken.getParentNode().insertBefore(newToken, firstOldToken);
	}

	// Delete old element tokens AFTER inserting new ones
	// delete as many old nodes as there are 'nbr_matched_tokens'
	// First delete all siblings to the right of the first node
	for (int i=0; i < nbr_matched_tokens - 1; i++){
	    Element siblingElement = MaryDomUtils.getNextSiblingElement(firstOldToken);
	    // System.out.println("Trying to remove node with text: " + MaryDomUtils.tokenText(siblingElement));
	    siblingElement.getParentNode().removeChild(siblingElement);
	}
	// Then delete the first node itself
	    // System.out.println("Trying to remove node with text: " + MaryDomUtils.tokenText(firstOldToken));
	firstOldToken.getParentNode().removeChild(firstOldToken);
 }
 
 private static String segment(String s){
	 // System.out.println("Method 'segment' called on the string: " + s);	 
	 //Pattern pattern = pattern.compile("([^\d]+");			
	 //Matcher matcher = pattern.matcher(s);
	 
	 String new_s = s.replaceFirst("([^\\d,.]+)", " $1 ");
	 
	 // System.err.println("Method 'segment' changed string '" + s + "' to '" + new_s + "'.");
	 
	 /*
	 int i = 0;
	 //for (int i=0; i<s.length(); i++){
	 while (true){		 
		if (i != 0 && Character.isDigit(s.charAt(i)) && Character.isDigit(s.charAt(i + 1) == false)){
			
		}			 
		i++;
	 }
	 */
	 return new_s.trim();
 }
}
