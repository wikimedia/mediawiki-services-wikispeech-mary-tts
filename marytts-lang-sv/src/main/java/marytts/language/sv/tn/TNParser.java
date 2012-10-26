package marytts.language.sv.tn;

import java.io.*;
import java.util.*;

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

/**
 * @author Erik Sterneberg 
 * 
 * Class that loads the language specific rules needed to do text normalization * 
 */
public class TNParser {
	/**
	 * query Loads an xml file and parses the containing text normalization rules	 
	 * @param xml A xml file containing language specific text normalization rules
	 * @return rules A hashmap containing the rules that will be used by TNNormalize.normalize
	 * to expand numbers etc. to words. 
	 */
    public static HashMap<String, String[]> query(String xml) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		// Standard of reading a XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		XPathExpression expr = null;
		builder = factory.newDocumentBuilder();
		doc = builder.parse(xml); // Language specific text normalization rules

		// Create a XPathFactory
		XPathFactory xFactory = XPathFactory.newInstance();

		// Create a XPath object
		XPath xpath = xFactory.newXPath();

		Object result;
		NodeList nodes;
						
		HashMap<String, String[]> rules = new HashMap<String, String[]>();
		expr = xpath.compile("//tn_rules/rule/@name");
		// Run the query and get a nodeset
		result = expr.evaluate(doc, XPathConstants.NODESET);		
		nodes = (NodeList) result;
		
		for (int i=0; i<nodes.getLength();i++){
			String rule_name = nodes.item(i).getNodeValue();
					
			// Make another query to get the referenced rules from rule_name
			expr = xpath.compile("//tn_rules/rule[@name='" + rule_name + "']/ref/@name");
			result = expr.evaluate(doc, XPathConstants.NODESET);		
			NodeList nodes_children = (NodeList) result;
			
			// If the rule had references to other rules, write rule_name:[list of refs] to the hashmap rules
			if (nodes_children.getLength() > 0){
				String[] refs = new String[100];
				for (int j=0; j<nodes_children.getLength(); j++)
					refs[j] =nodes_children.item(j).getNodeValue();				
								
				// Shrink refs
				int counter = 0;
				for (int j=0; j < refs.length; j++){
					if (refs[j] == null){
						break;
					}
					counter++;
				}				

				String[] tempArray = new String[counter];
				for (int j=0; j < counter; j++)
					tempArray[j] = refs[j];
				
				refs = tempArray;
				
				rules.put(rule_name, refs);
			}
			
			// Otherwise, make a new query and get the text() of the children 'in' and 'out' of the rule
			else{				
				String[] in_out_values = new String[3];
				expr = xpath.compile("//tn_rules/rule[@name='" + rule_name + "']/*/text()");
				result = expr.evaluate(doc, XPathConstants.NODESET);		
				nodes_children = (NodeList) result;
				in_out_values[0] = "INPUT_OUTPUT_RULE";
				
				for (int j=0; j<nodes_children.getLength(); j++)
					in_out_values[j+1] = nodes_children.item(j).getNodeValue();
				
				rules.put(rule_name, in_out_values);
				
				assert (rules.get(rule_name)[1] != "" && rules.get(rule_name)[1] != null): "Rule " + rule_name + " is malformed. There is no in-value.";
				assert (rules.get(rule_name)[2] != "" && rules.get(rule_name)[2] != null): "Rule " + rule_name + " is malformed. There is no out-value.";
			}			
		}
		
		assert (rules.isEmpty() != true): "No rules containing references to other rules found in " + xml + ".";		
		assert (rules.containsKey("start") == true): "No start node \"start\" found in " + xml + ".";				

		// System.out.println("TN rules loaded successfully.");

		return rules;
	}
	
	/**
	 * @return regexes Hashmap with the names of the uppercase rules used in the xml file as keys and their pertaining regular expressions as values.
	 */
	public static HashMap<String, String> loadRegexes(String regexTxt){
		  HashMap<String, String> regexes = new HashMap<String, String>();
			try{
				//FileInputStream fstream = new FileInputStream("test.txt");	
				BufferedReader infile = new BufferedReader(new FileReader(regexTxt));
				String s;
				while ((s = infile.readLine()) != null) {
					String[] line_items;
					line_items = s.split(": ");
					regexes.put(line_items[0], line_items[1]);
				}
				
			}
			catch (Exception e){
				// System.err.println("Error: " + e.getMessage());
			}
		
			assert (regexes.isEmpty() != true): "The hashmap 'regexes' contains no values or was not parsable.";

			// System.out.println("Regular expressions loaded successfully.");
			
			return regexes;
	  }
}
