# marytts-lang-ar

This is very much work in progress.
It sort of works for me - but there are plenty of errors and problems.

Test version is (sometimes) up on http://morf.se:59125

~~For vocalisation another server needs to be running in the background. Clone it from here https://github.com/HaraldBerthelsen/arabic_vocaliser.git, and run "nohup python vocalise.py server &"~~

For vocalisation a server needs to be running in the background. Clone it from here https://github.com/linuxscout/mishkal.git, and run "nohup python interfaces/web/mishkal-webserver.py &"

IMPORTANT: If you don't run the mishkal server in the background, there will be no vocalisation. In that case, only fully vocalised text will work as input.

If you want to correct the vocalisation, get vocalised text from the mishkal server at http://localhost:8080, paste it into marytts and edit.

If you're running Mishkal on another server or port, set environment variable `MARY_TTS_MISHKAL_URL="http://service:port"`.

-----------------------

By default marytts-lang-ar is not included when running

./gradlew build

If you want it, add this line to settings.gradle:

include 'marytts-languages:marytts-lang-ar'

Start the mishkal vocalisation server before starting marytts with "./gradlew run"


------------------------


First version test voice is available at https://github.com/HaraldBerthelsen/voice-ar-nah-hsmm.

Input text in arabic writing, without diacritics if running mishkal server, or else vocalised.

Numbers work to some extent, number expansion is done with icu4j.

Some punctuation and other "weird" characters may cause problems.

Sample text to try (please forgive me for all errors!):

Without diacritics if running mishkal server:

لسير ونستون ليونارد سبنسر تشرشل, رئيس وزراء 
المملكة المتحدة


Partly diacritised for correction of foreign words if running mishkal server:

السير وِنْسْتُونْ ليونارْدُ سْبِنْسِرْ تْشَرْشِلْ, رئيس وزراء 
المملكة المتحدة



Fully diacritised if running without mishkal server:


السَّيْرُ وِنْسْتُُونْ لِيُونَارْدُ سْبِنْسِرْ تْشَرْشِلْ, رَئِيسَ وُزَرَاءِ الْمَمْلَكَةِ الْمُتَّحِدَةِ



Next steps:

Fix problems with phonetiser rules. Some rules are clearly wrong - test examples (or a native speaker!) needed to sort them out.

Cleanup - the code is now very messy :-(

~~Convert vocaliser from python to java - or at least speed it up and fix problems.~~
