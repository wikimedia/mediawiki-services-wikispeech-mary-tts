/**
 * Copyright 2000-2009 DFKI GmbH.
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

import java.io.*;
import java.util.*;
import marytts.server.MaryProperties;
import marytts.util.MaryUtils;
import java.util.regex.*;

/**
 * Class used to find words that are phonotactically impossible, and that should consequently be read as a sequence of letters (LSEQ).
 * Any word having an impossible (or at least highly improbable) initial, mid-word consonant cluster or final will be labeled 'LSEQ'.
 * 
 * @author Erik Sterneberg
 *
 */
public class LSEQ_Finder {
	// These three HashSets contains the most common consonant initials, (mid-word) clusters and finals
	// in the Swedish language, extracted from a large corpus, the threshold for making the cut being 
	// ten instances.
	private HashSet<String> possibleInitials;
	private HashSet<String> possibleClusters;
	private HashSet<String> possibleFinals;
		
	private String consonants;
	private Pattern pattern;
	private Matcher matcher;
	private HashSet<String> vowels;
	
	/**
	 * 
	 * Constructor for the LSEQ_Finder, an object containing all the information needed to find
	 * impossible or improbable substrings of consonants in a word and thus identifying the word as
	 * a word that should be read out letter by letter.
	 * 
	 */
	public LSEQ_Finder(){
	    try {
		//this.possibleInitials = generateHashSet(MaryProperties.needFilename("sv_SE.initials"));
		//this.possibleClusters = generateHashSet(MaryProperties.needFilename("sv_SE.clusters"));
		//this.possibleFinals = generateHashSet(MaryProperties.needFilename("sv_SE.finals"));

		this.possibleInitials = generateHashSet(MaryProperties.needStream("sv_SE.initials"));
		this.possibleClusters = generateHashSet(MaryProperties.needStream("sv_SE.clusters"));
		this.possibleFinals = generateHashSet(MaryProperties.needStream("sv_SE.finals"));

		this.consonants = "[qwrtpsdfghjklzxcvbnm]";
		String[] list = new String[]{"e", "y", "u", "i", "o", "\u00E5", "a", "\u00F6", "\u00E4"};
		this.vowels = new HashSet<String>(Arrays.asList(list));	
	    } catch (Exception e) {
		System.err.println("Failed to initialise LSEQ_Finder: "+e.getMessage());
	    }
	}
	
	
	
	/**
	 * Identifies words that are to be read out letter by letter
	 * 
	 * @param s Inputstring to label as LSEQ or ASWD
	 * @return boolean Returns true if the string is phonotactically possible, otherwise returns false.
	 */
	protected boolean phonotactically_possible(String s){
		s = MaryUtils.normaliseUnicodeLetters(s, Locale.GERMAN).toLowerCase();
		
		s = s.replaceAll("[^\\w]+", ""); // removing all non-alpha substrings in the string, i.e. 'P.G.' -> 'PG' 
		
		String ini = null;
		String middle = null;
		String fin = null;
		
		if (! vowels.contains(s.substring(0, 1)) && ! vowels.contains(s.substring(s.length()-1, s.length()))){
			pattern = Pattern.compile("(^" + consonants + "+)(.+?)(" + consonants + "+$)");			
		}
		else if(! vowels.contains(s.substring(0, 1))){
			pattern = Pattern.compile("(^" + consonants + "+)(.+?)(" + consonants + "*$)");			
		}
		else if(! vowels.contains(s.substring(s.length()-1, s.length()))){
			pattern = Pattern.compile("(^" + consonants + "*)(.+?)(" + consonants + "+$)");			
		}
		else{
			pattern = Pattern.compile("(^" + consonants + "*)(.+?)(" + consonants + "*$)");			
		}
		
		matcher = pattern.matcher(s);
						
		if (matcher.find()){			
			ini = matcher.group(1);
			middle = matcher.group(2);
			fin = matcher.group(3);			
		}
		/*
		else{
			System.err.println("Error running method 'fonotactically_possible in LSEQ_Finder'.");
			System.err.println("Group count is" + matcher.groupCount());
		}
		*/
				
		if (ini.length() > 0 ){			
			if(! possibleInitials.contains(ini)){
				//System.out.println("The initial was not possible.");
				return false;
			}
		}
		if(fin.length() > 0){			
			if(! possibleFinals.contains(fin)){
				//System.out.println("The final is not possible.");
				return false;
			}
		}
		
		// loop over the word-internal  clusters, returning false if any of them are not in possibleClusters
		//System.err.println("Trying to match word-internal clusters.");
		pattern = Pattern.compile(consonants +"+");
		matcher = pattern.matcher(middle);
		
		Integer start = 0;
		try{
			while (matcher.find(start)){				
				if(! possibleClusters.contains(matcher.group(0))){
					// At this point it would be possible to try to match the cluster with a final + an initial.
					// As for now, this is left as future work
					//System.out.println("The cluster " + matcher.group(0) + " was not possible.");
					return false;								
				}
				/*
				else{
					System.out.println("The cluster " + matcher.group(0) + " was possible.");
				}
				*/
				start = matcher.end();
			}
		}			
		catch(Exception e){
			System.err.println("Tried to match word-internal clusters, but failed. Error: " + e.getMessage());
		}			
				
		return true;
	}
				
	private HashSet<String> generateHashSet(String filename){
		HashSet<String> set = new HashSet<String>();
	   	try{
			BufferedReader infile = new BufferedReader(new FileReader(filename));
			String s;
			while ((s = infile.readLine()) != null){
				s = s.trim();
				if (s.length() > 0){
					if (Character.isLetter(s.charAt(0))){
						set.add(s);
					}
				}
			}
		}
		catch (Exception e){
			System.err.println("Error when generating a hashset in class LSEQ_Finder: " + e.getMessage());
		}
		
		return set;
	}  

	private HashSet<String> generateHashSet(InputStream filestream){
		HashSet<String> set = new HashSet<String>();
	   	try{
		    BufferedReader infile = new BufferedReader(new InputStreamReader(filestream));
			String s;
			while ((s = infile.readLine()) != null){
				s = s.trim();
				if (s.length() > 0){
					if (Character.isLetter(s.charAt(0))){
						set.add(s);
					}
				}
			}
		}
		catch (Exception e){
			System.err.println("Error when generating a hashset in class LSEQ_Finder: " + e.getMessage());
		}
		
		return set;
	}  



}