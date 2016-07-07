
package marytts.language.no;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.List;

import marytts.fst.AlignerTrainer;
import marytts.fst.TransducerTrie;
import marytts.fst.FSTLookup;




public class LexToFst {

    public static void main(String[] args) throws IOException {
	// example usage

	// specify location of lexicon you want to encode


	// The lexicon needs to be in <word|t r a n s> format, NOT <word trans>
	// so the *dict file produced by LTSPosLexiconBuilder is good

	String filename = args[0];
	String baseName = null;
	if (filename.lastIndexOf(".") == -1) {
	    baseName = filename;
	} else {
	    baseName = filename.substring(0, filename.lastIndexOf("."));
	}
	String fstFileName = baseName + ".fst";

	BufferedReader lexReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));


	// initialize trainer
	// AlignerTrainer at = new AlignerTrainer(PhonemeSet.getPhonemeSet(phFileLoc), Locale.ENGLISH);
	AlignerTrainer at = new AlignerTrainer(false, true);

	System.out.println("reading lexicon...");

	// read lexicon for training
	at.readLexicon(lexReader, "\\|");

	int lSize = at.lexiconSize();

	System.out.println("...done! Read "+lSize+" entries.");

	System.out.println("aligning...");

	long start = System.currentTimeMillis();
	
	// make some alignment iterations
	for (int i = 0; i < 10; i++) {
	    System.out.println(" iteration " + (i + 1));
	    at.alignIteration();
	    
	}
	
	long time = System.currentTimeMillis() - start;
	
	System.out.println("...done!");
	
	System.out.println("alignment took " + time + "ms");
	
	TransducerTrie t = new TransducerTrie();
	
	System.out.println("entering alignments in trie...");
	for (int i = 0; i < at.lexiconSize(); i++) {
	    t.add(at.getAlignment(i));
	    t.add(at.getInfoAlignment(i));
	}
	System.out.println("...done!");

	
	for (int nr = 0; nr < at.lexiconSize(); nr = nr+50000) {
	    String[] as = at.getAlignmentString(nr);
	    for (String element : as) {
		System.out.println("Alignment of word "+nr+" is: "+element);
	    }
	}

	//If this is over 2047 writing the fst will fail
	//doesn't compile in marytts-lang-no, only in marytts
	//System.out.println("Number of labels: "+t.getLabelCount());

	//Will print all the labels to stderr, for inspection - not sure it will do any good.. 
	//doesn't compile in marytts-lang-no, only in marytts
	//t.printLabels();

	
	System.out.println("minimizing trie...");
	t.computeMinimization();
	System.out.println("...done!");

	System.out.println("Trie string representation size is: "+t.toString().length());

	
	System.out.println("writing transducer to disk...");
	File of = new File(fstFileName);
	
	DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(of)));
	
	t.writeFST(os, "UTF-8");
	os.flush();
	os.close();
	System.out.println("...done!");
	
	System.out.println("looking up test words...");
	FSTLookup fst = new FSTLookup(fstFileName);
	
	
	System.out.println("Zvonko -> " + Arrays.toString(fst.lookup("Zvonko")));
	System.out.println("Zvonimir -> " + Arrays.toString(fst.lookup("Zvonimir")));
	System.out.println("Zygmunt -> " + Arrays.toString(fst.lookup("Zygmunt")));
	
	System.out.println("...done!");
	
    }

}
