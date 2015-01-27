$i = 0;

while (<>) {
    if ( $i == 5 ) {
	print;
	$i = 0;
    }
    $i++;
}
