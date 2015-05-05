package marytts.language.da;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.ibm.icu.util.ULocale;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.modules.InternalModule;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import com.ibm.icu.text.RuleBasedNumberFormat;

/**
 * @author Tristan Hamilton + HB
 * 
 *         Processes cardinal and ordinal numbers.
 */
public class Preprocess extends InternalModule {

    private RuleBasedNumberFormat rbnf;
    protected final String cardinalRule;
    //protected final String ordinalRule;
    protected String ordinalRule;

    public Preprocess() {
	super("Preprocess", MaryDataType.TOKENS, MaryDataType.WORDS, new Locale("da"));
	this.rbnf = new RuleBasedNumberFormat(new ULocale("da"), RuleBasedNumberFormat.SPELLOUT);
	this.cardinalRule = "%spellout-numbering";
	try {
	    this.ordinalRule = getOrdinalRuleName(rbnf);
	    System.err.println("Ordinal rule: "+ordinalRule);
	} catch(UnsupportedOperationException e) {
	    System.err.println("Unable to load cardinal rule - falling back to rbnf "+this.cardinalRule+". Reason: "+e);
	    this.ordinalRule = this.cardinalRule;
	}
    }

	public MaryData process(MaryData d) throws Exception {
		Document doc = d.getDocument();
		System.err.println("preprocess 'da': calling checkForNumbers");
		checkForNumbers(doc);
		MaryData result = new MaryData(getOutputType(), d.getLocale());
		result.setDocument(doc);
		return result;
	}

	protected void checkForNumbers(Document doc) {
		TreeWalker tw = ((DocumentTraversal) doc).createTreeWalker(doc, NodeFilter.SHOW_ELEMENT,
				new NameNodeFilter(MaryXML.TOKEN), false);
		Element t = null;
		while ((t = (Element) tw.nextNode()) != null) {
			if (MaryDomUtils.hasAncestor(t, MaryXML.SAYAS) || t.hasAttribute("ph") || t.hasAttribute("sounds_like")) {
				// ignore token
				continue;
			}
			String origText = MaryDomUtils.tokenText(t);
			if (MaryDomUtils.tokenText(t).matches("\\d+(e|a)")) {
				String matched = MaryDomUtils.tokenText(t).split("e|a")[0];
				MaryDomUtils.setTokenText(t, expandOrdinal(Double.parseDouble(matched)));
			} else if (MaryDomUtils.tokenText(t).matches("\\d+")) {
				MaryDomUtils.setTokenText(t, expandNumber(Double.parseDouble(MaryDomUtils.tokenText(t))));
			}
			// if token isn't ignored but there is no handling rule don't add MTU
			if (!origText.equals(MaryDomUtils.tokenText(t))) {
				MaryDomUtils.encloseWithMTU(t, origText, null);
			}
		}
	}

	protected String expandNumber(double number) {
		this.rbnf.setDefaultRuleSet(cardinalRule);
		return this.rbnf.format(number);
	}

	protected String expandOrdinal(double number) {
		this.rbnf.setDefaultRuleSet(ordinalRule);
		return this.rbnf.format(number);
	}

	/**
	 * Try to extract the rule name for "expand ordinal" from the given RuleBasedNumberFormat.
	 * <p/>
	 * The rule name is locale sensitive, but usually starts with "%spellout-ordinal".
	 *
	 * @param rbnf
	 *            The RuleBasedNumberFormat from where we will try to extract the rule name.
	 * @return The rule name for "ordinal spell out".
	 */
	protected static String getOrdinalRuleName(final RuleBasedNumberFormat rbnf) {
		List<String> l = Arrays.asList(rbnf.getRuleSetNames());
		System.err.println("List of rbnf's for locale 'da': "+l);
		if (l.contains("%spellout-ordinal")) {
			return "%spellout-ordinal";
		} else if (l.contains("%spellout-ordinal-masculine")) {
			return "%spellout-ordinal-masculine";
		} else {
			for (String string : l) {
				if (string.startsWith("%spellout-ordinal")) {
					return string;
				}
			}
		}
		throw new UnsupportedOperationException("The locale " + rbnf.getLocale(ULocale.ACTUAL_LOCALE)
				+ " doesn't support ordinal spelling.");
	}
}
