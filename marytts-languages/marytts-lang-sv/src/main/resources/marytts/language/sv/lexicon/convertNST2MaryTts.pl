
$nst_language = "SWE";
$locale = "sv";
$do_split = 1;


#read phonemes from allophones.*.xml
open(PH, "allophones.$locale.xml");
while (<PH>) {
    chomp;

    next unless /ph=\"([^\"]+)\"/;
    $phn = $1;
    #print "$phn\n";
    push(@phnlist,$phn);
}
close(PH);

#print "@phnlist\n";
@sorted = sort { length $b <=> length $a } @phnlist;
$phonemes_re = join("|", @sorted);

$phonemes_re =~ s/\*/\\\*/g;

#print "$phonemes_re\n";
#exit;




%words = {};

while (<STDIN>) {
    #print STDERR;

    @line = split(";");

    $orth = $line[0];


    $pos = $line[1];
    #$morph = $line[2];

    $decomp = $line[3];
    $decpos = $line[4];

    $lang = $line[6];

    $garbage = $line[7];

    $acr_abbr = $line[9];
    $expansion = $line[10];

    $trans = $line[11];


    #Skip the following, and don't print at all
    #Skip any duplicates
    next if $words{$orth};
    $words{$orth} = 1;

    next if ($garbage eq "GARB");

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



    #Skip the following, print separately
    if ($do_split) {
	$skip = 0;

	#Skip acronyms (spelled out) and abbreviations (expanded)
	#They should be in lexicon but not used for lts training
	$skip = 1 if ($acr_abbr eq "ACR");
	$skip = 1 if ($acr_abbr eq "ABBR");

	#Also skip compound acronyms like ABS-bromsar
	#Words containing two consecutive capital letters
	$skip = 1 if ($orth =~ /[A-ZÅÄÖÆØ]{2}/);
	#Words containing capital letter + hyphen
	$skip = 1 if ($orth =~ /[A-ZÅÄÖÆØ]-/);


	#Skip foreign words
	#They should be in lexicon but not used for lts training
	$skip = 1 if ($lang ne $nst_language);



	#Skip names
	#They should be in lexicon (but the lexicon becomes too big to write to FST, maybe if they are separated out it will work). They potentially cause problems for lts.
	$skip = 1 if ($pos =~ /^PM/);

	#Skip words that have no POS (example no Hewlett, Switzerland)
	$skip = 1 if ($pos eq "");
	


    }




    #map transcription to mary format

    if ($locale eq "sv") {
	$trans =~ s/x\\/S/g;
	$trans =~ s/d`/rd/g;
	$trans =~ s/t`/rt/g;
	$trans =~ s/n`/rn/g;
	$trans =~ s/l`/rl/g;
	$trans =~ s/s`/rs/g;
	$trans =~ s/s'/C/g;
    }




    
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
    #$phonemes = "9\\*Y|O\\*Y|A\\*I|{\\*I|E\\*u0|}\\*I|u\\*I|n`=|l`=|n=|l=|A:|e:|i:|y:|2:|u:|}:|o:|{:|s`|n`|t`|d`|l`|A|e|E|I|Y|U|u0|O|{|9|@|p|t|k|b|d|g|f|v|s|h|S|C|l|m|n|N|r|j";

    $rest = $trans;
    while ($rest =~ /^(\"|\'|%|-)*($phonemes_re)(.*)$/) {
	#print STDERR "Extra: $1\n";
	#print STDERR "Phoneme: $2\n";
	#print STDERR "Rest: $3\n";
	$rest = $3;
    }

    if ($rest ne "") {
	print STDERR "ERROR:\t$orth\t$rest\t$trans\t$phonemes_re\n";
	exit;
	next;
    }




    #Mark function words with "functional"
    $functional = 0;
    if ($pos =~ /DT|IE|KN|PN|PP/) {
	$functional = 1
    }
    

    if ($functional) {
	$outstr = sprintf "%s %s functional\n", $orth, $trans;
    } else {
	$outstr = sprintf "%s %s\n", $orth, $trans;
    }

    if ($skip) {
	print STDERR $outstr;
    } else {
	print STDOUT $outstr;
    }



    #exit;


}
