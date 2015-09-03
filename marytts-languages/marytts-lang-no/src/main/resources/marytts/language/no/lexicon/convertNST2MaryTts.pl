

if ($ARGV[0] eq "lts") {
    $do_lts = 1;
    print STDERR "DOING LTS\n";
} else {
    $do_lts = 0;
    print STDERR "NOT DOING LTS\n";
}

%words = {};

while (<STDIN>) {
    #print STDERR;

    @line = split(";");

    $orth = $line[0];

    #Skip any duplicates
    next if $words{$orth};
    $words{$orth} = 1;

    $pos = $line[1];
    #$morph = $line[2];

    $decomp = $line[3];
    $decpos = $line[4];

    $lang = $line[6];

    $garbage = $line[7];

    $acr_abbr = $line[9];
    $expansion = $line[10];

    $trans = $line[11];


    #Skip garbage..
    next if ($garbage eq "GARB");

    #If doing lts training, skip the following
    if ($do_lts) {

	#Skip acronyms (spelled out) and abbreviations (expanded)
	#They should be in lexicon but not used for lts training
	next if ($acr_abbr eq "ACR");
	next if ($acr_abbr eq "ABBR");

	#Skip foreign words
	#They should be in lexicon but not used for lts training
	next if ($lang ne "NOR");


    }


    #Skip words containing something except letters and hyphen
    #They will cause failure in java build
    #But they would be nice to have in lexicon (?)
    #Accented letters included that occur in the nst lexicon
    next if ($orth =~ /[^a-zåæøäöüéôèêñçàáòóâíëA-ZÅÆØÄÖÉÜ-]/);



    #These are not useful (but multiword entries could be good to have in some, if there's a method for finding them)
    #Skip words beginning with -
    next if ($orth =~ /^-/);

    #Skrip phrases containing _
    next if ($orth =~ /_/);
    next if ($trans =~ /_/); #abbr expanded to phrase..



    #print STDERR "$garbage, $acr_abbr, $lang\n";


    #map transcription to mary format
    
    #syllable boundaries
    $trans =~ s/\$/-/g;

    #nst sampa has "" for accent2, " for accent1
    #marytts has " for accent2, ' for accent1 (not sure why, maybe nst sampa would work?)
    $trans =~ s/""/ACC2/g;
    $trans =~ s/"/ACC1/g;

    $trans =~ s/ACC2/"/g;
    $trans =~ s/ACC1/'/g;

    #Some transcriptions contain this symbol - why?
    $trans =~ s/¤//g;

    #Some transcriptions end with stress symbols - why?
    $trans =~ s/('|%)$//g;


    #check transcription is now correct according to allophones.no.xml
    $phonemes = "9\\*Y|O\\*Y|A\\*I|{\\*I|E\\*u0|}\\*I|u\\*I|n`=|l`=|n=|l=|A:|e:|i:|y:|2:|u:|}:|o:|{:|s`|n`|t`|d`|l`|A|e|E|I|Y|U|u0|O|{|9|@|p|t|k|b|d|g|f|v|s|h|S|C|l|m|n|N|r|j";

    $rest = $trans;
    while ($rest =~ /^(\"|\'|%|-)*($phonemes)(.*)$/) {
	#print STDERR "Extra: $1\n";
	#print STDERR "Phoneme: $2\n";
	#print STDERR "Rest: $3\n";
	$rest = $3;
    }

    if ($rest ne "") {
	print STDERR "ERROR:\t$orth\t$rest\t$trans\n";
	#exit;
	next;
    }




    #Mark function words with "functional"
    $functional = 0;
    if ($pos =~ /DT|IE|KN|PN|PP/) {
	$functional = 1
    }
    
    printf "%s %s", $orth, $trans;
    if ($functional) {
	print " functional";
    }
    print "\n";


    #exit;


}
