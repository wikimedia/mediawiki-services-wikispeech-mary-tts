package marytts.language.no;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.datatypes.MaryDataType;
import marytts.util.dom.DomUtils;

import org.junit.Test;
import org.w3c.dom.Document;

public class MaryInterfaceNoIT {

	@Test
	public void canSetLocale() throws Exception {
		MaryInterface mary = new LocalMaryInterface();
		Locale loc = new Locale("no");

		//Locale loc = Locale.GERMAN;
		assertTrue(!loc.equals(mary.getLocale()));
		mary.setLocale(loc);
		assertEquals(loc, mary.getLocale());
	}


	@Test
	public void canProcessTokensToAllophones() throws Exception {
		// setup
		MaryInterface mary = new LocalMaryInterface();
		mary.setInputType(MaryDataType.TOKENS.name());
		mary.setOutputType(MaryDataType.ALLOPHONES.name());
		mary.setLocale(new Locale("no"));
		String example = MaryDataType.getExampleText(MaryDataType.TOKENS, mary.getLocale());
		System.err.println("Norwegian example text: "+example);
		assertNotNull(example);
		Document tokens = DomUtils.parseDocument(example);
		// exercise
		Document allos = mary.generateXML(tokens);
		// verify
		assertNotNull(allos);
	}
}
